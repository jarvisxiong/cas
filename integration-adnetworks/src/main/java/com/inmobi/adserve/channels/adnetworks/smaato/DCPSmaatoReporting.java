package com.inmobi.adserve.channels.adnetworks.smaato;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.configuration.Configuration;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.BaseReportingImpl;
import com.inmobi.adserve.channels.api.ReportResponse;
import com.inmobi.adserve.channels.api.ReportTime;
import com.inmobi.adserve.channels.api.ServerException;


public class DCPSmaatoReporting extends BaseReportingImpl {
    private static final Logger LOG = LoggerFactory.getLogger(DCPSmaatoReporting.class);
    private final String        loginName;
    private final String        password;
    private String              startDate;
    private String              endDate;
    private final String        host;
    private final String        postData;
    private final String        advertiserId;
    private String              extSiteKey;

    public DCPSmaatoReporting(final Configuration config) {
        this.loginName = config.getString("smaato.user");
        this.password = config.getString("smaato.password");
        this.host = config.getString("smaato.host");
        this.advertiserId = config.getString("smaato.advertiserId");
        this.postData = config.getString("smaato.postData");
    }

    @Override
    public ReportResponse fetchRows(final ReportTime startTime, final String key, final ReportTime endTime)
            throws Exception {
        ReportResponse reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);
        LOG.debug("inside fetch rows of smaato");
        try {
            this.startDate = startTime.getStringDate("-");
            LOG.debug("start date inside smaato is {}", this.startDate);
            endDate = endTime == null ? getEndDate("-") : startDate;
            if (ReportTime.compareStringDates(this.endDate, this.startDate) == -1) {
                LOG.debug("date is greater than the current date reporting window for smaato");
                return null;
            }
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
            LOG.info("failed to obtain correct dates for fetching reports {}", exception);
            return null;
        }
        extSiteKey = key;
        String response = invokePostMethod(host, getRequestUrl());
        LOG.debug(response);
        reportResponse.status = ReportResponse.ResponseStatus.SUCCESS;
        String[] responseArray = response.split("\n");
        generateReportResponse(reportResponse, responseArray, key);
        return reportResponse;
    }

    @Override
    public String getRequestUrl() {
        String reportUrl = String.format(postData, loginName, password, startDate, endDate, extSiteKey);
        LOG.debug("url data is  {}", reportUrl);
        return reportUrl;
    }

    @Override
    public double getTimeZone() {
        return 1;
    }

    @Override
    public ReportGranularity getReportGranularity() {
        return ReportGranularity.DAY;
    }

    @Override
    public int ReportReconcilerWindow() {
        return 20;
    }

    @Override
    public String getName() {
        return "smaato";
    }

    @Override
    public String getAdvertiserId() {
        return (advertiserId);
    }

    public String getEndDate(final String seperator) {
        try {
            LOG.debug("calculating end date for smaato");
            ReportTime reportTime = ReportTime.getUTCTime();
            reportTime = ReportTime.getPreviousDay(reportTime);
            if (reportTime.getHour() < ReportReconcilerWindow()) {
                reportTime = ReportTime.getPreviousDay(reportTime);
            }
            return (reportTime.getStringDate(seperator));
        }
        catch (Exception exception) {
            LOG.info("failed to obtain end date inside smaato {}", exception);
            return "";
        }
    }

    private void generateReportResponse(final ReportResponse reportResponse, final String[] responseArray,
            final String key) {
        if (responseArray.length > 1) {
            int impressionIndex = -1;
            int clicksIndex = -1;
            int revenueIndex = -1;
            int dateIndex = -1;
            int siteNameIndex = -1;

            String[] header = responseArray[0].replace("'", "").split(";");

            for (int i = 0; i < header.length; i++) {
                if ("views".equalsIgnoreCase(header[i].trim())) {
                    impressionIndex = i;
                    continue;
                }
                else if ("clicks".equalsIgnoreCase(header[i].trim())) {
                    clicksIndex = i;
                    continue;
                }
                else if ("Revenue".equalsIgnoreCase(header[i].trim())) {
                    revenueIndex = i;
                    continue;
                }
                else if ("date".equalsIgnoreCase(header[i].trim())) {
                    dateIndex = i;
                    continue;
                }
                else if ("adspaceid".equalsIgnoreCase(header[i].trim())) {
                    siteNameIndex = i;
                    continue;
                }

            }
            for (int j = 1; j < responseArray.length; j++) {
                String[] rows1 = responseArray[j].replace(",", "").split(";");
                if (key.equalsIgnoreCase(rows1[siteNameIndex])) {
                    ReportResponse.ReportRow row = new ReportResponse.ReportRow();

                    row.impressions = Long.parseLong(rows1[impressionIndex]);
                    row.revenue = Double.valueOf(rows1[revenueIndex]).doubleValue();
                    row.clicks = Long.parseLong(rows1[clicksIndex]);
                    row.reportTime = new ReportTime(rows1[dateIndex], 0);
                    row.slotSize = getReportGranularity();
                    row.siteId = key;
                    reportResponse.addReportRow(row);
                }
            }
        }
    }

    private String invokePostMethod(final String url, final String postData) throws ServerException,
            NoSuchAlgorithmException, KeyManagementException {

        StringBuffer result = new StringBuffer();
        try {

            HttpClient client = new DefaultHttpClient();
            client = wrapClient(client);
            HttpPost post = new HttpPost(url);
            List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
            urlParameters.add(new BasicNameValuePair("login_name", loginName));
            urlParameters.add(new BasicNameValuePair("login_pass", password));
            urlParameters.add(new BasicNameValuePair("from", startDate));
            urlParameters.add(new BasicNameValuePair("until", endDate));
            urlParameters.add(new BasicNameValuePair("adspaceID", extSiteKey));
            urlParameters.add(new BasicNameValuePair("lang", "us"));
            urlParameters.add(new BasicNameValuePair("format", "csv"));

            post.setEntity(new UrlEncodedFormEntity(urlParameters));

            HttpResponse response = client.execute(post);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
                result.append("\n");

            }

        }

        catch (MalformedURLException exception) {
            LOG.info("malformed url {}", exception);
            throw new ServerException("Invalid Url");
        }
        catch (IOException exception) {
            LOG.info("io error while reading response {}", exception);
            throw new ServerException("Error While Reading Response");
        }
        return result.toString();
    }

    public static HttpClient wrapClient(final HttpClient base) {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            X509TrustManager tm = new X509TrustManager() {
                @Override
                public void checkClientTrusted(final X509Certificate[] xcs, final String string)
                        throws CertificateException {
                }

                @Override
                public void checkServerTrusted(final X509Certificate[] xcs, final String string)
                        throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            ctx.init(null, new TrustManager[] { tm }, null);
            SSLSocketFactory ssf = new SSLSocketFactory(ctx);
            ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            ClientConnectionManager ccm = base.getConnectionManager();
            SchemeRegistry sr = ccm.getSchemeRegistry();
            sr.register(new Scheme("https", ssf, 443));
            return new DefaultHttpClient(ccm, base.getParams());
        }
        catch (Exception ex) {
            return null;
        }
    }
}