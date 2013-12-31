package com.inmobi.adserve.channels.adnetworks.httpool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.Configuration;
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


/**
 * @author thushara
 * 
 */
public class DCPHttPoolReporting extends BaseReportingImpl {
    private static final Logger LOG              = LoggerFactory.getLogger(DCPHttPoolReporting.class);

    private final Configuration config;
    private final String        password;
    private String              date;
    private final String        host;
    private final String        emailId;
    private static String       reportStartDate  = null;
    private static String       entireReportData = null;
    private String              endDate;
    private final Connection    connection;

    public DCPHttPoolReporting(final Configuration config, final Connection connection) {
        this.config = config;
        this.connection = connection;
        password = config.getString("httpool.password");
        host = config.getString("httpool.host");
        emailId = config.getString("httpool.email");
    }

    @Override
    public String invokeHTTPUrl(final String url) throws ServerException {
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

        try {
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
            URLConnection conn = new URL(url).openConnection();
            // Setting connection and read timeout to 5 min
            conn.setReadTimeout(300000);
            conn.setConnectTimeout(300000);
            conn.setRequestProperty("X-WSSE", getHeader());
            conn.setDoOutput(true);
            InputStream in = conn.getInputStream();
            StringBuffer sBuffer = new StringBuffer();

            BufferedReader res = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String inputLine;
            while ((inputLine = res.readLine()) != null) {
                sBuffer.append(inputLine);
            }
            res.close();
            return sBuffer.toString();

        }
        catch (Exception e) {
            LOG.info("Error in Httpool invokeHTTPUrl : ", e.getMessage());
            throw new ServerException(e);
        }

    }

    @Override
    public int ReportReconcilerWindow() {
        return 10;
    }

    @Override
    public ReportResponse fetchRows(final ReportTime startTime, final String key, final ReportTime endTime)
            throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException, ServerException,
            IOException, ParserConfigurationException, SAXException {
        ReportResponse reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);

        LOG.debug("inside fetch rows of httpool");
        try {
            date = startTime.getStringDate("-");
            LOG.debug("start date inside httpool is {}", date);
            endDate = endTime == null ? getEndDate("-") : date;

            if (ReportTime.compareStringDates(endDate, date) == -1) {
                LOG.debug("date is greater than the current date reporting window for httpool");
                return null;
            }
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
            LOG.info("failed to obtain correct dates for fetching reports {}", exception);
            return null;
        }
        date = startTime.getStringDate("-");
        String requestUrl = host.replaceAll("date_from", date).replaceAll("date_to", getEndDate("-"));
        if (reportStartDate == null || (ReportTime.compareStringDates(reportStartDate, date) > 0)) {
            entireReportData = invokeHTTPUrl(requestUrl);
            reportStartDate = date;
        }
        LOG.debug("Response from Httpool is {}", entireReportData);

