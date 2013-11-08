package com.inmobi.adserve.channels.adnetworks.ifc;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.configuration.Configuration;
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
 * 
 * @author Sandeep.Barange
 * 
 */
public class IFCReporting extends BaseReportingImpl
{

    private static Configuration config;
    private String               startDate      = "";
    private String               endDate        = "";
    private String               baseUrl        = "";

    private String               responseString = "";
    private DebugLogger          logger;

    public IFCReporting(final Configuration config)
    {
        baseUrl = config.getString("ifc.baseUrl");
    }

    @Override
    public ReportResponse fetchRows(final DebugLogger logger, final ReportTime startTime, final String key,
            final ReportTime endTime) throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException,
            ServerException, IOException, ParserConfigurationException, SAXException, JSONException
    {
        // NO Op
        return null;
    }

    // Fetches the report from the TPAN
    @Override
    public ReportResponse fetchRows(final DebugLogger logger, final ReportTime startTime, final ReportTime endTime)
    {
        ReportResponse reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);
        this.logger = logger;
        logger.debug("inside fetch rows of ifc");
        try {
            startDate = startTime.getStringDate("-");
            logger.debug("start date inside ifc is " + startDate);
            endDate = endTime == null ? String.valueOf(getEndDate()) : startDate;
            logger.debug("end date inside ifc is " + endDate);
            if (ReportTime.compareStringDates(endDate, startDate) == -1) {
                logger.debug("end date is less than start date plus reporting window for ifc");
                return null;
            }
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
            logger.info("failed to obtain correct dates for fetching reports " + exception.getMessage());
        }
        try {
            String url = getRequestUrl();
            logger.debug("url inside ifc is " + url);
            responseString = invokeUrl(url, null, logger);
            logger.debug("response string inside ifc is " + responseString);
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_SERVER_ERROR;
            logger.info("failed to collect response from ifc " + exception.getMessage());
            return reportResponse;
        }

        try {
            reportResponse.status = ReportResponse.ResponseStatus.SUCCESS;
            JSONArray jsonArray = new JSONArray();
            responseString = responseString.trim();
            if ((responseString.charAt(0)) == '[') {
                jsonArray = new JSONArray(responseString);
            }
            else {
                jsonArray.put(new JSONObject(responseString));
            }
            for (int i = 0; i < jsonArray.length(); i++) {
                ReportResponse.ReportRow row = new ReportResponse.ReportRow();
                if (!decodeBlindedSiteId(jsonArray.getJSONObject(i).getString("blindedSiteId"), row)) {
                    logger.debug("Error decoded BlindedSite id in IFC Adapter",
                        jsonArray.getJSONObject(i).getString("blindedSiteId"));
                    continue;
                }
                row.isSiteData = true;
                row.request = jsonArray.getJSONObject(i).getInt("request");
                row.impressions = jsonArray.getJSONObject(i).getInt("impressions");
                row.clicks = jsonArray.getJSONObject(i).getInt("clicks");
                row.revenue = jsonArray.getJSONObject(i).getDouble("revenue");
                row.ecpm = jsonArray.getJSONObject(i).getDouble("ecpm");
                row.reportTime = getIFCReportTime(jsonArray.getJSONObject(i).getString("startTime"));
                row.slotSize = this.getReportGranularity();
                logger.debug("parsing data inside IFC " + row.request);
                reportResponse.addReportRow(row);
            }
            logger.debug("successfully parsed data inside IFC");
        }
        catch (JSONException e) {
            logger.debug("Error while parsing report JSON");
        }
        return reportResponse;
    }

    @Override
    public String getAdvertiserId()
    {
        return (config.getString("ifc.advertiserId"));
    }

    public String getEndDate() throws Exception
    {
        try {
            logger.debug("calculating end date for IFC");
            ReportTime reportTime = ReportTime.getUTCTime();
            reportTime = ReportTime.getPreviousDay(reportTime);
            if (reportTime.getHour() <= ReportReconcilerWindow()) {
                reportTime = ReportTime.getPreviousDay(reportTime);
            }
            return (reportTime.getStringDate("/"));
        }
        catch (Exception exception) {
            logger.info("failed to obtain end date inside IFC " + exception.getMessage());
            return "";
        }
    }

    @Override
    public String getName()
    {
        return "IFC";
    }

    @Override
    public ReportGranularity getReportGranularity()
    {
        return ReportGranularity.DAY;
    }

    @Override
    public String getRequestUrl()
    {
        String url = String.format("%s/?startDateTime=%s&endDateTime=%s", baseUrl, startDate, endDate);
        return url;
    }

    @Override
    public double getTimeZone()
    {
        return 0;
    }

    @Override
    public int ReportReconcilerWindow()
    {
        return 16;
    }

    public ReportTime getIFCReportTime(String startTime)
    {
        try {
            ReportTime reportTime = new ReportTime(startTime, 1);
            return reportTime;
        }
        catch (Exception e) {
            logger.info("failed to obtain reportTime inside IFC " + e.getMessage());
            return null;
        }
    }
}

