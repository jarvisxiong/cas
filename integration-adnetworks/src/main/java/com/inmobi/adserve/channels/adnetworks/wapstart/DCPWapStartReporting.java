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

import com.inmobi.adserve.channels.api.BaseReportingImpl;
import com.inmobi.adserve.channels.api.ReportResponse;
import com.inmobi.adserve.channels.api.ReportTime;
import com.inmobi.adserve.channels.api.ServerException;
import com.inmobi.adserve.channels.util.DebugLogger;


public class DCPWapStartReporting extends BaseReportingImpl {
    private final Configuration config;
    private final String        token;
    private String              startDate;
    private String              endDate;
    private final String        baseUrl;
    private DebugLogger         logger;
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
    public ReportResponse fetchRows(final DebugLogger logger, final ReportTime startTime, final String key,
            final ReportTime endTime) throws ClientProtocolException, IllegalStateException, ServerException,
            IOException, JSONException {
        this.logger = logger;
        ReportResponse reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);
        String responseStr = null;
        externalSiteId = key;
        logger.debug("inside fetch rows of wapstart");
        try {
            startDate = startTime.getStringDate("-");
            logger.debug("start date inside wapstart is ", startDate);

            endDate = endTime == null ? getEndDate() : ReportTime.getNextDay(startTime).getStringDate("-");
            if (ReportTime.compareStringDates(endDate, startDate) == -1) {
                logger.debug("date is greater than the current date reporting window for wapstart");
                return null;
            }
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
            logger.info("failed to obtain correct dates for fetching reports ", exception.getMessage());
            return null;
        }

        try {
            responseStr = invokeHTTPUrl(getRequestUrl());
        }
        catch (KeyManagementException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        logger.debug("response from wapstart is ", responseStr);

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

                JSONObject reportRow = jReportArr.getJSONObject(i);
                logger.debug("json array length is ", jReportArr.length());
                row.request = 0;
                row.reportTime = new ReportTime(reportRow.getString("day"), 0);
                row.clicks = Long.parseLong(reportRow.getString("clicks"));
                row.impressions = Long.parseLong(reportRow.getString("views"));
                row.revenue = Double.parseDouble(reportRow.getString("income"));
                double rate = getCurrencyConversionRate("RUB", startDate, logger, connection);
                if (rate > 0) {
                    row.revenue /= rate;
                }
                else {
                    logger.info("Failed to get RUB to USD rate for ", startDate);
                    return null;
                }
                logger.debug("parsing data inside wapstart ", row);
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
            logger.debug("calculating end date for wapstart");
            ReportTime reportTime = ReportTime.getUTCTime();
            reportTime = ReportTime.getPreviousDay(reportTime);
            if (reportTime.getHour() <= ReportReconcilerWindow()) {
                reportTime = ReportTime.getPreviousDay(reportTime);
            }
            return (reportTime.getStringDate("-"));
        }
        catch (Exception exception) {
            logger.info("failed to obtain end date inside wapstart ", exception.getMessage());
            return "";
        }
    }

    public String invokeHTTPUrl(final String url) throws ServerException, NoSuchAlgorithmException,
            KeyManagementException, MalformedURLException, IOException {
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
        try {
            res = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String inputLine;
            while ((inputLine = res.readLine()) != null) {
                sBuffer.append(inputLine);
            }
        }
        catch (IOException ioe) {
            logger.info("Error in Httpool invokeHTTPUrl : ", ioe.getMessage());
        }
        finally {
            if (res != null) {
                res.close();
            }
        }

        return sBuffer.toString();
    }
}