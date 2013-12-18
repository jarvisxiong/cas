package com.inmobi.adserve.channels.adnetworks.generic;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.configuration.Configuration;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.inmobi.adserve.channels.api.BaseReportingImpl;
import com.inmobi.adserve.channels.api.ReportResponse;
import com.inmobi.adserve.channels.api.ReportTime;
import com.inmobi.adserve.channels.api.ServerException;
import com.inmobi.adserve.channels.util.DebugLogger;


public class GenericReporting extends BaseReportingImpl {

    public Configuration  config;
    public DebugLogger    logger;
    public String         startDate      = "";
    public String         endDate        = "";
    public String         responseString = "";
    public ReportResponse reportResponse;
    public String         advertiserName = "";
    public String         responseFormat = "";
    public String         key            = "";
    public ReportTime     startTime;

    public GenericReporting(final Configuration config, final String advertiserName) {
        this.config = config;
        this.advertiserName = advertiserName;
        /*
         * if(advertiserName.equals("")) { DebugLogger.debug("no adapter in config for advertiser"); }
         * DebugLogger.debug("adapter found is " + advertiserName);
         */
        responseFormat = config.getString(this.advertiserName + MacrosAndStrings.RESPONSE_FORMAT);
    }

    @Override
    public ReportResponse fetchRows(final DebugLogger logger, final ReportTime startTime, final String key,
            final ReportTime endTime) {
        this.logger = logger;
        this.key = key;
        this.startTime = startTime;
        reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);
        logger.debug("inside the fetch rows of generic");
        try {
            startDate = startTime.getStringDate(config.getString(advertiserName + MacrosAndStrings.TIME_SEPARATOR));
            logger.debug("startdate inside " + advertiserName + " is " + startDate);
            endDate = endTime == null ? getEndDate() : startDate;
            logger.debug("enddate inside " + advertiserName + " is " + endDate);
            if (ReportTime.compareStringDates(endDate, startDate) == -1) {
                logger.debug(" end date is less than start date plus reportinf window for " + advertiserName);
                return null;
            }
        }
        catch (Exception e) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
            logger.debug("Failed to obtain correct dates for fetching reports " + e.getMessage());
        }
        try {
            String url = getRequestUrl();
            logger.debug("url inside " + advertiserName + " is " + url);
            if (responseFormat.equals(MacrosAndStrings.CSV)) {
                responseString = invokeUrl(url, null, logger);
            }
            else if (responseFormat.equals("json")) {
                responseString = invokeHTTPUrl(url);
            }
        }
        catch (Exception e) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_SERVER_ERROR;
            logger.debug("failed to get response from " + advertiserName);
        }
        if (responseFormat.equals(MacrosAndStrings.CSV)) {
            return parseResponseInCsv();
        }
        else if (responseFormat.equals(MacrosAndStrings.JSON)) {
            return parseResponseInJson();
        }
        else {
            return parseResponseInXml();
        }
    }

    public String invokeHTTPUrl(final String url) throws ServerException {
        String retStr = null;
        logger.debug("url inside tapit is " + url);
        DefaultHttpClient httpclient = new DefaultHttpClient();
        try {
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
        }
        catch (MalformedURLException mue) {
            logger.info("malformed url " + mue.getMessage());
            throw new ServerException("Invalid Url");
        }
        catch (IOException exception) {
            logger.info("io error while reading response " + exception.getMessage());
            throw new ServerException("Error While Reading Response");
        }

        return retStr;
    }

    @Override
    public String getRequestUrl() {
        String requestUrl = config.getString(advertiserName + MacrosAndStrings.REQUEST_URL);
        requestUrl = requestUrl.replace("$startDate", startDate).replace("$endDate", endDate).replace("$key", key);
        return requestUrl;
    }

    public String getEndDate() throws Exception {
        try {
            logger.debug("calculating end date for " + advertiserName);
            ReportTime reportTime = ReportTime.getUTCTime();
            reportTime = ReportTime.getPreviousDay(reportTime);
            if (reportTime.getHour() <= ReportReconcilerWindow()) {
                reportTime = ReportTime.getPreviousDay(reportTime);
            }
            return (reportTime.getStringDate(config.getString(advertiserName + MacrosAndStrings.TIME_SEPARATOR)));
        }
        catch (Exception exception) {
            logger.info("failed to obtain end date inside " + advertiserName + exception.getMessage());
            return "";
        }
    }

    @Override
    public String getName() {
        return advertiserName;
    }

    @Override
    public String getAdvertiserId() {
        return config.getString(advertiserName + MacrosAndStrings.ADVERTISER_ID);
    }

    @Override
    public int ReportReconcilerWindow() {
        return config.getInt(advertiserName + MacrosAndStrings.RR_WINDOW);
    }

    @Override
    public ReportGranularity getReportGranularity() {
        return ReportGranularity.DAY;
    }

    @Override
    public double getTimeZone() {
        return config.getDouble(advertiserName + MacrosAndStrings.TIMEZONE);
    }

    public ReportResponse parseResponseInCsv() {
        try {
            int externalSiteKeyField = config.getInt(advertiserName + MacrosAndStrings.EXTERNAL_SITE_KEY_FIELD);
            String[] responseArray = responseString.split("\n");
            logger.debug("successfuly got response inside " + advertiserName + " Number of lines of response is "
                    + responseArray.length);
            for (int i = 1; i < responseArray.length; i++) {
                if (responseArray[i].contains("\"") || responseArray[i].contains(" ")) {
                    responseArray[i] = responseArray[i].replaceAll("\"", "").replaceAll(" ", "");
                }
                String[] responseArrayRow = responseArray[i].split(",");
                if (externalSiteKeyField == -1 || responseArrayRow[externalSiteKeyField].equals(key)) {
                    int requestIndex = config.getInt(advertiserName + MacrosAndStrings.REQUEST_INDEX);
                    int clicksIndex = config.getInt(advertiserName + MacrosAndStrings.CLICKS_INDEX);
                    int impressionsIndex = config.getInt(advertiserName + MacrosAndStrings.IMPRESSIONS_INDEX);
                    int revenueIndex = config.getInt(advertiserName + MacrosAndStrings.REVENUE_INDEX);
                    int ecpmIndex = config.getInt(advertiserName + MacrosAndStrings.ECPM_INDEX);
                    int reportTimeIndex = config.getInt(advertiserName + MacrosAndStrings.REPORT_TIME_INDEX);
                    logger.debug("row length is " + responseArrayRow.length);
                    ReportResponse.ReportRow row = new ReportResponse.ReportRow();
                    row.request = 0;
                    row.clicks = 0;
                    row.impressions = 0;
                    row.revenue = 0.0;
                    row.ecpm = 0.0;
                    if (requestIndex != -1 && requestIndex < responseArrayRow.length) {
                        row.request = Long.parseLong(responseArrayRow[requestIndex]);
                    }
                    if (clicksIndex != -1 && clicksIndex < responseArrayRow.length) {
                        row.clicks = Long.parseLong(responseArrayRow[clicksIndex]);
                    }
                    if (impressionsIndex != -1 && impressionsIndex < responseArrayRow.length) {
                        row.impressions = Long.parseLong(responseArrayRow[impressionsIndex]);
                    }
                    if (revenueIndex != -1 && revenueIndex < responseArrayRow.length) {
                        row.revenue = Double.parseDouble(responseArrayRow[revenueIndex]);
                    }
                    if (ecpmIndex != -1 && ecpmIndex < responseArrayRow.length) {
                        row.ecpm = Double.parseDouble(responseArrayRow[ecpmIndex]);
                    }
                    row.siteId = key;
                    row.slotSize = getReportGranularity();
                    if (reportTimeIndex != -1 && reportTimeIndex < responseArrayRow.length) {
                        row.reportTime = new ReportTime(responseArrayRow[reportTimeIndex], 0);
                    }
                    else {
                        row.reportTime = new ReportTime(startTime);
                    }
                    logger.debug("parsing data inside " + advertiserName + " " + row.request);
                    reportResponse.addReportRow(row);
                }
            }
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_SERVER_ERROR;
            logger.debug("Invalid Response inside " + advertiserName + exception.getMessage());
            return reportResponse;
        }
        return reportResponse;
    }

    public ReportResponse parseResponseInJson() {
        try {
            String reportRowPath = config.getString(advertiserName + ".reportRowPath");
            String[] reportRowBlocks = reportRowPath.split("/");
            JSONObject reportRowObject = new JSONObject(responseString);
            for (int i = 0; i < reportRowBlocks.length - 1; i++) {
                reportRowObject = reportRowObject.getJSONObject(reportRowBlocks[i]);
            }
            JSONArray reportRowArray = reportRowObject.getJSONArray(reportRowBlocks[reportRowBlocks.length - 1]);
            String jsonFormat = config.getString(advertiserName + ".jsonFormat");
            if (jsonFormat.equals("object")) {
                String externalSiteKeyField = config.getString(advertiserName + ".externalSiteKeyField");
                for (int i = 0; i < reportRowArray.length(); i++) {
                    JSONObject reportRow = reportRowArray.getJSONObject(i);
                    if (externalSiteKeyField.equals("null")
                            || reportRow.getString(externalSiteKeyField).equalsIgnoreCase(key)) {
                        String requestIndex = config.getString(advertiserName + MacrosAndStrings.REQUEST_INDEX);
                        String clicksIndex = config.getString(advertiserName + MacrosAndStrings.CLICKS_INDEX);
                        String impressionsIndex = config.getString(advertiserName + MacrosAndStrings.IMPRESSIONS_INDEX);
                        String revenueIndex = config.getString(advertiserName + MacrosAndStrings.REVENUE_INDEX);
                        String ecpmIndex = config.getString(advertiserName + MacrosAndStrings.ECPM_INDEX);
                        String reportTimeIndex = config.getString(advertiserName + MacrosAndStrings.REPORT_TIME_INDEX);
                        ReportResponse.ReportRow row = new ReportResponse.ReportRow();
                        row.request = 0;
                        row.clicks = 0;
                        row.impressions = 0;
                        row.revenue = 0.0;
                        row.ecpm = 0.0;
                        if (requestIndex != null && reportRow.getString(requestIndex) != null
                                && !"null".equals(reportRow.getString(requestIndex))) {
                            row.request = Long.parseLong(reportRow.getString(requestIndex));
                        }
                        if (clicksIndex != null && reportRow.getString(clicksIndex) != null
                                && !"null".equals(reportRow.getString(clicksIndex))) {
                            row.clicks = Long.parseLong(reportRow.getString(clicksIndex));
                        }
                        if (impressionsIndex != null && reportRow.getString(impressionsIndex) != null
                                && !"null".equals(reportRow.getString(impressionsIndex))) {
                            row.impressions = Long.parseLong(reportRow.getString(impressionsIndex));
                        }
                        if (revenueIndex != null && reportRow.getString(revenueIndex) != null
                                && !"null".equals(reportRow.getString(revenueIndex))) {
                            row.revenue = Double.parseDouble(reportRow.getString(revenueIndex));
                        }
                        if (ecpmIndex != null && reportRow.getString(ecpmIndex) != null
                                && !"null".equals(reportRow.getString(ecpmIndex))) {
                            row.ecpm = Double.parseDouble(reportRow.getString(ecpmIndex));
                        }
                        row.siteId = key;
                        if (reportTimeIndex != null && reportRow.getString(reportTimeIndex) != null
                                && !"null".equals(reportRow.getString(reportTimeIndex))) {
                            row.reportTime = new ReportTime(reportRow.getString(reportTimeIndex), 0);
                        }
                        else {
                            row.reportTime = new ReportTime(startDate, 0);
                        }
                        row.slotSize = getReportGranularity();
                        logger.debug("parsing data inside " + advertiserName + " " + row.request);
                        reportResponse.addReportRow(row);
                        break;
                    }
                }
            }
            else {
                int externalSiteKeyField = config.getInt(advertiserName + ".externalSiteKeyField");
                for (int i = 0; i < reportRowArray.length(); i++) {
                    JSONArray reportRow = reportRowArray.getJSONArray(i);
                    if (externalSiteKeyField == -1 || reportRow.getString(externalSiteKeyField).equalsIgnoreCase(key)) {
                        int requestIndex = config.getInt(advertiserName + MacrosAndStrings.REQUEST_INDEX);
                        int clicksIndex = config.getInt(advertiserName + MacrosAndStrings.CLICKS_INDEX);
                        int impressionsIndex = config.getInt(advertiserName + MacrosAndStrings.IMPRESSIONS_INDEX);
                        int revenueIndex = config.getInt(advertiserName + MacrosAndStrings.REVENUE_INDEX);
                        int ecpmIndex = config.getInt(advertiserName + MacrosAndStrings.ECPM_INDEX);
                        int reportTimeIndex = config.getInt(advertiserName + MacrosAndStrings.REPORT_TIME_INDEX);
                        ReportResponse.ReportRow row = new ReportResponse.ReportRow();
                        row.request = 0;
                        row.clicks = 0;
                        row.impressions = 0;
                        row.revenue = 0.0;
                        row.ecpm = 0.0;
                        if (requestIndex != -1) {
                            row.request = Long.parseLong(reportRow.getString(requestIndex));
                        }
                        if (clicksIndex != -1) {
                            row.clicks = Long.parseLong(reportRow.getString(clicksIndex));
                        }
                        if (impressionsIndex != -1) {
                            row.impressions = Long.parseLong(reportRow.getString(impressionsIndex));
                        }
                        if (revenueIndex != -1) {
                            row.revenue = Double.parseDouble(reportRow.getString(revenueIndex));
                        }
                        if (ecpmIndex != -1) {
                            row.ecpm = Double.parseDouble(reportRow.getString(ecpmIndex));
                        }
                        row.siteId = key;
                        if (reportTimeIndex == -1) {
                            row.reportTime = new ReportTime(startDate, 0);
                        }
                        else {
                            row.reportTime = new ReportTime(reportRow.getString(reportTimeIndex), 0);
                        }
                        row.slotSize = getReportGranularity();
                        logger.debug("parsing data inside " + advertiserName + " " + row.request);
                        reportResponse.addReportRow(row);
                        break;
                    }
                }
            }
        }
        catch (JSONException jse) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_SERVER_ERROR;
            logger.info("failed to parse the json response from tapit " + jse.getMessage());
            return reportResponse;
        }
        return reportResponse;
    }

    public ReportResponse parseResponseInXml() {
        try {
            String externalSiteKeyField = config.getString(advertiserName + MacrosAndStrings.EXTERNAL_SITE_KEY_FIELD);
            String reportRowPath = config.getString(advertiserName + MacrosAndStrings.REPORT_ROW_PATH);
            String[] reportRowBlocks = reportRowPath.split("/");
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new InputSource(new java.io.StringReader(responseString)));
            doc.getDocumentElement().normalize();
            DateFormat formatDate = new SimpleDateFormat("MM-dd-yyyy");
            DateFormat repTimeFormatDate = new SimpleDateFormat("yyyyMMdd");
            NodeList reportNodes = doc.getElementsByTagName(reportRowBlocks[0]);
            for (int s = 0; s < reportNodes.getLength(); s++) {
                Node reportNode = reportNodes.item(s);
                if (reportNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element reportElement = (Element) reportNode;
                    Node statusNode = reportElement
                            .getElementsByTagName(config.getString(advertiserName + ".status"))
                                .item(0);
                    int status = Integer.parseInt(((Element) statusNode).getTextContent());
                    if (status != 0) {
                        logger.debug("status says error in response");
                        reportResponse.status = ReportResponse.ResponseStatus.FAIL_SERVER_ERROR;
                        return reportResponse;
                    }
                    NodeList reportRows = reportElement.getElementsByTagName(reportRowBlocks[1]);
                    String requestIndex = config.getString(advertiserName + MacrosAndStrings.REQUEST_INDEX);
                    String clicksIndex = config.getString(advertiserName + MacrosAndStrings.CLICKS_INDEX);
                    String impressionsIndex = config.getString(advertiserName + MacrosAndStrings.IMPRESSIONS_INDEX);
                    String revenueIndex = config.getString(advertiserName + MacrosAndStrings.REVENUE_INDEX);
                    String ecpmIndex = config.getString(advertiserName + MacrosAndStrings.ECPM_INDEX);
                    String reportTimeIndex = config.getString(advertiserName + MacrosAndStrings.REPORT_TIME_INDEX);
                    for (int ind = 0; ind < reportRows.getLength(); ind++) {
                        Element siteKey = null;
                        if (!externalSiteKeyField.equals("null")) {
                            siteKey = (Element) ((Element) reportRows.item(ind)).getElementsByTagName(
                                externalSiteKeyField).item(0);
                        }
                        if (externalSiteKeyField.equals("null") || siteKey.getTextContent().equals(key)) {
                            Element date = (Element) ((Element) reportRows.item(ind)).getElementsByTagName(
                                reportTimeIndex).item(0);
                            Element request = (Element) ((Element) reportRows.item(ind)).getElementsByTagName(
                                requestIndex).item(0);
                            Element impression = (Element) ((Element) reportRows.item(ind)).getElementsByTagName(
                                impressionsIndex).item(0);
                            Element click = (Element) ((Element) reportRows.item(ind))
                                    .getElementsByTagName(clicksIndex)
                                        .item(0);
                            Element revenue = (Element) ((Element) reportRows.item(ind)).getElementsByTagName(
                                revenueIndex).item(0);
                            Element ecpm = (Element) ((Element) reportRows.item(ind))
                                    .getElementsByTagName(ecpmIndex)
                                        .item(0);
                            Date dt = formatDate.parse(date.getTextContent().replaceAll("/", "-"));
                            ReportResponse.ReportRow row = new ReportResponse.ReportRow();
                            row.request = 0;
                            row.clicks = 0;
                            row.impressions = 0;
                            row.revenue = 0.0;
                            row.ecpm = 0.0;
                            row.reportTime = new ReportTime(repTimeFormatDate.format(dt), 0);
                            if (request != null) {
                                row.request = Long.parseLong(request.getTextContent());
                            }
                            if (impression != null) {
                                row.impressions = Long.parseLong(impression.getTextContent());
                            }
                            if (click != null) {
                                row.clicks = Long.parseLong(click.getTextContent());
                            }
                            if (revenue != null) {
                                row.revenue = Double.parseDouble(revenue.getTextContent());
                            }
                            if (ecpm != null) {
                                row.ecpm = Double.parseDouble(ecpm.getTextContent());
                            }
                            row.siteId = key;
                            row.slotSize = getReportGranularity();
                            logger.debug("parsing data inside " + advertiserName + row.request);
                            reportResponse.addReportRow(row);
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_SERVER_ERROR;
            logger.info("Invalid Response from " + advertiserName + e.getMessage());
        }
        return reportResponse;
    }

    public void debug(final Object... os) {
        System.out.println(Arrays.deepToString(os));
    }
}
