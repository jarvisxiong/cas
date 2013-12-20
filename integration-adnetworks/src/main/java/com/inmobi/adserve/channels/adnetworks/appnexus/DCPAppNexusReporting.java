package com.inmobi.adserve.channels.adnetworks.appnexus;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.inmobi.adserve.channels.api.BaseReportingImpl;
import com.inmobi.adserve.channels.api.ReportResponse;
import com.inmobi.adserve.channels.api.ReportTime;
import com.inmobi.adserve.channels.api.ServerException;
import com.inmobi.adserve.channels.util.DebugLogger;


public class DCPAppNexusReporting extends BaseReportingImpl {
    private final Configuration config;
    private final String        password;
    private String              date;
    private final String        host;
    private final String        authUrl;
    private final String        downloadUrl;
    private final String        userName;
    private DebugLogger         logger;
    private String              endDate;
    private final String        name;
    private final String        startDateFormatter = "%s 00:00:00";
    private final String        endDateFormatter   = "%s 23:59:59";
    private boolean             isTokenGenerated   = false;
    private String              token              = null;
    private static String       reportData         = null;

    public DCPAppNexusReporting(final Configuration config, final String name) {
        this.config = config;
        password = config.getString(name + ".password");
        host = config.getString(name + ".host");
        authUrl = config.getString(name + ".authUrl");
        downloadUrl = config.getString(name + ".downloadUrl");
        userName = config.getString(name + ".user");
        this.name = name;
    }

    @Override
    public int ReportReconcilerWindow() {
        return 1;
    }

    @Override
    public ReportResponse fetchRows(final DebugLogger logger, final ReportTime startTime, final String key,
            final ReportTime endTime) throws Exception, JSONException {
        this.logger = logger;
        ReportResponse reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);

        logger.debug("inside fetch rows of ", name);
        try {
            date = startTime.getStringDate("-");
            endDate = endTime == null ? getEndDate() : date;

            if (ReportTime.compareStringDates(endDate, date) == -1) {
                logger.debug("date is greater than the current date reporting window for ", name);
                return null;
            }
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
            logger.info("failed to obtain correct dates for fetching reports ", exception.getMessage());
            return null;
        }
        if (!Character.isDigit(key.charAt(0)) || key.contains("_")) {
            logger.debug("invalid key", key);
            return null;
        }
        if (!isTokenGenerated) {
            String tokenResponse = invokeUrl(authUrl, getRequestToken(), null);
            token = new JSONObject(tokenResponse).getJSONObject("response").getString("token");
            isTokenGenerated = true;
        }

