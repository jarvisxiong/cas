package com.inmobi.adserve.channels.adnetworks.ifc;

import org.apache.commons.configuration.Configuration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.BaseReportingImpl;
import com.inmobi.adserve.channels.api.ReportResponse;
import com.inmobi.adserve.channels.api.ReportTime;


/**
 * 
 * @author Sandeep.Barange
 * 
 */
public class IFCReporting extends BaseReportingImpl {
    private static final Logger  LOG            = LoggerFactory.getLogger(IFCReporting.class);

    private static Configuration config;
    private String               startDate      = "";
    private String               endDate        = "";
    private String               baseUrl        = "";

    private String               responseString = "";

    public IFCReporting(final Configuration config) {
        baseUrl = config.getString("ifc.baseUrl");
    }

    // Fetches the report from the TPAN
    @Override
    public ReportResponse fetchRows(final ReportTime startTime, final ReportTime endTime) {
        ReportResponse reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);
        LOG.debug("inside fetch rows of ifc");
        try {
            startDate = startTime.getStringDate("-");
            LOG.debug("start date inside ifc is {}", startDate);
            endDate = endTime == null ? String.valueOf(getEndDate()) : startDate;
            LOG.debug("end date inside ifc is {}", endDate);
            if (ReportTime.compareStringDates(endDate, startDate) == -1) {
                LOG.debug("end date is less than start date plus reporting window for ifc");
                return null;
            }
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
            LOG.info("failed to obtain correct dates for fetching reports {}", exception);
        }
        try {
            String url = getRequestUrl();
            LOG.debug("url inside ifc is {}", url);
            responseString = invokeUrl(url, null);
            LOG.debug("response string inside ifc is {}", responseString);
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_SERVER_ERROR;
            LOG.info("failed to collect response from ifc {}", exception);
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
                    LOG.debug("Error decoded BlindedSite id in IFC Adapter {}",
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
                LOG.debug("parsing data inside IFC {}", row.request);
                reportResponse.addReportRow(row);
            }
            LOG.debug("successfully parsed data inside IFC");
        }
        catch (JSONException e) {
            LOG.debug("Error while parsing report JSON");
        }
        return reportResponse;
    }

    @Override
    public String getAdvertiserId() {
        return (config.getString("ifc.advertiserId"));
    }

    public String getEndDate() throws Exception {
        try {
            LOG.debug("calculating end date for IFC");
            ReportTime reportTime = ReportTime.getUTCTime();
            reportTime = ReportTime.getPreviousDay(reportTime);
            if (reportTime.getHour() <= ReportReconcilerWindow()) {
                reportTime = ReportTime.getPreviousDay(reportTime);
            }
            return (reportTime.getStringDate("/"));
        }
        catch (Exception exception) {
            LOG.info("failed to obtain end date inside IFC {}", exception);
            return "";
        }
    }

    @Override
    public String getName() {
        return "IFC";
    }

    @Override
    public ReportGranularity getReportGranularity() {
        return ReportGranularity.DAY;
    }

    @Override
    public String getRequestUrl() {
        String url = String.format("%s/?startDateTime=%s&endDateTime=%s", baseUrl, startDate, endDate);
        return url;
    }

    @Override
    public double getTimeZone() {
        return 0;
    }

    @Override
    public int ReportReconcilerWindow() {
        return 16;
    }

    public ReportTime getIFCReportTime(final String startTime) {
        try {
            ReportTime reportTime = new ReportTime(startTime, 1);
            return reportTime;
        }
        catch (Exception e) {
            LOG.info("failed to obtain reportTime inside IFC " + e.getMessage());
            return null;
        }
    }
}
