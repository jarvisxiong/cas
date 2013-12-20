package com.inmobi.adserve.channels.adnetworks.placeiq;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;

import com.inmobi.adserve.channels.api.BaseReportingImpl;
import com.inmobi.adserve.channels.api.ReportResponse;
import com.inmobi.adserve.channels.api.ReportResponse.ReportRow;
import com.inmobi.adserve.channels.api.ReportTime;
import com.inmobi.adserve.channels.util.DebugLogger;


public class DCPPlaceIQReporting extends BaseReportingImpl {

    private final Configuration    config;
    private DebugLogger            logger;
    private String                 startDate         = "";
    private String                 endDate           = "";
    private String                 accessKey         = "";
    private String                 secretKey         = "";
    private String                 bucketName        = "";
    private static String          entireReportData  = null;
    private String                 externalSiteId    = null;
    private final SimpleDateFormat placeiqDateFormat = new SimpleDateFormat("MM/dd/yy");
    private final SimpleDateFormat dateFormat        = new SimpleDateFormat("yyyy-MM-dd");

    public DCPPlaceIQReporting(final Configuration config) {
        this.config = config;
        accessKey = config.getString("placeiq.accessKey");
        secretKey = config.getString("placeiq.secretKey");
        bucketName = config.getString("placeiq.bucketName");
    }

    public String getEndDate() throws Exception {
        try {
            logger.debug("calculating latest date for PlaceIQ");
            ReportTime reportTime = ReportTime.getUTCTime();
            reportTime = ReportTime.getPreviousDay(reportTime);
            if (reportTime.getHour() <= ReportReconcilerWindow()) {
                reportTime = ReportTime.getPreviousDay(reportTime);
            }
            return (reportTime.getStringDate(""));
        }
        catch (Exception exception) {
            logger.error("failed to obtain end date inside PlaceIQ ", exception.getMessage());
            return null;
        }
    }

    @Override
    public String getRequestUrl() {

        return null;
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
        return 23;
    }

    @Override
    public String getName() {
        return "PlaceIQ";
    }

    @Override
    public String getAdvertiserId() {
        return (config.getString("placeiq.advertiserId"));
    }

    @Override
    public ReportResponse fetchRows(final DebugLogger logger, final ReportTime startTime, final String key,
            final ReportTime endTime) throws Exception {
        this.logger = logger;
        ReportResponse reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);
        logger.debug("inside fetch rows of PlaceIQ");
        try {
            startDate = startTime.getStringDate("");
            endDate = endTime == null ? getEndDate() : startDate;
            logger.debug("start date inside PlaceIQ is ", startDate);
            if (ReportTime.compareStringDates(endDate, startDate) == -1) {
                logger.debug("end date is less than start date plus reporting window for PlaceIQ");
                return null;
            }
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
            logger.error("failed to obtain correct dates for fetching reports ", exception.getMessage());
            return null;
        }
        RestS3Service s3Service = new RestS3Service(new AWSCredentials(accessKey, secretKey));
        S3Object s3obj = null;