        if (StringUtils.isBlank(reportData)) {
            for (int i = 0; i < 20; i++) {
                String reportIdResponse = invokeUrl(host, getRequestObject(key), token);
                String reportId = new JSONObject(reportIdResponse).getJSONObject("response").getString("report_id");
                Thread.sleep(30000);
                reportData = invokeUrl(String.format(downloadUrl, reportId), token);
                if (StringUtils.isNotBlank(reportData)) {
                    break;
                }

            }
        }
        generateReportResponse(logger, reportResponse, reportData.split("\n"), key);
        return reportResponse;
    }

    @Override
    public String getAdvertiserId() {
        return (config.getString(name + ".advertiserId"));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ReportGranularity getReportGranularity() {
        return ReportGranularity.DAY;
    }

    @Override
    public String getRequestUrl() {
        return host;
    }

    @Override
    public double getTimeZone() {
        return 1;
    }

    public String getEndDate() {
        try {
            logger.debug("calculating end date for ", name);
            ReportTime reportTime = ReportTime.getUTCTime();
            reportTime = ReportTime.getPreviousDay(reportTime);
            reportTime = ReportTime.getPreviousDay(reportTime);
            if (reportTime.getHour() <= ReportReconcilerWindow()) {
                reportTime = ReportTime.getPreviousDay(reportTime);
            }
            return (reportTime.getStringDate("-"));
        }
        catch (Exception exception) {
            logger.info("failed to obtain end date inside  ", name, exception.getMessage());
            return "";
        }
    }

    private JSONObject getRequestToken() throws JSONException, ServerException {

        JSONObject userParams = new JSONObject();
        userParams.put("password", password);
        userParams.put("username", userName);
        JSONObject authParams = new JSONObject();
        authParams.put("auth", userParams);
        return authParams;
    }

    private JSONObject getRequestObject(final String extSiteKey) throws JSONException {
        JSONArray columnArray = new JSONArray();
        columnArray.put("day");
        columnArray.put("placement_id");
        columnArray.put("clicks");
        columnArray.put("imps_total");
        columnArray.put("publisher_revenue");
        JSONObject requestObject = new JSONObject();
        requestObject.put("columns", columnArray);
        // requestObject.put("filters", filtersArray);
        requestObject.put("report_type", "publisher_analytics");
        requestObject.put("start_date", String.format(startDateFormatter, date));
        requestObject.put("end_date", String.format(endDateFormatter, endDate));
        JSONObject reportObject = new JSONObject();
        reportObject.put("report", requestObject);
        return reportObject;
    }

    public String invokeUrl(final String host, final JSONObject queryObject, final String token) throws HttpException,
            IOException {
        URL url = new URL(host);
        HttpURLConnection.setFollowRedirects(false);
        HttpURLConnection hconn = (HttpURLConnection) url.openConnection();
        hconn.setRequestMethod("POST");
        if (token != null) {
            hconn.setRequestProperty("Authorization", token);
        }
        hconn.setDoInput(true);
        hconn.setDoOutput(true);
        hconn.setRequestProperty("Content-Type", "application/json");
        DataOutputStream wr = new DataOutputStream(hconn.getOutputStream());
        wr.writeBytes(queryObject.toString());
        wr.flush();
        wr.close();

        StringBuilder sBuffer = new StringBuilder();
        BufferedReader res = new BufferedReader(new InputStreamReader(hconn.getInputStream(), "UTF-8"));
        String inputLine;
        while ((inputLine = res.readLine()) != null) {
            sBuffer.append(inputLine);
        }
        res.close();
        return sBuffer.toString();
    }

    public String invokeUrl(final String host, final String token) throws HttpException, IOException {
        URL url = new URL(host);
        HttpURLConnection.setFollowRedirects(false);
        HttpURLConnection hconn = (HttpURLConnection) url.openConnection();
        hconn.setRequestMethod("GET");
        hconn.setRequestProperty("Authorization", token);
        hconn.setDoInput(true);
        hconn.setDoOutput(true);
        hconn.setRequestProperty("Content-Type", "application/json");
        StringBuilder sBuffer = new StringBuilder();
        BufferedReader res = new BufferedReader(new InputStreamReader(hconn.getInputStream(), "UTF-8"));
        String inputLine;
        while ((inputLine = res.readLine()) != null) {
            sBuffer.append(inputLine);
            sBuffer.append("\n");
        }
        res.close();
        return sBuffer.toString();
    }

    private void generateReportResponse(final DebugLogger logger, final ReportResponse reportResponse,
            final String[] responseArray, final String extSiteKey) throws ParseException {
        if (responseArray.length > 1) {
            int impressionIndex = -1;
            int clicksIndex = -1;
            int revenueIndex = -1;
            int dateIndex = -1;
            int placementIdIndex = -1;

            String[] header = responseArray[0].replace("'", "").split(",");

            for (int i = 0; i < header.length; i++) {
                if ("imps_total".equalsIgnoreCase(header[i].trim())) {
                    impressionIndex = i;
                    continue;
                }
                else if ("clicks".equalsIgnoreCase(header[i].trim())) {
                    clicksIndex = i;
                    continue;
                }
                else if ("publisher_revenue".equalsIgnoreCase(header[i].trim())) {
                    revenueIndex = i;
                    continue;
                }
                else if ("day".equalsIgnoreCase(header[i].trim())) {
                    dateIndex = i;
                    continue;
                }
                else if ("placement_id".equalsIgnoreCase(header[i].trim())) {
                    placementIdIndex = i;
                    continue;
                }
            }

            for (int j = 1; j < responseArray.length; j++) {

                String[] reportRow = responseArray[j].split(",");
                if (reportRow.length == 0) {
                    continue;
                }

                if (!extSiteKey.equalsIgnoreCase(reportRow[placementIdIndex])) {
                    continue;
                }

                ReportResponse.ReportRow row = new ReportResponse.ReportRow();
                row.siteId = extSiteKey;
                row.impressions = Long.parseLong(reportRow[impressionIndex]);
                row.revenue = Double.parseDouble(reportRow[revenueIndex]);
                row.clicks = Long.parseLong(reportRow[clicksIndex]);
                row.reportTime = new ReportTime(reportRow[dateIndex], 0);
                row.slotSize = getReportGranularity();
                reportResponse.addReportRow(row);
            }
        }

        if (reportResponse.rows.size() > 0) {
            logger.debug("successfully generated reporting log for external site id : ", extSiteKey);
        }
        else {
            logger.debug("failed to generate reporting log for external site id : ", extSiteKey);
            logger.debug("Default row added external site id : ", extSiteKey);
            ReportResponse.ReportRow row = new ReportResponse.ReportRow();
            row.siteId = extSiteKey;
            row.impressions = 0;
            row.revenue = 0;
            row.request = 0;
            row.clicks = 0;
            row.reportTime = new ReportTime(endDate, 0);
            row.slotSize = getReportGranularity();
            reportResponse.addReportRow(row);
        }
    }
}