package com.inmobi.adserve.channels.adnetworks.mobilecommerce;

import org.apache.commons.configuration.Configuration;

import com.inmobi.adserve.channels.api.BaseReportingImpl;
import com.inmobi.adserve.channels.api.ReportResponse;
import com.inmobi.adserve.channels.api.ReportTime;
import com.inmobi.adserve.channels.api.ServerException;
import com.inmobi.adserve.channels.util.DebugLogger;


public class MobileCommerceReportingAdapter extends BaseReportingImpl
{

    private Configuration config;
    private DebugLogger   logger;
    private String        currency       = "";
    private String        startDate      = "";
    private String        endDate        = "";
    private String        pubId          = "";
    private String        responseFormat = "";
    private String        display        = "";
    private String        baseUrl        = "";
    private boolean       isInvoked      = false;
    // private ReportResponse reportResponse;
    private String        responseString = "";

    public MobileCommerceReportingAdapter(final Configuration config)
    {
        this.config = config;
        pubId = config.getString("mobilecommerce.pubId");
        responseFormat = config.getString("mobilecommerce.responseFormat");
        display = config.getString("mobilecommerce.display");
        baseUrl = config.getString("mobilecommerce.baseUrl");
        currency = config.getString("mobilecommerce.currency");
    }

    // Fetches the report from the TPAN
    @Override
    public ReportResponse fetchRows(final DebugLogger logger, final ReportTime startTime, final String key,
            final ReportTime endTime) throws ServerException
    {
        this.logger = logger;
        ReportResponse reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);
        logger.debug("inside fetch rows of mobile commerce");
        try {
            startDate = startTime.getStringDate("");
            logger.debug("start date inside mobile commerce is ", startDate);
            endDate = endTime == null ? getEndDate() : startDate;
            logger.debug("end date inside mobile commerce is ", endDate);
            if (ReportTime.compareStringDates(endDate, startDate) == -1) {
                logger.debug("end date is less than start date plus reporting window for mobile commerce");
                return null;
            }
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
            logger.info("failed to obtain correct dates for fetching reports ", exception.getMessage());
            return null;
        }
        if (!isInvoked) {
            String url = getRequestUrl();
            logger.debug("url inside mobile commerce is ", url);
            responseString = invokeUrl(url, null, logger);
            isInvoked = true;
        }
        reportResponse.status = ReportResponse.ResponseStatus.SUCCESS;
        String[] responseArray = responseString.split("\n");
        logger.debug("successfuly got response inside mobile commerce. Number of lines of response is ",
            responseArray.length);
        boolean gotReport = false;
        if (responseArray.length == 1) {
            logger.info("0 rows reported by Mobile Commerce");
            isInvoked = false;
            return null;
        }
        for (int i = 1; i < responseArray.length; i++) {
            String[] rows1 = responseArray[i].split(",");
            ReportResponse.ReportRow row = new ReportResponse.ReportRow();
            if (rows1[0].equals("\"" + key + "\"")) {
                if (i + 1 < responseArray.length) {
                    String[] rows2 = responseArray[i + 1].split(",");
                    if (rows1[0].equals(rows2[0]) && rows2[2].equals(rows1[2])) {
                        row.request = Long.parseLong(rows1[3]) + Long.parseLong(rows2[3]);
                        row.impressions = Long.parseLong(rows1[4]) + Long.parseLong(rows2[4]);
                        row.clicks = Long.parseLong(rows1[5]) + Long.parseLong(rows2[5]);
                        row.revenue = Double.valueOf(rows1[6]).doubleValue() + Double.valueOf(rows2[6]).doubleValue();
                        row.ecpm = Double.valueOf(rows1[7]).doubleValue() + Double.valueOf(rows2[7]).doubleValue();
                        i++;
                    }
                    else {
                        row.request = Long.parseLong(rows1[3]);
                        row.impressions = Long.parseLong(rows1[4]);
                        row.clicks = Long.parseLong(rows1[5]);
                        row.revenue = Double.valueOf(rows1[6]).doubleValue();
                        row.ecpm = Double.valueOf(rows1[7]).doubleValue();
                    }
                }
                else {
                    row.request = Long.parseLong(rows1[3]);
                    row.impressions = Long.parseLong(rows1[4]);
                    row.clicks = Long.parseLong(rows1[5]);
                    row.revenue = Double.valueOf(rows1[6]).doubleValue();
                    row.ecpm = Double.valueOf(rows1[7]).doubleValue();
                }

                row.siteId = key;
                row.reportTime = new ReportTime(rows1[2], 0);
                row.slotSize = getReportGranularity();
                logger.debug("parsing data inside mobile commerce ", row.request);
                reportResponse.addReportRow(row);
                gotReport = true;
            }
        }

        if (!gotReport) {
            logger.debug("Adding default row");
            ReportResponse.ReportRow row = new ReportResponse.ReportRow();
            row.request = 0;
            row.clicks = 0;
            row.impressions = 0;
            row.revenue = 0.0;
            row.siteId = key;
            row.reportTime = new ReportTime(endDate, 0);
            row.slotSize = getReportGranularity();
            logger.debug("parsing data inside tapit ", row.request);
            reportResponse.addReportRow(row);
        }
        logger.debug("successfully parsed data inside mobile commerce");
        return reportResponse;
    }

    public String getEndDate() throws Exception
    {
        try {
            logger.debug("calculating end date for mobile commerce");
            ReportTime reportTime = ReportTime.getPacificTime();
            reportTime = ReportTime.getPreviousDay(reportTime);
            if (reportTime.getHour() <= ReportReconcilerWindow()) {
                reportTime = ReportTime.getPreviousDay(reportTime);
            }
            return (reportTime.getStringDate(""));
        }
        catch (Exception exception) {
            logger.info("failed to obtain end date inside mobile commerce ", exception.getMessage());
            return "";
        }
    }

    @Override
    public String getRequestUrl()
    {
        String url = String.format("%s%s/?pubid=%s&startdate=%s&enddate=%s&currency=%s&display=%s", baseUrl,
            responseFormat, pubId, startDate, endDate, currency, display);
        return url;
    }

    @Override
    public double getTimeZone()
    {
        return -7;
    }

    @Override
    public ReportGranularity getReportGranularity()
    {
        return ReportGranularity.DAY;
    }

    @Override
    public int ReportReconcilerWindow()
    {
        return 16;
    }

    @Override
    public String getName()
    {
        return "MobileCommerce";
    }

    @Override
    public String getAdvertiserId()
    {
        return (config.getString("mobilecommerce.advertiserId"));
    }
}
