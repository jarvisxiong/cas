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
import org.xml.sax.SAXException;

import com.inmobi.adserve.channels.api.BaseReportingImpl;
import com.inmobi.adserve.channels.api.ReportResponse;
import com.inmobi.adserve.channels.api.ReportTime;
import com.inmobi.adserve.channels.api.ServerException;
import com.inmobi.adserve.channels.util.DebugLogger;


/**
 * @author Deepak
 * 
 */
public class DCPNexageReporting extends BaseReportingImpl
{
    private final Configuration config;
    private final String        companyid;
    private final String        host;
    private final String        accesskey;
    private final String        secretkey;
    private DebugLogger         logger;
    private String              startDate;
    private String              endDate;
    private String              externalSiteId;

    public DCPNexageReporting(final Configuration config)
    {
        this.config = config;
        host = config.getString("nexage.host");
        companyid = config.getString("nexage.companyid");
        accesskey = config.getString("nexage.accesskey");
        secretkey = config.getString("nexage.secretkey");
    }

    @Override
    public int ReportReconcilerWindow()
    {
        return 20;
    }

    @Override
    public ReportResponse fetchRows(final DebugLogger logger, final ReportTime startTime, final String externalSiteId,
            final ReportTime endTime) throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException,
            ServerException, IOException, ParserConfigurationException, SAXException
    {
        this.logger = logger;
        ReportResponse reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);
        this.externalSiteId = externalSiteId;

        logger.debug("inside fetch rows of nexage");
        try {
            startDate = startTime.getStringDate("-");
            logger.debug("start date inside nexage is " + startDate);
            endDate = endTime == null ? getEndDate("-") : startDate;

            if (ReportTime.compareStringDates(endDate, startDate) == -1) {
                logger.debug("date is greater than the current date reporting window for nexage");
                return null;
            }
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
            logger.info("failed to obtain correct dates for fetching reports " + exception.getMessage());
            return null;
        }

        String reportData = invokeHTTPUrl(getRequestUrl());
        logger.debug("Response from Nexage is " + reportData);

        try {
            JSONObject data = new JSONObject(reportData);
            if (!StringUtils.isEmpty(data.getString("detail"))) {
                JSONArray jReportArr = data.getJSONArray("detail");
                for (int i = 0; i < jReportArr.length(); i++) {
                    JSONObject reportRow = jReportArr.getJSONObject(i);
                    logger.debug("json array length is " + jReportArr.length());
                    String logDate = reportRow.getString("day").substring(0, 10);
                    if (this.startDate.compareTo(logDate) < 1) {
                        logger.debug("coming here to get log date");
                        ReportResponse.ReportRow row = new ReportResponse.ReportRow();
                        row.reportTime = new ReportTime(logDate, 0);
                        row.request = Long.parseLong(reportRow.getString("requests"));
                        row.clicks = Long.parseLong(reportRow.getString("clicked"));
                        row.impressions = Long.parseLong(reportRow.getString("delivered"));
                        row.revenue = Double.parseDouble(reportRow.getString("revenue").replaceAll(",", ""));
                        row.siteId = externalSiteId;
                        row.slotSize = this.getReportGranularity();
                        logger.debug("parsing data inside nexage " + row.request);
                        reportResponse.addReportRow(row);
                        // gotReport = true;
                    }
                }
            }
        }
        catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return reportResponse;
    }

    @Override
    public String getAdvertiserId()
    {
        return (config.getString("nexage.advertiserId"));
    }

    @Override
    public String getName()
    {
        return "nexage";
    }

    @Override
    public ReportGranularity getReportGranularity()
    {
        return ReportGranularity.DAY;
    }

    @Override
    public double getTimeZone()
    {
        return -5; // EST
    }

    public String getEndDate(final String seperator)
    {
        try {
            logger.debug("calculating end date for nexage");
            ReportTime reportTime = ReportTime.getUTCTime();
            reportTime = ReportTime.getPreviousDay(reportTime);
            reportTime = ReportTime.getPreviousDay(reportTime);
            if (reportTime.getHour() <= ReportReconcilerWindow()) {
                reportTime = ReportTime.getPreviousDay(reportTime);
            }
            return (reportTime.getStringDate(seperator));
        }
        catch (Exception exception) {
            logger.info("failed to obtain end date inside nexage " + exception.getMessage());
            return "";
        }
    }

    private String invokeHTTPUrl(String urlString)
    {
        StringBuffer sBuffer = new StringBuffer();
        try {
            Authenticator.setDefault(new Authenticator()
            {
                protected PasswordAuthentication getPasswordAuthentication()
                {
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
            logger.info(mue.getMessage());
        }
        catch (IOException ioe) {
            logger.info(ioe.getMessage());
        }
        return sBuffer.toString();
    }

    @Override
    public String getRequestUrl()
    {
        return host
                .replace("$companyId", companyid)
                    .replace("$startDate", startDate)
                    .replace("$endDate", endDate)
                    .replace("$site", externalSiteId);
    }
}