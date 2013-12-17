package com.inmobi.adserve.channels.adnetworks.logan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.configuration.Configuration;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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


public class DCPLoganReporting extends BaseReportingImpl {
    private static final Logger LOG              = LoggerFactory.getLogger(DCPLoganReporting.class);

    private final Configuration config;
    private final String        apiKey;
    private String              date;
    private final String        baseUrl;
    private String              endDate;

    private static String       reportStartDate  = null;
    private static String       entireReportData = null;
    private final String        networkId;
    private static final String requestTemplate  = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\""
                                                         + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:rep=\"http://adserver.moceanmobile.net/soap/reporting\">"
                                                         + "<soapenv:Header><mns1:authHeader xmlns:mns1=\"http://adserver.moceanmobile.net/soap/reporting\"><apiKey>%s</apiKey></mns1:authHeader></soapenv:Header>"
                                                         + "<soapenv:Body><rep:getZoneReportByZone><fromDate xsi:type=\"xsd:string\">%s</fromDate><toDate xsi:type=\"xsd:string\">%s</toDate>"
                                                         + "<networkIds xsi:type=\"rep:ArrayOfInt\"><item xsi:type=\"xsd:int\">%s</item> </networkIds></rep:getZoneReportByZone></soapenv:Body>"
                                                         + "</soapenv:Envelope>";

    public DCPLoganReporting(final Configuration config) {
        this.config = config;
        networkId = config.getString("logan.networkId");
        apiKey = config.getString("logan.apiKey");
        baseUrl = config.getString("logan.host");

    }

    @Override
    public int ReportReconcilerWindow() {
        return 23;
    }

    @Override
    public ReportResponse fetchRows(final ReportTime startTime, final String key, final ReportTime endTime)
            throws ClientProtocolException, IOException, ServerException, JSONException, ParserConfigurationException,
            SAXException {
        ReportResponse reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);
        LOG.debug("inside fetch rows of logan");
        try {
            date = startTime.getStringDate("-");
            LOG.debug("start date inside logan is {}", date);
            endDate = endTime == null ? getEndDate("-") : date;
            if (ReportTime.compareStringDates(endDate, date) == -1) {
                LOG.debug("date is greater than the current date reporting window for logan");
                return null;
            }
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
            LOG.info("failed to obtain correct dates for fetching reports {}", exception.getMessage());
            return null;
        }
        date = startTime.getStringDate("-");
        if (reportStartDate == null || (ReportTime.compareStringDates(reportStartDate, date) > 0)) {
            entireReportData = invokeHTTPUrl();
            reportStartDate = date;
        }
        LOG.debug("response is {}", entireReportData);

        // parse xml
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new java.io.StringReader(entireReportData)));
        doc.getDocumentElement().normalize();
        NodeList nodeList = doc.getElementsByTagName("item");
        for (int s = 0; s < nodeList.getLength(); s++) {

            Node zoneReportNode = nodeList.item(s);
            if (zoneReportNode.getNodeType() == Node.ELEMENT_NODE) {
                Element reportZoneElement = (Element) zoneReportNode;
                String logDate = ((Element) reportZoneElement.getElementsByTagName("date").item(0)).getTextContent();
                String zoneId = ((Element) reportZoneElement.getElementsByTagName("zoneId").item(0)).getTextContent();

                if (date.compareTo(logDate) < 1 && zoneId.equals(key)) {
                    ReportResponse.ReportRow row = new ReportResponse.ReportRow();
                    Element request = (Element) reportZoneElement.getElementsByTagName("requests").item(0);
                    Element impression = (Element) reportZoneElement.getElementsByTagName("impressions").item(0);
                    Element clicks = (Element) reportZoneElement.getElementsByTagName("clicks").item(0);
                    Element revenue = (Element) reportZoneElement.getElementsByTagName("earnings").item(0);
                    Element ecpm = (((Element) reportZoneElement.getElementsByTagName("ecpmEarnings").item(0)));

                    row.request = Long.parseLong(request.getTextContent());
                    row.clicks = Long.parseLong(clicks.getTextContent());
                    row.impressions = Long.parseLong(impression.getTextContent());
                    row.revenue = Double.parseDouble(revenue.getTextContent());
                    ReportTime reportDate = new ReportTime(logDate, 0);
                    row.ecpm = Double.parseDouble(ecpm.getTextContent());
                    row.reportTime = reportDate;
                    row.siteId = zoneId;
                    row.slotSize = getReportGranularity();
                    LOG.debug("parsing data inside Logan {}", row.request);
                    reportResponse.addReportRow(row);
                }
            }
        }
        return reportResponse;
    }

    @Override
    public String getAdvertiserId() {
        return (config.getString("logan.advertiserId"));
    }

    @Override
    public String getName() {
        return "Logan";
    }

    @Override
    public ReportGranularity getReportGranularity() {
        return ReportGranularity.DAY;
    }

    @Override
    public String getRequestUrl() {
        return String.format(requestTemplate, apiKey, date, endDate, networkId);
    }

    @Override
    public double getTimeZone() {
        return -3;
    }

    public String getEndDate(final String seperator) {
        try {
            LOG.debug("calculating end date for logan");
            ReportTime reportTime = ReportTime.getUTCTime();
            reportTime = ReportTime.getPreviousDay(reportTime);
            if (reportTime.getHour() <= ReportReconcilerWindow()) {
                reportTime = ReportTime.getPreviousDay(reportTime);
            }
            return (reportTime.getStringDate(seperator));
        }
        catch (Exception exception) {
            LOG.info("failed to obtain end date inside logan {}", exception);
            return "";
        }
    }

    public String invokeHTTPUrl() throws ServerException, ClientProtocolException, IOException {
        URL url = new URL(baseUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        String soapMessage = getRequestUrl();
        connection.setRequestProperty("Content-Length", String.valueOf(soapMessage.length()));
        connection.setRequestProperty("Content-Type", "text/xml");
        connection.setRequestProperty("Connection", "Close");
        connection.setRequestProperty("SoapAction", "");
        connection.setDoOutput(true);
        PrintWriter pw = new PrintWriter(connection.getOutputStream());
        pw.write(soapMessage);
        pw.flush();
        connection.connect();
        InputStream in = connection.getInputStream();
        BufferedReader res = null;
        StringBuffer sBuffer = new StringBuffer();
        try {
            res = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String inputLine;
            while ((inputLine = res.readLine()) != null) {
                sBuffer.append(inputLine);
            }
        }
        catch (IOException ioe) {
            LOG.info("Error in Logan invokeHTTPUrl : {}", ioe);
        }
        finally {
            if (res != null) {
                res.close();
            }
        }

        return sBuffer.toString();
    }

}