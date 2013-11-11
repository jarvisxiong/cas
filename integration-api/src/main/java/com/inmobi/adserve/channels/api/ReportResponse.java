package com.inmobi.adserve.channels.api;

import java.util.ArrayList;


public class ReportResponse {

    public static class ReportRow {
        public ReportTime                           reportTime;
        public long                                 clicks      = -1;
        public long                                 impressions = -1;
        public double                               revenue     = -1;
        // public String country;
        public String                               adGroupId;
        public long                                 request     = -1;
        public String                               siteId;
        public long                                 siteIncId;
        public long                                 adGroupIncId;
        public String                               advertiserId;
        public double                               ecpm;
        public ReportingInterface.ReportGranularity slotSize;
        public boolean                              isSiteData  = false; // not segment level data

        public String toLogLine() {
            StringBuilder logLine = new StringBuilder();
            logLine.append("{");
            logLine.append("\"rDt\":\"").append(reportTime.getStringDate("-")).append('\"');
            logLine.append(",\"rHr\":\"").append(reportTime.getHour()).append('\"');
            logLine.append(",\"adv\":\"").append(advertiserId).append('\"');
            if (isSiteData) {
                logLine.append(",\"adGroupIncId\":\"").append(adGroupIncId);
                logLine.append("\",\"siteIncId\":\"").append(siteIncId);
            }
            else {
                logLine.append(",\"extSiteId\":\"").append(siteId);
            }
            logLine.append("\",\"granularity\":\"").append(slotSize.name()).append('\"');
            if (request >= 0) {
                logLine.append(",\"req\":\"").append(request).append('\"');
            }
            if (impressions >= 0) {
                logLine.append(",\"imp\":\"").append(impressions).append('\"');
            }
            if (clicks >= 0) {
                logLine.append(",\"clk\":\"").append(clicks).append('\"');
            }
            if (revenue >= 0) {
                logLine.append(",\"rev\":\"").append(revenue).append('\"');
            }
            logLine.append("}");
            return logLine.toString();
        }
    }

    public enum ResponseStatus {
        SUCCESS,
        NO_NEW_UPDATE,
        FAIL_SERVER_ERROR,
        FAIL_CONNECTION_ERROR,
        FAIL_INVALID_DATE_ERROR
    }

    public ArrayList<ReportRow> rows;
    public ResponseStatus       status;

    public ReportResponse(ResponseStatus status) {
        this.status = status;
        rows = new ArrayList<ReportRow>();
    }

    public void addReportRow(ReportRow row) {
        rows.add(row);
    }

    public ResponseStatus getResponseStatus() {
        return status;
    }

    public ArrayList<ReportRow> getReportRows() {
        return rows;
    }
}