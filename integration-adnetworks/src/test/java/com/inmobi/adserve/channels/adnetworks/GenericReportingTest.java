package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.adnetworks.generic.GenericReporting;
import com.inmobi.adserve.channels.api.ReportResponse;
import com.inmobi.adserve.channels.util.DebugLogger;


public class GenericReportingTest extends TestCase {
    private Configuration    mockConfig     = null;
    private String           advertiserName = "drawbridge";
    private GenericReporting genericReporting;

    public void prepareMockConfig() {

        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString(advertiserName + ".requestUrl")).andReturn(
            "https://www.xyz.com/a?sd=$startDate&ed=$endDate&zoneid=$key").anyTimes();
        expect(mockConfig.getString("debug")).andReturn("debug").anyTimes();
        expect(mockConfig.getString("loggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-reporting.properties");
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        expect(mockConfig.getString(advertiserName + ".responseFormat")).andReturn("csv").anyTimes();
        expect(mockConfig.getString(advertiserName + ".timeSeparator")).andReturn("-").anyTimes();
        expect(mockConfig.getInt(advertiserName + ".requestIndex")).andReturn(1).anyTimes();
        expect(mockConfig.getInt(advertiserName + ".clicksIndex")).andReturn(2).anyTimes();
        expect(mockConfig.getInt(advertiserName + ".impressionsIndex")).andReturn(3).anyTimes();
        expect(mockConfig.getInt(advertiserName + ".revenueIndex")).andReturn(4).anyTimes();
        expect(mockConfig.getInt(advertiserName + ".ecpmIndex")).andReturn(5).anyTimes();
        expect(mockConfig.getInt(advertiserName + ".reportTimeIndex")).andReturn(6).anyTimes();
        expect(mockConfig.getInt(advertiserName + ".externalSiteKeyField")).andReturn(0).anyTimes();
        expect(mockConfig.getString(advertiserName + ".externalSiteKeyField")).andReturn("null").anyTimes();
        expect(mockConfig.getString(advertiserName + ".reportRowPath")).andReturn("adResponse/reportRow").anyTimes();
        expect(mockConfig.getString(advertiserName + ".jsonFormat")).andReturn("array").anyTimes();
        expect(mockConfig.getString(advertiserName + ".requestIndex")).andReturn("request").anyTimes();
        expect(mockConfig.getString(advertiserName + ".clicksIndex")).andReturn("clicks").anyTimes();
        expect(mockConfig.getString(advertiserName + ".impressionsIndex")).andReturn("impressions").anyTimes();
        expect(mockConfig.getString(advertiserName + ".revenueIndex")).andReturn("revenue").anyTimes();
        expect(mockConfig.getString(advertiserName + ".ecpmIndex")).andReturn("ecpm").anyTimes();
        expect(mockConfig.getString(advertiserName + ".reportTimeIndex")).andReturn("date").anyTimes();
        expect(mockConfig.getString(advertiserName + ".status")).andReturn("status").anyTimes();
        replay(mockConfig);
    }

    public void setUp() throws Exception {
        prepareMockConfig();
        DebugLogger.init(mockConfig);
        genericReporting = new GenericReporting(mockConfig, advertiserName);
        genericReporting.logger = new DebugLogger();
    }

    @Test
    public void testgetRequestUrl() {
        genericReporting.startDate = "10/11/2012";
        genericReporting.endDate = "14/01/2013";
        genericReporting.key = "111";
        String expectedUrl = "https://www.xyz.com/a?sd=10/11/2012&ed=14/01/2013&zoneid=111";
        String actualUrl = genericReporting.getRequestUrl();
        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    public void testparseResonseInCsv() {
        genericReporting.responseString = "siteid,requests,clicks,impressions,revenue,ecpm,date\n111,1,1,1,1.0,0.1,2012-11-10";
        genericReporting.key = "111";
        genericReporting.reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);
        genericReporting.parseResponseInCsv();
        assertEquals(genericReporting.reportResponse.rows.get(0).request, 1);
    }

    /*
     * @Test public void testparseResponseInJson() { //String responseInJson =
     * "{\"reportoutput\": {\"reportbody\": {\"reportdata\": [{\"request\": \"1\",\"clicks\": \"1\",\"impressions\": \"1\",\"revenue\": \"1.0\",\"ecpm\": \"0.1\",\"site\": \"111\",\"date\": \"2012-11-10\"}]}},\"info\":\"xyz\"}"
     * ; String responseInJson =
     * "{\"reportoutput\": {\"reportbody\": {\"reportdata\": [[\"111\", \"1\", \"1\", \"1\", \"1.0\", \"0.1\", \"2012-11-10\"]]}},\"info\":\"xyz\"}"
     * ; genericReporting.key = "111"; genericReporting.reportResponse = new
     * ReportResponse(ReportResponse.ResponseStatus.SUCCESS); genericReporting.parseResponseInJson(responseInJson, "0");
     * assertEquals(genericReporting.reportResponse.rows.get(0).request, 1); }
     */

    @Test
    public void testparseResponseInXml() {
        genericReporting.responseString = "<?xml version=\"1.0\"?><adResponse> <status>0</status> <count>2</count> <reportRow>  <date>08/04/2010</date>  <request>38</request>  <impressions>31</impressions>  <clicks>3</clicks>  <revenue>0.08</revenue> </reportRow> <reportRow>  <date>08/05/2010</date>  <request>0</request>  <impression>0</impression>  <clicks>10</clicks>  <revenue>0.00</revenue> </reportRow></adResponse>";
        genericReporting.reportResponse = new ReportResponse(ReportResponse.ResponseStatus.SUCCESS);
        genericReporting.key = "111";
        genericReporting.parseResponseInXml();
        assertEquals(genericReporting.reportResponse.rows.get(0).request, 38);
        assertEquals(genericReporting.reportResponse.rows.get(1).clicks, 10);
    }

    public void debug(Object... os) {
        System.out.println(Arrays.deepToString(os));
    }

}
