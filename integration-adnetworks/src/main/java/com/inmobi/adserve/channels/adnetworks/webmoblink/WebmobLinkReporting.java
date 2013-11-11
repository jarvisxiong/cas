package com.inmobi.adserve.channels.adnetworks.webmoblink;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.configuration.Configuration;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.inmobi.adserve.channels.api.BaseReportingImpl;
import com.inmobi.adserve.channels.api.ReportResponse;
import com.inmobi.adserve.channels.api.ReportTime;
import com.inmobi.adserve.channels.api.ServerException;
import com.inmobi.adserve.channels.util.DebugLogger;


public class WebmobLinkReporting extends BaseReportingImpl {
    private DebugLogger         logger;
    private final Configuration config;
    private final String        token;
    private String              pubId;
    private final String        baseUrl;
    private String              startDate;
    private String              endDate;
    private final DateFormat    webmoblinkFormatDate = new SimpleDateFormat("MM-dd-yyyy");
    private final DateFormat    repTimeFormatDate    = new SimpleDateFormat("yyyyMMdd");

    public WebmobLinkReporting(final Configuration config) {
        this.config = config;
        token = config.getString("webmoblink.token");
        baseUrl = config.getString("webmoblink.baseUrl");
        webmoblinkFormatDate.setTimeZone(TimeZone.getTimeZone("GMT"));
        repTimeFormatDate.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    @Override
    public int ReportReconcilerWindow() {
        return 3;
    }

    @Override
    public ReportResponse fetchRows(final DebugLogger logger, final ReportTime startTime, final String key,
            final ReportTime endTime) throws ParserConfigurationException, DOMException, ParseException, SAXException,
            IOException {
        this.logger = logger;
        pubId = key;
        ReportResponse reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);
        String responseString = null;
        logger.debug("inside fetch rows of Webmoblink");
        try {
            startDate = startTime.getStringDate("/");
            logger.debug("start date inside Webmoblink is " + startDate);
            endDate = endTime == null ? getEndDate() : startDate;
            logger.debug("end date inside Webmoblink is " + endDate);
            if (ReportTime.compareStringDates(endDate, startDate) == -1) {
                logger.debug("end date is less than start date plus reporting window for Webmoblink");
                return null;
            }
            String url = getRequestUrl();
            logger.debug("url inside Webmoblink is " + url);
            responseString = invokeUrl(url, null, logger);
        }
        catch (ServerException se) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_SERVER_ERROR;
            logger.info("failed to collect response from Webmoblink " + se.getMessage());
            return reportResponse;
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
            logger.info("failed to obtain correct dates for fetching reports " + exception.getMessage());
            return reportResponse;
        }
        logger.debug("successfully invoked url, so will do xml parsing");
        // XML Parsing
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(new InputSource(new java.io.StringReader(responseString)));
        doc.getDocumentElement().normalize();
        NodeList reportNodes = doc.getElementsByTagName("adResponse");
        for (int s = 0; s < reportNodes.getLength(); s++) {
            Node reportNode = reportNodes.item(s);
            if (reportNode.getNodeType() == Node.ELEMENT_NODE) {
                Element reportElement = (Element) reportNode;
                Node statusNode = reportElement.getElementsByTagName("status").item(0);
                int status = Integer.parseInt(((Element) statusNode).getTextContent());
                if (status != 0) {
                    switch (status) {
                        case 1:
                            logger.info("Error. Missing mandatory parameters");
                            break;
                        case 2:
                            logger.info("Error. The parameter 'pid' is invalid");
                            break;
                        case 3:
                            logger.info("Error. The parameter 'startDate' is invalid");
                            break;
                        case 4:
                            logger.info("Error. The parameter 'endDate' is invalid");
                            break;
                        case 5:
                            logger.info("Error. The time period is invalid");
                            break;
                        case 6:
                            logger.info("Error. Unknown publisher ID or Credential");
                            break;
                        case 7:
                            logger.info("Error. The period is more than 60 days");
                            break;
                    }
                    reportResponse.status = ReportResponse.ResponseStatus.FAIL_SERVER_ERROR;
                    return reportResponse;
                }

                NodeList reportRows = reportElement.getElementsByTagName("reportRow");
                for (int ind = 0; ind < reportRows.getLength(); ind++) {
                    ReportResponse.ReportRow row = new ReportResponse.ReportRow();
                    Element date = (Element) ((Element) reportRows.item(ind)).getElementsByTagName("date").item(0);
                    Element request = (Element) ((Element) reportRows.item(ind))
                            .getElementsByTagName("request")
                                .item(0);
                    Element impression = (Element) ((Element) reportRows.item(ind))
                            .getElementsByTagName("impression")
                                .item(0);
                    Element click = (Element) ((Element) reportRows.item(ind)).getElementsByTagName("click").item(0);
                    Element revenue = (Element) ((Element) reportRows.item(ind))
                            .getElementsByTagName("revenue")
                                .item(0);
                    Date dt = webmoblinkFormatDate.parse(date.getTextContent().replaceAll("/", "-"));
                    row.reportTime = new ReportTime(repTimeFormatDate.format(dt), 0);
                    row.request = Long.parseLong(request.getTextContent());
                    row.impressions = Long.parseLong(impression.getTextContent());
                    row.clicks = Long.parseLong(click.getTextContent());
                    row.revenue = Double.parseDouble(revenue.getTextContent());
                    row.siteId = key;
                    row.slotSize = getReportGranularity();
                    logger.debug("parsing data inside Webmoblink " + row.request);
                    reportResponse.addReportRow(row);
                }
            }
        }

        return reportResponse;
    }

    @Override
    public String getAdvertiserId() {
        return (config.getString("webmoblink.advertiserId"));
    }

    @Override
    public String getName() {
        return "webmoblink";
    }

    @Override
    public ReportGranularity getReportGranularity() {
        return ReportGranularity.DAY;
    }

    @Override
    public String getRequestUrl() {
        String url = String.format("%s?pid=%s&credential=%s&startdate=%s&enddate=%s", baseUrl, pubId, token, startDate,
            endDate);
        return url;
    }

    @Override
    public double getTimeZone() {
        return 0;
    }

    public String getEndDate() throws Exception {
        try {
            logger.debug("calculating end date for WebmobLink");
            ReportTime reportTime = ReportTime.getUTCTime();
            reportTime = ReportTime.getPreviousDay(reportTime);
            if (reportTime.getHour() <= ReportReconcilerWindow()) {
                reportTime = ReportTime.getPreviousDay(reportTime);
            }
            return (reportTime.getStringDate("/"));
        }
        catch (Exception exception) {
            logger.info("failed to obtain end date inside WebMobLink " + exception.getMessage());
            return "";
        }
    }

}
