package com.inmobi.adserve.channels.adnetworks.appier;

import org.apache.commons.configuration.Configuration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.BaseReportingImpl;
import com.inmobi.adserve.channels.api.ReportResponse;
import com.inmobi.adserve.channels.api.ReportTime;


public class DCPAppierReporting extends BaseReportingImpl {
    private static final Logger LOG            = LoggerFactory.getLogger(DCPAppierReporting.class);

    private final Configuration config;
    private String              startDate      = "";
    private String              endDate        = "";
    private String              responseString = "";
    private String              host           = "";
    private String              publisherId    = "";
    private String              password       = "";

    public DCPAppierReporting(final Configuration config) {
        this.config = config;
        host = config.getString("appier.reportUrl");
        publisherId = config.getString("appier.pubId");
        password = config.getString("appier.password");
    }

    @Override
    public ReportResponse fetchRows(final ReportTime startTime, final ReportTime endTime) throws Exception {
        ReportResponse reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);
        LOG.debug("inside fetch rows of appier");
        try {
            startDate = startTime.getStringDate("");
            endDate = endTime == null ? getEndDate() : (ReportTime.getNextDay(startTime)).getStringDate("");
            LOG.debug("start date inside appier is {}", startDate);
            if (ReportTime.compareStringDates(endDate, startDate) == -1) {
                LOG.debug("end date is less than start date plus reporting window for appier");
                return null;
            }
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
            LOG.info("failed to obtain correct dates for fetching reports {}", exception);
            return null;
        }

        String url = getRequestUrl();
        LOG.debug("url inside appier is {}", url);
        responseString = invokeHTTPUrl(url);
        reportResponse.status = ReportResponse.ResponseStatus.SUCCESS;

        JSONArray responseArray = new JSONObject(responseString).getJSONArray("report");
        LOG.debug("successfuly got response inside appier. Number of lines of response is {}", responseArray.length());
        generateReportResponse(reportResponse, responseArray);
        ReportTime reportTime = new ReportTime(startDate, 0);
        reportTime = ReportTime.getNextDay(reportTime);
        startDate = reportTime.getStringDate("-");
        LOG.debug("start date inside appier now is {}", startDate);

        LOG.debug("successfully parsed data inside appier");
        return reportResponse;
    }

    public String getEndDate() throws Exception {
        try {
            LOG.debug("calculating latest date for appier");
            ReportTime reportTime = ReportTime.getUTCTime();
            // reportTime = ReportTime.getPreviousDay(reportTime);
            if (reportTime.getHour() <= ReportReconcilerWindow()) {
                reportTime = ReportTime.getPreviousDay(reportTime);
            }
            return (reportTime.getStringDate(""));
        }
        catch (Exception exception) {
            LOG.info("failed to obtain end date inside appier {}", exception);
            return null;
        }
    }

    @Override
    public String getRequestUrl() {

        return String.format(host, publisherId, password, startDate, endDate);
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
        return 18;
    }

    @Override
    public String getName() {
        return "Appier";
    }

    @Override
    public String getAdvertiserId() {
        return (config.getString("appier.advertiserId"));
    }

    private void generateReportResponse(final ReportResponse reportResponse, final JSONArray responseArray)
            throws JSONException {

        for (int i = 0; i < responseArray.length(); i++) {
            JSONObject reportRow = responseArray.getJSONObject(i);
            ReportResponse.ReportRow row = new ReportResponse.ReportRow();
            if (!decodeBlindedSiteId(reportRow.getString("bsiteid"), row)) {
                LOG.debug("Error decoded BlindedSite id in appier {}", reportRow.getString("bsiteid"));
                continue;
            }
            row.isSiteData = true;
            row.impressions = reportRow.getLong("impression");
            row.revenue = reportRow.getDouble("revenue");
            row.request = reportRow.getLong("request");
            row.clicks = reportRow.getLong("clicks");
            row.reportTime = new ReportTime(reportRow.getString("date"), 0);
            row.slotSize = getReportGranularity();
            reportResponse.addReportRow(row);

        }
    }
}
