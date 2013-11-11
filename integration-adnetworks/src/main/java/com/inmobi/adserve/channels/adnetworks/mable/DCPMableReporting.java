package com.inmobi.adserve.channels.adnetworks.mable;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;

import org.apache.commons.configuration.Configuration;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONObject;

import com.inmobi.adserve.channels.api.BaseReportingImpl;
import com.inmobi.adserve.channels.api.ReportResponse;
import com.inmobi.adserve.channels.api.ReportTime;
import com.inmobi.adserve.channels.api.ServerException;
import com.inmobi.adserve.channels.util.DebugLogger;


public class DCPMableReporting extends BaseReportingImpl {
    private final Configuration config;
    private String              date;
    private String              dbDate;
    private final String        baseUrl;
    private DebugLogger         logger;
    private final String        authKey;
    private String              endDate = "";
    private Connection          connection;

    public DCPMableReporting(final Configuration config, final Connection connection) {

        this.config = config;
        this.connection = connection;
        baseUrl = config.getString("mable.baseUrl");
        authKey = config.getString("mable.authKey");
    }

    private String invokeHTTPUrl(final String url, final String urlParameters) throws ServerException,
            ClientProtocolException, IOException, IllegalStateException {
        logger.debug("url inside mable is ", url);
        URLConnection conn = new URL(url).openConnection();
        // Setting connection and read timeout to 5 min
        conn.setReadTimeout(300000);
        conn.setConnectTimeout(300000);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();
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

    @Override
    public int ReportReconcilerWindow() {
        return 5;
    }

    @Override
    public ReportResponse fetchRows(final DebugLogger logger, final ReportTime startTime, final ReportTime endTime)
            throws Exception {
        this.logger = logger;
        ReportResponse reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);
        String responseStr = null;
        logger.debug("inside fetch rows of mable");
        try {
            date = startTime.getStringDate("");
            dbDate = startTime.getStringDate("-");
            logger.debug("start date inside mable is ", date);
            endDate = endTime == null ? getEndDate() : date;
            if (ReportTime.compareStringDates(endDate, date) == -1) {
                logger.debug("date is greater than the current date reporting window for mable");
                return null;
            }
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
            logger.error("failed to obtain correct dates for fetching reports ", exception.getMessage());
            return null;
        }
        while (ReportTime.compareStringDates(date, endDate) != 1) {
            JSONObject requestObject = new JSONObject();
            requestObject.put("auth_key", authKey);
            requestObject.put("report_dt", date);

            responseStr = invokeHTTPUrl(getRequestUrl(), requestObject.toString());
            logger.debug("response from mable is ", responseStr);
            if (responseStr != null && !responseStr.startsWith("{\"error")) {
                JSONArray reportArray = new JSONArray(responseStr);
                for (int i = 0; i < reportArray.length(); i++) {
                    JSONObject reportRow = reportArray.getJSONObject(i);
                    ReportResponse.ReportRow row = new ReportResponse.ReportRow();
                    String key = reportRow.getString("blind_id");
                    if (!decodeBlindedSiteId(key, row)) {
                        logger.debug("Error decoded BlindedSite id in Mable", key);
                        continue;
                    }
                    row.isSiteData = true;
                    row.impressions = reportRow.getLong("imps");
                    row.revenue = reportRow.getDouble("revenue");
                    double rate = getCurrencyConversionRate("KRW", dbDate, logger, connection);
                    if (rate > 0) {
                        row.revenue /= rate;
                    }
                    else {
                        logger.info("Failed to get KWR to USD rate for ", date);
                        return null;
                    }

                    row.request = reportRow.getLong("reqs");
                    row.clicks = reportRow.getLong("clks");
                    row.reportTime = new ReportTime(date, 0);
                    row.slotSize = getReportGranularity();
                    reportResponse.addReportRow(row);

                }
            }
            ReportTime reportTime = new ReportTime(date, 0);
            reportTime = ReportTime.getNextDay(reportTime);
            date = reportTime.getStringDate("");
            dbDate = reportTime.getStringDate("-");
        }
        return reportResponse;
    }

    @Override
    public String getAdvertiserId() {
        return (config.getString("mable.advertiserId"));
    }

    @Override
    public String getName() {
        return "mable";
    }

    @Override
    public ReportGranularity getReportGranularity() {
        return ReportGranularity.DAY;
    }

    @Override
    public String getRequestUrl() {
        return baseUrl;
    }

    @Override
    public double getTimeZone() {
        return 0;
    }

    public String getEndDate() throws Exception {
        try {
            logger.debug("calculating end date for mable");
            ReportTime reportTime = ReportTime.getUTCTime();
            reportTime = ReportTime.getPreviousDay(reportTime);
            if (reportTime.getHour() <= ReportReconcilerWindow()) {
                reportTime = ReportTime.getPreviousDay(reportTime);
            }
            return (reportTime.getStringDate(""));
        }
        catch (Exception exception) {
            logger.error("failed to obtain end date inside mable ", exception.getMessage());
            return "";
        }
    }

    @Override
    public ReportResponse fetchRows(DebugLogger logger, ReportTime startTime, String key, ReportTime endTime)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }
}
