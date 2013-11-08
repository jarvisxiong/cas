package com.inmobi.adserve.channels.api;

import com.inmobi.adserve.channels.util.DebugLogger;


public interface ReportingInterface
{

    public enum ReportGranularity
    {
        HOUR,
        HOUR_LIST,
        DAY,
        DAY_LIST
    };

    // Fetches the report from the TPAN exterSiteId wise
    ReportResponse fetchRows(final DebugLogger logger, final ReportTime startTime, final String key,
            final ReportTime endTime) throws Exception;

    // Fetches the report from the TPAN Site inc id wise
    ReportResponse fetchRows(final DebugLogger logger, final ReportTime startTime, final ReportTime endTime)
            throws Exception;

    // Constructs the request url. This is made public for testing purpose.
    String getRequestUrl();

    // Returns the time zone(x) GMT+x. For GMT it returns 0. IST it returns 5.5
    double getTimeZone();

    // Returns the report format wrt granularity.
    ReportGranularity getReportGranularity();

    // Time taken to reconcile report
    int ReportReconcilerWindow();

    // get name of the reporting channel
    String getName();

    // Returns the Inmobi Advertiser Id.
    String getAdvertiserId();
}