        while (ReportTime.compareStringDates(startDate, endDate) != 1) {
            try {
                s3obj = s3Service.getObject(bucketName, String.format("%s.csv", startDate));
            }
            catch (Exception exception) {
                logger.error("Reports are not uploaded by placeiq");
                ReportTime reportTime = new ReportTime(startDate, 0);
                reportTime = ReportTime.getNextDay(reportTime);
                startDate = reportTime.getStringDate("");
                continue;
            }
            InputStream stream = s3obj.getDataInputStream();
            int content;
            StringBuilder responseBuilder = new StringBuilder();
            while ((content = stream.read()) != -1) {
                if (content == 13) {
                    responseBuilder.append("\n");
                }
                else {
                    responseBuilder.append((char) content);
                }
            }
            entireReportData = responseBuilder.toString();
            entireReportData = entireReportData.replace('$', ' ');
            entireReportData = entireReportData.replaceAll("$-", "$0.00");

            reportResponse.status = ReportResponse.ResponseStatus.SUCCESS;

            String[] responseArray = entireReportData.split("\n");

            logger.debug("successfuly got response inside PlaceIQ. Number of lines of response is ",
                responseArray.length);
            this.externalSiteId = key;
            generateReportResponse(logger, reportResponse, responseArray);
            ReportTime reportTime = new ReportTime(startDate, 0);
            reportTime = ReportTime.getNextDay(reportTime);
            startDate = reportTime.getStringDate("");
        }
        logger.debug("successfully parsed data inside PlaceIQ");
        return reportResponse;
    }

    private void generateReportResponse(final DebugLogger logger, final ReportResponse reportResponse,
            final String[] responseArray) throws ParseException {
        if (responseArray.length > 1) {
            int impressionIndex = -1;
            int clicksIndex = -1;
            int revenueIndex = -1;
            int ecpmIndex = -1;
            int dateIndex = -1;
            int externalSiteIdIndex = -1;

            String[] header = responseArray[0].replace("'", "").split(",");

            for (int i = 0; i < header.length; i++) {
                if ("Total impressions".equalsIgnoreCase(header[i].trim())) {
                    impressionIndex = i;
                    continue;
                }
                else if ("Total clicks".equalsIgnoreCase(header[i].trim())) {
                    clicksIndex = i;
                    continue;
                }
                else if (header[i].trim().contains("Revenue") || header[i].trim().contains("Spend") || header[i].trim().contains("Spent")) {
                    revenueIndex = i;
                    continue;
                }
                else if ("eCPM".equalsIgnoreCase(header[i].trim())) {
                    ecpmIndex = i;
                    continue;
                }
                else if ("Date".equalsIgnoreCase(header[i].trim())) {
                    dateIndex = i;
                    continue;
                }
                else if ((header[i].trim()).startsWith("Ad unit 1")) {
                    externalSiteIdIndex = i;
                    continue;
                }
            }

            HashMap<String, ReportResponse.ReportRow> reportMap = new HashMap<String, ReportResponse.ReportRow>();
            for (int j = 1; j < responseArray.length; j++) {

                String[] reportRow = responseArray[j].split(",");
                if (reportRow.length == 0 || StringUtils.isBlank(reportRow[externalSiteIdIndex])) {
                    continue;
                }
                String extSiteId = reportRow[externalSiteIdIndex].split("/")[0];
                Date reportingDate = placeiqDateFormat.parse(reportRow[dateIndex]);
                ReportTime rowDate = new ReportTime(dateFormat.format(reportingDate), 0);

                if (extSiteId.equals(externalSiteId)) {
                    String[] rows1 = cleanUpEntry(reportRow);
                    ReportResponse.ReportRow row = new ReportResponse.ReportRow();
                    row.siteId = extSiteId;
                    row.impressions = Long.parseLong(rows1[impressionIndex]);// .replace("\"",
                                                                             // "").trim());
                    row.revenue = Double.parseDouble(rows1[revenueIndex]);// .replace("\"",
                                                                          // "").trim());
                    row.clicks = Long.parseLong(rows1[clicksIndex]);// .replace("\"",
                                                                    // "").trim());
                    row.reportTime = rowDate;
                    row.slotSize = getReportGranularity();
                    if (reportMap.get(reportRow[dateIndex]) == null) {
                        reportMap.put(reportRow[dateIndex], row);
                    }
                    else {
                        ReportResponse.ReportRow existingRow = reportMap.get(reportRow[dateIndex]);
                        existingRow.impressions += row.impressions;
                        existingRow.revenue += row.revenue;
                        existingRow.clicks += row.clicks;
                    }

                }
            }
            Iterator<Entry<String, ReportRow>> it = reportMap.entrySet().iterator();
            while (it.hasNext()) {
                reportResponse.addReportRow(it.next().getValue());
            }
        }

        if (reportResponse.rows.size() > 0) {
            logger.debug("successfully generated reporting log for external site id : ", externalSiteId);
        }
        else {
            logger.debug("failed to generate reporting log for external site id : ", externalSiteId);
            logger.debug("Default row added external site id : ", externalSiteId);
            ReportResponse.ReportRow row = new ReportResponse.ReportRow();
            row.siteId = externalSiteId;
            row.impressions = 0;
            row.revenue = 0;
            row.request = 0;
            row.clicks = 0;
            row.reportTime = new ReportTime(startDate, 0);
            row.slotSize = getReportGranularity();
            reportResponse.addReportRow(row);
        }
    }

    // Combining the comma separated value within '"'
    private String[] cleanUpEntry(final String[] reportEntry) {
        boolean isNewEntry = true;
        int pos = 0;
        String[] newVal = new String[15];
        for (int i = 0; i < reportEntry.length; i++) {

            if (isNewEntry) {
                newVal[pos] = reportEntry[i].replaceAll("\"", "").trim();
            }
            else {
                newVal[pos] += reportEntry[i].replaceAll("\"", "").trim();
            }
            if (reportEntry[i].startsWith("\"")) {
                isNewEntry = false;
            }
            if (reportEntry[i].endsWith("\"")) {
                isNewEntry = true;
            }
            if (isNewEntry) {
                pos++;
            }
        }
        return newVal;
    }
}
