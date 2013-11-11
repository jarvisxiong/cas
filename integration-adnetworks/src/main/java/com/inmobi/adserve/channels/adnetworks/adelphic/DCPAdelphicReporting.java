package com.inmobi.adserve.channels.adnetworks.adelphic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.Configuration;

import com.inmobi.adserve.channels.api.BaseReportingImpl;
import com.inmobi.adserve.channels.api.ReportResponse;
import com.inmobi.adserve.channels.api.ReportTime;
import com.inmobi.adserve.channels.util.DebugLogger;


public class DCPAdelphicReporting extends BaseReportingImpl {

    private final Configuration config;
    private DebugLogger         logger;
    private String              startDate      = "";
    private String              endDate        = "";
    private String              responseString = "";
    private String              host           = "";
    private String              userName       = "";
    private String              password       = "";

    public DCPAdelphicReporting(final Configuration config) {
        this.config = config;
        host = config.getString("adelphic.reportUrl");
        userName = config.getString("adelphic.userName");
        password = config.getString("adelphic.password");
    }

    @Override
    public ReportResponse fetchRows(final DebugLogger logger, final ReportTime startTime, final ReportTime endTime)
            throws Exception {
        this.logger = logger;
        ReportResponse reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);
        logger.debug("inside fetch rows of Adelphic");
        try {
            startDate = startTime.getStringDate("-");
            endDate = endTime == null ? getEndDate() : startDate;
            logger.debug("start date inside Adelphic is ", startDate);
            if (ReportTime.compareStringDates(endDate, startDate) == -1) {
                logger.debug("end date is less than start date plus reporting window for Adelphic");
                return null;
            }
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
            logger.info("failed to obtain correct dates for fetching reports ", exception.getMessage());
            return null;
        }
        while (ReportTime.compareStringDates(startDate, endDate) != 1) {
            String url = getRequestUrl();
            logger.debug("url inside Adelphic is ", url);
            responseString = invokeHTTPUrl(url);
            reportResponse.status = ReportResponse.ResponseStatus.SUCCESS;

            String[] responseArray = responseString.split("\n");
            logger.debug("successfuly got response inside Adelphic. Number of lines of response is ",
                responseArray.length);
            generateReportResponse(logger, reportResponse, responseArray);
            ReportTime reportTime = new ReportTime(startDate, 0);
            reportTime = ReportTime.getNextDay(reportTime);
            startDate = reportTime.getStringDate("-");
            logger.debug("start date inside Adelphic now is ", startDate);
        }
        logger.debug("successfully parsed data inside Adelphic");
        return reportResponse;
    }

    public String getEndDate() throws Exception {
        try {
            logger.debug("calculating latest date for Adelphic");
            ReportTime reportTime = ReportTime.getUTCTime();
            reportTime = ReportTime.getPreviousDay(reportTime);
            if (reportTime.getHour() <= ReportReconcilerWindow()) {
                reportTime = ReportTime.getPreviousDay(reportTime);
            }
            return (reportTime.getStringDate("-"));
        }
        catch (Exception exception) {
            logger.info("failed to obtain end date inside Adelphic ", exception.getMessage());
            return null;
        }
    }

    @Override
    public String getRequestUrl() {

        return String.format(host, userName, password, startDate, startDate);
    }

    @Override
    public double getTimeZone() {
        return 0;
    }

    @Override
    public ReportGranularity getReportGranularity() {
        return ReportGranularity.DAY;
    }

    @Override
    public int ReportReconcilerWindow() {
        return 10;
    }

    @Override
    public String getName() {
        return "Adelphic";
    }

    @Override
    public String getAdvertiserId() {
        return (config.getString("adelphic.advertiserId"));
    }

    private String invokeHTTPUrl(final String url) throws MalformedURLException, IOException {
        URLConnection conn = new URL(url).openConnection();
        String authStr = userName + ":" + password;
        String authEncoded = new String(Base64.encodeBase64(authStr.getBytes()));
        conn.setRequestProperty("Authorization", "Basic " + authEncoded);
        conn.setDoOutput(true);
        InputStream in = conn.getInputStream();
        BufferedReader res = null;
        StringBuffer sBuffer = new StringBuffer();
        try {
            res = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String inputLine;
            while ((inputLine = res.readLine()) != null) {
                sBuffer.append(inputLine).append("\n");
            }
        }
        catch (IOException ioe) {
            logger.info("Error in Httpool invokeHTTPUrl : ", ioe.getMessage());
        }
        finally {
            if (res != null) {
                res.close();
            }
        }

        return sBuffer.toString();

    }

    @Override
    public ReportResponse fetchRows(DebugLogger logger, ReportTime startTime, String key, ReportTime endTime)
            throws Exception {
        // not applicable so returns null
        return null;
    }

    private void generateReportResponse(final DebugLogger logger, ReportResponse reportResponse, String[] responseArray) {
        if (responseArray.length > 1) {
            int impressionIndex = -1;
            int clicksIndex = -1;
            int revenueIndex = -1;
            int ecpmIndex = -1;
            int requestIndex = -1;
            int siteNameIndex = -1;

            String[] header = responseArray[0].replace("'", "").split(",");

            for (int i = 0; i < header.length; i++) {
                if ("impressions".equalsIgnoreCase(header[i].trim())) {
                    impressionIndex = i;
                    continue;
                }
                else if ("clicks".equalsIgnoreCase(header[i].trim())) {
                    clicksIndex = i;
                    continue;
                }
                else if ("net_revenue".equalsIgnoreCase(header[i].trim())) {
                    revenueIndex = i;
                    continue;
                }
                else if ("ecpm".equalsIgnoreCase(header[i].trim())) {
                    ecpmIndex = i;
                    continue;
                }
                else if ("requests".equalsIgnoreCase(header[i].trim())) {
                    requestIndex = i;
                    continue;
                }
                else if ("site_name".equalsIgnoreCase(header[i].trim())) {
                    siteNameIndex = i;
                    continue;
                }

            }
            for (int j = 1; j < responseArray.length; j++) {
                String[] rows1 = responseArray[j].split(",");
                ReportResponse.ReportRow row = new ReportResponse.ReportRow();
                if (!decodeBlindedSiteId(rows1[siteNameIndex], row)) {
                    logger.debug("Error decoded BlindedSite id in Adelphic", rows1[siteNameIndex]);
                    continue;
                }
                row.isSiteData = true;
                row.impressions = Long.parseLong(rows1[impressionIndex]);
                row.revenue = Double.valueOf(rows1[revenueIndex]).doubleValue();
                row.request = Long.parseLong(rows1[requestIndex]);
                row.clicks = Long.parseLong(rows1[clicksIndex]);
                row.ecpm = Double.valueOf(rows1[ecpmIndex]).doubleValue();
                row.reportTime = new ReportTime(startDate, 0);
                row.slotSize = getReportGranularity();
                reportResponse.addReportRow(row);
            }
        }
    }
}