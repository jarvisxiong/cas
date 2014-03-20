package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.adnetworks.madnet.DCPMadNetAdNetwork;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;


public class DCPMadNetAdNetworkTest extends TestCase {
    private Configuration      mockConfig   = null;
    private final String       debug        = "debug";
    private final String       loggerConf   = "/tmp/channel-server.properties";

    private DCPMadNetAdNetwork dcpMadNetAdNetwork;
    private final String       madnetHost   = "http://p.madnet.ru/ads2s";
    private final String       madnetStatus = "on";
    private final String       madnetAdvId  = "madnetadv1";

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("madnet.host")).andReturn(madnetHost).anyTimes();
        expect(mockConfig.getString("madnet.status")).andReturn(madnetStatus).anyTimes();
        expect(mockConfig.getString("madnet.advertiserId")).andReturn(madnetAdvId).anyTimes();
        expect(mockConfig.getString("debug")).andReturn(debug).anyTimes();
        expect(mockConfig.getString("madnet.clientId")).andReturn("inmob").anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        replay(mockConfig);
    }

    @Override
    public void setUp() throws Exception {
        File f;
        f = new File(loggerConf);
        if (!f.exists()) {
            f.createNewFile();
        }
        Formatter.init();
        Channel serverChannel = createMock(Channel.class);
        HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
        prepareMockConfig();
        SlotSizeMapping.init();
        dcpMadNetAdNetwork = new DCPMadNetAdNetwork(mockConfig, null, base, serverChannel);
    }

    @Test
    public void testDCPMadNetConfigureParameters() throws JSONException {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSlot("11");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        casInternalRequestParameters.uid = "23e2ewq445545";
        sasParams.setOsId(HandSetOS.iPhone_OS.getValue());
        casInternalRequestParameters.uidIFA = "23e2ewq445545";
        casInternalRequestParameters.uidADT = "0";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                madnetAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
                null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(true,
                dcpMadNetAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPMadNetConfigureParametersBlankIP() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                madnetAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
                null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(false,
                dcpMadNetAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPMadNetConfigureParametersBlankUA() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                madnetAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
                null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(false,
                dcpMadNetAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPMadNetRequestUri() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.blockedCategories = new ArrayList<Long>(Arrays.asList(new Long[] { 50l, 51l }));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("APP");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        List<Long> category = new ArrayList<Long>();
        category.add(3l);
        sasParams.setCategories(category);
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("15");
        sasParams.setOsId(HandSetOS.iPhone_OS.getValue());
        casInternalRequestParameters.uidIFA = "23e2ewq445545";
        casInternalRequestParameters.uidADT = "0";
        String externalKey = "0344343";
        SlotSizeMapping.init();
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                madnetAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
                null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 0));
        if (dcpMadNetAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null)) {
            String actualUrl = dcpMadNetAdNetwork.getRequestUri().toString();
            String expectedUrl = "http://p.madnet.ru/ads2s?uuid=0&t=json&html=0&type=text%2Cimage&ip=206.29.182.240&nid=inmob&pid=00000000-0000-0000-0000-000000000000&w=320&h=50&ua=Mozilla&cat=IAB19-15%2CIAB5-15%2CIAB3%2CIAB4&apid=00000000-0000-0000-0000-000000000000&gps=37.4429%2C-122.1514&idfa=23e2ewq445545&idfatracking=0";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPMadNetRequestUriBlankLatLong() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("WAP");
        casInternalRequestParameters.blockedCategories = new ArrayList<Long>(Arrays.asList(new Long[] { 50l, 51l }));
        casInternalRequestParameters.latLong = "38.5,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot("15");
        sasParams.setOsId(HandSetOS.iPhone_OS.getValue());
        casInternalRequestParameters.uidIFA = "23e2ewq445545";
        casInternalRequestParameters.uidADT = "0";
        List<Long> category = new ArrayList<Long>();
        category.add(3l);
        category.add(2l);
        sasParams.setCategories(category);

        String externalKey = "01212121";
        SlotSizeMapping.init();
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0"
                + "/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1" + "/9cddca11?ds=1";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                madnetAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
                null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                        "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"), new ArrayList<Integer>(), 0.0d,
                null, null, 0));
        if (dcpMadNetAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null)) {
            String actualUrl = dcpMadNetAdNetwork.getRequestUri().toString();
            System.out.println(actualUrl);
            String expectedUrl = "http://p.madnet.ru/ads2s?uuid=0&t=json&html=0&type=text%2Cimage&ip=206.29.182.240&nid=inmob&pid=00000000-0000-0000-0000-000000000000&w=320&h=50&ua=Mozilla&cat=IAB1-1%2CIAB19-15%2CIAB5-15%2CIAB3%2CIAB4%2CIAB5&sid=00000000-0000-0000-0000-000000000000&gps=38.5%2C-122.1514&idfa=23e2ewq445545&idfatracking=0";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPMadNetParseAdImage() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.blockedCategories = new ArrayList<Long>(Arrays.asList(new Long[] { 50l, 51l }));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot("15");
        sasParams.setOsId(HandSetOS.iPhone_OS.getValue());
        casInternalRequestParameters.uidIFA = "23e2ewq445545";
        casInternalRequestParameters.uidADT = "0";
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0"
                + "/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1"
                + "/9cddca11?beacon=true";

        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                madnetAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
                null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpMadNetAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl);
        String response = "{   \"response\" : {      \"ads\" : [        {          \"components\" : {            \"beacons\" : [               \"http://p.madnet.ru/beacon?ic=0gq-KIsrO1mKjtApKGJW63ktk6Jjc88Rl5lCjxfZ9rq5skcn8sX1mZbW6BV6l76XBs7RWxkmn0KSA2tQ26oTLjGBMoYadTyZvO3YDUIxCYKI733VqpyDnvQg.G.NGwYQIJhgZh2DDBYUD.wnfyG9u1ou4-JVXFOgNCvGDmqIzxqaWhdV1jsnR3JwVRsxKZAmoi1G5eKM.ERhEygh600goq.9nwPvd.3vjKxvPQRkuTplCUfOOZ5TyAwuIooAHtcjYVN.zX113m3SrHaMbn3mUpjybV-d-9.fWheCCa6LW0XjhMPIQkSvxDUE-luALdZhznOvTI5R6ajOJ9uhwqK4hR6Ke6fxK38qvL3EwC68pymPU9dn.2g9yqAOVSPyTuvOIyPOjB7g7qVlu3lI6K.zLejD-Se6AG3ieRhKWuUfS1Qz-enrQ8HJ1uKFseS.5CsXBSygFoijQHvyOl2oS.aGRKApmYZwwVUAM7SxDveLX22Sy9cKf.LoaHjLBEjOk5QFLK5HUsCTDbuaZpegex50axaBUR49T.rRBWWd-BoOyJQZhZI8vPdsfypOGzUSE7wfZLG3Q7OOCzv206VsjmWchfvHgIBC3uHIqbFfW59rzfNr.yIQrwCzPxKWkiVlEnnIyCEpZvQr9kTMSVJ7dh5BDZ6O6vLkwAesTLs4sgeSVSZUG0yX1s8AXGS07OSGmMcV-.jE-WYNJM7ueMGkEPkBtEOwr0mE.OyGqmd8xfcq8AHX96duF.44V2Fj8OObnHeN.Pn3wBsQk78tjVaNet86fY5Y8T9DAB6owtRZysC.5xhqbP2WE6lsxMLMs7Iwk2Cw6oD52vCa3JcWN8qHQL91Gxt1BFAq4NAPDGU0dxD2qXNdRlWjVj-arD8J8IuhtQ==&cachebuster=0902ee6d-4046-4ce4-8355-06179c0020b7\"            ],            \"click_url\" : \"http://p.madnet.ru/click/0gq-KJy4Axfv-49dEX2gvfrmufehG2HBHkT6pEipCx4LGwHlkYc47uvg5sDhXe9J50SgQDG.0tb.ZfCrfY6EpneAaVE8U8qB1H4wKKUcg2vgTxsJw8yIAd.BVlWw3HoWQWGD0sSdX5JXKUAxYkJ7c-Aeq4IijFKK6IaZ2vBIrUOh4pLhNPFGfmOeANXehbGHJnZgKgE8-TbqfA8qE4NWZovmQOnpS.fGIX7Bt4Qv9fGs8X0d6sKHGDLCMKNGXyym1DbCovczCrjeXE6JmKQQ8F7ZGQqHrxTyQPvkLm2KYO01yYbrkaQ0KOpYlj1UR4u3RHNCa6eHxa2nbh-mR1BLuiObft82L7KnEl2PzggygW6Knja-zfCATVv-Yzi0Ons6TMAFGF8pzQmUfYd-KUjIP0TuI1c8n9B4tOhDvVcxrIr9iDX58nndWc0y1BNhSvvqgomlofBQYY8-7IKYJNkrtOzwgnvw-cW8zB9QiLNWX8RTmKkAwyqGHg5dAgw2NgwgmZKMLA7JcCE0RkDmOzFtCWWri9.zjxaoq1.Qr7YbF7CDYmk1cGJaNsoqrre5T.fEsBILH0CAV2e1T93VPPvpdliBkWc56BaKxZ9CUZL75Z3v3siojbVqpGe-EoOVE6vHzdmJ9DxWdFrvyFSUSLHqziZoHzDKWUAW3pGNZJbFCNcB7BDqySoIj0.TekTr8yfTLnUuCEA.nh9MKCNMnprs232hxyXhwc5S9tHfOKPALQ-kvwPAQtuoq38nC-Kf8ZC8D69YMye5MCo4qJs2kKcUiDgJLhZigrX28yOM1is2cPc9dMVavBkCYp8hmZgLw9eHuu2Xad.KvMYaKRu9KO8jaKkFQ-auz3LjjjvuWJc7JTYUicp5iGtXuvefC3yzPg==/http%3A%2F%2Fwww.tcsbank.ru%2Fmobile%2Fcards%2F%3Futm_source%3Dmobile_madnet_cc%26utm_medium%3Dcpm%26utm_content%3D320x53_card_white2_300%26utm_campaign%3Dcreditcard%26wm%3Dmadvertise_old_mobile_cc%26sid%3D320x53_card_white2_300\",            \"image_url\" : \"http://media.madnet.ru/ffd07026052aaa2dc00ed006d72bb767.gif\"          },          \"height\" : \"50\",          \"id\" : \"ad2b6392-5aa8-456a-8a0a-df1d9b97095f\",          \"type\" : \"image\",          \"width\" : \"320\"        }      ],      \"status\" : \"OK\"   } }";
        dcpMadNetAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(200, dcpMadNetAdNetwork.getHttpResponseStatusCode());
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"mraid.js\"></script><a href='http://p.madnet.ru/click/0gq-KJy4Axfv-49dEX2gvfrmufehG2HBHkT6pEipCx4LGwHlkYc47uvg5sDhXe9J50SgQDG.0tb.ZfCrfY6EpneAaVE8U8qB1H4wKKUcg2vgTxsJw8yIAd.BVlWw3HoWQWGD0sSdX5JXKUAxYkJ7c-Aeq4IijFKK6IaZ2vBIrUOh4pLhNPFGfmOeANXehbGHJnZgKgE8-TbqfA8qE4NWZovmQOnpS.fGIX7Bt4Qv9fGs8X0d6sKHGDLCMKNGXyym1DbCovczCrjeXE6JmKQQ8F7ZGQqHrxTyQPvkLm2KYO01yYbrkaQ0KOpYlj1UR4u3RHNCa6eHxa2nbh-mR1BLuiObft82L7KnEl2PzggygW6Knja-zfCATVv-Yzi0Ons6TMAFGF8pzQmUfYd-KUjIP0TuI1c8n9B4tOhDvVcxrIr9iDX58nndWc0y1BNhSvvqgomlofBQYY8-7IKYJNkrtOzwgnvw-cW8zB9QiLNWX8RTmKkAwyqGHg5dAgw2NgwgmZKMLA7JcCE0RkDmOzFtCWWri9.zjxaoq1.Qr7YbF7CDYmk1cGJaNsoqrre5T.fEsBILH0CAV2e1T93VPPvpdliBkWc56BaKxZ9CUZL75Z3v3siojbVqpGe-EoOVE6vHzdmJ9DxWdFrvyFSUSLHqziZoHzDKWUAW3pGNZJbFCNcB7BDqySoIj0.TekTr8yfTLnUuCEA.nh9MKCNMnprs232hxyXhwc5S9tHfOKPALQ-kvwPAQtuoq38nC-Kf8ZC8D69YMye5MCo4qJs2kKcUiDgJLhZigrX28yOM1is2cPc9dMVavBkCYp8hmZgLw9eHuu2Xad.KvMYaKRu9KO8jaKkFQ-auz3LjjjvuWJc7JTYUicp5iGtXuvefC3yzPg==/http%3A%2F%2Fwww.tcsbank.ru%2Fmobile%2Fcards%2F%3Futm_source%3Dmobile_madnet_cc%26utm_medium%3Dcpm%26utm_content%3D320x53_card_white2_300%26utm_campaign%3Dcreditcard%26wm%3Dmadvertise_old_mobile_cc%26sid%3D320x53_card_white2_300' onclick=\"document.getElementById('click').src='$IMClickUrl';\" target=\"_blank\" style=\"text-decoration: none\"><img src='http://media.madnet.ru/ffd07026052aaa2dc00ed006d72bb767.gif'  /></a><img src='http://p.madnet.ru/beacon?ic=0gq-KIsrO1mKjtApKGJW63ktk6Jjc88Rl5lCjxfZ9rq5skcn8sX1mZbW6BV6l76XBs7RWxkmn0KSA2tQ26oTLjGBMoYadTyZvO3YDUIxCYKI733VqpyDnvQg.G.NGwYQIJhgZh2DDBYUD.wnfyG9u1ou4-JVXFOgNCvGDmqIzxqaWhdV1jsnR3JwVRsxKZAmoi1G5eKM.ERhEygh600goq.9nwPvd.3vjKxvPQRkuTplCUfOOZ5TyAwuIooAHtcjYVN.zX113m3SrHaMbn3mUpjybV-d-9.fWheCCa6LW0XjhMPIQkSvxDUE-luALdZhznOvTI5R6ajOJ9uhwqK4hR6Ke6fxK38qvL3EwC68pymPU9dn.2g9yqAOVSPyTuvOIyPOjB7g7qVlu3lI6K.zLejD-Se6AG3ieRhKWuUfS1Qz-enrQ8HJ1uKFseS.5CsXBSygFoijQHvyOl2oS.aGRKApmYZwwVUAM7SxDveLX22Sy9cKf.LoaHjLBEjOk5QFLK5HUsCTDbuaZpegex50axaBUR49T.rRBWWd-BoOyJQZhZI8vPdsfypOGzUSE7wfZLG3Q7OOCzv206VsjmWchfvHgIBC3uHIqbFfW59rzfNr.yIQrwCzPxKWkiVlEnnIyCEpZvQr9kTMSVJ7dh5BDZ6O6vLkwAesTLs4sgeSVSZUG0yX1s8AXGS07OSGmMcV-.jE-WYNJM7ueMGkEPkBtEOwr0mE.OyGqmd8xfcq8AHX96duF.44V2Fj8OObnHeN.Pn3wBsQk78tjVaNet86fY5Y8T9DAB6owtRZysC.5xhqbP2WE6lsxMLMs7Iwk2Cw6oD52vCa3JcWN8qHQL91Gxt1BFAq4NAPDGU0dxD2qXNdRlWjVj-arD8J8IuhtQ==&cachebuster=0902ee6d-4046-4ce4-8355-06179c0020b7' height=1 width=1 border=0 style=\"display:none;\"/><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>",
                dcpMadNetAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPMadNetParseAdText() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.blockedCategories = new ArrayList<Long>(Arrays.asList(new Long[] { 50l, 51l }));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot("15");
        sasParams.setOsId(HandSetOS.iPhone_OS.getValue());
        casInternalRequestParameters.uidIFA = "23e2ewq445545";
        casInternalRequestParameters.uidADT = "0";
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";

        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                madnetAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
                null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpMadNetAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl);
        String response = "{   \"response\" : {      \"ads\" : [        {          \"components\" : {            \"beacons\" : [               \"http://p.madnet.ru/beacon?ic=0gq-KIsrO1mKjtApKGJW63ktk6Jjc88Rl5lCjxfZ9rq5skcn8sX1mZbW6BV6l76XBs7RWxkmn0KSA2tQ26oTLjGBMoYadTyZvO3YDUIxCYKI733VqpyDnvQg.G.NGwYQIJhgZh2DDBYUD.wnfyG9u1ou4-JVXFOgNCvGDmqIzxqaWhdV1jsnR3JwVRsxKZAmoi1G5eKM.ERhEygh600goq.9nwPvd.3vjKxvPQRkuTplCUfOOZ5TyAwuIooAHtcjYVN.zX113m3SrHaMbn3mUpjybV-d-9.fWheCCa6LW0XjhMPIQkSvxDUE-luALdZhznOvTI5R6ajOJ9uhwqK4hR6Ke6fxK38qvL3EwC68pymPU9dn.2g9yqAOVSPyTuvOIyPOjB7g7qVlu3lI6K.zLejD-Se6AG3ieRhKWuUfS1Qz-enrQ8HJ1uKFseS.5CsXBSygFoijQHvyOl2oS.aGRKApmYZwwVUAM7SxDveLX22Sy9cKf.LoaHjLBEjOk5QFLK5HUsCTDbuaZpegex50axaBUR49T.rRBWWd-BoOyJQZhZI8vPdsfypOGzUSE7wfZLG3Q7OOCzv206VsjmWchfvHgIBC3uHIqbFfW59rzfNr.yIQrwCzPxKWkiVlEnnIyCEpZvQr9kTMSVJ7dh5BDZ6O6vLkwAesTLs4sgeSVSZUG0yX1s8AXGS07OSGmMcV-.jE-WYNJM7ueMGkEPkBtEOwr0mE.OyGqmd8xfcq8AHX96duF.44V2Fj8OObnHeN.Pn3wBsQk78tjVaNet86fY5Y8T9DAB6owtRZysC.5xhqbP2WE6lsxMLMs7Iwk2Cw6oD52vCa3JcWN8qHQL91Gxt1BFAq4NAPDGU0dxD2qXNdRlWjVj-arD8J8IuhtQ==&cachebuster=0902ee6d-4046-4ce4-8355-06179c0020b7\"            ],            \"click_url\" : \"http://p.madnet.ru/click/0gq-KJy4Axfv-49dEX2gvfrmufehG2HBHkT6pEipCx4LGwHlkYc47uvg5sDhXe9J50SgQDG.0tb.ZfCrfY6EpneAaVE8U8qB1H4wKKUcg2vgTxsJw8yIAd.BVlWw3HoWQWGD0sSdX5JXKUAxYkJ7c-Aeq4IijFKK6IaZ2vBIrUOh4pLhNPFGfmOeANXehbGHJnZgKgE8-TbqfA8qE4NWZovmQOnpS.fGIX7Bt4Qv9fGs8X0d6sKHGDLCMKNGXyym1DbCovczCrjeXE6JmKQQ8F7ZGQqHrxTyQPvkLm2KYO01yYbrkaQ0KOpYlj1UR4u3RHNCa6eHxa2nbh-mR1BLuiObft82L7KnEl2PzggygW6Knja-zfCATVv-Yzi0Ons6TMAFGF8pzQmUfYd-KUjIP0TuI1c8n9B4tOhDvVcxrIr9iDX58nndWc0y1BNhSvvqgomlofBQYY8-7IKYJNkrtOzwgnvw-cW8zB9QiLNWX8RTmKkAwyqGHg5dAgw2NgwgmZKMLA7JcCE0RkDmOzFtCWWri9.zjxaoq1.Qr7YbF7CDYmk1cGJaNsoqrre5T.fEsBILH0CAV2e1T93VPPvpdliBkWc56BaKxZ9CUZL75Z3v3siojbVqpGe-EoOVE6vHzdmJ9DxWdFrvyFSUSLHqziZoHzDKWUAW3pGNZJbFCNcB7BDqySoIj0.TekTr8yfTLnUuCEA.nh9MKCNMnprs232hxyXhwc5S9tHfOKPALQ-kvwPAQtuoq38nC-Kf8ZC8D69YMye5MCo4qJs2kKcUiDgJLhZigrX28yOM1is2cPc9dMVavBkCYp8hmZgLw9eHuu2Xad.KvMYaKRu9KO8jaKkFQ-auz3LjjjvuWJc7JTYUicp5iGtXuvefC3yzPg==/http%3A%2F%2Fwww.tcsbank.ru%2Fmobile%2Fcards%2F%3Futm_source%3Dmobile_madnet_cc%26utm_medium%3Dcpm%26utm_content%3D320x53_card_white2_300%26utm_campaign%3Dcreditcard%26wm%3Dmadvertise_old_mobile_cc%26sid%3D320x53_card_white2_300\",            \"text_title\" : \"Welcome to MedNet\"          },          \"height\" : \"50\",          \"id\" : \"ad2b6392-5aa8-456a-8a0a-df1d9b97095f\",          \"type\" : \"text\",          \"width\" : \"320\"        }      ],      \"status\" : \"OK\"   } }";
        dcpMadNetAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(200, dcpMadNetAdNetwork.getHttpResponseStatusCode());
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\" content=\"text/html; charset=utf-8\"/></head><style>body, html {margin: 0; padding: 0; overflow: hidden;}.template_120_20 { width: 120px; height: 19px; }.template_168_28 { width: 168px; height: 27px; }.template_216_36 { width: 216px; height: 35px;}.template_300_50 {width: 300px; height: 49px;}.template_320_50 {width: 320px; height: 49px;}.template_320_48 {width: 320px; height: 47px;}.template_468_60 {width: 468px; height: 59px;}.template_728_90 {width: 728px; height: 89px;}.container { overflow: hidden; position: relative;}.adbg { border-top: 1px solid #00577b; background: #003229;background: -moz-linear-gradient(top, #003e57 0%, #003229 100%);background: -webkit-gradient(linear, left top, left bottom, color-stop(0%, #003e57), color-stop(100%, #003229));background: -webkit-linear-gradient(top, #003e57 0%, #003229 100%);background: -o-linear-gradient(top, #003e57 0%, #003229 100%);background: -ms-linear-gradient(top, #003e57 0%, #003229 100%);background: linear-gradient(top, #003e57 0%, #003229 100%); }.right-image { position: absolute; top: 0; right: 5px; display: table-cell; vertical-align: middle}.right-image-cell { padding-left:4px!important;white-space: nowrap; vertical-align: middle; height: 100%; width:40px;background:url(http://r.edge.inmobicdn.net/IphoneActionsData/line.png) repeat-y left top; } .adtable,.adtable td{border: 0; padding: 0; margin:0;}.adtable {padding:5px;}.adtext-cell {width:100%}.adtext-cell-div {font-family:helvetica;color:#fff;float:left;v-allign:middle}.template_120_20 .adtable{padding-top:0px;}.template_120_20 .adtable .adtext-cell div{font-size: 0.42em!important;}.template_168_28 .adtable{padding-top: 2px;}.template_168_28 .adtable .adtext-cell div{font-size: 0.5em!important;}.template_168_28 .adtable .adimg{width:18px;height:18px; }.template_216_36 .adtable{padding-top: 3px;}.template_216_36 .adtable .adtext-cell div{font-size: 0.6em!important;padding-left:3px;}.template_216_36 .adtable .adimg{width:25px;height:25px;}.template_300_50 .adtable{padding-top: 7px;}.template_300_50 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_300_50 .adtable .adimg{width:30px;height:30px;}.template_320_48 .adtable{padding-top: 6px;}.template_320_48 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_320_48 .adtable .adimg{width:30px;height:30px;}.template_320_50 .adtable{padding-top: 7px;}.template_320_50 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_320_50 .adtable .adimg{width:30px;height:30px;}.template_468_60 .adtable{padding-top:10px;}.template_468_60 .adtable .adtext-cell div{font-size: 1.1em!important;padding-left:8px;}.template_468_60 .adtable .adimg{width:35px;height:35px;}.template_728_90 .adtable{padding-top:19px;}.template_728_90 .adtable .adtext-cell div{font-size: 1.4em!important;padding-left:10px;}.template_728_90 .adtable .adimg{width:50px;height:50px;}</style><body style=\"margin:0;padding:0;overflow: hidden;\"><script type=\"text/javascript\" src=\"mraid.js\"></script><a style=\"text-decoration:none; \" href=\"http://p.madnet.ru/click/0gq-KJy4Axfv-49dEX2gvfrmufehG2HBHkT6pEipCx4LGwHlkYc47uvg5sDhXe9J50SgQDG.0tb.ZfCrfY6EpneAaVE8U8qB1H4wKKUcg2vgTxsJw8yIAd.BVlWw3HoWQWGD0sSdX5JXKUAxYkJ7c-Aeq4IijFKK6IaZ2vBIrUOh4pLhNPFGfmOeANXehbGHJnZgKgE8-TbqfA8qE4NWZovmQOnpS.fGIX7Bt4Qv9fGs8X0d6sKHGDLCMKNGXyym1DbCovczCrjeXE6JmKQQ8F7ZGQqHrxTyQPvkLm2KYO01yYbrkaQ0KOpYlj1UR4u3RHNCa6eHxa2nbh-mR1BLuiObft82L7KnEl2PzggygW6Knja-zfCATVv-Yzi0Ons6TMAFGF8pzQmUfYd-KUjIP0TuI1c8n9B4tOhDvVcxrIr9iDX58nndWc0y1BNhSvvqgomlofBQYY8-7IKYJNkrtOzwgnvw-cW8zB9QiLNWX8RTmKkAwyqGHg5dAgw2NgwgmZKMLA7JcCE0RkDmOzFtCWWri9.zjxaoq1.Qr7YbF7CDYmk1cGJaNsoqrre5T.fEsBILH0CAV2e1T93VPPvpdliBkWc56BaKxZ9CUZL75Z3v3siojbVqpGe-EoOVE6vHzdmJ9DxWdFrvyFSUSLHqziZoHzDKWUAW3pGNZJbFCNcB7BDqySoIj0.TekTr8yfTLnUuCEA.nh9MKCNMnprs232hxyXhwc5S9tHfOKPALQ-kvwPAQtuoq38nC-Kf8ZC8D69YMye5MCo4qJs2kKcUiDgJLhZigrX28yOM1is2cPc9dMVavBkCYp8hmZgLw9eHuu2Xad.KvMYaKRu9KO8jaKkFQ-auz3LjjjvuWJc7JTYUicp5iGtXuvefC3yzPg==/http%3A%2F%2Fwww.tcsbank.ru%2Fmobile%2Fcards%2F%3Futm_source%3Dmobile_madnet_cc%26utm_medium%3Dcpm%26utm_content%3D320x53_card_white2_300%26utm_campaign%3Dcreditcard%26wm%3Dmadvertise_old_mobile_cc%26sid%3D320x53_card_white2_300\" onclick=\"document.getElementById('click').src='$IMClickUrl';\" target=\"_blank\"><div class=\"container template_320_50 adbg\"><table class=\"adtable\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\"><tr><td class=\"adtext-cell\"><div class=\"adtext-cell-div\" style=\"font-weight:bold;\">Welcome to MedNet</div></div></td><td class=\"right-image-cell\">&nbsp<img width=\"28\" height=\"28\" class=\"adimg\" border=\"0\" src=\"http://r.edge.inmobicdn.net/IphoneActionsData/web.png\" /></td></tr></table></div></a><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 \"display:none;\"/><img src=\"http://p.madnet.ru/beacon?ic=0gq-KIsrO1mKjtApKGJW63ktk6Jjc88Rl5lCjxfZ9rq5skcn8sX1mZbW6BV6l76XBs7RWxkmn0KSA2tQ26oTLjGBMoYadTyZvO3YDUIxCYKI733VqpyDnvQg.G.NGwYQIJhgZh2DDBYUD.wnfyG9u1ou4-JVXFOgNCvGDmqIzxqaWhdV1jsnR3JwVRsxKZAmoi1G5eKM.ERhEygh600goq.9nwPvd.3vjKxvPQRkuTplCUfOOZ5TyAwuIooAHtcjYVN.zX113m3SrHaMbn3mUpjybV-d-9.fWheCCa6LW0XjhMPIQkSvxDUE-luALdZhznOvTI5R6ajOJ9uhwqK4hR6Ke6fxK38qvL3EwC68pymPU9dn.2g9yqAOVSPyTuvOIyPOjB7g7qVlu3lI6K.zLejD-Se6AG3ieRhKWuUfS1Qz-enrQ8HJ1uKFseS.5CsXBSygFoijQHvyOl2oS.aGRKApmYZwwVUAM7SxDveLX22Sy9cKf.LoaHjLBEjOk5QFLK5HUsCTDbuaZpegex50axaBUR49T.rRBWWd-BoOyJQZhZI8vPdsfypOGzUSE7wfZLG3Q7OOCzv206VsjmWchfvHgIBC3uHIqbFfW59rzfNr.yIQrwCzPxKWkiVlEnnIyCEpZvQr9kTMSVJ7dh5BDZ6O6vLkwAesTLs4sgeSVSZUG0yX1s8AXGS07OSGmMcV-.jE-WYNJM7ueMGkEPkBtEOwr0mE.OyGqmd8xfcq8AHX96duF.44V2Fj8OObnHeN.Pn3wBsQk78tjVaNet86fY5Y8T9DAB6owtRZysC.5xhqbP2WE6lsxMLMs7Iwk2Cw6oD52vCa3JcWN8qHQL91Gxt1BFAq4NAPDGU0dxD2qXNdRlWjVj-arD8J8IuhtQ==&cachebuster=0902ee6d-4046-4ce4-8355-06179c0020b7\" height=\"1\" width=\"1\" border=\"0\" style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>",
                dcpMadNetAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPMadNetParseNoAd() throws Exception {
        String response = "";
        dcpMadNetAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpMadNetAdNetwork.getHttpResponseStatusCode());
    }

    @Test
    public void testDCPMadNetParseEmptyResponseCode() throws Exception {
        String response = "";
        dcpMadNetAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpMadNetAdNetwork.getHttpResponseStatusCode());
        assertEquals("", dcpMadNetAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPMadNetGetId() throws Exception {
        assertEquals(madnetAdvId, dcpMadNetAdNetwork.getId());
    }

    @Test
    public void testDCPMadNetGetImpressionId() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0"
                + "/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1" + "/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                madnetAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
                null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                        "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"), new ArrayList<Integer>(), 0.0d,
                null, null, 32));
        dcpMadNetAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null);
        assertEquals("4f8d98e2-4bbd-40bc-8795-22da170700f9", dcpMadNetAdNetwork.getImpressionId());
    }

    @Test
    public void testDCPMadNetGetName() throws Exception {
        assertEquals("madnet", dcpMadNetAdNetwork.getName());
    }

    @Test
    public void testDCPMadNetIsClickUrlReq() throws Exception {
        assertTrue(dcpMadNetAdNetwork.isClickUrlRequired());
    }
}