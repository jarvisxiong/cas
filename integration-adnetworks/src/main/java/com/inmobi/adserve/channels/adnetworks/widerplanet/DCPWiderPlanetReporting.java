package com.inmobi.adserve.channels.adnetworks.widerplanet;

import java.io.IOException;
import java.sql.Connection;

import org.apache.commons.configuration.Configuration;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.inmobi.adserve.channels.api.BaseReportingImpl;
import com.inmobi.adserve.channels.api.ReportResponse;
import com.inmobi.adserve.channels.api.ReportTime;
import com.inmobi.adserve.channels.api.ServerException;
import com.inmobi.adserve.channels.util.DebugLogger;


public class DCPWiderPlanetReporting extends BaseReportingImpl
{
    private final Configuration config;
    private final String        userName;
    private final String        password;
    private final String        publisherId;
    private final String        tokenApiUrl;
    private final String        reportingApiUrl;
    private String              date;
    private String              sessionId;
    private DebugLogger         logger;
    // private static String reportStartDate = null;
    // private static String entireReportData = null;
    private Connection          connection;

    public DCPWiderPlanetReporting(final Configuration config, final Connection connection)
    {

        this.config = config;
        this.connection = connection;
        tokenApiUrl = config.getString("widerplanet.tokenHost");
        reportingApiUrl = config.getString("widerplanet.host");
        userName = config.getString("widerplanet.userName");
        password = config.getString("widerplanet.password");
        publisherId = config.getString("widerplanet.pubId");

    }

    @Override
    public int ReportReconcilerWindow()
    {
        return 10;
    }

    @Override
    public ReportResponse fetchRows(final DebugLogger logger, final ReportTime startTime, final String key,
            final ReportTime endTime) throws ClientProtocolException, IllegalStateException, ServerException,
            IOException, JSONException
    {
        this.logger = logger;
        ReportResponse reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);
        String responseStr = null;
        String endDate = null;
        ReportTime localStartTime = startTime;
        logger.debug("inside fetch rows of widerplanet");
        try {
            date = startTime.getStringDate("-");
            logger.debug("start date inside widerplanet is ", date);
            endDate = endTime == null ? getEndDate() : date;
            if (ReportTime.compareStringDates(endDate, date) == -1) {
                logger.debug("date is greater than the current date reporting window for widerplanet");
                return null;
            }
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
            logger.info("failed to obtain correct dates for fetching reports ", exception.getMessage());
            return null;
        }

        String sessionResponse = invokeHTTPUrl(String.format(tokenApiUrl, userName, password), logger);
        JSONObject sessionObject = new JSONObject(sessionResponse);
        if (sessionObject.getInt("code") != 0) {
            logger.info("Issue with session Id generation ", sessionResponse);
            return null;
        }
        sessionId = sessionObject.getString("data");
        date = localStartTime.getStringDate("-");

        while (ReportTime.compareStringDates(date, endDate) < 1) {
            logger.debug("response from widerplanet is ", responseStr);
            responseStr = invokeHTTPUrl(getRequestUrl(), logger).replaceAll("null", "0");

            // parse the json response
            if (responseStr != null) {
                JSONObject data = new JSONObject(responseStr);
                if (data.getInt("code") != 0) {
                    logger.info("Error response from widerplanet ", responseStr);
                    return null;
                }
                JSONArray jReportArr = data.getJSONArray("data");
                ReportResponse.ReportRow row = new ReportResponse.ReportRow();
                for (int i = 0; i < jReportArr.length(); i++) {
                    JSONObject reportRow = jReportArr.getJSONObject(i);
                    if (reportRow.getString("zoneid").equalsIgnoreCase(key)) {
                        row.request = reportRow.getLong("requests");
                        row.clicks = reportRow.getLong("clicks");
                        row.impressions = reportRow.getLong("impressions");
                        row.revenue = reportRow.getDouble("revenue");
                        double rate = getCurrencyConversionRate("KRW", date, logger, connection);
                        if (rate > 0) {
                            row.revenue /= rate;
                        }
                        else {
                            logger.info("Failed to get KWR to USD rate for ", date);
                            return null;
                        }
                        logger.debug("parsing data inside widerplanet ", row.request);
                        row.reportTime = new ReportTime(date, 0);
                        row.advertiserId = this.getAdvertiserId();
                        row.siteId = key;
                        row.slotSize = getReportGranularity();
                        reportResponse.addReportRow(row);
                    }
                }
                localStartTime = ReportTime.getNextDay(localStartTime);
                date = localStartTime.getStringDate("-");
            }
        }

        return reportResponse;
    }

    @Override
    public String getAdvertiserId()
    {
        return (config.getString("widerplanet.advertiserId"));
    }

    @Override
    public String getName()
    {
        return "widerplanet";
    }

    @Override
    public ReportGranularity getReportGranularity()
    {
        return ReportGranularity.DAY;
    }

    @Override
    public String getRequestUrl()
    {
        return String.format(reportingApiUrl, sessionId, publisherId, date, date);
    }

    @Override
    public double getTimeZone()
    {
        return 0;
    }

    public String getEndDate() throws Exception
    {
        try {
            logger.debug("calculating end date for widerplanet");
            ReportTime reportTime = ReportTime.getUTCTime();
            reportTime = ReportTime.getPreviousDay(reportTime);
            if (reportTime.getHour() <= ReportReconcilerWindow()) {
                reportTime = ReportTime.getPreviousDay(reportTime);
            }
            return (reportTime.getStringDate("-"));
        }
        catch (Exception exception) {
            logger.info("failed to obtain end date inside widerplanet ", exception.getMessage());
            return "";
        }
    }
}
