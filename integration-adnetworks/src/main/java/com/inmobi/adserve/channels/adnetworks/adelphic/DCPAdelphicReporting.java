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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.BaseReportingImpl;
import com.inmobi.adserve.channels.api.ReportResponse;
import com.inmobi.adserve.channels.api.ReportTime;


public class DCPAdelphicReporting extends BaseReportingImpl {
    private static final Logger LOG            = LoggerFactory.getLogger(DCPAdelphicReporting.class);

    private final Configuration config;
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
    public ReportResponse fetchRows(final ReportTime startTime, final ReportTime endTime) throws Exception {
        ReportResponse reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);
        LOG.debug("inside fetch rows of Adelphic");
        try {
            startDate = startTime.getStringDate("-");
            endDate = endTime == null ? getEndDate() : startDate;
            LOG.debug("start date inside Adelphic is {}", startDate);
            if (ReportTime.compareStringDates(endDate, startDate) == -1) {
                LOG.debug("end date is less than start date plus reporting window for Adelphic");
                return null;
            }
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
            LOG.info("failed to obtain correct dates for fetching reports {}", exception);
            return null;
        }
        while (ReportTime.compareStringDates(startDate, endDate) != 1) {
            String url = getRequestUrl();
            LOG.debug("url inside Adelphic is {}", url);
            responseString = invokeHTTPUrl(url);
            reportResponse.status = ReportResponse.ResponseStatus.SUCCESS;

            String[] responseArray = responseString.split("\n");
            LOG.debug("successfuly got response inside Adelphic. Number of lines of response is {}",
                responseArray.length);
            generateReportResponse(reportResponse, responseArray);
            ReportTime reportTime = new ReportTime(startDate, 0);
            reportTime = ReportTime.getNextDay(reportTime);
            startDate = reportTime.getStringDate("-");
            LOG.debug("start date inside Adelphic now is {}", startDate);
        }
        LOG.debug("successfully parsed data inside Adelphic");
        return reportResponse;
    }

    public String getEndDate() throws Exception {
        try {
            LOG.debug("calculating latest date for Adelphic");
            ReportTime reportTime = ReportTime.getUTCTime();
            reportTime = ReportTime.getPreviousDay(reportTime);
            if (reportTime.getHour() <= ReportReconcilerWindow()) {
                reportTime = ReportTime.getPreviousDay(reportTime);
            }
            return (reportTime.getStringDate("-"));
        }
        catch (Exception exception) {
            LOG.info("failed to obtain end date inside Adelphic {}", exception);
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

    @Override
    protected String invokeHTTPUrl(final String url) throws MalformedURLException, IOException {
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
            LOG.info("Error in Httpool invokeHTTPUrl : {}", ioe);
        }
        finally {
            if (res != null) {
                res.close();
            }
        }

        return sBuffer.toString();

    }

    private void generateReportResponse(final ReportResponse reportResponse, final String[] responseArray) {
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
                    LOG.debug("Error decoded BlindedSite id in Adelphic {}", rows1[siteNameIndex]);
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