package com.inmobi.adserve.channels.adnetworks.madnet;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.BaseReportingImpl;
import com.inmobi.adserve.channels.api.ReportResponse;
import com.inmobi.adserve.channels.api.ReportTime;
import com.inmobi.adserve.channels.api.ServerException;


public class DCPMadNetReporting extends BaseReportingImpl {
    private static final Logger LOG            = LoggerFactory.getLogger(DCPMadNetReporting.class);

    private final Configuration config;
    private String              startDate      = "";
    private String              endDate        = "";
    private String              responseString = "";
    private String              reportUrl      = "";
    private String              authUrl        = "";
    private String              userName       = "";
    private String              password       = "";
    private String              accessToken;

    public DCPMadNetReporting(final Configuration config) {
        this.config = config;
        reportUrl = config.getString("madnet.reportUrl");
        authUrl = config.getString("madnet.authUrl");
        userName = config.getString("madnet.userName");
        password = config.getString("madnet.password");
    }

    @Override
    public ReportResponse fetchRows(final ReportTime startTime, final ReportTime endTime) throws Exception {
        ReportResponse reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);
        LOG.debug("inside fetch rows of MadNet");
        try {
            startDate = startTime.getStringDate("-");
            endDate = endTime == null ? getEndDate() : startDate;
            LOG.debug("start date inside MadNet is {}", startDate);
            if (ReportTime.compareStringDates(endDate, startDate) == -1) {
                LOG.debug("end date is less than start date plus reporting window for MadNet");
                return null;
            }
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
            LOG.info("failed to obtain correct dates for fetching reports {}", exception);
            return null;
        }
        accessToken = getAccessToken();
        if (StringUtils.isBlank(accessToken)) {
            LOG.debug("Failed to generate token");
            return null;
        }
        while (ReportTime.compareStringDates(startDate, endDate) != 1) {
            String url = getRequestUrl();
            LOG.debug("url inside MadNet is {}", url);
            LOG.debug("start date inside MadNet now is {}", startDate);
            responseString = invokeHTTPUrl(url);
            reportResponse.status = ReportResponse.ResponseStatus.SUCCESS;

            try {
                JSONObject data = new JSONObject(responseString);
                JSONArray dataArray = data.getJSONArray("data");

                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject jo = dataArray.getJSONObject(i);
                    JSONObject fields = jo.getJSONObject("fields");
                    LOG.debug("coming here to get log date");
                    ReportResponse.ReportRow row = new ReportResponse.ReportRow();
                    String bSiteId = jo.getString("key_value");
                    decodeBlindedSiteId(bSiteId, row);
                    row.reportTime = new ReportTime(startDate, 0);
                    row.request = fields.getLong("requests");
                    row.impressions = fields.getLong("imps");
                    row.clicks = fields.getLong("clicks");
                    row.revenue = fields.getDouble("pub_payout");
                    row.slotSize = getReportGranularity();
                    row.isSiteData = true;
                    reportResponse.addReportRow(row);
                }
                startDate = ReportTime.getNextDay(new ReportTime(startDate, 0)).getStringDate("-");
            }
            catch (JSONException e) {
                LOG.info("Error parsing reporting data for MadNet");
                return reportResponse;
            }
        }
        LOG.debug("successfully parsed data inside MadNet");
        return reportResponse;
    }

    private String getAccessToken() {
        WebDriver wDriver = new HtmlUnitDriver();
        try {
            wDriver.get(authUrl);
            WebElement userNameElement = wDriver.findElement(By.id("username"));
            userNameElement.sendKeys(userName);
            WebElement passwordElement = wDriver.findElement(By.id("password"));
            passwordElement.sendKeys(password);
            wDriver.findElement(By.id("_submit")).click();
            wDriver.findElement(By.name("accepted")).click();

            return wDriver.getCurrentUrl().split("=")[1].split("&")[0];
        }
        catch (Exception e) {
            LOG.info("Failed to generate access token for MadNet : {}", e);
            return null;
        }
    }

    @Override
    protected boolean decodeBlindedSiteId(final String blindedSiteId, final ReportResponse.ReportRow row) {
        if (blindedSiteId.length() < 36) {
            LOG.info("failed to decodeBlindedSiteId for {} {}", getName(), blindedSiteId);
            return false;
        }
        String bSiteId = blindedSiteId.substring(2, blindedSiteId.length());
        return super.decodeBlindedSiteId(bSiteId, row);
    }

    public String getEndDate() throws Exception {
        try {
            LOG.debug("calculating latest date for MadNet");
            ReportTime reportTime = ReportTime.getUTCTime();
            reportTime = ReportTime.getPreviousDay(reportTime);
            if (reportTime.getHour() <= ReportReconcilerWindow()) {
                reportTime = ReportTime.getPreviousDay(reportTime);
            }
            return (reportTime.getStringDate("-"));
        }
        catch (Exception exception) {
            LOG.info("failed to obtain end date inside MadNet {}", exception);
            return null;
        }
    }

    @Override
    public String getRequestUrl() {
        return String.format(reportUrl, startDate, startDate, accessToken);
    }

    @Override
    public double getTimeZone() {
        return 4;
    }

    @Override
    public ReportGranularity getReportGranularity() {
        return ReportGranularity.DAY;
    }

    @Override
    public int ReportReconcilerWindow() {
        return 14;
    }

    @Override
    public String getName() {
        return "MadNet";
    }

    @Override
    public String getAdvertiserId() {
        return (config.getString("madnet.advertiserId"));
    }

    @Override
    public String invokeHTTPUrl(final String url) throws ServerException {
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
        catch (Exception e) {
            LOG.info("Error in MadNet invokeHTTPUrl : {}", e);
            throw new ServerException(e);
        }

    }
}
