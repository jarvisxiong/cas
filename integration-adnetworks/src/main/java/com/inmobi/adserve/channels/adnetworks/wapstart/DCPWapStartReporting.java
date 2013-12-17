package com.inmobi.adserve.channels.adnetworks.wapstart;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.sql.Connection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.configuration.Configuration;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.BaseReportingImpl;
import com.inmobi.adserve.channels.api.ReportResponse;
import com.inmobi.adserve.channels.api.ReportTime;
import com.inmobi.adserve.channels.api.ServerException;


public class DCPWapStartReporting extends BaseReportingImpl {
    private static final Logger LOG = LoggerFactory.getLogger(DCPWapStartReporting.class);

    private final Configuration config;
    private final String        token;
    private String              startDate;
    private String              endDate;
    private final String        baseUrl;
    private String              externalSiteId;
    private final Connection    connection;

    public DCPWapStartReporting(final Configuration config, final Connection connection) {
        this.config = config;
        token = config.getString("wapstart.token");
        baseUrl = config.getString("wapstart.host");
        this.connection = connection;
    }

    @Override
    public int ReportReconcilerWindow() {
        return 10;
    }

    @Override
    public ReportResponse fetchRows(final ReportTime startTime, final String key, final ReportTime endTime)
            throws ClientProtocolException, IllegalStateException, ServerException, IOException, JSONException {
        ReportResponse reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);
        String responseStr = null;
        externalSiteId = key;
        LOG.debug("inside fetch rows of wapstart");
        try {
            startDate = startTime.getStringDate("-");
            LOG.debug("start date inside wapstart is {}", startDate);

            endDate = endTime == null ? getEndDate() : ReportTime.getNextDay(startTime).getStringDate("-");
            if (ReportTime.compareStringDates(endDate, startDate) == -1) {
                LOG.debug("date is greater than the current date reporting window for wapstart");
                return null;
            }
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
            LOG.info("failed to obtain correct dates for fetching reports {}", exception);
            return null;
        }

        responseStr = invokeHTTPUrl(getRequestUrl());
        LOG.debug("response from wapstart is {}", responseStr);

        // parse the json response
        if (responseStr != null && !responseStr.startsWith("{\"error")) {
            JSONObject data = new JSONObject(responseStr);
            JSONArray jReportArr = data.getJSONArray("statistic");
            for (int i = 0; i < jReportArr.length(); i++) {
                ReportResponse.ReportRow row = new ReportResponse.ReportRow();
                row.impressions = 0;
                row.clicks = 0;
                row.request = 0;
                row.revenue = 0;
                double rate = getCurrencyConversionRate("RUB", startDate, connection);
                if (rate > 0) {
                    row.revenue /= rate;
                }
                else {
                    LOG.info("Failed to get RUB to USD rate for {}", startDate);
                    return null;
                }
                JSONObject reportRow = jReportArr.getJSONObject(i);
                LOG.debug("json array length is {}", jReportArr.length());
                row.request = 0;
                row.reportTime = new ReportTime(reportRow.getString("day"), 0);
                row.clicks = Long.parseLong(reportRow.getString("clicks"));
                row.impressions = Long.parseLong(reportRow.getString("views"));
                row.revenue = Double.parseDouble(reportRow.getString("income"));
                LOG.debug("parsing data inside wapstart {}", row);
                row.advertiserId = this.getAdvertiserId();
                row.siteId = externalSiteId;
                row.slotSize = getReportGranularity();
                reportResponse.addReportRow(row);
            }
        }

        return reportResponse;
    }

    @Override
    public String getAdvertiserId() {
        return (config.getString("wapstart.advertiserId"));
    }

    @Override
    public String getName() {
        return "wapstart";
    }

    @Override
    public ReportGranularity getReportGranularity() {
        return ReportGranularity.DAY;
    }

    @Override
    public String getRequestUrl() {
        return String.format(baseUrl, externalSiteId, token, startDate, endDate);
    }

    @Override
    public double getTimeZone() {
        return 0;
    }

    public String getEndDate() throws Exception {
        try {
            LOG.debug("calculating end date for wapstart");
            ReportTime reportTime = ReportTime.getUTCTime();
            reportTime = ReportTime.getPreviousDay(reportTime);
            if (reportTime.getHour() <= ReportReconcilerWindow()) {
                reportTime = ReportTime.getPreviousDay(reportTime);
            }
            return (reportTime.getStringDate("-"));
        }
        catch (Exception exception) {
            LOG.info("failed to obtain end date inside wapstart {}", exception);
            return "";
        }
    }

    @Override
    public String invokeHTTPUrl(final String url) throws ServerException, MalformedURLException, IOException {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(final X509Certificate[] certs, final String authType) {
            }

            @Override
            public void checkServerTrusted(final X509Certificate[] certs, final String authType) {
            }
        } };

        try {
            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                @Override
                public boolean verify(final String hostname, final SSLSession session) {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
            URLConnection conn = new URL(url).openConnection();
            // Setting connection and read timeout to 5 min
            conn.setReadTimeout(300000);
            conn.setConnectTimeout(300000);
            // conn.setRequestProperty("X-WSSE", getHeader());
            conn.setDoOutput(true);
            InputStream in = conn.getInputStream();
            BufferedReader res = null;
            StringBuffer sBuffer = new StringBuffer();

            res = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String inputLine;
            while ((inputLine = res.readLine()) != null) {
                sBuffer.append(inputLine);
            }
            res.close();
            return sBuffer.toString();
        }
        catch (Exception exception) {
            LOG.info("Error in Httpool invokeHTTPUrl : ", exception.getMessage());
            throw new ServerException(exception);
        }

    }
}