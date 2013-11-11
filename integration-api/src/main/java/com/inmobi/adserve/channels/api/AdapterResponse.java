package com.inmobi.adserve.channels.api;

import java.util.ArrayList;


public class AdapterResponse {

    public static class ResponseRow {
        public ReportTime                           reportTime;
        public long                                 clicks      = -1;
        public long                                 impressions = -1;
        public double                               revenue     = -1;
        public String                               adGroupId;
        public long                                 request     = -1;
        public String                               siteId;
        public String                               advertiserId;
        public double                               ecpm;
        public ReportingInterface.ReportGranularity slotSize;

        public String toLogLine() {
            StringBuilder logLine = new StringBuilder();
            logLine.append("{");
            logLine.append("\"rDt\":\"").append(reportTime.getStringDate("-")).append('\"');
            logLine.append(",\"rHr\":\"").append(reportTime.getHour()).append('\"');
            logLine.append(",\"adGrp\":\"").append(adGroupId).append('\"');
            logLine.append(",\"adv\":\"").append(advertiserId).append('\"');
            logLine.append(",\"extSiteId\":\"").append(siteId).append('\"');
            logLine.append(",\"granularity\":\"").append(slotSize.name()).append('\"');
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
            logLine.append("}\n");
            return logLine.toString();
        }
    }

    public enum ResponseStatus {
        SUCCESS,
        NO_NEW_UPDATE,
        FAIL_SERVER_ERROR,
        FAIL_CONNECTION_ERROR
    }

    public ArrayList<ResponseRow> rows;
    public ResponseStatus         status;

    public AdapterResponse(ResponseStatus status) {
        this.status = status;
        rows = new ArrayList<ResponseRow>();
    }

    public void addReportRow(ResponseRow row) {
        rows.add(row);
    }

    public ResponseStatus getResponseStatus() {
        return status;
    }

    public ArrayList<ResponseRow> getReportRows() {
        return rows;
    }
}
