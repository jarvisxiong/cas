package com.inmobi.adserve.channels.adnetworks.amobee;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.commons.configuration.Configuration;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.inmobi.adserve.channels.api.BaseReportingImpl;
import com.inmobi.adserve.channels.api.ReportResponse;
import com.inmobi.adserve.channels.api.ReportTime;
import com.inmobi.adserve.channels.api.ServerException;
import com.inmobi.adserve.channels.util.DebugLogger;


public class DCPAmobeeReporting extends BaseReportingImpl
{
    private final Configuration config;
    private final String        reportingKey;
    private String              date;
    private final String        baseUrl;
    private DebugLogger         logger;
    private final String        userName;
    private final String        password;
    private String              endDate = "";

    public DCPAmobeeReporting(final Configuration config)
    {

        this.config = config;
        baseUrl = config.getString("amobee.baseUrl");
        userName = config.getString("amobee.userName");
        password = config.getString("amobee.password");
        reportingKey = config.getString("amobee.key");
    }

    private String invokeHTTPUrl(final String url) throws ServerException, ClientProtocolException, IOException,
            IllegalStateException
    {
        String retStr = null;
        logger.debug("url inside amobee is ", url);
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(url);
        httpget.setHeader("username", userName);
        httpget.setHeader("password", password);
        HttpResponse response = httpclient.execute(httpget);
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new ServerException("Erroneous status code");
        }
        InputStream is = response.getEntity().getContent();
        StringBuffer buffer = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = is.read(b)) != -1;) {
            buffer.append(new String(b, 0, n));
        }
        retStr = buffer.toString();

        return retStr;
    }

    @Override
    public int ReportReconcilerWindow()
    {
        return 20;
    }

    @Override
    public ReportResponse fetchRows(final DebugLogger logger, final ReportTime startTime, final ReportTime endTime)
            throws Exception
    {
        this.logger = logger;
        ReportResponse reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);
        String responseStr = null;
        logger.debug("inside fetch rows of amobee");
        try {
            date = startTime.getStringDate("");
            logger.debug("start date inside amobee is ", date);
            endDate = endTime == null ? getEndDate() : date;
            if (ReportTime.compareStringDates(endDate, date) == -1) {
                logger.debug("date is greater than the current date reporting window for amobee");
                return null;
            }
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
            logger.error("failed to obtain correct dates for fetching reports ", exception.getMessage());
            return null;
        }

        while (ReportTime.compareStringDates(date, endDate) != 1) {
            responseStr = invokeHTTPUrl(getRequestUrl());
            logger.debug("response from amobee is ", responseStr);
            // parse the json response
            if (responseStr != null && !responseStr.startsWith("{\"error")) {
                JSONObject data = new JSONObject(responseStr).getJSONObject("reportMap");
                Iterator<?> keys = data.keys();

                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    if (data.get(key) instanceof JSONArray) {

                        JSONArray jReportArr = data.getJSONArray(key);
                        ReportResponse.ReportRow row = new ReportResponse.ReportRow();
                        row.impressions = 0;
                        row.clicks = 0;
                        row.request = 0;
                        row.revenue = 0;
                        if (jReportArr.length() > 0) {
                            JSONObject reportRow = jReportArr.getJSONObject(0);
                            if (!decodeBlindedSiteId(key, row)) {
                                logger.debug("Error decoded BlindedSite id in Amobee", key);
                                continue;
                            }
                            row.isSiteData = true;
                            row.impressions = reportRow.getLong("impressions");
                            row.revenue = reportRow.getDouble("revenue");
                            row.request = reportRow.getLong("requests");
                            row.clicks = reportRow.getLong("clicks");
                            row.reportTime = new ReportTime(date, 0);
                            row.slotSize = getReportGranularity();
                            reportResponse.addReportRow(row);
                        }
                    }
                }

            }
            ReportTime reportTime = new ReportTime(date, 0);
            reportTime = ReportTime.getNextDay(reportTime);
            date = reportTime.getStringDate("");
        }
        return reportResponse;
    }

    @Override
    public String getAdvertiserId()
    {
        return (config.getString("amobee.advertiserId"));
    }

    @Override
    public String getName()
    {
        return "amobee";
    }

    @Override
    public ReportGranularity getReportGranularity()
    {
        return ReportGranularity.DAY;
    }

    @Override
    public String getRequestUrl()
    {
        return String.format(baseUrl, reportingKey, date, date);
    }

    @Override
    public double getTimeZone()
    {
        return 0;
    }

    public String getEndDate() throws Exception
    {
        try {
            logger.debug("calculating end date for amobee");
            ReportTime reportTime = ReportTime.getUTCTime();
            reportTime = ReportTime.getPreviousDay(reportTime);
            if (reportTime.getHour() <= ReportReconcilerWindow()) {
                reportTime = ReportTime.getPreviousDay(reportTime);
            }
            return (reportTime.getStringDate(""));
        }
        catch (Exception exception) {
            logger.error("failed to obtain end date inside amobee ", exception.getMessage());
            return "";
        }
    }

    @Override
    public ReportResponse fetchRows(DebugLogger logger, ReportTime startTime, String key, ReportTime endTime)
            throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }
}