        // boolean gotReport = false;
        // parse the xml response
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        entireReportData = entireReportData.substring(entireReportData.indexOf("<?xml version"));
        Document doc = builder.parse(new InputSource(new java.io.StringReader(entireReportData)));
        doc.getDocumentElement().normalize();
        NodeList reportNodes = doc.getElementsByTagName("ASN");
        String advertiserName = "httpool";
        for (int s = 0; s < reportNodes.getLength(); s++) {
            Node reportNode = reportNodes.item(s);
            if (reportNode.getNodeType() == Node.ELEMENT_NODE) {
                Element reportElement = (Element) reportNode;
                NodeList reportRows = reportElement.getElementsByTagName("Zone");
                for (int ind = 0; ind < reportRows.getLength(); ind++) {
                    Node zoneReportNode = reportRows.item(ind);
                    if (zoneReportNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element reportZoneElement = (Element) zoneReportNode;
                        String logDate = reportZoneElement.getAttribute("Date");
                        if (key.equals(reportZoneElement.getAttribute("Id")) && date.compareTo(logDate) < 1) {
                            Element impression = (Element) (((Element) reportZoneElement.getElementsByTagName(
                                "Impressions").item(0)).getElementsByTagName("Total").item(0));
                            Element clicks = (Element) (((Element) reportZoneElement
                                    .getElementsByTagName("Clicks")
                                        .item(0)).getElementsByTagName("Total").item(0));
                            Element revenue = (Element) (((Element) reportZoneElement
                                    .getElementsByTagName("Revenue")
                                        .item(0)).getElementsByTagName("Total").item(0));
                            ReportResponse.ReportRow row = new ReportResponse.ReportRow();
                            row.request = 0;
                            row.clicks = Long.parseLong(clicks.getTextContent());
                            row.impressions = Long.parseLong(impression.getTextContent());
                            row.revenue = Double.parseDouble(revenue.getTextContent());
                            ReportTime reportDate = new ReportTime(logDate, 0);
                            double rate = getCurrencyConversionRate("EUR", reportDate.getStringDate("-"), connection);
                            if (rate > 0) {
                                row.revenue /= rate;
                            }
                            else {
                                LOG.info("Failed to get EUR to USD rate for ", logDate);
                                return null;
                            }
                            row.ecpm = 0.0;
                            row.reportTime = reportDate;
                            row.siteId = key;
                            row.slotSize = getReportGranularity();
                            // gotReport = true;
                            LOG.debug("parsing data inside {} {}", advertiserName, row.request);
                            reportResponse.addReportRow(row);
                        }
                    }
                }
            }
        }

        return reportResponse;
    }

    @Override
    public String getAdvertiserId() {
        return (config.getString("httpool.advertiserId"));
    }

    @Override
    public String getName() {
        return "httpool";
    }

    @Override
    public ReportGranularity getReportGranularity() {
        return ReportGranularity.DAY;
    }

    @Override
    public String getRequestUrl() {
        StringBuilder url = new StringBuilder();
        url.append(host).append("?token=").append(password);
        url.append("&p=start=").append(date).append(";end=").append(endDate);
        url.append(";group=day");
        url.append("&format=json");
        return url.toString();
    }

    @Override
    public double getTimeZone() {
        return 1;
    }

    public String getEndDate(final String seperator) {
        try {
            LOG.debug("calculating end date for httpool");
            ReportTime reportTime = ReportTime.getUTCTime();
            reportTime = ReportTime.getPreviousDay(reportTime);
            if (reportTime.getHour() <= ReportReconcilerWindow()) {
                reportTime = ReportTime.getPreviousDay(reportTime);
            }
            return (reportTime.getStringDate(seperator));
        }
        catch (Exception exception) {
            LOG.info("failed to obtain end date inside httpool ", exception.getMessage());
            return "";
        }
    }

    private String getHeader() throws UnsupportedEncodingException {
        byte[] nonceB = generateNonce();
        String nonce = base64Encode(nonceB);
        String created = generateTimestamp();
        String password64 = getBase64Digest(nonceB, created.getBytes("UTF-8"), password.getBytes("UTF-8"));
        StringBuffer header = new StringBuffer("UsernameToken Username=\"");
        header.append(emailId);
        header.append("\", ");
        header.append("PasswordDigest=\"");
        header.append(password64.trim());
        header.append("\", ");
        header.append("Nonce=\"");
        header.append(nonce.trim());
        header.append("\", ");
        header.append("Created=\"");
        header.append(created);
        header.append("\"");
        return header.toString();
    }

    private static byte[] generateNonce() {
        String nonce = Long.toString(new Date().getTime());
        return nonce.getBytes();
    }

    /**
     * @return current date and time in required format (W3DTF)
     */
    private String generateTimestamp() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        return dateFormatter.format(new Date());
    }

    private static synchronized String getBase64Digest(final byte[] nonce, final byte[] created, final byte[] password) {
        try {
            MessageDigest messageDigester = MessageDigest.getInstance("SHA-1");
            // SHA-1 ( nonce + created + password )
            messageDigester.reset();
            messageDigester.update(nonce);
            messageDigester.update(created);
            messageDigester.update(password);
            return base64Encode(messageDigester.digest());
        }
        catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String base64Encode(final byte[] bytes) {
        return new String(Base64.encodeBase64(bytes));
    }
}