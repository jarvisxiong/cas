package com.inmobi.adserve.channels.adnetworks.paypal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.inmobi.adserve.channels.api.BaseReportingImpl;
import com.inmobi.adserve.channels.api.ReportResponse;
import com.inmobi.adserve.channels.api.ReportTime;
import com.inmobi.adserve.channels.api.ServerException;


public class DCPPaypalReporting extends BaseReportingImpl {
    private static final Logger        LOG = LoggerFactory.getLogger(DCPPaypalReporting.class);

    private String                     startDate;
    private String                     endDate;
    private final String               host;
    private String                     externalSiteId;
    private final String               advertiserId;
    private static Map<String, String> reportingKeyMap;

    static {
        reportingKeyMap = new HashMap<String, String>();
        reportingKeyMap.put("207e5fc231", "54b5e311");
        reportingKeyMap.put("f181ed00cb", "0690c7de");
        reportingKeyMap.put("b73652a399", "420e6d0b");
        reportingKeyMap.put("792cc54300", "eb65bbce");
        reportingKeyMap.put("ee4a66b7d1", "fb52c64e");
        reportingKeyMap.put("f468cfcc86", "5fe9d653");
        reportingKeyMap.put("4ff73ef7af", "207c143e");
        reportingKeyMap.put("1f346fdb86", "72147730");
        reportingKeyMap.put("588712f524", "c8371b89");
        reportingKeyMap.put("aec3168e7b", "19474174");
        reportingKeyMap.put("211800c1b6", "de5b39fb");
        reportingKeyMap.put("76cb710453", "264b5ea7");
        reportingKeyMap.put("7ee9513501", "8953b25c");
        reportingKeyMap.put("a95b8f20b2", "bc1d6331");
        reportingKeyMap.put("7605ba8640", "d780969a");
        reportingKeyMap.put("fa091603a5", "07a9bfb9");
        reportingKeyMap.put("7359b9070a", "77b4c230");
        reportingKeyMap.put("93e8fae89e", "7504e4e3");
        reportingKeyMap.put("acc0c687b5", "8ac983e9");
        reportingKeyMap.put("5b883f61c2", "0cae55dc");
        reportingKeyMap.put("6aeec0e519", "57e1c23f");
        reportingKeyMap.put("9c1fb5f796", "53a24192");
        reportingKeyMap.put("4947f4240e", "2deeb634");
        reportingKeyMap.put("88df4219cf", "19c764ef");
        reportingKeyMap.put("e481c5b72b", "2397c754");
        reportingKeyMap.put("87fe4d8604", "ae6a7a59");
        reportingKeyMap.put("0fe733a5fd", "bcd44b16");
        reportingKeyMap.put("da095fc538", "f0cdb963");
        reportingKeyMap.put("6d33a8cc3c", "229c8a53");
        reportingKeyMap.put("28e560e03b", "a1a0b3c8");
        reportingKeyMap.put("64fc8b2541", "0675a1dc");
        reportingKeyMap.put("f8402bc822", "b29a7fba");
        reportingKeyMap.put("46beff4053", "7ca275c7");
        reportingKeyMap.put("69e75c0dd3", "fb475fa9");
        reportingKeyMap.put("ed9870553d", "3c5ce633");
        reportingKeyMap.put("39fffa2f52", "8c28ba0f");
        reportingKeyMap.put("4e61e78fca", "5695d1d3");
        reportingKeyMap.put("193e3e1fc5", "2e585d67");
        reportingKeyMap.put("3f7d4a6ac7", "213bf80f");
        reportingKeyMap.put("5297f9fcf9", "99bb28d7");
        reportingKeyMap.put("b466ad8abc", "33927951");
        reportingKeyMap.put("aa9f863f16", "5ffc996a");
        reportingKeyMap.put("31eee5333a", "0c5e5bb6");
        reportingKeyMap.put("6bfaaa3e83", "b4e12b36");
        reportingKeyMap.put("5658b33502", "9aa4f7f6");
        reportingKeyMap.put("513b38d964", "670efc2c");
        reportingKeyMap.put("570e7cead8", "117d9d49");
        reportingKeyMap.put("aa4e2a87ed", "1255ea47");
        reportingKeyMap.put("7f0d842f91", "76fceea4");
        reportingKeyMap.put("705665a7d2", "9a3cfc64");
        reportingKeyMap.put("d0c2de3710", "25d0cbb0");
        reportingKeyMap.put("889a8760fc", "e1c90e14");
        reportingKeyMap.put("c0e41269cf", "7239e46e");
        reportingKeyMap.put("4faa73c133", "61ad9211");
        reportingKeyMap.put("4bc126fb29", "098381ec");
        reportingKeyMap.put("eef449991d", "86b2b154");
        reportingKeyMap.put("ff44892e62", "274f145b");
        reportingKeyMap.put("4d1c8449b4", "784b3cfe");
        reportingKeyMap.put("f52450882d", "eb2d2fce");
        reportingKeyMap.put("d095df3cd7", "34cb3aff");
        reportingKeyMap.put("0f7d14e44b", "d1152de2");
        reportingKeyMap.put("b7a6c2f1f9", "789b93c0");
        reportingKeyMap.put("d14f53377e", "ddbf6e61");
        reportingKeyMap.put("0dfac1d1ce", "b3a8cdb6");
        reportingKeyMap.put("a270cefd0d", "a5d1820d");
        reportingKeyMap.put("2fd9aaa7ba", "19e88cfa");
        reportingKeyMap.put("5fc6ca072e", "833f2699");
        reportingKeyMap.put("6aa38a6c53", "9a7d18c2");
        reportingKeyMap.put("a5945c6f68", "a00aa296");
        reportingKeyMap.put("7c13fb5455", "672f6932");
        reportingKeyMap.put("d4c4bc67af", "8abdf691");
        reportingKeyMap.put("93d6226cfa", "044227a9");
        reportingKeyMap.put("03b9b5d1ca", "bda65038");
        reportingKeyMap.put("e264008ce9", "17e3b72b");
        reportingKeyMap.put("de3258fa43", "cb93667f");
        reportingKeyMap.put("06dc9ef276", "6614241e");
        reportingKeyMap.put("0770b6793f", "ab13526f");
        reportingKeyMap.put("f32a4ae2ba", "4a718658");
        reportingKeyMap.put("ce26806d50", "1a42bcc2");
    }

