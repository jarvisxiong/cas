package com.inmobi.adserve.channels.adnetworks.lomark;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.configuration.Configuration;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.BaseReportingImpl;
import com.inmobi.adserve.channels.api.ReportResponse;
import com.inmobi.adserve.channels.api.ReportTime;


public class DCPLomarkReporting extends BaseReportingImpl {
    private static final Logger LOG              = LoggerFactory.getLogger(DCPLomarkReporting.class);

    private String              startDate;
    private String              endDate;
    private final String        host;
    private final String        advertiserId;
    private final String        reportKey;
    private final String        reportSecretKey;
    private static String       reportStartDate  = null;
    private static String       entireReportData = null;
    private double              fixedCPCRateUSD  = 0;

    public DCPLomarkReporting(final Configuration config) {
        this.host = config.getString("lomark.host");
        this.advertiserId = config.getString("lomark.advertiserId");
        this.reportKey = config.getString("lomark.key");
        this.reportSecretKey = config.getString("lomark.secretkey");
        this.fixedCPCRateUSD = Double.parseDouble(config.getString("lomark.fixedCPCRateUSD"));
    }

    @Override
    public ReportResponse fetchRows(final ReportTime startTime, final String key, final ReportTime endTime)
            throws Exception {
        if (fixedCPCRateUSD <= 0) {
            LOG.debug("Fixed CPC rate for lomark should be > 0");
            new ReportResponse(ReportResponse.ResponseStatus.FAIL_SERVER_ERROR);
            return null;
        }
        ReportResponse reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);
        LOG.debug("inside fetch rows of lomark");
        try {
            this.startDate = startTime.getStringDate("-");
            LOG.debug("start date inside lomark is {}", this.startDate);
            endDate = endTime == null ? getEndDate("-") : startDate;
            if (ReportTime.compareStringDates(this.endDate, this.startDate) == -1) {
                LOG.debug("date is greater than the current date reporting window for lomark");
                return null;
            }
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
            LOG.info("failed to obtain correct dates for fetching reports {}", exception);
            return null;
        }
        if (reportStartDate == null || (ReportTime.compareStringDates(reportStartDate, startDate) > 0)) {
            entireReportData = invokeHTTPUrl(getRequestUrl());
            reportStartDate = startDate;
        }

        JSONObject jsonResponse = new JSONObject(entireReportData);
        if (jsonResponse.getInt("status") != 100) {
            LOG.info("Error response from Lomark : {}", entireReportData);
            reportStartDate = null;
            return null;
        }
        JSONArray jsonObjectArray = jsonResponse.getJSONArray("data");
        Map<String, ReportResponse.ReportRow> map = new HashMap<String, ReportResponse.ReportRow>();
        for (int i = 0; i < jsonObjectArray.length(); i++) {
            JSONObject jsonReportObject = jsonObjectArray.getJSONObject(i);
            if (key.equals(jsonReportObject.getString("appid"))) {
                String reportDate = jsonReportObject.getString("date");
                ReportResponse.ReportRow row = map.get(reportDate);
                if (row == null) {
                    row = new ReportResponse.ReportRow();
                    row.reportTime = new ReportTime(reportDate, 0);
                    row.siteId = key;
                    row.slotSize = getReportGranularity();
                    row.request = 0;
                    row.impressions = 0;
                    row.ecpm = 0;
                    row.revenue = 0;
                    reportResponse.addReportRow(row);
                    map.put(reportDate, row);
                }
                row.request += jsonReportObject.getLong("request");
                row.clicks += jsonReportObject.getLong("click");
                row.impressions += jsonReportObject.getLong("impression");
                // Calculate the revenue using the fixed CPC
                row.revenue = this.fixedCPCRateUSD * row.clicks;
                if (row.impressions > 0) {
                    row.ecpm = (row.revenue / row.impressions) * 1000;
                }
                LOG.debug("parsing data inside lomark {}", row.request);
            }
        }

        return reportResponse;
    }

    @Override
    public String getRequestUrl() {
        StringBuilder reportUrl = new StringBuilder(host);
        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("key", reportKey);
        reportUrl.append("?key=").append(reportKey);
        requestMap.put("startdate", startDate);
        reportUrl.append("&startdate=").append(startDate);
        requestMap.put("enddate", endDate);
        reportUrl.append("&enddate=").append(endDate);
        String millisec = String.valueOf(Calendar.getInstance().getTimeInMillis()).substring(0, 10);
        reportUrl.append("&timestamp=").append(millisec);
        requestMap.put("timestamp", millisec);
        try {
            reportUrl.append("&sign=").append(getSignature(requestMap, reportSecretKey));
        }
        catch (IOException exception) {
            LOG.info("{}", exception);
        }

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
        return 1;
    }

    @Override
    public String getName() {
        return "lomark";
    }

    @Override
    public String getAdvertiserId() {
        return (advertiserId);
    }

    public String getEndDate(final String seperator) {
        try {
            LOG.debug("calculating end date for lomark");
            ReportTime reportTime = ReportTime.getUTCTime();
            reportTime = ReportTime.getPreviousDay(reportTime);
            reportTime = ReportTime.getPreviousDay(reportTime);
            if (reportTime.getHour() < ReportReconcilerWindow()) {
                reportTime = ReportTime.getPreviousDay(reportTime);
            }
            return (reportTime.getStringDate(seperator));
        }
        catch (Exception exception) {
            LOG.info("failed to obtain end date inside lomark {}", exception);
            return "";
        }
    }

    public static String getSignature(final Map<String, String> params, final String secret) throws IOException {
        // first sort asc as per the paramter names
        Map<String, String> sortedParams = new TreeMap<String, String>(params);
        Set<Entry<String, String>> entrys = sortedParams.entrySet();
        // after sorting，organize all paramters with key=value"format
        StringBuilder basestring = new StringBuilder();
        for (Entry<String, String> param : entrys) {
            basestring.append(param.getKey()).append("=").append(param.getValue());
        }
        basestring.append(secret);
        // MD5 Hashed
        byte[] bytes = DigestUtils.md5(basestring.toString().getBytes());
        StringBuilder sign = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() == 1) {
                sign.append("0");
            }
            sign.append(hex);
        }
        return sign.toString();
    }
}