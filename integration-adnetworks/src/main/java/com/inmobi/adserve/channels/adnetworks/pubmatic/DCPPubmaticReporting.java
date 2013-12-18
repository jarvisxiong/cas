package com.inmobi.adserve.channels.adnetworks.pubmatic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.Configuration;
import org.apache.cxf.frontend.ClientProxy;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.BaseReportingImpl;
import com.inmobi.adserve.channels.api.ReportResponse;
import com.inmobi.adserve.channels.api.ReportTime;
import com.inmobi.adserve.channels.api.ServerException;
import com.pubmatic.core.reporting.webservices.Currency;
import com.pubmatic.core.reporting.webservices.PublisherDemandReportingWebService;
import com.pubmatic.core.reporting.webservices.PublisherDemandReportingWebServiceImplService;
import com.pubmatic.core.reporting.webservices.PublisherDemandStatistics;
import com.pubmatic.core.reporting.webservices.ReportingException_Exception;
import com.pubmatic.core.reporting.webservices.ReportingOptionalParams;

public class DCPPubmaticReporting extends BaseReportingImpl {
    private static final Logger                    LOG             = LoggerFactory
                                                                           .getLogger(DCPPubmaticReporting.class);

    private final Configuration                    config;
    private String                                 startDate       = "";
    private String                                 endDate         = "";
    private ReportResponse                         reportResponse;
    private final String                           accessToken;
    private final long                             publisherId;
    private static List<PublisherDemandStatistics> result          = null;
    private static String                          resultStartDate = null;
    private final String                           userName;
    private final String                           password;
    private final String                           tokenApiHost;
    private final String                           tokenApiUrlParams;

    public DCPPubmaticReporting(final Configuration config) {
        this.config = config;
        accessToken = config.getString("pubmatic.accessToken");
        userName = config.getString("pubmatic.userName");
        password = config.getString("pubmatic.password");
        publisherId = Long.parseLong(config.getString("pubmatic.publisherId"));
        tokenApiHost = config.getString("pubmatic.tokenApiHost");
        tokenApiUrlParams = config.getString("pubmatic.tokenUrlParam");
    }

    // Fetches the report from the TPAN
    @Override
    public ReportResponse fetchRows(final ReportTime startTime, final String key, final ReportTime endTime)
            throws ServerException, IOException, JSONException {
        reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);
        LOG.debug("inside fetch rows of draw bridge");
        try {
            startDate = startTime.getStringDate("-");
            LOG.debug("start date inside pubmatic is {}", startDate);
            endDate = endTime == null ? getEndDate() : startDate;
            LOG.debug("end date inside pubmatic is {}", endDate);
            if (ReportTime.compareStringDates(endDate, startDate) == -1) {
                LOG.debug("end date is less than start date plus reporting window for mobile commerce");
                return null;
            }
        }
        catch (Exception exception) {
            reportResponse.status = ReportResponse.ResponseStatus.FAIL_INVALID_DATE_ERROR;
            LOG.info("failed to obtain correct dates for fetching reports {}", exception);
            return null;
        }

        if (!startDate.equals(resultStartDate) || result == null) {

            String token = getToken();
            PublisherDemandReportingWebService service = new PublisherDemandReportingWebServiceImplService(
                    PublisherDemandReportingWebServiceImplService.WSDL_LOCATION,
                    PublisherDemandReportingWebServiceImplService.SERVICE)
                    .getPublisherDemandReportingWebServiceImplPort();
            setRequestHeader(token, service);
            ReportingOptionalParams params = new ReportingOptionalParams();
            try {
                params.setCurrency(Currency.USD);
                result = service.getPublisherDailySiteReport(token, publisherId, startDate, endDate, params);
                if (result != null) {
                    resultStartDate = startDate;
                }
                else {
                    return null;
                }

            }
            catch (ReportingException_Exception e) {
                reportResponse.status = ReportResponse.ResponseStatus.FAIL_SERVER_ERROR;
                LOG.info("{}", e);
                return null;
            }
        }

