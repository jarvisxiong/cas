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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.BaseReportingImpl;
import com.inmobi.adserve.channels.api.ReportResponse;
import com.inmobi.adserve.channels.api.ReportTime;


public class DCPVerveReporting extends BaseReportingImpl {
    private static final Logger LOG              = LoggerFactory.getLogger(DCPVerveReporting.class);

    private String              startDate;
    private String              endDate;
    private final String        host;
    private final String        advertiserId;
    private final String        token;
    private static String       reportStartDate  = null;
    private static String       entireReportData = null;

    public DCPVerveReporting(final Configuration config) {
        this.host = config.getString("verve.host");
        this.advertiserId = config.getString("verve.advertiserId");
        this.token = config.getString("verve.token");
    }

    @Override
    public ReportResponse fetchRows(final ReportTime startTime, final String key, final ReportTime endTime)
            throws Exception {
        ReportResponse reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);
        LOG.debug("inside fetch rows of verve");
        try {
            this.startDate = startTime.getStringDate("-");
            LOG.debug("start date inside verve is {}", this.startDate);
            endDate = endTime == null ? getEndDate("-") : startDate;
            if (ReportTime.compareStringDates(this.endDate, this.startDate) == -1) {
                LOG.debug("date is greater than the current date reporting window for verve");
                return null;
            }
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
            LOG.info("failed to obtain correct dates for fetching reports {}", exception);
            return null;
        }
        if (reportStartDate == null || (ReportTime.compareStringDates(reportStartDate, startDate) > 0)) {
            entireReportData = invokeHttpUrl(getRequestUrl());
            LOG.debug("Response from Verve : {}", entireReportData);
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
                LOG.debug("parsing data inside verve {}", row.request);
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
            LOG.debug("calculating end date for Verve");
            ReportTime reportTime = ReportTime.getUTCTime();
            reportTime = ReportTime.getPreviousDay(reportTime);
            reportTime = ReportTime.getPreviousDay(reportTime);
            if (reportTime.getHour() < ReportReconcilerWindow()) {
                reportTime = ReportTime.getPreviousDay(reportTime);
            }
            return (reportTime.getStringDate(seperator));
        }
        catch (Exception exception) {
            LOG.info("failed to obtain end date inside verve {}", exception);
            return "";
        }
    }

    private String invokeHttpUrl(final String requestUrl) throws IOException, NoSuchAlgorithmException,
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
            LOG.info("Error in Httpool invokeHTTPUrl : {}", ioe);
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