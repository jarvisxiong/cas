package com.inmobi.adserve.channels.adnetworks.verve;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.Configuration;
import org.json.JSONArray;
import org.json.JSONObject;

import com.inmobi.adserve.channels.api.BaseReportingImpl;
import com.inmobi.adserve.channels.api.ReportResponse;
import com.inmobi.adserve.channels.api.ReportTime;
import com.inmobi.adserve.channels.util.DebugLogger;


public class DCPVerveReporting extends BaseReportingImpl {
    private String        startDate;
    private String        endDate;
    private DebugLogger   logger;
    private final String  host;
    private final String  advertiserId;
    private final String  token;
    private static String reportStartDate  = null;
    private static String entireReportData = null;

    public DCPVerveReporting(final Configuration config) {
        this.host = config.getString("verve.host");
        this.advertiserId = config.getString("verve.advertiserId");
        this.token = config.getString("verve.token");
    }

    @Override
    public ReportResponse fetchRows(DebugLogger logger, ReportTime startTime, String key, ReportTime endTime)
            throws Exception {
        this.logger = logger;
        ReportResponse reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);
        logger.debug("inside fetch rows of verve");
        try {
            this.startDate = startTime.getStringDate("-");
            logger.debug("start date inside verve is ", this.startDate);
            endDate = endTime == null ? getEndDate("-") : startDate;
            if (ReportTime.compareStringDates(this.endDate, this.startDate) == -1) {
                logger.debug("date is greater than the current date reporting window for verve");
                return null;
            }
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
            logger.info("failed to obtain correct dates for fetching reports ", exception.getMessage());
            return null;
        }
        if (reportStartDate == null || (ReportTime.compareStringDates(reportStartDate, startDate) > 0)) {
            entireReportData = invokeHttpUrl(getRequestUrl());
            logger.debug("Response from Verve : ", entireReportData);
            reportStartDate = startDate;
        }

        JSONObject jsonResponse = new JSONObject(entireReportData);
        JSONArray jsonObjectArray = jsonResponse.getJSONArray("entries");
        for (int i = 0; i < jsonObjectArray.length(); i++) {
            JSONObject jsonReportObject = jsonObjectArray.getJSONObject(i);
            if (key.equals(jsonReportObject.getString("property_keyname"))) {
                ReportResponse.ReportRow row = new ReportResponse.ReportRow();
                row.request = 0;
                row.clicks = ("null".equals(jsonReportObject.getString("clicks"))) ? 0 : jsonReportObject
                        .getLong("clicks");
                row.impressions = ("null".equals(jsonReportObject.getString("impressions"))) ? 0 : jsonReportObject
                        .getLong("impressions");
                row.revenue = ("null".equals(jsonReportObject.getString("net_revenue"))) ? 0 : jsonReportObject
                        .getDouble("net_revenue");
                ReportTime reportDate = new ReportTime(jsonReportObject.getString("date"), 0);
                row.ecpm = ("null".equals(jsonReportObject.getString("net_ecpm"))) ? 0 : jsonReportObject
                        .getDouble("net_ecpm");
                row.reportTime = reportDate;
                row.siteId = key;
                row.slotSize = getReportGranularity();
                logger.debug("parsing data inside verve", row.request);
                reportResponse.addReportRow(row);
            }
        }

        return reportResponse;
    }

    @Override
    public String getRequestUrl() {
        StringBuilder reportUrl = new StringBuilder(host);
        reportUrl
                .append("&by=date|property_keyname&where[date.between]=")
                    .append(startDate)
                    .append("..")
                    .append(endDate);
        return reportUrl.toString();
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
        return 19;
    }

    @Override
    public String getName() {
        return "verve";
    }

    @Override
    public String getAdvertiserId() {
        return (advertiserId);
    }

    public String getEndDate(final String seperator) {
        try {
            logger.debug("calculating end date for Verve");
            ReportTime reportTime = ReportTime.getUTCTime();
            reportTime = ReportTime.getPreviousDay(reportTime);
            reportTime = ReportTime.getPreviousDay(reportTime);
            if (reportTime.getHour() < ReportReconcilerWindow()) {
                reportTime = ReportTime.getPreviousDay(reportTime);
            }
            return (reportTime.getStringDate(seperator));
        }
        catch (Exception exception) {
            logger.info("failed to obtain end date inside verve ", exception.getMessage());
            return "";
        }
    }

    private String invokeHttpUrl(String requestUrl) throws IOException, NoSuchAlgorithmException,
            KeyManagementException {

        URL url = new URL(requestUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Basic " + new String(Base64.encodeBase64(token.getBytes())));
        StringBuilder responseBuilder = new StringBuilder();
        BufferedReader reader = null;
        try {
            String line;
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
        }
        catch (IOException ioe) {
            logger.info("Error in Httpool invokeHTTPUrl : ", ioe.getMessage());
        }
        finally {
            if (reader != null) {
                reader.close();
            }
            connection.disconnect();
        }
        return responseBuilder.toString();
    }
}