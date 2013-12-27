package com.inmobi.adserve.channels.adnetworks.mopub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.BaseReportingImpl;
import com.inmobi.adserve.channels.api.ReportResponse;
import com.inmobi.adserve.channels.api.ReportTime;


public class DCPMoPubReporting extends BaseReportingImpl {
    private static final Logger LOG               = LoggerFactory.getLogger(DCPMoPubReporting.class);

    private final Configuration config;
    private String              startDate         = "";
    private String              endDate           = "";
    private static String       responseStartDate = "";
    private static String       responseString    = "";
    private String              host              = "";
    private String              accountId         = "";
    private String              secretKey         = "";
    private static final String name              = "mopub";

    public DCPMoPubReporting(final Configuration config) {
        this.config = config;
        host = config.getString("mopub.reportUrl");
        accountId = config.getString("mopub.accountId");
        secretKey = config.getString("mopub.secretKey");
    }

    @Override
    public ReportResponse fetchRows(final ReportTime startTime, final String key, final ReportTime endTime)
            throws Exception {
        ReportResponse reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);
        LOG.debug("inside fetch rows of MoPub");
        try {
            startDate = startTime.getStringDate("-");
            endDate = endTime == null ? getEndDate() : startDate;
            LOG.debug("start date inside MoPub is {}", startDate);
            if (ReportTime.compareStringDates(endDate, startDate) == -1) {
                LOG.debug("end date is less than start date plus reporting window for MoPub");
                return null;
            }
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
            LOG.info("failed to obtain correct dates for fetching reports {}", exception);
            return null;
        }

        if (StringUtils.isBlank(responseStartDate) || StringUtils.isBlank(responseString)
                || ReportTime.compareStringDates(responseStartDate, startDate) > 0) {
            String url = getRequestUrl();
            LOG.debug("url inside MoPub is {}", url);
            responseString = invokeHTTPUrl(url);
            responseStartDate = startDate;
        }
        reportResponse.status = ReportResponse.ResponseStatus.SUCCESS;

        LOG.debug("successfuly got response inside MoPub. Response is {}", responseString);
        generateReportResponse(reportResponse, responseString, key);
        LOG.debug("successfully parsed data inside MoPub");
        return reportResponse;
    }

    public String getEndDate() throws Exception {
        try {
            LOG.debug("calculating latest date for MoPub");
            ReportTime reportTime = ReportTime.getUTCTime();
            reportTime = ReportTime.getPreviousDay(reportTime);
            reportTime = ReportTime.getPreviousDay(reportTime);
            if (reportTime.getHour() <= ReportReconcilerWindow()) {
                reportTime = ReportTime.getPreviousDay(reportTime);
            }
            return (reportTime.getStringDate("-"));
        }
        catch (Exception exception) {
            LOG.info("failed to obtain end date inside MoPub {}", exception);
            return null;
        }
    }

    @Override
    public String getRequestUrl() {
        try {
            return String.format(host, accountId, secretKey, startDate, endDate,
                URLEncoder.encode("date,adunit", "UTF-8"));
        }
        catch (UnsupportedEncodingException e) {
            LOG.info("Erorr encoding URL");
        }
        return null;
    }

    @Override
    public double getTimeZone() {
        return -7;
    }

    @Override
    public ReportGranularity getReportGranularity() {
        return ReportGranularity.DAY;
    }

    @Override
    public int ReportReconcilerWindow() {
        return 10;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAdvertiserId() {
        return (config.getString("mopub.advertiserId"));
    }

    @Override
    protected String invokeHTTPUrl(final String url) throws MalformedURLException, IOException {
        URLConnection conn = new URL(url).openConnection();
        String authStr = accountId + ":" + secretKey;
        String authEncoded = new String(Base64.encodeBase64(authStr.getBytes()));
        conn.setRequestProperty("Authorization", "Basic " + authEncoded);
        conn.setDoOutput(true);
        InputStream in = conn.getInputStream();
        BufferedReader res = null;
        StringBuffer sBuffer = new StringBuffer();
        try {
            res = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String inputLine;
            while ((inputLine = res.readLine()) != null) {
                sBuffer.append(inputLine).append("\n");
            }
        }
        catch (IOException ioe) {
            LOG.info("Error in Httpool invokeHTTPUrl : {}", ioe.getMessage());
        }
        finally {
            if (res != null) {
                res.close();
            }
        }

        return sBuffer.toString();

    }

    private void generateReportResponse(final ReportResponse reportResponse, final String response, final String key)
            throws JSONException {
        Boolean dataFound = false;
        JSONObject responseObject = new JSONObject(response);
        JSONArray responseHeader = responseObject.getJSONArray("columns");
        if (responseHeader.length() > 1) {
            int impressionIndex = -1;
            int clicksIndex = -1;
            int revenueIndex = -1;
            int requestIndex = -1;
            int dateIndex = -1;
            int adUnitIndex = -1;

            for (int i = 0; i < responseHeader.length(); i++) {
                if ("impressions".equalsIgnoreCase(responseHeader.getString(i).trim())) {
                    impressionIndex = i;
                    continue;
                }
                else if ("clicks".equalsIgnoreCase(responseHeader.getString(i).trim())) {
                    clicksIndex = i;
                    continue;
                }
                else if ("revenue".equalsIgnoreCase(responseHeader.getString(i).trim())) {
                    revenueIndex = i;
                    continue;
                }
                else if ("attempts".equalsIgnoreCase(responseHeader.getString(i).trim())) {
                    requestIndex = i;
                    continue;
                }
                else if ("date".equalsIgnoreCase(responseHeader.getString(i).trim())) {
                    dateIndex = i;
                    continue;
                }
                else if ("adunit".equalsIgnoreCase(responseHeader.getString(i).trim())) {
                    adUnitIndex = i;
                    continue;
                }

            }
            JSONArray responseArray = responseObject.getJSONArray("rows");
            for (int j = 0; j < responseArray.length(); j++) {
                JSONArray revenueArray = responseArray.getJSONArray(j);
                if (revenueArray.length() > 0 && key.equals(revenueArray.getString(adUnitIndex))) {
                    ReportResponse.ReportRow row = new ReportResponse.ReportRow();
                    row.impressions = revenueArray.getLong(impressionIndex);
                    row.revenue = revenueArray.getDouble(revenueIndex);
                    row.request = revenueArray.getLong(requestIndex);
                    row.clicks = revenueArray.getLong(clicksIndex);
                    row.reportTime = new ReportTime(revenueArray.getString(dateIndex), 0);
                    row.slotSize = getReportGranularity();
                    row.siteId = key;
                    dataFound = true;
                    reportResponse.addReportRow(row);
                }
            }

            if (!dataFound) {
                ReportResponse.ReportRow row = new ReportResponse.ReportRow();
                row.impressions = 0;
                row.revenue = 0;
                row.request = 0;
                row.clicks = 0;
                row.reportTime = new ReportTime(startDate, 0);
                row.slotSize = getReportGranularity();
                row.siteId = key;
                reportResponse.addReportRow(row);
            }
        }
    }
}