package com.inmobi.adserve.channels.adnetworks.openx;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.inmobi.adserve.channels.api.BaseReportingImpl;
import com.inmobi.adserve.channels.api.ReportResponse;
import com.inmobi.adserve.channels.api.ReportTime;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.openx.oauth.client.Client;


public class OpenxReporting extends BaseReportingImpl
{
    private Configuration    config;
    private DebugLogger      logger;
    private String           startDate        = "";
    private String           endDate          = "";
    private BasicCookieStore cookieStore      = null;
    private static String    reportStartDate  = null;
    private static String    entireReportData = null;
    private final String     domain;

    public OpenxReporting(final Configuration config)
    {
        this.config = config;
        domain = config.getString("openx.domain");
    }

    private boolean authenticate()
    {
        String apiKey = config.getString("openx.apiKey");
        String apiSecret = config.getString("openx.apiSecret");
        String loginUrl = config.getString("openx.loginUrl");
        String username = config.getString("openx.username");
        String password = config.getString("openx.password");
        String path = config.getString("openx.path");
        String requestTokenUrl = config.getString("openx.requestTokenUrl");
        String accessTokenUrl = config.getString("openx.accessTokenUrl");
        String realm = config.getString("openx.realm");
        String authorizeUrl = config.getString("openx.authorizeUrl");
        logger.debug("building client for authentication");
        Client cl = new Client(apiKey, apiSecret, loginUrl, username, password, domain, path, requestTokenUrl,
                accessTokenUrl, realm, authorizeUrl);
        try {
            cl.OX3OAuth();
            cookieStore = cl.getHelper().getCookieStore();
        }
        catch (Exception ex) {
            logger.info("Failed to authenticate the user inside openx ", ex.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public ReportResponse fetchRows(final DebugLogger logger, final ReportTime startTime, final String key,
            final ReportTime endTime) throws ClientProtocolException, IOException, JSONException
    {
        this.logger = logger;

        ReportResponse reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);
        logger.debug("inside fetch rows of openx");
        try {
            startDate = startTime.getStringDate("-");
            logger.debug("start date inside openx is ", startDate);
            endDate = endTime == null ? getEndDate() : startDate;
            logger.debug("end date inside openx is ", endDate);
            if (ReportTime.compareStringDates(endDate, startDate) == -1) {
                logger.debug("end date is less than start date plus reporting window for openx");
                return null;
            }
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
            logger.info("failed to obtain correct dates for fetching reports ", exception.getMessage());
            return null;
        }
        if (partnerContactFlag() && !authenticate()) {
            return null;
        }
        DefaultHttpClient httpclient = new DefaultHttpClient();
        httpclient.setCookieStore(cookieStore);

        while (ReportTime.compareStringDates(startDate, endDate) != 1) {
            boolean gotReport = false;
            if (partnerContactFlag()) {
                String url = getRequestUrl();
                logger.debug("url inside openx is ", url);
                HttpGet httpget = new HttpGet(url);
                HttpResponse response = httpclient.execute(httpget);
                InputStream is = response.getEntity().getContent();
                StringBuffer buffer = new StringBuffer();
                byte[] b = new byte[4096];
                for (int n; (n = is.read(b)) != -1;) {
                    buffer.append(new String(b, 0, n));
                }
                String reportString = buffer.toString();
                if (!StringUtils.isEmpty(reportString) && reportString.contains("\"ReportOutput")) {
                    entireReportData = reportString;
                    reportStartDate = startDate;
                }
                logger.debug("response inside openx is ", entireReportData);
            }
            JSONObject data = new JSONObject(entireReportData);
            JSONObject report = data.getJSONObject("ReportOutput");
            JSONArray dailyReports = report.getJSONObject("reportBody").getJSONArray("ReportData");
            for (int arrayIndex = 0; arrayIndex < dailyReports.length(); arrayIndex++) {
                JSONArray adunitReport = dailyReports.getJSONArray(arrayIndex);
                if (adunitReport.getString(3).equals(key)) {
                    ReportResponse.ReportRow row = new ReportResponse.ReportRow();
                    row.request = adunitReport.getLong(5);
                    row.impressions = adunitReport.getLong(6);
                    row.revenue = adunitReport.getDouble(7);
                    row.ecpm = adunitReport.getDouble(8);
                    row.clicks = 0;
                    row.siteId = key;
                    row.reportTime = new ReportTime(startDate, 0);
                    row.slotSize = getReportGranularity();
                    logger.debug("parsing data inside mobile commerce ", row.request);
                    reportResponse.addReportRow(row);
                    gotReport = true;
                    break;
                }
            }
            if (!gotReport) {
                logger.debug("No record found for the segment");
                ReportResponse.ReportRow row = new ReportResponse.ReportRow();
                row.request = 0;
                row.clicks = 0;
                row.impressions = 0;
                row.revenue = 0.0;
                row.siteId = key;
                row.reportTime = new ReportTime(startDate, 0);
                row.slotSize = getReportGranularity();
                logger.debug("parsing data inside OpenX ", row.request);
                reportResponse.addReportRow(row);
            }

            ReportTime reportTime = new ReportTime(startDate, 0);
            reportTime = ReportTime.getNextDay(reportTime);
            startDate = reportTime.getStringDate("-");
            logger.debug("start date inside openx now is ", startDate);
        }

        return reportResponse;
    }

    /**
     * @return
     */
    private boolean partnerContactFlag()
    {
        return reportStartDate == null || (ReportTime.compareStringDates(reportStartDate, startDate) != 0);
    }

    // Constructs the request url. This is made public for testing purpose.
    @Override
    public String getRequestUrl()
    {
        String url = domain + "/ox/3.0/a/report/run?report=adunit_sum&start_date=" + startDate
                + "%2000:00:00&end_date=" + startDate + "%2023:59:59&report_format=json";
        return url;
    }

    // Returns the time zone(x) GMT+x. For GMT it returns 0. IST it returns 5.5
    @Override
    public double getTimeZone()
    {
        return -7.0;
    }

    @Override
    public String getAdvertiserId()
    {
        return config.getString("openx.advertiserId");
    }

    // Returns the report format wrt granularity.
    @Override
    public ReportGranularity getReportGranularity()
    {
        return ReportGranularity.DAY;
    }

    @Override
    public String getName()
    {
        return "openx";
    }

    @Override
    public int ReportReconcilerWindow()
    {
        return 21;
    }

    public String getEndDate() throws Exception
    {
        try {
            logger.debug("calculating end date for openx");
            ReportTime reportTime = ReportTime.getUTCTime();
            reportTime = ReportTime.getPreviousDay(reportTime);
            reportTime = ReportTime.getPreviousDay(reportTime);
            if (reportTime.getHour() <= ReportReconcilerWindow()) {
                reportTime = ReportTime.getPreviousDay(reportTime);
            }
            return (reportTime.getStringDate("-"));
        }
        catch (Exception exception) {
            logger.info("failed to obtain end date inside OpenX ", exception.getMessage());
            return "";
        }
    }
}