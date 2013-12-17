package com.inmobi.adserve.channels.adnetworks.mullahmedia;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.BaseReportingImpl;
import com.inmobi.adserve.channels.api.ReportResponse;
import com.inmobi.adserve.channels.api.ReportResponse.ReportRow;
import com.inmobi.adserve.channels.api.ReportTime;


public abstract class BaseMoolahMediaReporting extends BaseReportingImpl {
    private static final Logger LOG            = LoggerFactory.getLogger(BaseMoolahMediaReporting.class);

    private final Configuration config;
    private String              startDate      = "";
    private String              responseString = "";
    private final String        api            = "";
    protected String            advertiserId;
    protected String            email;
    protected String            password;
    protected String            host;
    protected String            advertiserName;

    public BaseMoolahMediaReporting(final Configuration config) {
        this.config = config;
    }

    @Override
    public ReportResponse fetchRows(final ReportTime startTime, final ReportTime endTime) throws Exception {
        ReportResponse reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);
        LOG.debug("inside fetch rows of mullah media");
        ReportTime endDate = null;
        try {
            startDate = startTime.getMullahMediaStringDate();
            endDate = endTime == null ? getLatestDate() : startTime;
            LOG.debug("start date inside mullah media is {}", startDate);
            if (endDate.compareDates(startTime) < 0) {
                LOG.debug("end date is less than start date plus reporting window for mullah media");
                return null;
            }
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
            LOG.info("failed to obtain correct dates for fetching reports {}", exception);
            return null;
        }
        ReportTime reportStartTime = startTime;
        while (endDate.compareDates(reportStartTime) != -1) {
            String url = getRequestUrl();
            LOG.debug("url inside mullah media is {}", url);
            responseString = invokeUrl(url, null);

            reportResponse.status = ReportResponse.ResponseStatus.SUCCESS;

            String[] responseArray = responseString.replace(')', ' ').replace("\"", "").replaceAll(" ", "").split("\n");
            LOG.debug("successfuly got response inside mullah media. Number of lines of response is {}",
                responseArray.length);
            if (responseArray.length > 1) {
                int impressionIndex = -1;
                int clicksIndex = -1;
                int payoutIndex = -1;

                String[] header = responseArray[0].replace("'", "").split(",");

                for (int i = 0; i < header.length; i++) {
                    if ("Impressions".equalsIgnoreCase(header[i].trim())) {
                        impressionIndex = i;
                        continue;
                    }
                    else if ("Clicks".equalsIgnoreCase(header[i].trim())) {
                        clicksIndex = i;
                        continue;
                    }
                    else if ("Payout".equalsIgnoreCase(header[i].trim())) {
                        payoutIndex = i;
                        continue;
                    }
                }
                Map<UUID, ReportRow> siteMap = new HashMap<UUID, ReportRow>();

                if (payoutIndex == -1) {
                    LOG.info("Cannot find 'Payout' info in the moolah media reponse");
                    LOG.info("response from mullah media : {}", responseString);
                    return reportResponse;
                }

                for (int j = 1; j < responseArray.length; j++) {
                    String[] rows1 = responseArray[j].split(",");
                    ReportResponse.ReportRow row = new ReportResponse.ReportRow();
                    if (!decodeBlindedSiteId(rows1[1], row)) {
                        LOG.debug("Error decoded BlindedSite id in Moolahmedia {}", rows1[0]);
                        continue;
                    }
                    UUID uuid = new UUID(row.adGroupIncId, row.siteIncId);
                    if (siteMap.containsKey(uuid)) {
                        row = siteMap.get(uuid);
                    }
                    else {
                        row.isSiteData = true;
                        row.impressions = 0;
                        row.revenue = 0;
                        row.request = 0;
                        row.clicks = 0;
                        row.reportTime = new ReportTime(startDate, 0, "/");
                        row.slotSize = getReportGranularity();
                        reportResponse.addReportRow(row);
                        siteMap.put(uuid, row);
                    }
                    row.revenue += Double.valueOf(rows1[payoutIndex]).doubleValue();
                    if (impressionIndex > -1) {
                        row.impressions += Long.parseLong(rows1[impressionIndex]);
                        if (row.impressions > 0) {
                            row.ecpm = row.revenue / row.impressions * 1000;
                        }
                    }
                    if (clicksIndex > -1) {
                        row.clicks += Long.valueOf(rows1[clicksIndex]);
                    }
                }
            }

            reportStartTime = ReportTime.getNextDay(reportStartTime);
            startDate = reportStartTime.getMullahMediaStringDate();
            LOG.debug("start date inside MullahMedia now is {}", startDate);
        }
        LOG.debug("successfully parsed data inside mullah media");
        return reportResponse;
    }

    @Override
    protected boolean decodeBlindedSiteId(final String blindedSiteId, final ReportResponse.ReportRow row) {
        if (blindedSiteId.length() < 36) {
            LOG.info("failed to decodeBlindedSiteId for {} , {}", getName(), blindedSiteId);
            return false;
        }
        String bSiteId = blindedSiteId.substring(0, 36);
        return super.decodeBlindedSiteId(bSiteId, row);
    }

    public ReportTime getLatestDate() throws Exception {
        try {
            LOG.debug("calculating latest date for mullah media");
            ReportTime reportTime = ReportTime.getUTCTime();
            reportTime = ReportTime.getPreviousDay(reportTime);
            if (reportTime.getHour() <= ReportReconcilerWindow()) {
                reportTime = ReportTime.getPreviousDay(reportTime);
            }
            return (reportTime);
        }
        catch (Exception exception) {
            LOG.info("failed to obtain end date inside mullah media {}", exception);
            return null;
        }
    }

    @Override
    public String getRequestUrl() {
        String url = String.format("%semail=%s&password=%s&from=%s&to=%s", host, email, password, startDate, startDate);
        if (StringUtils.isNotBlank(api)) {
            url += "&site_name=" + api;
        }
        return url;
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
        return 22;
    }

    @Override
    public String getName() {
        return advertiserName;
    }

    @Override
    public String getAdvertiserId() {
        return advertiserId;
    }
}