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

import com.inmobi.adserve.channels.adnetworks.mopub.DCPMoPubAdNetwork;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;


public class DCPMoPubAdNetworkTest extends TestCase {
    private Configuration     mockConfig  = null;
    private final String      debug       = "debug";
    private final String      loggerConf  = "/tmp/channel-server.properties";

    private DCPMoPubAdNetwork dcpMoPubAdNetwork;
    private final String      mopubHost   = "http://ads.mopub.com/m/ad";
    private final String      mopubStatus = "on";
    private final String      mopubAdvId  = "mopubadv1";
    private final String      mopubTest   = "1";

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("mopub.host")).andReturn(mopubHost).anyTimes();
        expect(mockConfig.getString("mopub.status")).andReturn(mopubStatus).anyTimes();
        expect(mockConfig.getString("mopub.test")).andReturn(mopubTest).anyTimes();
        expect(mockConfig.getString("mopub.advertiserId")).andReturn(mopubAdvId).anyTimes();
        expect(mockConfig.getString("debug")).andReturn(debug).anyTimes();
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
        Channel serverChannel = createMock(Channel.class);
        HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
        prepareMockConfig();
        SlotSizeMapping.init();
        dcpMoPubAdNetwork = new DCPMoPubAdNetwork(mockConfig, null, base, serverChannel);
    }

    @Test
    public void testDCPMoPubConfigureParameters() throws JSONException {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSlot((short) 11);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        casInternalRequestParameters.uid = "23e2ewq445545";
        sasParams.setOsId(HandSetOS.iOS.getValue());
        casInternalRequestParameters.uidIFA = "23e2ewq445545";
        casInternalRequestParameters.uidADT = "0";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                mopubAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
                null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(true,
                dcpMoPubAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPMoPubConfigureParametersBlankIP() {
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
                mopubAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
                null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(false,
                dcpMoPubAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPMoPubConfigureParametersAdditionalParams() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                mopubAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
                null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(false,
                dcpMoPubAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPMoPubConfigureParametersBlankUA() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                mopubAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
                null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        assertEquals(false,
                dcpMoPubAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null));
    }

    @Test
    public void testDCPMoPubRequestUri() throws Exception {
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
        sasParams.setSlot((short) 15);
        sasParams.setOsId(HandSetOS.iOS.getValue());
        casInternalRequestParameters.uidIFA = "23e2ewq445545";
        casInternalRequestParameters.uidADT = "0";
        String externalKey = "0344343";
        SlotSizeMapping.init();
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                mopubAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
                null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                        "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"), new ArrayList<Integer>(), 0.0d,
                null, null, 32));
        if (dcpMoPubAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null)) {
            String actualUrl = dcpMoPubAdNetwork.getRequestUri().toString();
            String expectedUrl = "http://ads.mopub.com/m/ad?v=1&id=0344343&ip=206.29.182.240&udid=ifa:23e2ewq445545&dnt=0&q=Business&ll=37.4429%2C-122.1514";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPMoPubRequestUriBlankLatLong() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("WAP");
        casInternalRequestParameters.blockedCategories = new ArrayList<Long>(Arrays.asList(new Long[] { 50l, 51l }));
        casInternalRequestParameters.latLong = "38.5,-122.1514";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        sasParams.setSlot((short) 15);
        sasParams.setOsId(HandSetOS.iOS.getValue());
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
                mopubAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
                null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                        "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"), new ArrayList<Integer>(), 0.0d,
                null, null, 32));
        if (dcpMoPubAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null)) {
            String actualUrl = dcpMoPubAdNetwork.getRequestUri().toString();
            System.out.println(actualUrl);
            String expectedUrl = "http://ads.mopub.com/m/ad?v=1&id=01212121&ip=206.29.182.240&udid=ifa:23e2ewq445545&dnt=0&q=Business%2CBooks+%26+Reference&ll=38.5%2C-122.1514";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPMoPubParseAd() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.blockedCategories = new ArrayList<Long>(Arrays.asList(new Long[] { 50l, 51l }));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSlot((short) 15);
        sasParams.setOsId(HandSetOS.iOS.getValue());
        casInternalRequestParameters.uidIFA = "23e2ewq445545";
        casInternalRequestParameters.uidADT = "0";
        String externalKey = "19100";
        String beaconUrl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";

        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                mopubAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
                null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32));
        dcpMoPubAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl);
        String response = "<html>   <head>                       <style type='text/css'>       .mp_center {           position: fixed;           top: 50%;           left: 50%;           margin-left: -160px !important;           margin-top: -25px !important;           }     </style>          <script type= \"text/javascript \">   // Define trackImpressionsHelper if it hasn't been defined   if (trackImpressionHelper == null || typeof(trackImpressionHelper) !=  \"function \") {     function trackImpressionHelper(){       var urls = new Array();       var i = 0;                urls[i++]= \"http://ads.mopub.com/m/imp?accid=e49c9978b66311e295fa123138070049&c=642d290ab66411e281c11231392559e4&req=e9e0f29eb6ea11e2b88a0025907b586b&cpid=641f1fa4b66411e281c11231392559e4&hourly=0&reqt=1367913083.0&cid=642d290ab66411e281c11231392559e4&click_hourly=0&real_appid=6407d286b66411e281c11231392559e4&daily=0&agid=642144dcb66411e281c11231392559e4&click_daily=0&appid=&mpx_clk=http%3A%2F%2Fcpp.imp.mpx.mopub.com%2Fclick%3Fad_domain%3Dwfaa.com%26adgroup_id%3D642144dcb66411e281c11231392559e4%26adunit_id%3D640991e8b66411e281c11231392559e4%26ads_creative_id%3D642d290ab66411e281c11231392559e4%26app_id%3D6407d286b66411e281c11231392559e4%26app_name%3DUS%2520Gaming%2520iOS%2520with%2520ID%26bid_price%3D0.14%26bidder_id%3D4f3c3753bef41d3479000000%26bidder_name%3DDataXu%26campaign_id%3D641f1fa4b66411e281c11231392559e4%26charge_price%3D0.1%26ck%3D12CFC%26ckenc%3D0%26creative_id%3D0RzQ8fUZBr%26currency%3DUSD%26impression_id%3D38cb79f8-a966-4ba0-bad6-0778ea618ec1%26latency%3D0.021%26mopub_id%3Dsha%253Aa3c5258636a00040f46f865cf12fa0883944029d%26paid%3D0%26pub_id%3De49c9978b66311e295fa123138070049%26pub_name%3DLeading%2520Global%2520Mobile%2520Ad%2520Network%26pub_rev0%3D9203199574129111976%26request_id%3D38cb79f8-a966-4ba0-bad6-0778ea618ec1%26response_id%3DMFnFSEG9hu%26units%3D0&id=640991e8b66411e281c11231392559e4&udid=sha:a3c5258636a00040f46f865cf12fa0883944029d&rev=0.0000540&ckv=2&ck=4DC1D&cppck=A17E4 \";                             var hiddenSpan = document.createElement('span');       hiddenSpan.style.display = 'none';        var i = 0;       for (var i=0;i<urls.length;i++)       {         var img = document.createElement('img');         img.src = urls[i];         hiddenSpan.appendChild(img);       }            var body = document.getElementsByTagName('body')[0];       body.appendChild(hiddenSpan);     }   } </script>    <script type= \"text/javascript \">     function webviewDidClose(){       if(typeof webviewDidCloseHelper == 'function') {          webviewDidCloseHelper();       }     }     function webviewDidAppear(){         // inserts impression tracking         // when the interstitial is presented on screen                  // calls a user defined function if it exists         // useful for starting animations, videos, etc         // this would exist as part of the html for the         //  \"html \" creative         if(typeof webviewDidAppearHelper == 'function') {           webviewDidAppearHelper();         }     }     window.addEventListener( \"load \", function() {       var links = document.getElementsByTagName('a');       for(var i=0; i < links.length; i++) {         links[i].setAttribute('target','_top');       }     }, false);   </script>      </head>   <body style= \"margin:0;padding:0; \">        <div class= \" \"><IFRAME SRC= \"http://ad.doubleclick.net/adi/N7384.158901.DATAXU1/B7650773.2;dcopt=anid;dcove=r;sz=300x50;click=http://i.w55c.net/cl?t=1&btid=MzhjYjc5ZjgtYTk2Ni00YmEwLWJhZDYtMDc3OGVhNjE4ZWMxfE1GbkZTRUc5aHV8MTM2NzkxMzA4MzcwOHwzOGNiNzlmOC1hOTY2LTRiYTAtYmFkNi0wNzc4ZWE2MThlYzF8MEZtM25Qc0dnYnwwUnpROGZVWkJyfHwxNDkwMDB8fHx8fFVTRA&ei=MOPUB&tpc=&rurl=;ord=1546476314271527? \" WIDTH=300 HEIGHT=50 MARGINWIDTH=0 MARGINHEIGHT=0 HSPACE=0 VSPACE=0 FRAMEBORDER=0 SCROLLING=no BORDERCOLOR='#000000'> <SCRIPT language='JavaScript1.1' SRC= \"http://ad.doubleclick.net/adj/N7384.158901.DATAXU1/B7650773.2;dcopt=anid;dcove=r;abr=!ie;sz=300x50;click=http://i.w55c.net/cl?t=1&btid=MzhjYjc5ZjgtYTk2Ni00YmEwLWJhZDYtMDc3OGVhNjE4ZWMxfE1GbkZTRUc5aHV8MTM2NzkxMzA4MzcwOHwzOGNiNzlmOC1hOTY2LTRiYTAtYmFkNi0wNzc4ZWE2MThlYzF8MEZtM25Qc0dnYnwwUnpROGZVWkJyfHwxNDkwMDB8fHx8fFVTRA&ei=MOPUB&tpc=&rurl=;ord=1546476314271527? \"> </SCRIPT> <NOSCRIPT> <A HREF= \"http://ad.doubleclick.net/jump/N7384.158901.DATAXU1/B7650773.2;dcopt=anid;dcove=r;abr=!ie4;abr=!ie5;sz=300x50;click=http://i.w55c.net/cl?t=1&btid=MzhjYjc5ZjgtYTk2Ni00YmEwLWJhZDYtMDc3OGVhNjE4ZWMxfE1GbkZTRUc5aHV8MTM2NzkxMzA4MzcwOHwzOGNiNzlmOC1hOTY2LTRiYTAtYmFkNi0wNzc4ZWE2MThlYzF8MEZtM25Qc0dnYnwwUnpROGZVWkJyfHwxNDkwMDB8fHx8fFVTRA&ei=MOPUB&tpc=&rurl=;ord=1546476314271527? \"> <IMG SRC= \"http://ad.doubleclick.net/ad/N7384.158901.DATAXU1/B7650773.2;dcopt=anid;dcove=r;abr=!ie4;abr=!ie5;sz=300x50;ord=1546476314271527? \" BORDER=0 WIDTH=300 HEIGHT=50 ALT= \"Advertisement \"></A> </NOSCRIPT> </IFRAME><img style= \"position:absolute;left:0px;top:0px;z-index:-10; \" src= \"http://i.w55c.net/a.gif?&t=0&rtbhost=rtb01-c.us.dataxu.net&btid=MzhjYjc5ZjgtYTk2Ni00YmEwLWJhZDYtMDc3OGVhNjE4ZWMxfE1GbkZTRUc5aHV8MTM2NzkxMzA4MzcwOHwzOGNiNzlmOC1hOTY2LTRiYTAtYmFkNi0wNzc4ZWE2MThlYzF8MEZtM25Qc0dnYnwwUnpROGZVWkJyfHwxNDkwMDB8fHx8fFVTRA&ei=MOPUB&wp_exchange=0.1&js=0&ob=0&ccw=SUFCOS0zMCMwLjB8SUFCOSMwLjA&ci=0CgOwfjh6Z&s=http%3A%2F%2F6407d286b66411e281c11231392559e4.e49c9978b66311e295fa123138070049.mopub&ts=1367913083710&tpce=&gan=%3Bdcopt%3Danid&dapp=%3Bdcove%3Dr&c=US&r=TX&m=623&pc=75217&rnd=1546476314271527&mi=YXBw&epid=TVBlNDljOTk3OGI2NjMxMWUyOTVmYTEyMzEzODA3MDA0OQ&esid=TVA2NDA3ZDI4NmI2NjQxMWUyODFjMTEyMzEzOTI1NTllNA&ean=US+Gaming+iOS+with+ID&ct=2edbcbcb257b47a4bbf656c64651aeff&os=RGVza3RvcA&dc=MzQyNjY4ZTA3YWQyM2ViZjExNzE1&dv=TW96aWxsYQ&l=fHw&euid=YTNjNTI1ODYzNmEwMDA0MGY0NmY4NjVjZjEyZmEwODgzOTQ0MDI5ZA \" /><img style= \"position:absolute;left:0px;top:0px;z-index:-5; \" src= \"http://rtb01-c.us.dataxu.net/x/bcs0?&btid=MzhjYjc5ZjgtYTk2Ni00YmEwLWJhZDYtMDc3OGVhNjE4ZWMxfE1GbkZTRUc5aHV8MTM2NzkxMzA4MzcwOHwzOGNiNzlmOC1hOTY2LTRiYTAtYmFkNi0wNzc4ZWE2MThlYzF8MEZtM25Qc0dnYnwwUnpROGZVWkJyfHwxNDkwMDB8fHx8fFVTRA&ei=MOPUB&wp_exchange=0.1 \" /><div style='display:none;'><img src='http://cpp.imp.mpx.mopub.com/imp?ad_domain=wfaa.com&adgroup_id=642144dcb66411e281c11231392559e4&adunit_id=640991e8b66411e281c11231392559e4&ads_creative_id=642d290ab66411e281c11231392559e4&app_id=6407d286b66411e281c11231392559e4&app_name=US%20Gaming%20iOS%20with%20ID&bid_price=0.14&bidder_id=4f3c3753bef41d3479000000&bidder_name=DataXu&campaign_id=641f1fa4b66411e281c11231392559e4&charge_price=0.1&ck=12CFC&ckenc=0&creative_id=0RzQ8fUZBr&currency=USD&impression_id=38cb79f8-a966-4ba0-bad6-0778ea618ec1&latency=0.021&mopub_id=sha%3Aa3c5258636a00040f46f865cf12fa0883944029d&paid=0&pub_id=e49c9978b66311e295fa123138070049&pub_name=Leading%20Global%20Mobile%20Ad%20Network&pub_rev0=9203199574129111976&request_id=38cb79f8-a966-4ba0-bad6-0778ea618ec1&response_id=MFnFSEG9hu&units=0'/><img src='http://in.metamx.com/mopub/v0.gif?ad_domain=wfaa.com&adgroup_id=642144dcb66411e281c11231392559e4&adunit_id=640991e8b66411e281c11231392559e4&ads_creative_id=642d290ab66411e281c11231392559e4&app_id=6407d286b66411e281c11231392559e4&app_name=US%20Gaming%20iOS%20with%20ID&bid_price=0.14&bidder_id=4f3c3753bef41d3479000000&bidder_name=DataXu&campaign_id=641f1fa4b66411e281c11231392559e4&charge_price=0.1&ck=12CFC&ckenc=0&creative_id=0RzQ8fUZBr&currency=USD&impression_id=38cb79f8-a966-4ba0-bad6-0778ea618ec1&latency=0.021&mopub_id=sha%3Aa3c5258636a00040f46f865cf12fa0883944029d&paid=0&pub_id=e49c9978b66311e295fa123138070049&pub_name=Leading%20Global%20Mobile%20Ad%20Network&pub_rev0=9203199574129111976&request_id=38cb79f8-a966-4ba0-bad6-0778ea618ec1&response_id=MFnFSEG9hu&units=0'/></div></div>    <script type= \"text/javascript \">     // just call mopubFinishLoad upon window's load     if (typeof htmlWillCallFinishLoad ==  \"undefined \" || !htmlWillCallFinishLoad) {       if(typeof mopubFinishLoad == 'function') {         window.onload = mopubFinishLoad;       }     }             if(typeof trackImpressionHelper == 'function') {         trackImpressionHelper();       }         </script>    </body> </html>";
        dcpMoPubAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(200, dcpMoPubAdNetwork.getHttpResponseStatusCode());
        assertEquals(
                "<html>   <head>                       <style type='text/css'>       .mp_center {           position: fixed;           top: 50%;           left: 50%;           margin-left: -160px !important;           margin-top: -25px !important;           }     </style>          <script type= \"text/javascript \">   // Define trackImpressionsHelper if it hasn't been defined   if (trackImpressionHelper == null || typeof(trackImpressionHelper) !=  \"function \") {     function trackImpressionHelper(){       var urls = new Array();       var i = 0;                urls[i++]= \"http://ads.mopub.com/m/imp?accid=e49c9978b66311e295fa123138070049&c=642d290ab66411e281c11231392559e4&req=e9e0f29eb6ea11e2b88a0025907b586b&cpid=641f1fa4b66411e281c11231392559e4&hourly=0&reqt=1367913083.0&cid=642d290ab66411e281c11231392559e4&click_hourly=0&real_appid=6407d286b66411e281c11231392559e4&daily=0&agid=642144dcb66411e281c11231392559e4&click_daily=0&appid=&mpx_clk=http%3A%2F%2Fcpp.imp.mpx.mopub.com%2Fclick%3Fad_domain%3Dwfaa.com%26adgroup_id%3D642144dcb66411e281c11231392559e4%26adunit_id%3D640991e8b66411e281c11231392559e4%26ads_creative_id%3D642d290ab66411e281c11231392559e4%26app_id%3D6407d286b66411e281c11231392559e4%26app_name%3DUS%2520Gaming%2520iOS%2520with%2520ID%26bid_price%3D0.14%26bidder_id%3D4f3c3753bef41d3479000000%26bidder_name%3DDataXu%26campaign_id%3D641f1fa4b66411e281c11231392559e4%26charge_price%3D0.1%26ck%3D12CFC%26ckenc%3D0%26creative_id%3D0RzQ8fUZBr%26currency%3DUSD%26impression_id%3D38cb79f8-a966-4ba0-bad6-0778ea618ec1%26latency%3D0.021%26mopub_id%3Dsha%253Aa3c5258636a00040f46f865cf12fa0883944029d%26paid%3D0%26pub_id%3De49c9978b66311e295fa123138070049%26pub_name%3DLeading%2520Global%2520Mobile%2520Ad%2520Network%26pub_rev0%3D9203199574129111976%26request_id%3D38cb79f8-a966-4ba0-bad6-0778ea618ec1%26response_id%3DMFnFSEG9hu%26units%3D0&id=640991e8b66411e281c11231392559e4&udid=sha:a3c5258636a00040f46f865cf12fa0883944029d&rev=0.0000540&ckv=2&ck=4DC1D&cppck=A17E4 \";                             var hiddenSpan = document.createElement('span');       hiddenSpan.style.display = 'none';        var i = 0;       for (var i=0;i<urls.length;i++)       {         var img = document.createElement('img');         img.src = urls[i];         hiddenSpan.appendChild(img);       }            var body = document.getElementsByTagName('body')[0];       body.appendChild(hiddenSpan);     }   } </script>    <script type= \"text/javascript \">     function webviewDidClose(){       if(typeof webviewDidCloseHelper == 'function') {          webviewDidCloseHelper();       }     }     function webviewDidAppear(){         // inserts impression tracking         // when the interstitial is presented on screen                  // calls a user defined function if it exists         // useful for starting animations, videos, etc         // this would exist as part of the html for the         //  \"html \" creative         if(typeof webviewDidAppearHelper == 'function') {           webviewDidAppearHelper();         }     }     window.addEventListener( \"load \", function() {       var links = document.getElementsByTagName('a');       for(var i=0; i < links.length; i++) {         links[i].setAttribute('target','_top');       }     }, false);   </script>      </head>   <body style= \"margin:0;padding:0; \">        <div class= \" \"><IFRAME SRC= \"http://ad.doubleclick.net/adi/N7384.158901.DATAXU1/B7650773.2;dcopt=anid;dcove=r;sz=300x50;click=http://i.w55c.net/cl?t=1&btid=MzhjYjc5ZjgtYTk2Ni00YmEwLWJhZDYtMDc3OGVhNjE4ZWMxfE1GbkZTRUc5aHV8MTM2NzkxMzA4MzcwOHwzOGNiNzlmOC1hOTY2LTRiYTAtYmFkNi0wNzc4ZWE2MThlYzF8MEZtM25Qc0dnYnwwUnpROGZVWkJyfHwxNDkwMDB8fHx8fFVTRA&ei=MOPUB&tpc=&rurl=;ord=1546476314271527? \" WIDTH=300 HEIGHT=50 MARGINWIDTH=0 MARGINHEIGHT=0 HSPACE=0 VSPACE=0 FRAMEBORDER=0 SCROLLING=no BORDERCOLOR='#000000'> <SCRIPT language='JavaScript1.1' SRC= \"http://ad.doubleclick.net/adj/N7384.158901.DATAXU1/B7650773.2;dcopt=anid;dcove=r;abr=!ie;sz=300x50;click=http://i.w55c.net/cl?t=1&btid=MzhjYjc5ZjgtYTk2Ni00YmEwLWJhZDYtMDc3OGVhNjE4ZWMxfE1GbkZTRUc5aHV8MTM2NzkxMzA4MzcwOHwzOGNiNzlmOC1hOTY2LTRiYTAtYmFkNi0wNzc4ZWE2MThlYzF8MEZtM25Qc0dnYnwwUnpROGZVWkJyfHwxNDkwMDB8fHx8fFVTRA&ei=MOPUB&tpc=&rurl=;ord=1546476314271527? \"> </SCRIPT> <NOSCRIPT> <A HREF= \"http://ad.doubleclick.net/jump/N7384.158901.DATAXU1/B7650773.2;dcopt=anid;dcove=r;abr=!ie4;abr=!ie5;sz=300x50;click=http://i.w55c.net/cl?t=1&btid=MzhjYjc5ZjgtYTk2Ni00YmEwLWJhZDYtMDc3OGVhNjE4ZWMxfE1GbkZTRUc5aHV8MTM2NzkxMzA4MzcwOHwzOGNiNzlmOC1hOTY2LTRiYTAtYmFkNi0wNzc4ZWE2MThlYzF8MEZtM25Qc0dnYnwwUnpROGZVWkJyfHwxNDkwMDB8fHx8fFVTRA&ei=MOPUB&tpc=&rurl=;ord=1546476314271527? \"> <IMG SRC= \"http://ad.doubleclick.net/ad/N7384.158901.DATAXU1/B7650773.2;dcopt=anid;dcove=r;abr=!ie4;abr=!ie5;sz=300x50;ord=1546476314271527? \" BORDER=0 WIDTH=300 HEIGHT=50 ALT= \"Advertisement \"></A> </NOSCRIPT> </IFRAME><img style= \"position:absolute;left:0px;top:0px;z-index:-10; \" src= \"http://i.w55c.net/a.gif?&t=0&rtbhost=rtb01-c.us.dataxu.net&btid=MzhjYjc5ZjgtYTk2Ni00YmEwLWJhZDYtMDc3OGVhNjE4ZWMxfE1GbkZTRUc5aHV8MTM2NzkxMzA4MzcwOHwzOGNiNzlmOC1hOTY2LTRiYTAtYmFkNi0wNzc4ZWE2MThlYzF8MEZtM25Qc0dnYnwwUnpROGZVWkJyfHwxNDkwMDB8fHx8fFVTRA&ei=MOPUB&wp_exchange=0.1&js=0&ob=0&ccw=SUFCOS0zMCMwLjB8SUFCOSMwLjA&ci=0CgOwfjh6Z&s=http%3A%2F%2F6407d286b66411e281c11231392559e4.e49c9978b66311e295fa123138070049.mopub&ts=1367913083710&tpce=&gan=%3Bdcopt%3Danid&dapp=%3Bdcove%3Dr&c=US&r=TX&m=623&pc=75217&rnd=1546476314271527&mi=YXBw&epid=TVBlNDljOTk3OGI2NjMxMWUyOTVmYTEyMzEzODA3MDA0OQ&esid=TVA2NDA3ZDI4NmI2NjQxMWUyODFjMTEyMzEzOTI1NTllNA&ean=US+Gaming+iOS+with+ID&ct=2edbcbcb257b47a4bbf656c64651aeff&os=RGVza3RvcA&dc=MzQyNjY4ZTA3YWQyM2ViZjExNzE1&dv=TW96aWxsYQ&l=fHw&euid=YTNjNTI1ODYzNmEwMDA0MGY0NmY4NjVjZjEyZmEwODgzOTQ0MDI5ZA \" /><img style= \"position:absolute;left:0px;top:0px;z-index:-5; \" src= \"http://rtb01-c.us.dataxu.net/x/bcs0?&btid=MzhjYjc5ZjgtYTk2Ni00YmEwLWJhZDYtMDc3OGVhNjE4ZWMxfE1GbkZTRUc5aHV8MTM2NzkxMzA4MzcwOHwzOGNiNzlmOC1hOTY2LTRiYTAtYmFkNi0wNzc4ZWE2MThlYzF8MEZtM25Qc0dnYnwwUnpROGZVWkJyfHwxNDkwMDB8fHx8fFVTRA&ei=MOPUB&wp_exchange=0.1 \" /><div style='display:none;'><img src='http://cpp.imp.mpx.mopub.com/imp?ad_domain=wfaa.com&adgroup_id=642144dcb66411e281c11231392559e4&adunit_id=640991e8b66411e281c11231392559e4&ads_creative_id=642d290ab66411e281c11231392559e4&app_id=6407d286b66411e281c11231392559e4&app_name=US%20Gaming%20iOS%20with%20ID&bid_price=0.14&bidder_id=4f3c3753bef41d3479000000&bidder_name=DataXu&campaign_id=641f1fa4b66411e281c11231392559e4&charge_price=0.1&ck=12CFC&ckenc=0&creative_id=0RzQ8fUZBr&currency=USD&impression_id=38cb79f8-a966-4ba0-bad6-0778ea618ec1&latency=0.021&mopub_id=sha%3Aa3c5258636a00040f46f865cf12fa0883944029d&paid=0&pub_id=e49c9978b66311e295fa123138070049&pub_name=Leading%20Global%20Mobile%20Ad%20Network&pub_rev0=9203199574129111976&request_id=38cb79f8-a966-4ba0-bad6-0778ea618ec1&response_id=MFnFSEG9hu&units=0'/><img src='http://in.metamx.com/mopub/v0.gif?ad_domain=wfaa.com&adgroup_id=642144dcb66411e281c11231392559e4&adunit_id=640991e8b66411e281c11231392559e4&ads_creative_id=642d290ab66411e281c11231392559e4&app_id=6407d286b66411e281c11231392559e4&app_name=US%20Gaming%20iOS%20with%20ID&bid_price=0.14&bidder_id=4f3c3753bef41d3479000000&bidder_name=DataXu&campaign_id=641f1fa4b66411e281c11231392559e4&charge_price=0.1&ck=12CFC&ckenc=0&creative_id=0RzQ8fUZBr&currency=USD&impression_id=38cb79f8-a966-4ba0-bad6-0778ea618ec1&latency=0.021&mopub_id=sha%3Aa3c5258636a00040f46f865cf12fa0883944029d&paid=0&pub_id=e49c9978b66311e295fa123138070049&pub_name=Leading%20Global%20Mobile%20Ad%20Network&pub_rev0=9203199574129111976&request_id=38cb79f8-a966-4ba0-bad6-0778ea618ec1&response_id=MFnFSEG9hu&units=0'/></div></div>    <script type= \"text/javascript \">     // just call mopubFinishLoad upon window's load     if (typeof htmlWillCallFinishLoad ==  \"undefined \" || !htmlWillCallFinishLoad) {       if(typeof mopubFinishLoad == 'function') {         window.onload = mopubFinishLoad;       }     }             if(typeof trackImpressionHelper == 'function') {         trackImpressionHelper();       }         </script>    </body> </html> <img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/>",
                dcpMoPubAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPMoPubParseNoAd() throws Exception {
        String response = "";
        dcpMoPubAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpMoPubAdNetwork.getHttpResponseStatusCode());
    }

    @Test
    public void testDCPMoPubParseEmptyResponseCode() throws Exception {
        String response = "";
        dcpMoPubAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpMoPubAdNetwork.getHttpResponseStatusCode());
        assertEquals("", dcpMoPubAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPMoPubGetId() throws Exception {
        assertEquals(mopubAdvId, dcpMoPubAdNetwork.getId());
    }

    @Test
    public void testDCPMoPubGetImpressionId() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        String clurl = "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                + "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        String externalKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                mopubAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
                null, 0, null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                        "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"), new ArrayList<Integer>(), 0.0d,
                null, null, 32));
        dcpMoPubAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null);
        assertEquals("4f8d98e2-4bbd-40bc-8795-22da170700f9", dcpMoPubAdNetwork.getImpressionId());
    }

    @Test
    public void testDCPMoPubGetName() throws Exception {
        assertEquals("mopub", dcpMoPubAdNetwork.getName());
    }

    @Test
    public void testDCPMoPubIsClickUrlReq() throws Exception {
        assertEquals(false, dcpMoPubAdNetwork.isClickUrlRequired());
    }
}
