package com.inmobi.adserve.channels.adnetworks.drawbridge;

import org.apache.commons.configuration.Configuration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.inmobi.adserve.channels.api.BaseReportingImpl;
import com.inmobi.adserve.channels.api.ReportResponse;
import com.inmobi.adserve.channels.api.ReportTime;
import com.inmobi.adserve.channels.api.ServerException;
import com.inmobi.adserve.channels.util.DebugLogger;


public class DrawBridgeReporting extends BaseReportingImpl
{
    private Configuration  config;
    private String         startDate = "";
    private String         endDate   = "";
    private DebugLogger    logger;
    private ReportResponse reportResponse;

    public DrawBridgeReporting(final Configuration config)
    {
        this.config = config;
    }

    @Override
    public ReportResponse fetchRows(DebugLogger logger, ReportTime startTime, String key, ReportTime endTime)
            throws Exception
    {
        // NO OP
        return null;
    }

    // Fetches the report from the TPAN
    @Override
    public ReportResponse fetchRows(final DebugLogger logger, final ReportTime startTime, final ReportTime endTime)
            throws ServerException, JSONException
    {
        this.logger = logger;
        reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);
        logger.debug("inside fetch rows of draw bridge");
        try {
            startDate = startTime.getStringDate("-");
            logger.debug("start date inside drawbridge is " + startDate);
            endDate = endTime == null ? getEndDate() : startDate;
            logger.debug("end date inside drawbridge is " + endDate);
            if (ReportTime.compareStringDates(endDate, startDate) == -1) {
                logger.debug("end date is less than start date plus reporting window for mobile commerce");
                return null;
            }
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
            logger.info("failed to obtain correct dates for fetching reports " + exception.getMessage());
            return null;
        }
        String url = getRequestUrl();
        logger.debug("url inside drawbridge is " + url);
        String responseString = invokeUrl(url, null, logger);
        reportResponse.status = ReportResponse.ResponseStatus.SUCCESS;
        String[] responseArray = responseString.split("\n");
        logger.debug("successfuly got response inside drawbridge. Number of lines of response is "
                + responseArray.length + "response : " + responseString);
        generateReportResponse(logger, reportResponse, responseString);
        logger.debug("successfully parsed data inside drawbridge");
        return reportResponse;
    }

    // obtain end date for the date range in which data is to be fetched
    public String getEndDate() throws Exception
    {
        try {
            logger.debug("calculating end date for drawbridge");
            ReportTime reportTime = ReportTime.getUTCTime();
            reportTime = ReportTime.getPreviousDay(reportTime);
            if (reportTime.getHour() <= ReportReconcilerWindow()) {
                reportTime = ReportTime.getPreviousDay(reportTime);
            }
            return (reportTime.getStringDate("-"));
        }
        catch (Exception exception) {
            logger.info("failed to obtain end date inside mobile commerce " + exception.getMessage());
            return "";
        }
    }

    // Constructs the request url. This is made public for testing purpose.
    @Override
    public String getRequestUrl()
    {
        String interval = "day";
        if (getReportGranularity() == ReportGranularity.HOUR) {
            interval = "hour";
        }
        String url = String.format(config.getString("drawbridge.host"), config.getString("drawbridge.partnerId"),
            config.getString("drawbridge.partnerSignature"), startDate, endDate,
            config.getString("drawbridge.responseFormat"), interval);
        logger.debug("url inside draw bridge is " + url);
        return url;
    }

    // Returns the time zone(x) GMT+x. For GMT it returns 0. IST it returns 5.5
    @Override
    public double getTimeZone()
    {
        return 0.0;
    }

    @Override
    public String getAdvertiserId()
    {
        return config.getString("drawbridge.advertiserId");
    }

    // Returns the report format wrt granularity.
    @Override
    public ReportGranularity getReportGranularity()
    {
        return ReportGranularity.DAY;
    }

    @Override
    public String getName()
    {
        return "drawbridge";
    }

    @Override
    public int ReportReconcilerWindow()
    {
        return 7;
    }

    private void generateReportResponse(final DebugLogger logger, ReportResponse reportResponse, String response)
            throws JSONException
    {

        JSONObject responseObject = new JSONObject(response);
        JSONArray responseStatsArray = responseObject.getJSONObject("result").getJSONArray("stats");

        for (int i = 0; i < responseStatsArray.length(); i++) {
            JSONObject reportRow = responseStatsArray.getJSONObject(i);
            ReportResponse.ReportRow row = new ReportResponse.ReportRow();
            if (!decodeBlindedSiteId(reportRow.getString("property"), row)) {
                logger.debug("Error decoded BlindedSite id in Drawbridge", reportRow.getString("property"));
                continue;
            }
            row.isSiteData = true;
            row.impressions = reportRow.getLong("impressions");
            row.revenue = reportRow.getDouble("revenue");
            row.request = reportRow.getLong("requests");
            row.clicks = reportRow.getLong("clicks");
            row.reportTime = new ReportTime(reportRow.getString("interval"), 0);
            row.slotSize = getReportGranularity();
            reportResponse.addReportRow(row);

        }

    }
}