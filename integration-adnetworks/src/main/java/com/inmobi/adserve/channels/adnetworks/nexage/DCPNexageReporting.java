package com.inmobi.adserve.channels.adnetworks.nexage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.inmobi.adserve.channels.api.BaseReportingImpl;
import com.inmobi.adserve.channels.api.ReportResponse;
import com.inmobi.adserve.channels.api.ReportTime;
import com.inmobi.adserve.channels.api.ServerException;


/**
 * @author Deepak
 * 
 */
public class DCPNexageReporting extends BaseReportingImpl {
    private static final Logger LOG = LoggerFactory.getLogger(DCPNexageReporting.class);

    private final Configuration config;
    private final String        companyid;
    private final String        host;
    private final String        accesskey;
    private final String        secretkey;
    private String              startDate;
    private String              endDate;
    private String              externalSiteId;

    public DCPNexageReporting(final Configuration config) {
        this.config = config;
        host = config.getString("nexage.host");
        companyid = config.getString("nexage.companyid");
        accesskey = config.getString("nexage.accesskey");
        secretkey = config.getString("nexage.secretkey");
    }

    @Override
    public int ReportReconcilerWindow() {
        return 20;
    }

    @Override
    public ReportResponse fetchRows(final ReportTime startTime, final String externalSiteId, final ReportTime endTime)
            throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException, ServerException,
            IOException, ParserConfigurationException, SAXException {
        ReportResponse reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);
        this.externalSiteId = externalSiteId;

        LOG.debug("inside fetch rows of nexage");
        try {
            startDate = startTime.getStringDate("-");
            LOG.debug("start date inside nexage is {}", startDate);
            endDate = endTime == null ? getEndDate("-") : startDate;

            if (ReportTime.compareStringDates(endDate, startDate) == -1) {
                LOG.debug("date is greater than the current date reporting window for nexage");
                return null;
            }
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
            LOG.info("failed to obtain correct dates for fetching reports {}", exception);
            return null;
        }

        String reportData = invokeHTTPUrl(getRequestUrl());
        LOG.debug("Response from Nexage is {}", reportData);

        try {
            JSONObject data = new JSONObject(reportData);
            if (!StringUtils.isEmpty(data.getString("detail"))) {
                JSONArray jReportArr = data.getJSONArray("detail");
                for (int i = 0; i < jReportArr.length(); i++) {
                    JSONObject reportRow = jReportArr.getJSONObject(i);
                    LOG.debug("json array length is {}", jReportArr.length());
                    String logDate = reportRow.getString("day").substring(0, 10);
                    if (this.startDate.compareTo(logDate) < 1) {
                        LOG.debug("coming here to get log date");
                        ReportResponse.ReportRow row = new ReportResponse.ReportRow();
                        row.reportTime = new ReportTime(logDate, 0);
                        row.request = Long.parseLong(reportRow.getString("requests"));
                        row.clicks = Long.parseLong(reportRow.getString("clicked"));
                        row.impressions = Long.parseLong(reportRow.getString("delivered"));
                        row.revenue = Double.parseDouble(reportRow.getString("revenue").replaceAll(",", ""));
                        row.siteId = externalSiteId;
                        row.slotSize = this.getReportGranularity();
                        LOG.debug("parsing data inside nexage {}", row.request);
                        reportResponse.addReportRow(row);
                        // gotReport = true;
                    }
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return reportResponse;
    }

    @Override
    public String getAdvertiserId() {
        return (config.getString("nexage.advertiserId"));
    }

    @Override
    public String getName() {
        return "nexage";
    }

    @Override
    public ReportGranularity getReportGranularity() {
        return ReportGranularity.DAY;
    }

    @Override
    public double getTimeZone() {
        return -5; // EST
    }

    public String getEndDate(final String seperator) {
        try {
            LOG.debug("calculating end date for nexage");
            ReportTime reportTime = ReportTime.getUTCTime();
            reportTime = ReportTime.getPreviousDay(reportTime);
            reportTime = ReportTime.getPreviousDay(reportTime);
            if (reportTime.getHour() <= ReportReconcilerWindow()) {
                reportTime = ReportTime.getPreviousDay(reportTime);
            }
            return (reportTime.getStringDate(seperator));
        }
        catch (Exception exception) {
            LOG.info("failed to obtain end date inside nexage {}", exception.getMessage());
            return "";
        }
    }

    @Override
    protected String invokeHTTPUrl(final String urlString) {
        StringBuffer sBuffer = new StringBuffer();
        try {
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(accesskey, secretkey.toCharArray());
                }
            });

            URL url = new URL(urlString);
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection hconn = (HttpURLConnection) url.openConnection();
            hconn.setRequestMethod("GET");

            BufferedReader res = new BufferedReader(new InputStreamReader(hconn.getInputStream(), "UTF-8"));
            String inputLine;
            while ((inputLine = res.readLine()) != null) {
                sBuffer.append(inputLine);
            }
            res.close();

            System.out.println(hconn.getResponseCode());
            System.out.println(hconn.getResponseMessage());
            System.out.println(sBuffer.toString());
        }
        catch (MalformedURLException mue) {
            LOG.info("{}", mue.getMessage());
        }
        catch (IOException ioe) {
            LOG.info("{}", ioe.getMessage());
        }
        return sBuffer.toString();
    }

    @Override
    public String getRequestUrl() {
        return host
                .replace("$companyId", companyid)
                    .replace("$startDate", startDate)
                    .replace("$endDate", endDate)
                    .replace("$site", externalSiteId);
    }
}