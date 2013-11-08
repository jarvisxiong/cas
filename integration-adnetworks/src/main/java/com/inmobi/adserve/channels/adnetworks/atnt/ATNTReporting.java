package com.inmobi.adserve.channels.adnetworks.atnt;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.configuration.Configuration;
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


public class ATNTReporting extends BaseReportingImpl
{
    private Configuration config;
    private String        userName;
    private String        password;
    private String        format;
    private String        startDate   = "";
    private String        endDate     = "";
    private String        apiKey;
    private String        granularity = null;

    private DebugLogger   logger;

    public ATNTReporting(final Configuration config)
    {
        this.config = config;
        userName = config.getString("atnt.username");
        password = config.getString("atnt.password");
        format = config.getString("atnt.format");
    }

    // Fetches the report from the TPAN
    @Override
    public ReportResponse fetchRows(final DebugLogger logger, final ReportTime startTime, final String key,
            final ReportTime endTime)
    {
        this.logger = logger;
        logger.debug("fetching rows inside atnt reporter");
        String soapMessage = "";
        ReportResponse reportResponse = null;
        if (getReportGranularity() == ReportGranularity.DAY) {
            granularity = ReportGranularity.DAY.toString();
        }
        else {
            granularity = ReportGranularity.HOUR.toString();
        }
        apiKey = key;
        String responseString = "";
        String url = getRequestUrl();
        logger.debug("url inside atnt is" + url);
        try {
            endDate = endTime == null ? getEndDate(startTime) : startDate;
            logger.debug("start date inside atnt is " + startDate);
            logger.debug("end date inside atnt is " + endDate);
            if (ReportTime.compareStringDates(endDate, startDate) == -1) {
                logger.debug("end date smaller than starting date plus reporting window, so returning back");
                return null;
            }
            soapMessage = getSoapMessage();
            logger.debug("soap message is " + soapMessage);
        }
        catch (Exception exception) {
            logger.info("invalid date arguments so cant send request to ant reporter " + exception.getMessage());
            reportResponse = new ReportResponse(ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR);
            return reportResponse;
        }
        try {
            responseString = invokeUrl(url, soapMessage, logger);
            logger.debug("response string inside atnt is " + responseString);
        }
        catch (ServerException exception) {
            reportResponse = new ReportResponse(ReportResponse.ResponseStatus.FAIL_SERVER_ERROR);
            logger.info("failed to invoke url properly inside atnt " + exception.getMessage());
            return reportResponse;
        }
        if (responseString == null) {
            logger.debug("Errorneous data so exiting application");
            reportResponse = new ReportResponse(ReportResponse.ResponseStatus.FAIL_SERVER_ERROR);
            return reportResponse;
        }
        reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);
        logger.debug("successfully invoked url, so will do xml parsing");
        try {
            // XML Parsing
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new InputSource(new java.io.StringReader(responseString)));
            doc.getDocumentElement().normalize();
            NodeList dateNodes = doc.getElementsByTagName("dateNodes");

            for (int s = 0; s < dateNodes.getLength(); s++) {
                Node dateNode = dateNodes.item(s);
                if (dateNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element dateElement = (Element) dateNode;
                    NodeList apiNodeList = dateElement.getElementsByTagName("apiKeyList");

                    for (int t = 0; t < apiNodeList.getLength(); t++) {
                        Node apiNode = apiNodeList.item(t);
                        if (dateNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element apiNodeElement = (Element) apiNode;
                            NodeList apiKeyList = apiNodeElement.getElementsByTagName("apiKeyNode");
                            for (int index = 0; index < apiKeyList.getLength(); index++) {
                                Node apiKeyNode = apiKeyList.item(index);
                                Element apiKeyElement = (Element) apiKeyNode;
                                if ((apiKeyElement.getAttribute("apiKey")).equals(key)) {
                                    ReportResponse.ReportRow row = new ReportResponse.ReportRow();
                                    row.ecpm = Double.valueOf(apiKeyElement.getAttribute("ecpm")).doubleValue();
                                    row.impressions = Long.parseLong(apiKeyElement.getAttribute("impressions"));
                                    row.revenue = Double
                                            .valueOf(apiKeyElement.getAttribute("estearnings"))
                                                .doubleValue();
                                    row.clicks = Long.parseLong(apiKeyElement.getAttribute("bannerclicks"));
                                    row.request = -1;
                                    row.reportTime = new ReportTime(dateElement.getAttribute("date"), 0);
                                    row.siteId = key;
                                    row.slotSize = getReportGranularity();
                                    logger.debug("fetching rows inside atnt");
                                    reportResponse.addReportRow(row);
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_SERVER_ERROR;
            logger.info("Invalid Response from atnt " + exception.getMessage());
            return reportResponse;
        }
        logger.debug("returning from fetch rows of atnt");
        return reportResponse;
    }

    public String getEndDate(final ReportTime startTime)
    {
        logger.debug("fetching end date of atnt");
        ReportTime reportTime = ReportTime.getPacificTime();
        reportTime = ReportTime.getPreviousDay(reportTime);
        if (reportTime.getHour() <= ReportReconcilerWindow()) {
            reportTime = ReportTime.getPreviousDay(reportTime);
        }
        ReportTime previousMonth = ReportTime.getPreviousMonth(reportTime);
        if (ReportTime.compareDates(previousMonth, startTime) == 1) {
            logger.info("start date too early for date to be fetched so changing it accordingly");
            startDate = previousMonth.getStringDate("-");
        }
        else {
            startDate = startTime.getStringDate("-");
        }

        return (reportTime.getStringDate("-"));
    }

    @Override
    public double getTimeZone()
    {
        return -7.0;
    }

    @Override
    public String getAdvertiserId()
    {
        return config.getString("atnt.advertiserId");
    }

    @Override
    public String getRequestUrl()
    {
        return config.getString("atnt.server");
    }

    private String getSoapMessage()
    {

        String soapMessage = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\""
                + "\n\n"
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:rep=\"http://report.webservice.lemur.atti.com/\">"
                + "\n\n"
                + "<soapenv:Header xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">"
                + "\n"
                + "<wsse:Security><wsse:UsernameToken><wsse:Username>"
                + userName
                + "</wsse:Username><wsse:Password>"
                + password
                + "</wsse:Password></wsse:UsernameToken></wsse:Security></soapenv:Header><soapenv:Body><rep:getPerformanceByRange>"
                + "<request><apiKey>" + apiKey + "</apiKey><format>" + format + "</format><startDate>" + startDate
                + "</startDate><endDate>" + endDate + "</endDate><granularity>" + granularity
                + "</granularity></request></rep:getPerformanceByRange></soapenv:Body>" + "</soapenv:Envelope>";

        return soapMessage;
    }

    @Override
    public ReportGranularity getReportGranularity()
    {
        return ReportGranularity.DAY;
    }

    @Override
    public String getName()
    {
        return "ATNT";
    }

    @Override
    public int ReportReconcilerWindow()
    {
        return 6;
    }
}
