package com.inmobi.adserve.channels.server;

import java.util.Arrays;

import net.sf.ehcache.config.Configuration;

import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.util.ConfigurationLoader;
import com.inmobi.adserve.channels.util.DebugLogger;

import junit.framework.TestCase;

public class RequestParserTest extends TestCase {

  
  /*  public static void main(String[] args) throws JSONException{ 
      int a[] = {1,2,3};
      System.out.println(Arrays.toString(a));
    }
   */
  public void setUp() {
    ConfigurationLoader config = ConfigurationLoader.getInstance("/opt/mkhoj/conf/cas/channel-server.properties");
    DebugLogger.init(config.loggerConfiguration());
  }

  @Test
  public void testParseRequestParameters() throws JSONException {
    JSONObject jObject = new JSONObject(
        "{\"site-type\":\"PE (iPod; U; CPU iPhone OS 4_3_1 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Mobile/8G4\""
            + ",\"handset\":[42279,\"apple_ipod_touch_ver4_3_1_subua\"],\"rq-mk-adcount\":\"1\",\"new-category\":[70,42],\"site-floor\":0"
            + ",\"rq-mk-ad-slot\":\"9\",\"u-id-params\":{\"O1\":\"8d10846582eef7c6f5873883b09a5a63\",\"u-id-s\":\"O1\",\"IX\":\"4fa7!506c!508902de!iPod3,1!8G4!19800\"}"
            + ",\"carrier\":[406,94,\"US\",12328,31118],\"site-url\":\"ww.inmobi.com\",\"tid\":\"0e919b0a-73c4-44cb-90ec-2b37b2249219\""
            + ",\"rq-mk-siteid\":\"4028cba631d63df10131e1d3191d00cb\",\"site\":[34093,60],\"w-s-carrier\":\"3.0.0.0\",\"loc-src\":\"wifi\""
            + ",\"slot-served\":\"9\",\"uparams\":{\"u-appdnm\":\"RichMediaSDK.app\",\"u-appver\":\"1008000\",\"u-postalcode\":\"302015\""
            + ",\"u-key-ver\":\"1\",\"u-areacode\":\"bangalore\",\"u-appbid\":\"com.inmobi.profile1\"},\"r-format\":\"xhtml\",\"site-allowBanner\":true"
            + ",\"category\":[13,8,19,4,17,16,14,3,11,29,23],\"source\":\"APP\",\"rich-media\":false,\"adcode\":\"NON-JS\",\"sdk-version\":\"i357\""
            + ",\"pub-id\":\"4028cb9731d7d0ad0131e1d1996101ef\",\"os-id\":6}");
    SASRequestParameters sasRequestParameters = RequestParser.parseRequestParameters(jObject, new DebugLogger());
    assertNotNull(sasRequestParameters);
    assertEquals(
        "PE (iPod; U; CPU iPhone OS 4_3_1 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Mobile/8G4".toUpperCase(),
        sasRequestParameters.getSiteType());
    assertEquals(sasRequestParameters.getHandset().toString(), "[42279,\"apple_ipod_touch_ver4_3_1_subua\"]");
    assertEquals(sasRequestParameters.getRqMkAdcount(), "1");
    assertEquals(sasRequestParameters.getSiteFloor(), 0.0);
    assertEquals(sasRequestParameters.getOsId(), 6);
    assertEquals(sasRequestParameters.getRqMkSlot(), "9");
    assertEquals(sasRequestParameters.getUidParams(),
        "{\"O1\":\"8d10846582eef7c6f5873883b09a5a63\",\"u-id-s\":\"O1\",\"IX\":\"4fa7!506c!508902de!iPod3,1!8G4!19800\"}");
    assertEquals(sasRequestParameters.getCarrier().toString(), "[406,94,\"US\",12328,31118]");
    assertEquals(sasRequestParameters.getTid(), "0e919b0a-73c4-44cb-90ec-2b37b2249219");
    assertEquals(sasRequestParameters.getSiteId(), "4028cba631d63df10131e1d3191d00cb");
    assertEquals(sasRequestParameters.getSiteIncId(), 34093);
    assertEquals(sasRequestParameters.getRemoteHostIp(), "3.0.0.0");
    assertEquals(sasRequestParameters.getLocSrc(), "wifi");
    assertEquals(sasRequestParameters.getSlot(), "9");
    assertEquals(sasRequestParameters.getRFormat(), "xhtml");
    assertEquals(sasRequestParameters.getAllowBannerAds(), new Boolean(true));
    assertEquals(sasRequestParameters.getCategories().toString(), "[70, 42]");
    assertEquals(sasRequestParameters.getSource(), "APP");
    assertEquals(sasRequestParameters.getAdcode(), "NON-JS");
    assertEquals(sasRequestParameters.getSdkVersion(), "i357");
  }
}
