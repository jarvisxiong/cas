package com.inmobi.adserve.channels.adnetworks.siquis;

import java.io.IOException;
import java.sql.Connection;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
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


public class DCPSiquisReporting extends BaseReportingImpl {
    private static final Logger LOG = LoggerFactory.getLogger(DCPSiquisReporting.class);

    private final Configuration config;
    private final String        partnerId;
    private final String        password;
    private String              startDate;
    private String              endDate;
    private final String        host;
    private final Connection    connection;

    public DCPSiquisReporting(final Configuration config, final Connection connection) {
        this.config = config;
        this.connection = connection;
        this.partnerId = config.getString("siquis.partnerId");
        this.password = config.getString("siquis.password");
        this.host = config.getString("siquis.host");
    }

    @Override
    public int ReportReconcilerWindow() {
        return 40;
    }

    @Override
    public ReportResponse fetchRows(final ReportTime startTime, final String key, final ReportTime endTime)
            throws ClientProtocolException, ServerException, IOException, JSONException {
        ReportResponse reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);
        String responseStr = null;
        LOG.debug("inside fetch rows of siquis");
        try {
            this.startDate = startTime.getStringDate("-");
            LOG.debug("start date inside siquis is {}", this.startDate);
            endDate = endTime == null ? getEndDate("-") : startDate;
            if (ReportTime.compareStringDates(this.endDate, this.startDate) == -1) {
                LOG.debug("date is greater than the current date reporting window for siquis");
                return null;
            }
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
            LOG.info("failed to obtain correct dates for fetching reports {}", exception);
            return null;
        }
        responseStr = invokeHTTPUrl(getRequestUrl());
        LOG.debug("response is {}", responseStr);

        boolean gotReport = false;
        // parse the json response
        JSONObject data = new JSONObject(responseStr);
        if (!StringUtils.isEmpty(data.getString(key))) {
            JSONArray jReportArr = data.getJSONArray(key);
            for (int i = 0; i < jReportArr.length(); i++) {
                JSONObject reportRow = jReportArr.getJSONObject(i);
                LOG.debug("json array length is {}", jReportArr.length());
                String logDate = reportRow.getString("log_date");
                if (this.startDate.compareTo(logDate) < 1) {
                    LOG.debug("coming here to get log date");
                    ReportResponse.ReportRow row = new ReportResponse.ReportRow();
                    ReportTime reportDate = new ReportTime(logDate, 0);
                    row.reportTime = reportDate;
                    row.request = Long.parseLong(reportRow.getString("searchs").trim());
                    row.clicks = Long.parseLong(reportRow.getString("clicks").trim());
                    row.impressions = Long.parseLong(reportRow.getString("impression").trim());
                    row.revenue = Double.parseDouble(reportRow.getString("revenue").trim().replaceAll(",", ""));
                    double rate = getCurrencyConversionRate("KRW", reportDate.getStringDate("-"), connection);
                    if (rate > 0) {
                        row.revenue /= rate;
                    }
                    else {
                        LOG.info("Failed to get KWR to USD rate for {}", logDate);
                        return null;
                    }
                    row.siteId = key;
                    row.slotSize = this.getReportGranularity();
                    LOG.debug("parsing data inside siquis {}", row.request);
                    reportResponse.addReportRow(row);
                    gotReport = true;
                }
            }
        }

        if (!gotReport) {
            LOG.debug("coming here to get log_date");
            ReportResponse.ReportRow row = new ReportResponse.ReportRow();
            row.request = 0;
            row.clicks = 0;
            row.impressions = 0;
            row.revenue = 0.0;
            row.siteId = key;
            row.reportTime = new ReportTime(this.startDate, 0);
            row.slotSize = this.getReportGranularity();
            LOG.debug("parsing data inside siquis {}", row.request);
            reportResponse.addReportRow(row);
        }
        return reportResponse;
    }

    @Override
    public String getAdvertiserId() {
        return (config.getString("siquis.advertiserId"));
    }

    @Override
    public String getName() {
        return "siquis";
    }

    @Override
    public ReportGranularity getReportGranularity() {
        return ReportGranularity.DAY;
    }

    @Override
    public String getRequestUrl() {
        String url = String.format("%s?partner_id=%s&passwd=%s", this.host, this.partnerId, this.password);
        if (this.startDate != null) {
            url += String.format("&start_date=%s&end_date=%s", this.startDate, this.endDate);
        }
        return url;
    }

    @Override
    public double getTimeZone() {
        return 9;
    }

    public String getEndDate(final String seperator) {
        try {
            LOG.debug("calculating end date for siquis");
            ReportTime reportTime = ReportTime.getUTCTime();
            reportTime = ReportTime.getPreviousDay(reportTime);
            reportTime = ReportTime.getPreviousDay(reportTime);
            if (reportTime.getHour() < 3) {
                reportTime = ReportTime.getPreviousDay(reportTime);
            }
            return (reportTime.getStringDate(seperator));
        }
        catch (Exception exception) {
            LOG.info("failed to obtain end date inside siquis {}", exception.getMessage());
            return "";
        }
    }

}