        for (PublisherDemandStatistics rrow : result) {
            if (key.equals(String.valueOf(rrow.getSiteId()))) {
                ReportResponse.ReportRow row = new ReportResponse.ReportRow();
                row.request = 0;
                row.impressions = 0;
                row.clicks = 0;
                row.revenue = 0.0;
                row.ecpm = 0.0;
                row.siteId = key;
                row.slotSize = getReportGranularity();
                row.reportTime = new ReportTime(rrow.getDate(), 0);
                row.impressions = (long) rrow.getPmStats().getTotalImpr();
                row.clicks = rrow.getPmStats().getClicks();
                row.revenue = rrow.getPmStats().getRevenue();
                row.ecpm = rrow.getPmStats().getEcpm();
                reportResponse.addReportRow(row);
            }
        }
        reportResponse.status = ReportResponse.ResponseStatus.SUCCESS;
        LOG.debug("successfuly got response inside pubmatic. Number of lines of response is ",
            reportResponse.rows.size());
        LOG.debug("successfully parsed data inside pubmatic");
        return reportResponse;
    }

    // obtain end date for the date range in which data is to be fetched
    public String getEndDate() throws Exception {
        try {
            LOG.debug("calculating end date for pubmatic");
            ReportTime reportTime = ReportTime.getUTCTime();
            reportTime = ReportTime.getPreviousDay(reportTime);
            reportTime = ReportTime.getPreviousDay(reportTime);
            if (reportTime.getHour() <= ReportReconcilerWindow()) {
                reportTime = ReportTime.getPreviousDay(reportTime);
            }
            return (reportTime.getStringDate("-"));
        }
        catch (Exception exception) {
            LOG.info("failed to obtain end date inside mobile commerce {}", exception.getMessage());
            return "";
        }
    }

    // Returns the time zone(x) GMT+x. For GMT it returns 0. IST it returns 5.5
    @Override
    public double getTimeZone() {
        return 0.0;
    }

    @Override
    public String getAdvertiserId() {
        return config.getString("pubmatic.advertiserId");
    }

    // Returns the report format wrt granularity.
    @Override
    public ReportGranularity getReportGranularity() {
        return ReportGranularity.DAY;
    }

    @Override
    public String getName() {
        return "pubmatic";
    }

    @Override
    public int ReportReconcilerWindow() {
        return 19;
    }

    @Override
    public String getRequestUrl() {
        return null;
    }

    private String getToken() throws MalformedURLException, IOException, JSONException {
        String urlParams = String.format(tokenApiUrlParams, accessToken, accessToken);
        String tokenResponse = invokeHTTPUrl(tokenApiHost, urlParams);
        String token = new JSONObject(tokenResponse).getString("access_token");
        return token;
    }

    private String invokeHTTPUrl(final String url, final String urlParameters) throws MalformedURLException,
            IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        String authStr = userName + ":" + password;
        String authEncoded = new String(Base64.encodeBase64(authStr.getBytes()));
        conn.setRequestProperty("Authorization", "Basic " + authEncoded);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

        writer.write(urlParameters);
        writer.flush();

        String line;
        StringBuilder sBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        while ((line = reader.readLine()) != null) {
            sBuilder.append(line);
        }
        writer.close();
        reader.close();
        return sBuilder.toString();

    }

    private void setRequestHeader(final String oauthToken, final Object port) {

        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        List<String> headList = new ArrayList<String>();
        headList.add("Bearer " + oauthToken);
        headers.put("Authorization", headList);
        headList = new ArrayList<String>();
        headList.add("text/xml");
        headers.put("Content-Type", headList);
        ClientProxy.getClient(port).getRequestContext().put(org.apache.cxf.message.Message.PROTOCOL_HEADERS, headers);
    }
}
