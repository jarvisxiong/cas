package com.inmobi.adserve.channels.server.requesthandler;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Marker;
import org.testng.annotations.Test;

import com.google.inject.Provider;
import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;


public class RequestParserTest extends TestCase {
    RequestParser requestParser;

    @Override
    public void setUp() {
        final Configuration mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("debug")).andReturn("debug").anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        replay(mockConfig);
        requestParser = new RequestParser(new Provider<Marker>() {
            @Override
            public Marker get() {
                return null;
            }
        });
    }

    @Test
    public void testParseRequestParameters() throws JSONException {
        final JSONObject jObject =
                new JSONObject(
                        "{\"site-type\":\"PERFORMANCE\""
                                + ",\"handset\":[42279,\"apple_ipod_touch_ver4_3_1_subua\"],\"rqMkAdcount\":\"1\",\"new-category\":[70,42],\"site-floor\":0"
                                + ",\"rqMkAdSlot\":\"9\",\"raw-uid\":{\"O1\":\"8d10846582eef7c6f5873883b09a5a63\",\"u-id-s\":\"O1\",\"IX\":\"4fa7!506c!508902de!iPod3,1!8G4!19800\"}"
                                + ",\"carrier\":[406,94,\"US\",12328,31118],\"site-url\":\"ww.inmobi.com\",\"tid\":\"0e919b0a-73c4-44cb-90ec-2b37b2249219\""
                                + ",\"rqMkSiteid\":\"4028cba631d63df10131e1d3191d00cb\",\"site\":[34093,60],\"w-s-carrier\":\"3.0.0.0\",\"loc-src\":\"wifi\""
                                + ",\"slot-served\":\"9\",\"uparams\":{\"u-appdnm\":\"RichMediaSDK.app\",\"u-appver\":\"1008000\",\"u-postalcode\":\"302015\""
                                + ",\"u-key-ver\":\"1\",\"u-areacode\":\"bangalore\",\"u-appbid\":\"com.inmobi.profile1\"},\"r-format\":\"xhtml\",\"site-allowBanner\":true"
                                + ",\"category\":[13,8,19,4,17,16,14,3,11,29,23],\"source\":\"APP\",\"rich-media\":false,\"adcode\":\"NON-JS\",\"sdk-version\":\"i357\""
                                + ",\"pub-id\":\"4028cb9731d7d0ad0131e1d1996101ef\",\"os-id\":6}");
        final SASRequestParameters sasRequestParameters = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        requestParser.parseRequestParameters(jObject, sasRequestParameters, casInternalRequestParameters);
        assertNotNull(sasRequestParameters);
        assertEquals(ContentType.PERFORMANCE, sasRequestParameters.getSiteContentType());
        assertEquals(sasRequestParameters.getHandsetInternalId(), 42279);
        assertEquals(sasRequestParameters.getRqMkAdcount(), new Short("1"));
        assertEquals(sasRequestParameters.getSiteFloor(), 0.0);
        assertEquals(sasRequestParameters.getOsId(), 6);
        assertEquals(sasRequestParameters.getRqMkSlot().get(0), new Short("9"));
        assertEquals(sasRequestParameters.getUidParams(),
                "{\"O1\":\"8d10846582eef7c6f5873883b09a5a63\",\"u-id-s\":\"O1\",\"IX\":\"4fa7!506c!508902de!iPod3,1!8G4!19800\"}");
        assertEquals(sasRequestParameters.getCarrierId(), 406);
        assertEquals(sasRequestParameters.getCountryId(), new Long(94));
        assertEquals(sasRequestParameters.getCountryCode(), "US");
        assertEquals(sasRequestParameters.getState(), new Integer(31118));
        assertEquals(sasRequestParameters.getCity(), new Integer(12328));
        assertEquals(sasRequestParameters.getTid(), "0e919b0a-73c4-44cb-90ec-2b37b2249219");
        assertEquals(sasRequestParameters.getSiteId(), "4028cba631d63df10131e1d3191d00cb");
        assertEquals(sasRequestParameters.getSiteIncId(), 34093);
        assertEquals(sasRequestParameters.getRemoteHostIp(), "3.0.0.0");
        assertEquals(sasRequestParameters.getLocSrc(), "wifi");
        assertEquals(sasRequestParameters.getSlot(), new Short("9"));
        assertEquals(sasRequestParameters.getRFormat(), "xhtml");
        assertEquals(sasRequestParameters.getAllowBannerAds(), Boolean.TRUE);
        assertEquals(sasRequestParameters.getCategories().toString(), "[70, 42]");
        assertEquals(sasRequestParameters.getSource(), "APP");
        assertEquals(sasRequestParameters.getAdcode(), "NON-JS");
        assertEquals(sasRequestParameters.getSdkVersion(), "i357");
        assertEquals(sasRequestParameters.getPostalCode(), Integer.valueOf(302015));
        assertEquals(casInternalRequestParameters.getUidO1(), "8d10846582eef7c6f5873883b09a5a63");
        assertNull(casInternalRequestParameters.getUidSO1());
        assertNull(casInternalRequestParameters.getUidMd5());
        assertNull(casInternalRequestParameters.getUidIFA());
        assertNull(casInternalRequestParameters.getUidIFV());
        assertNull(casInternalRequestParameters.getUidIDUS1());
        assertNull(casInternalRequestParameters.getUidADT());
    }
}
