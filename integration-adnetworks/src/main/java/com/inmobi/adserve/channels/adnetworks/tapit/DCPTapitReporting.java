package com.inmobi.adserve.channels.adnetworks.tapit;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.configuration.Configuration;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.BaseReportingImpl;
import com.inmobi.adserve.channels.api.ReportResponse;
import com.inmobi.adserve.channels.api.ReportTime;
import com.inmobi.adserve.channels.api.ServerException;


public class DCPTapitReporting extends BaseReportingImpl {
    private static final Logger LOG              = LoggerFactory.getLogger(DCPTapitReporting.class);

    private final Configuration config;
    private final String        token;
    private String              date;
    private final String        baseUrl;
    private static String       reportStartDate  = null;
    private static String       entireReportData = null;

    public DCPTapitReporting(final Configuration config) {
        this.config = config;
        token = config.getString("tapit.token");
        baseUrl = config.getString("tapit.baseUrl");
    }

    @Override
    protected String invokeHTTPUrl(final String url) throws ServerException, ClientProtocolException, IOException,
            IllegalStateException {
        String retStr = null;
        LOG.debug("url inside tapit is {}", url);
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(url);
        HttpResponse response = httpclient.execute(httpget);
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new ServerException("Erroneous status code");
        }
        InputStream is = response.getEntity().getContent();
        StringBuffer buffer = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = is.read(b)) != -1;) {
            buffer.append(new String(b, 0, n));
        }
        retStr = buffer.toString();

        return retStr;
    }

    @Override
    public int ReportReconcilerWindow() {
        return 10;
    }

    @Override
    public ReportResponse fetchRows(final ReportTime startTime, final String key, final ReportTime endTime)
            throws ClientProtocolException, IllegalStateException, ServerException, IOException, JSONException {
        ReportResponse reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);
        String responseStr = null;
        LOG.debug("inside fetch rows of tapit");
        try {
            date = startTime.getStringDate("");
            LOG.debug("start date inside tapit is {}", date);
            String currDate = endTime == null ? getEndDate() : date;
            if (ReportTime.compareStringDates(currDate, date) == -1) {
                LOG.debug("date is greater than the current date reporting window for tapit");
                return null;
            }
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
            LOG.info("failed to obtain correct dates for fetching reports {}", exception);
            return null;
        }

        date = startTime.getStringDate("");

        if (reportStartDate == null || (ReportTime.compareStringDates(reportStartDate, date) != 0)) {
            entireReportData = invokeHTTPUrl(getRequestUrl());
            reportStartDate = date;
        }

        responseStr = entireReportData;
        LOG.debug("response from tapit is {}", responseStr);
        date = startTime.getStringDate("-");

        // parse the json response
        if (responseStr != null && !responseStr.startsWith("{\"error")) {
            JSONObject data = new JSONObject(responseStr);
            JSONArray jReportArr = data.getJSONArray("report");
            ReportResponse.ReportRow row = new ReportResponse.ReportRow();
            row.impressions = 0;
            row.clicks = 0;
            row.request = 0;
            row.revenue = 0;
            for (int i = 0; i < jReportArr.length(); i++) {
                JSONObject reportRow = jReportArr.getJSONObject(i);
                LOG.debug("json array length is {}", jReportArr.length());
                if (reportRow.getString("zone_id").equalsIgnoreCase(key) && reportRow.getString("date").equals(date)) {
                    LOG.debug("coming here to get zone id");
                    String reqStr = (reportRow.getString("requests") != null && !"null".equalsIgnoreCase(reportRow
                            .getString("requests"))) ? (reportRow.getString("requests").trim()) : "0";
                    row.request = Long.parseLong(reqStr);
                    row.clicks = Long.parseLong((reportRow.getString("clicks") != null && !"null"
                            .equalsIgnoreCase(reportRow.getString("clicks"))) ? (reportRow.getString("clicks").trim())
                            : "0");
                    row.impressions = Long.parseLong((reportRow.getString("impressions") != null && !"null"
                            .equalsIgnoreCase(reportRow.getString("impressions"))) ? (reportRow
                            .getString("impressions").trim()) : "0");
                    row.revenue = Double.parseDouble((reportRow.getString("earnings") != null && !"null"
                            .equalsIgnoreCase(reportRow.getString("earnings"))) ? (reportRow.getString("earnings")
                            .trim()) : "0.00");
                    LOG.debug("parsing data inside tapit {}", row.request);
                    break;
                }
            }

            row.reportTime = new ReportTime(date, 0);
            row.advertiserId = this.getAdvertiserId();
            row.siteId = key;
            row.slotSize = getReportGranularity();
            reportResponse.addReportRow(row);
        }

        return reportResponse;
    }

    @Override
    public String getAdvertiserId() {
        return (config.getString("tapit.advertiserId"));
    }

    @Override
    public String getName() {
        return "Tapit";
    }

    @Override
    public ReportGranularity getReportGranularity() {
        return ReportGranularity.DAY;
    }

    @Override
    public String getRequestUrl() {
        return String.format(baseUrl, token, date);
    }

    @Override
    public double getTimeZone() {
        return 0;
    }

    public String getEndDate() throws Exception {
        try {
            LOG.debug("calculating end date for tapit");
            ReportTime reportTime = ReportTime.getUTCTime();
            reportTime = ReportTime.getPreviousDay(reportTime);
            if (reportTime.getHour() <= ReportReconcilerWindow()) {
                reportTime = ReportTime.getPreviousDay(reportTime);
            }
            return (reportTime.getStringDate(""));
        }
        catch (Exception exception) {
            LOG.info("failed to obtain end date inside tapit {}", exception);
            return "";
        }
    }
}