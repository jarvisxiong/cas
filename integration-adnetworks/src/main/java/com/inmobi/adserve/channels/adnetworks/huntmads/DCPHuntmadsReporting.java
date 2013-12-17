package com.inmobi.adserve.channels.adnetworks.huntmads;

import java.io.IOException;

import org.apache.commons.configuration.Configuration;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.BaseReportingImpl;
import com.inmobi.adserve.channels.api.ReportResponse;
import com.inmobi.adserve.channels.api.ReportTime;
import com.inmobi.adserve.channels.api.ServerException;


/**
 * @author thushara
 * 
 */
public class DCPHuntmadsReporting extends BaseReportingImpl {
    private static final Logger LOG              = LoggerFactory.getLogger(DCPHuntmadsReporting.class);

    private final Configuration config;
    private final String        token;
    private String              date;
    private final String        baseUrl;
    private final String        zoneListApiUrl;
    private String              endDate;

    private static String       zoneList         = null;
    private static String       reportStartDate  = null;
    private static String       entireReportData = null;

    public DCPHuntmadsReporting(final Configuration config) {
        this.config = config;
        token = config.getString("huntmads.token");
        baseUrl = config.getString("huntmads.baseUrl");
        zoneListApiUrl = config.getString("huntmads.zoneListUrl");
    }

    @Override
    public int ReportReconcilerWindow() {
        return 23;
    }

    @Override
    public ReportResponse fetchRows(final ReportTime startTime, final String key, final ReportTime endTime)
            throws ClientProtocolException, IOException, ServerException, JSONException {
        ReportResponse reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);
        LOG.debug("inside fetch rows of huntmads");
        try {
            date = startTime.getStringDate("-");
            LOG.debug("start date inside huntmads is {}", date);
            endDate = endTime == null ? getEndDate("-") : date;
            if (ReportTime.compareStringDates(endDate, date) == -1) {
                LOG.debug("date is greater than the current date reporting window for huntmads");
                return null;
            }
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
            LOG.info("failed to obtain correct dates for fetching reports {}", exception);
            return null;
        }
        date = startTime.getStringDate("-");
        if (zoneList == null) {
            zoneList = invokeHTTPUrl(zoneListApiUrl);
        }
        if (reportStartDate == null || (ReportTime.compareStringDates(reportStartDate, date) > 0)) {
            entireReportData = invokeHTTPUrl(getRequestUrl());
            reportStartDate = date;
        }
        LOG.debug("response is {}", entireReportData);

        // parse the json response
        String reportingZoneId = null;
        JSONObject zoneIdMapObject = new JSONObject(zoneList);
        JSONArray zoneArray = zoneIdMapObject.getJSONArray("zone");
        for (int i = 0; i < zoneArray.length(); i++) {
            JSONObject zoneRow = zoneArray.getJSONObject(i);
            JSONObject zoneAttribute = zoneRow.getJSONObject("@attributes");
            if (zoneAttribute.getString("adserver_id").equals(key)) {
                reportingZoneId = zoneAttribute.getString("id");
                break;
            }
        }
        if (reportingZoneId == null) {
            LOG.info("Zone reporting id not found for zone : {}", key);
            return addDefaultReportRow(key, reportResponse);
        }
        if ("[]".equals(entireReportData)) {
            // No Data found for the specified date. so updating the rows with 0
            return addDefaultReportRow(key, reportResponse);
        }
        JSONObject reportData = new JSONObject(entireReportData);
        JSONArray reportDataArrayForZone = reportData.getJSONArray("zone");

        for (int i = 0; i < reportDataArrayForZone.length(); i++) {
            JSONObject reportRow = reportDataArrayForZone.getJSONObject(i);
            JSONObject reportAttribute = reportRow.getJSONObject("@attributes");
            if (reportAttribute.getString("id").equals(reportingZoneId)) {
                JSONObject reportObject = reportRow.getJSONObject("report");
                String reportDate = reportObject.getString("date");
                if (date.compareTo(reportDate) < 1) {
                    generateReportRow(key, reportResponse, reportObject, reportDate);
                }

            }
        }

        if (reportResponse.rows.size() == 0) {
            return addDefaultReportRow(key, reportResponse);
        }
        return reportResponse;
    }

    /**
     * @param logger
     * @param key
     * @param reportResponse
     * @param reportRow
     * @param reportDate
     * @throws JSONException
     */
    private void generateReportRow(final String key, final ReportResponse reportResponse, final JSONObject reportRow,
            final String reportDate) throws JSONException {
        ReportResponse.ReportRow row = new ReportResponse.ReportRow();
        String reqStr = (reportRow.getString("requests") != null && !"null".equalsIgnoreCase(reportRow
                .getString("requests"))) ? (reportRow.getString("requests").trim()) : "0";
        row.request = Long.parseLong(reqStr);
        row.clicks = Long.parseLong((reportRow.getString("clicks") != null && !"null".equalsIgnoreCase(reportRow
                .getString("clicks"))) ? (reportRow.getString("clicks").trim()) : "0");
        row.impressions = Long.parseLong((reportRow.getString("impressions") != null && !"null"
                .equalsIgnoreCase(reportRow.getString("impressions"))) ? (reportRow.getString("impressions").trim())
                : "0");
        row.revenue = Double.parseDouble((reportRow.getString("revenue") != null && !"null".equalsIgnoreCase(reportRow
                .getString("revenue"))) ? (reportRow.getString("revenue").trim()) : "0.00");
        row.siteId = key;
        row.reportTime = new ReportTime(reportDate, 0);
        row.slotSize = getReportGranularity();
        LOG.debug("parsing data inside huntmads {}", row.request);
        reportResponse.addReportRow(row);
    }

    @Override
    public String getAdvertiserId() {
        return (config.getString("huntmads.advertiserId"));
    }

    @Override
    public String getName() {
        return "Huntmads";
    }

    @Override
    public ReportGranularity getReportGranularity() {
        return ReportGranularity.DAY;
    }

    @Override
    public String getRequestUrl() {
        StringBuilder url = new StringBuilder();
        url.append(baseUrl).append("?token=").append(token);
        url.append("&p=start=").append(date).append(";end=").append(endDate);
        url.append(";group=day");
        url.append("&format=json");
        return url.toString();
    }

    @Override
    public double getTimeZone() {
        return -3;
    }

    public String getEndDate(final String seperator) {
        try {
            LOG.debug("calculating end date for huntmads");
            ReportTime reportTime = ReportTime.getUTCTime();
            reportTime = ReportTime.getPreviousDay(reportTime);
            reportTime = ReportTime.getPreviousDay(reportTime);
            if (reportTime.getHour() <= ReportReconcilerWindow()) {
                reportTime = ReportTime.getPreviousDay(reportTime);
            }
            return (reportTime.getStringDate(seperator));
        }
        catch (Exception exception) {
            LOG.info("failed to obtain end date inside huntmads {}", exception);
            return "";
        }
    }

    private ReportResponse addDefaultReportRow(final String key, final ReportResponse reportResponse) {
        LOG.debug("coming here to get log_date");
        ReportResponse.ReportRow row = new ReportResponse.ReportRow();
        row.request = 0;
        row.clicks = 0;
        row.impressions = 0;
        row.revenue = 0.0;
        row.siteId = key;
        row.reportTime = new ReportTime(getEndDate("-"), 0);
        row.slotSize = this.getReportGranularity();
        LOG.debug("parsing data inside siquis {}", row.request);
        reportResponse.addReportRow(row);
        return reportResponse;
    }
}