/*
 * package com.inmobi.adserve.channels.adnetworks.ifc;
 * 
 * import org.apache.commons.configuration.Configuration; import org.json.JSONArray; import org.json.JSONException;
 * import org.json.JSONObject;
 * 
 * import com.inmobi.adserve.channels.api.BaseReportingImpl; import com.inmobi.adserve.channels.api.ReportResponse;
 * import com.inmobi.adserve.channels.api.ReportTime; import com.inmobi.adserve.channels.util.DebugLogger;
 *//**
 * 
 * @author Sandeep.Barange
 * 
 */
/*
 * public class IFCReporting extends BaseReportingImpl {
 * 
 * private static Configuration config; private String adGreoupId = ""; private String startDate = ""; private String
 * endDate = ""; private String baseUrl = "";
 * 
 * private String responseString = ""; private DebugLogger logger;
 * 
 * public IFCReporting(final Configuration config) { baseUrl = config.getString("ifc.baseUrl"); }
 * 
 * // Fetches the report from the TPAN
 * 
 * @Override public ReportResponse fetchRows(final DebugLogger logger, final ReportTime startTime, final String key,
 * final ReportTime endTime) { ReportResponse reportResponse = new ReportResponse(
 * ReportResponse.ResponseStatus.SUCCESS); this.logger = logger; logger.debug("inside fetch rows of ifc"); try {
 * adGreoupId = key; startDate = startTime.getStringDate("-"); logger.debug("start date inside ifc is " + startDate);
 * endDate = endTime == null ? String.valueOf(getEndDate()) : startDate; logger.debug("end date inside ifc is " +
 * endDate); if (ReportTime.compareStringDates(endDate, startDate) == -1) {
 * logger.debug("end date is less than start date plus reporting window for ifc"); return null; } } catch (Exception
 * exception) { reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
 * logger.info("failed to obtain correct dates for fetching reports " + exception.getMessage()); } try { String url =
 * getRequestUrl(); logger.debug("url inside ifc is " + url); responseString = invokeUrl(url, null, logger);
 * logger.debug("response string inside ifc is " + responseString); } catch (Exception exception) {
 * reportResponse.status = ReportResponse.ResponseStatus.FAIL_SERVER_ERROR;
 * logger.info("failed to collect response from ifc " + exception.getMessage()); return reportResponse; }
 * 
 * try { reportResponse.status = ReportResponse.ResponseStatus.SUCCESS; JSONArray jsonArray = new JSONArray();
 * responseString = responseString.trim(); if ((responseString.charAt(0)) == '[') { jsonArray = new
 * JSONArray(responseString); } else { jsonArray.put(new JSONObject(responseString)); } for (int i = 0; i <
 * jsonArray.length(); i++) { ReportResponse.ReportRow row = new ReportResponse.ReportRow(); row.request =
 * jsonArray.getJSONObject(i).getInt("request"); row.impressions = jsonArray.getJSONObject(i).getInt( "impressions");
 * row.clicks = jsonArray.getJSONObject(i).getInt("clicks"); row.revenue =
 * jsonArray.getJSONObject(i).getDouble("revenue"); row.ecpm = jsonArray.getJSONObject(i).getDouble("ecpm"); //
 * row.adGroupId = jsonArray.getJSONObject(i).getString( // "adGroupId"); row.siteId = key; row.reportTime =
 * getIFCReportTime(jsonArray.getJSONObject(i) .getString("startTime")); row.slotSize = this.getReportGranularity();
 * logger.debug("parsing data inside IFC " + row.request); reportResponse.addReportRow(row); }
 * logger.debug("successfully parsed data inside IFC"); } catch (JSONException e) {
 * logger.debug("Error while parsing report JSON"); } return reportResponse; }
 * 
 * @Override public String getAdvertiserId() { return (config.getString("ifc.advertiserId")); }
 * 
 * public String getEndDate() throws Exception { try { logger.debug("calculating end date for IFC"); ReportTime
 * reportTime = ReportTime.getUTCTime(); reportTime = ReportTime.getPreviousDay(reportTime); if (reportTime.getHour() <=
 * ReportReconcilerWindow()) { reportTime = ReportTime.getPreviousDay(reportTime); } return
 * (reportTime.getStringDate("/")); } catch (Exception exception) { logger.info("failed to obtain end date inside IFC "
 * + exception.getMessage()); return ""; } }
 * 
 * @Override public String getName() { return "IFC"; }
 * 
 * @Override public ReportGranularity getReportGranularity() { return ReportGranularity.DAY; }
 * 
 * @Override public String getRequestUrl() { String url = String.format(
 * "%s/?adgroupid=%s&startDateTime=%s&endDateTime=%s", baseUrl, adGreoupId, startDate, endDate); return url; }
 * 
 * @Override public double getTimeZone() { return 0; }
 * 
 * @Override public int ReportReconcilerWindow() { return 16; }
 * 
 * public ReportTime getIFCReportTime(String startTime) { try { ReportTime reportTime = new ReportTime(startTime, 1);
 * return reportTime; } catch (Exception e) { logger.info("failed to obtain reportTime inside IFC " + e.getMessage());
 * return null; } } }
 */