    public DCPPaypalReporting(final Configuration config) {
        this.host = config.getString("paypal.host");
        this.advertiserId = config.getString("paypal.advertiserId");
    }

    @Override
    public ReportResponse fetchRows(final ReportTime startTime, final String key, final ReportTime endTime)
            throws Exception {
        ReportResponse reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);
        LOG.debug("inside fetch rows of paypal");
        try {
            this.startDate = startTime.getStringDate("");
            LOG.debug("start date inside paypal is {}", this.startDate);
            endDate = endTime == null ? getEndDate("") : startDate;
            if (ReportTime.compareStringDates(this.endDate, this.startDate) == -1) {
                LOG.debug("date is greater than the current date reporting window for paypal");
                return null;
            }
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
            LOG.info("failed to obtain correct dates for fetching reports {}", exception);
            return null;
        }
        externalSiteId = key;
        while (ReportTime.compareStringDates(startDate, endDate) != 1) {
            String url = getRequestUrl();
            if (StringUtils.isBlank(url)) {
                return null;
            }
            String response = invokeHTTPUrl(url);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new java.io.StringReader(response)));
            doc.getDocumentElement().normalize();
            NodeList reportNodes = doc.getElementsByTagName("PublisherReport");

            Node rootNode = reportNodes.item(0);
            if (rootNode.getNodeType() == Node.ELEMENT_NODE) {
                Element rootElement = (Element) rootNode;

                Element pubID = (Element) rootElement.getElementsByTagName("pubID").item(0);
                if (pubID.getTextContent().equals(externalSiteId)) {
                    NodeList reportRows = rootElement.getElementsByTagName("Summary");
                    for (int ind = 0; ind < reportRows.getLength(); ind++) {
                        Node zoneReportNode = reportRows.item(ind);
                        if (zoneReportNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element reportZoneElement = (Element) zoneReportNode;
                            ReportResponse.ReportRow row = new ReportResponse.ReportRow();
                            if (reportZoneElement.getAttributes() != null) {

                                Element request = (Element) reportZoneElement.getElementsByTagName("Requests").item(0);
                                Element impression = (Element) reportZoneElement.getElementsByTagName("Filled").item(0);
                                Element clicks = (Element) reportZoneElement.getElementsByTagName("Clicked").item(0);
                                Element revenue = (Element) reportZoneElement.getElementsByTagName("Revenue").item(0);
                                if (revenue == null) {
                                    addDefaultRow(row, reportResponse);
                                    continue;
                                }

                                row.request = Long.parseLong(request.getTextContent());
                                row.clicks = Long.parseLong(clicks.getTextContent());
                                row.impressions = Long.parseLong(impression.getTextContent());
                                row.revenue = Double.parseDouble(revenue.getTextContent());
                                ReportTime reportDate = new ReportTime(startDate, 0);
                                row.reportTime = reportDate;
                                row.siteId = externalSiteId;
                                row.slotSize = getReportGranularity();
                            }
                            else {
                                addDefaultRow(row, reportResponse);
                            }
                            reportResponse.addReportRow(row);

                        }
                    }
                }
            }
            ReportTime reportTime = new ReportTime(startDate, 0);
            reportTime = ReportTime.getNextDay(reportTime);
            startDate = reportTime.getStringDate("");
        }
        return reportResponse;
    }

    private void addDefaultRow(final ReportResponse.ReportRow row, final ReportResponse reportResponse) {
        row.request = 0;
        row.clicks = 0;
        row.impressions = 0;
        row.revenue = 0;
        ReportTime reportDate = new ReportTime(startDate, 0);
        row.reportTime = reportDate;
        row.siteId = externalSiteId;
        row.slotSize = getReportGranularity();
        reportResponse.addReportRow(row);
        LOG.debug("parsing data inside paypal {}", row.request);
    }

    @Override
    public String getRequestUrl() {
        String reportingKey = reportingKeyMap.get(externalSiteId);
        if (StringUtils.isNotBlank(reportingKey)) {
            return String.format(host, externalSiteId, reportingKey, startDate, startDate);
        }
        else {
            LOG.info("PayPal: Reporting key not present in map for external site id : {}", externalSiteId);
            return null;
        }
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
        return 5;
    }

    @Override
    public String getName() {
        return "paypal";
    }

    @Override
    public String getAdvertiserId() {
        return (advertiserId);
    }

    public String getEndDate(final String seperator) {
        try {
            LOG.debug("calculating end date for paypal");
            ReportTime reportTime = ReportTime.getUTCTime();
            reportTime = ReportTime.getPreviousDay(reportTime);
            reportTime = ReportTime.getPreviousDay(reportTime);
            if (reportTime.getHour() < ReportReconcilerWindow()) {
                reportTime = ReportTime.getPreviousDay(reportTime);
            }
            return (reportTime.getStringDate(seperator));
        }
        catch (Exception exception) {
            LOG.info("failed to obtain end date inside paypal {}", exception);
            return "";
        }
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

        BufferedReader res = null;
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
            // conn.setRequestProperty("X-WSSE", getHeader());
            conn.setDoOutput(true);
            InputStream in = conn.getInputStream();

            StringBuffer sBuffer = new StringBuffer();

            res = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String inputLine;
            while ((inputLine = res.readLine()) != null) {
                sBuffer.append(inputLine);
            }
            return sBuffer.toString();

        }
        catch (Exception exception) {
            LOG.info("Error in Httpool invokeHTTPUrl : {}", exception.getMessage());
            throw new ServerException(exception);
        }
        finally {
            if (res != null) {
                try {
                    res.close();
                }
                catch (IOException exception) {
                    LOG.info("Error in closing BufferedReader {}", exception);
                    throw new ServerException(exception);
                }
            }
        }

    }
}
