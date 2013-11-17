package com.inmobi.adserve.channels.adnetworks.xad;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.configuration.Configuration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.inmobi.adserve.channels.api.BaseReportingImpl;
import com.inmobi.adserve.channels.api.ReportResponse;
import com.inmobi.adserve.channels.api.ReportTime;
import com.inmobi.adserve.channels.util.DebugLogger;


public class DCPxAdReporting extends BaseReportingImpl {
    private final String userId;
    private final String password;
    private String       startDate;
    private String       endDate;
    private DebugLogger  logger;
    private final String host;
    private String       reportingKey;
    private String       authToken;
    private boolean      sslFlag = true;
    private final String advertiserId;

    public DCPxAdReporting(final Configuration config) {
        this.userId = config.getString("xad.userId");
        this.password = config.getString("xad.password");
        this.host = config.getString("xad.host");
        this.advertiserId = config.getString("xad.advertiserId");
    }

    @Override
    public ReportResponse fetchRows(final DebugLogger logger, final ReportTime startTime, final String key,
            final ReportTime endTime) throws Exception {
        this.logger = logger;
        ReportResponse reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);
        logger.debug("inside fetch rows of xad");
        try {
            reportingKey = key;
            this.startDate = startTime.getStringDate("-");
            logger.debug("start date inside xad is ", this.startDate);
            endDate = endTime == null ? getEndDate("-") : startDate;
            if (ReportTime.compareStringDates(this.endDate, this.startDate) == -1) {
                logger.debug("date is greater than the current date reporting window for xad");
                return null;
            }
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
            logger.info("failed to obtain correct dates for fetching reports " + exception.getMessage());
            return null;
        }
        authToken = getToken();
        String response = invokeHttpPostUrl(getRequestUrl(), host + "/revreport");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new java.io.StringReader(response)));
        doc.getDocumentElement().normalize();
        int responseStatus = Integer.parseInt(doc
                .getElementsByTagName("results")
                    .item(0)
                    .getAttributes()
                    .getNamedItem("ecode")
                    .getNodeValue());
        if (responseStatus != 0) {
            logger.info("Got non zero status from xAd . So retry required . Status", responseStatus);
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_SERVER_ERROR;
            return reportResponse;
        }
        NodeList reportNodes = doc.getElementsByTagName("report");
        // reportNodes.item(0).getAttributes().getNamedItem("appid").getNodeValue();
        // if(reportNodes.item(0).getAttributes().getNamedItem("appid").getNodeValue().equals("all")) {
        for (int s = 0; s < reportNodes.getLength(); s++) {
            String appId = reportNodes.item(0).getAttributes().getNamedItem("appid").getNodeValue();
            if (appId.length() < 36) {
                continue;
            }
            appId = appId.substring(0, 36);
            Node reportNode = reportNodes.item(s);
            if (reportNode.getNodeType() == Node.ELEMENT_NODE) {
                Element reportElement = (Element) reportNode;
                NodeList reportRows = reportElement.getElementsByTagName("record");
                for (int ind = 0; ind < reportRows.getLength(); ind++) {
                    Node zoneReportNode = reportRows.item(ind);
                    if (zoneReportNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element reportZoneElement = (Element) zoneReportNode;
                        String logDate = reportZoneElement.getAttribute("date");
                        if (startDate.compareTo(logDate) < 1) {
                            Element request = (Element) reportZoneElement.getElementsByTagName("ad_request").item(0);
                            Element impression = (Element) reportZoneElement
                                    .getElementsByTagName("ad_impression")
                                        .item(0);
                            Element clicks = (Element) reportZoneElement.getElementsByTagName("ad_click").item(0);
                            Element revenue = (Element) reportZoneElement.getElementsByTagName("net_revenue").item(0);
                            // Element ecpm = (Element) (((Element)
                            // reportZoneElement.getElementsByTagName("avg_cpm").item(0)));
                            ReportResponse.ReportRow row = new ReportResponse.ReportRow();
                            if (!decodeBlindedSiteId(appId, row)) {
                                logger.debug("Error decoded BlindedSite id in Drawbridge", appId);
                                continue;
                            }
                            row.request = Long.parseLong(request.getTextContent());
                            row.clicks = Long.parseLong(clicks.getTextContent());
                            row.impressions = Long.parseLong(impression.getTextContent());
                            row.revenue = Double.parseDouble(revenue.getTextContent());
                            ReportTime reportDate = new ReportTime(logDate, 0);
                            row.reportTime = reportDate;
                            // row.siteId = key;
                            row.isSiteData = true;
                            row.slotSize = getReportGranularity();
                            reportResponse.addReportRow(row);
                        }
                    }
                }
            }
        }
        // }
        return reportResponse;
    }

    @Override
    public String getRequestUrl() {
        StringBuilder reportUrl = new StringBuilder();
        reportUrl
                .append("v=1.0&appid=all&k=")
                    .append(reportingKey)
                    .append("&token=")
                    .append(authToken)
                    .append("&S_date=")
                    .append(startDate)
                    .append("&E_date=")
                    .append(endDate);
        return reportUrl.toString();
    }

    @Override
    public double getTimeZone() {
        return 1;
    }

    @Override
    public ReportGranularity getReportGranularity() {
        return ReportGranularity.DAY;
    }

    @Override
    public int ReportReconcilerWindow() {
        return 20;
    }

    @Override
    public String getName() {
        return "xad";
    }

    @Override
    public String getAdvertiserId() {
        return (advertiserId);
    }

    public String getEndDate(final String seperator) {
        try {
            logger.debug("calculating end date for xad");
            ReportTime reportTime = ReportTime.getUTCTime();
            reportTime = ReportTime.getPreviousDay(reportTime);
            reportTime = ReportTime.getPreviousDay(reportTime);
            if (reportTime.getHour() < ReportReconcilerWindow()) {
                reportTime = ReportTime.getPreviousDay(reportTime);
            }
            return (reportTime.getStringDate(seperator));
        }
        catch (Exception exception) {
            logger.info("failed to obtain end date inside xad " + exception.getMessage());
            return "";
        }
    }

    private String getToken() throws IOException, NoSuchAlgorithmException, KeyManagementException,
            ParserConfigurationException, SAXException {
        StringBuilder authUrlParam = new StringBuilder();
        authUrlParam.append("v=1.0&user=").append(userId).append("&pass=").append(password);
        String tokenOutput = invokeHttpPostUrl(authUrlParam.toString(), host + "/auth");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new java.io.StringReader(tokenOutput)));
        doc.getDocumentElement().normalize();
        return (doc.getDocumentElement().getAttributes().getNamedItem("token").getNodeValue());
    }

    private String invokeHttpPostUrl(final String urlParameters, final String hostAddress) throws IOException,
            NoSuchAlgorithmException, KeyManagementException {

        if (sslFlag == true) {
            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(final X509Certificate[] certs, final String authType) {
                }

                @Override
                public void checkServerTrusted(final X509Certificate[] certs, final String authType) {
                }
            } };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                @Override
                public boolean verify(final String hostname, final SSLSession session) {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
            sslFlag = false;
        }
        URL url = new URL(hostAddress);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("charset", "utf-8");
        connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
        connection.setUseCaches(false);

        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(urlParameters);
        String line;
        BufferedReader reader = null;
        StringBuilder responseBuilder = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
        }
        catch (IOException ioe) {
            logger.info("Error in Httpool invokeHTTPUrl : ", ioe.getMessage());
        }
        finally {
            wr.flush();
            wr.close();
            if (reader != null) {
                reader.close();
            }
            connection.disconnect();
        }
        return responseBuilder.toString();
    }
}