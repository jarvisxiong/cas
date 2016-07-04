package com.inmobi.adserve.channels.adnetworks;

import com.inmobi.adserve.adpool.ConnectionType;
import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.channels.adnetworks.tappx.DCPTappxAdnetwork;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.IPRepository;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.configuration.Configuration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replay;

/**
 * Created by deepak.jha on 5/10/16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BaseAdNetworkImpl.class)
public class DCPTappxAdNetworkTest {
    private static final String debug = "debug";
    private static final String loggerConf = "/tmp/channel-server.properties";
    private static final Bootstrap clientBootstrap = null;
    private static final String TapxAdvId = "Tappxadv1";
    private static final String pubId = "/120940746/Pub-10023-Android-0217";
    private static Configuration mockConfig = null;
    private static DCPTappxAdnetwork dcpTappxAdnetwork;
    private static String TappxHost = "http://ssp.api.tappx.com/req_v1";
    private static RepositoryHelper repositoryHelper;

    public static void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("tappx.host")).andReturn(TappxHost).anyTimes();
        expect(mockConfig.getString("tappx.key")).andReturn(pubId).anyTimes();
        expect(mockConfig.getString("tappx.istest")).andReturn("0").anyTimes();
        expect(mockConfig.getString("debug")).andReturn(debug).anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        replay(mockConfig);
    }

    @BeforeClass
    public static void setUp() throws Exception {
        File f;
        f = new File(loggerConf);
        if (!f.exists()) {
            f.createNewFile();
        }
        Formatter.init();
        final Channel serverChannel = createMock(Channel.class);
        final HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
        prepareMockConfig();
        final SlotSizeMapEntity slotSizeMapEntityFor4 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor4.getDimension()).andReturn(new Dimension(300, 50)).anyTimes();
        replay(slotSizeMapEntityFor4);
        final SlotSizeMapEntity slotSizeMapEntityFor9 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor9.getDimension()).andReturn(new Dimension(320, 48)).anyTimes();
        replay(slotSizeMapEntityFor9);
        final SlotSizeMapEntity slotSizeMapEntityFor11 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor11.getDimension()).andReturn(new Dimension(728, 90)).anyTimes();
        replay(slotSizeMapEntityFor11);
        final SlotSizeMapEntity slotSizeMapEntityFor14 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor14.getDimension()).andReturn(new Dimension(320, 480)).anyTimes();
        replay(slotSizeMapEntityFor14);
        final SlotSizeMapEntity slotSizeMapEntityFor15 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor15.getDimension()).andReturn(new Dimension(320, 50)).anyTimes();
        replay(slotSizeMapEntityFor15);
        repositoryHelper = createMock(RepositoryHelper.class);
        expect(repositoryHelper.querySlotSizeMapRepository((short) 4))
                .andReturn(slotSizeMapEntityFor4).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 9))
                .andReturn(slotSizeMapEntityFor9).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 11))
                .andReturn(slotSizeMapEntityFor11).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 14))
                .andReturn(slotSizeMapEntityFor14).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 15))
                .andReturn(slotSizeMapEntityFor15).anyTimes();
        replay(repositoryHelper);

        dcpTappxAdnetwork = new DCPTappxAdnetwork(mockConfig, clientBootstrap, base, serverChannel);
        dcpTappxAdnetwork.setName("Tappx");

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        dcpTappxAdnetwork.setHost(TappxHost);
    }

    @Test
    public void testDCPTappxConfigureParameters() {

        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        casInternalRequestParameters.setGpid("gpidtest123");
        sasParams.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(TapxAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertTrue(dcpTappxAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity,
                (short) 4, repositoryHelper));
    }

    @Test
    public void testDCPTappxConfigureParametersBlankIP() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(TapxAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpTappxAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper));
    }

    @Test
    public void testDCPTappxConfigureParametersBlankExtKey() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(TapxAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpTappxAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper));
    }

    @Test
    public void testTappxRequestUri() throws Exception {
        setUp();
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setOsId(3);
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setImpressionId("IMPID");
        sasParams.setOsMajorVersion("4.4");
        sasParams.setDeviceMake("Apple iPhone");
        sasParams.setDeviceModel("J5");
        sasParams.setConnectionType(ConnectionType.WIFI);
        casInternalRequestParameters.setUidIFA("idfa202cb962ac59075b964b07152d234b70");
        casInternalRequestParameters.setGpid("gpid202cb962ac59075b964b07152d234b70");
        sasParams.setCategories(Arrays.asList(new Long[] {10l, 13l, 30l}));
        final String externalKey = "123qwe";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(TapxAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 0, new Integer[] {0}));
        if (dcpTappxAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper)) {
            final String actualUrl = dcpTappxAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://ssp.api.tappx.com/req_v1?key=123qwe&sz=320x50&os=android&cb=IMPID&aid=idfa202cb962ac59075b964b07152d234b70&aidl=0&ip=206.29.182.240&ua=Mozilla&lat=37.4429&lon=-122.1514&ov=4.4&mn=Apple+iPhone&mo=J5&ct=wifi&e=1&test=0";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
        }
    }

    @Test
    public void testDCPTappxParseResponse() throws Exception {
        setUp();
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        //casInternalRequestParameters.setUdid("weweweweee");
        casInternalRequestParameters.setUid("uid");
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSource("app");
        sasParams.setSdkVersion("i360");
        sasParams.setCategories(Arrays.asList(new Long[] {10l, 13l, 30l}));
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSiteContentType(ContentType.PERFORMANCE);
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(TapxAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpTappxAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper);
        final String response =
                "<!DOCTYPE html><html class='httppx' lang='en' xmlns='http://www.w3.org/1999/xhtml'><head><meta name='viewport' content='width=device-width, initial-scale=1, maximum-scale=1, user-scalable=0' /><meta charset='utf-8'/><script type=\"text/javascript\">window.NREUM||(NREUM={}),__nr_require=function(e,t,n){function r(n){if(!t[n]){var o=t[n]={exports:{}};e[n][0].call(o.exports,function(t){var o=e[n][1][t];return r(o||t)},o,o.exports)}return t[n].exports}if(\"function\"==typeof __nr_require)return __nr_require;for(var o=0;o<n.length;o++)r(n[o]);return r}({1:[function(e,t,n){function r(e,t){return function(){o(e,[(new Date).getTime()].concat(a(arguments)),null,t)}}var o=e(\"handle\"),i=e(2),a=e(3);\"undefined\"==typeof window.newrelic&&(newrelic=NREUM);var u=[\"setPageViewName\",\"setCustomAttribute\",\"finished\",\"addToTrace\",\"inlineHit\"],c=[\"addPageAction\"],f=\"api-\";i(u,function(e,t){newrelic[t]=r(f+t,\"api\")}),i(c,function(e,t){newrelic[t]=r(f+t)}),t.exports=newrelic,newrelic.noticeError=function(e){\"string\"==typeof e&&(e=new Error(e)),o(\"err\",[e,(new Date).getTime()])}},{}],2:[function(e,t,n){function r(e,t){var n=[],r=\"\",i=0;for(r in e)o.call(e,r)&&(n[i]=t(r,e[r]),i+=1);return n}var o=Object.prototype.hasOwnProperty;t.exports=r},{}],3:[function(e,t,n){function r(e,t,n){t||(t=0),\"undefined\"==typeof n&&(n=e?e.length:0);for(var r=-1,o=n-t||0,i=Array(0>o?0:o);++r<o;)i[r]=e[t+r];return i}t.exports=r},{}],ee:[function(e,t,n){function r(){}function o(e){function t(e){return e&&e instanceof r?e:e?u(e,a,i):i()}function n(n,r,o){e&&e(n,r,o);for(var i=t(o),a=l(n),u=a.length,c=0;u>c;c++)a[c].apply(i,r);var s=f[g[n]];return s&&s.push([m,n,r,i]),i}function p(e,t){w[e]=l(e).concat(t)}function l(e){return w[e]||[]}function d(e){return s[e]=s[e]||o(n)}function v(e,t){c(e,function(e,n){t=t||\"feature\",g[n]=t,t in f||(f[t]=[])})}var w={},g={},m={on:p,emit:n,get:d,listeners:l,context:t,buffer:v};return m}function i(){return new r}var a=\"nr@context\",u=e(\"gos\"),c=e(2),f={},s={},p=t.exports=o();p.backlog=f},{}],gos:[function(e,t,n){function r(e,t,n){if(o.call(e,t))return e[t];var r=n();if(Object.defineProperty&&Object.keys)try{return Object.defineProperty(e,t,{value:r,writable:!0,enumerable:!1}),r}catch(i){}return e[t]=r,r}var o=Object.prototype.hasOwnProperty;t.exports=r},{}],handle:[function(e,t,n){function r(e,t,n,r){o.buffer([e],r),o.emit(e,t,n)}var o=e(\"ee\").get(\"handle\");t.exports=r,r.ee=o},{}],id:[function(e,t,n){function r(e){var t=typeof e;return!e||\"object\"!==t&&\"function\"!==t?-1:e===window?0:a(e,i,function(){return o++})}var o=1,i=\"nr@id\",a=e(\"gos\");t.exports=r},{}],loader:[function(e,t,n){function r(){if(!w++){var e=v.info=NREUM.info,t=s.getElementsByTagName(\"script\")[0];if(e&&e.licenseKey&&e.applicationID&&t){c(l,function(t,n){e[t]||(e[t]=n)});var n=\"https\"===p.split(\":\")[0]||e.sslForHttp;v.proto=n?\"https://\":\"http://\",u(\"mark\",[\"onload\",a()],null,\"api\");var r=s.createElement(\"script\");r.src=v.proto+e.agent,t.parentNode.insertBefore(r,t)}}}function o(){\"complete\"===s.readyState&&i()}function i(){u(\"mark\",[\"domContent\",a()],null,\"api\")}function a(){return(new Date).getTime()}var u=e(\"handle\"),c=e(2),f=window,s=f.document;NREUM.o={ST:setTimeout,CT:clearTimeout,XHR:f.XMLHttpRequest,REQ:f.Request,EV:f.Event,PR:f.Promise,MO:f.MutationObserver},e(1);var p=\"\"+location,l={beacon:\"bam.nr-data.net\",errorBeacon:\"bam.nr-data.net\",agent:\"js-agent.newrelic.com/nr-943.min.js\"},d=window.XMLHttpRequest&&XMLHttpRequest.prototype&&XMLHttpRequest.prototype.addEventListener&&!/CriOS/.test(navigator.userAgent),v=t.exports={offset:a(),origin:p,features:{},xhrWrappable:d};s.addEventListener?(s.addEventListener(\"DOMContentLoaded\",i,!1),f.addEventListener(\"load\",r,!1)):(s.attachEvent(\"onreadystatechange\",o),f.attachEvent(\"onload\",r)),u(\"mark\",[\"firstbyte\",a()],null,\"api\");var w=0},{}]},{},[\"loader\"]);</script><title>:tappx</title><style type='text/css'>html.httppx, body.bdtppx, table.tbltppx, tr.trtppx, td.tdtppx {  -webkit-touch-callout: none; overflow:hidden; border: 0; margin: 0; padding: 0; text-align: center; vertical-align: middle; color: white !important; } html.httppx { margin: -1px !important; background-color: transparent !important; } html.httppx, body.bdtppx, table.tbltppx, tr.trtppx, td.tdtppx { height: 100%; width: 100%; }</style></head><body class=\"bdtppx\" style=\"background-color:black\"><table class='tbltppx' style=\"background-color:black\"><tr class='trtppx'><td class='tdtppx'><div><a href='http://www.tappx.com' target=\"_blank\">\t<img src='data:image/gif;base64,R0lGODlhQAEyAPf/AMPDxfe/t91HLbu8vb2+wMXGx40ZBLKztuFVPsRcZNo5HeTt7dsAA64iBtU/RhoXG0dESPr6+sWFjtLS1Ot+baOjpr6/wfDw8PMAE7W2uc4DHLm6va+tr7i5vPb298HCw6utr7CxtJ2eorS1uOgBHMvMzfedp+/v8KbFxc3O0CMhJcnKy93e3/T09MjJyvR6g3R0dtXW12dnauQAFrO0thsYHYWDhs/P0McoCLy9v+3t7pGSlNra24IKHfn+/ra3ug0KD9jZ2vLy8ucQIvAADt4FCK6wsufn6PuKld/f4be4u6y1uOXl5o+Pkam6u6yztvPz9MbHyeIAC80pCOnp6eHh4gIBAtfY2dXV1qq/wN/f4Nvb3JucnU9OUTg1OgcaHfX29uMFF+7u7+vr6xUSF+Xo6POsoI2NkKy4u5YEFdfX2NDR0l9fYszNzsDBwwEOEqyusPBpdQYZG52doe2LepmanWAQHg8ZHa2vsS4uMa4FHCwpLgkGC/nMxbGztQUFB+RjTvCYi/zm4/3y8PWzqOhxXfrZ1PKlme6LfPSuouyDcuRjTfCYiul5Zv39/f7+/to5HNQrCfj4+P39/uHj4/z8/K+xs/Hx8uLi493d3vn5+aiqrLGxtObm5+rq6+/v78nLzPn5+txDJ+nr69TV1tPU1fz08+Lk5ODh4rOztu3v79nZ2uPl5drb3Ojo6cpWQjoWHvj5+S4rMO9ZZq+xtJSVlvCUhapfUj49QF1dX/2iqd3X2fHy8lYHFvjg3UY4Pdzd3ugABP319DcfI6+xs/vW0fnIwP76+eocJubj5L2hpv79/dGQmKtMOsvLy9xxe/Xr6fQAGubGwv3p5vv7+/X19dcxEfvb1/HLxezQzPOEjv7z8ZiYmzYEC97e3+p8aa+3uvnNxrq4urC8v+sEDA8NERIOE+2GdbO0t8MuPfXz9K/Cw+/i4H9+gOzs7ZSUlZWWmbW2utvc3bi4vMVCKtZGLf6TmjEWHKyxs7GxteRgSJtNP9UrCf///9UrC////yH/C05FVFNDQVBFMi4wAwEAAAAh/wtYTVAgRGF0YVhNUDw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYxIDY0LjE0MDk0OSwgMjAxMC8xMi8wNy0xMDo1NzowMSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iIHhtbG5zOnN0UmVmPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VSZWYjIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtcE1NOk9yaWdpbmFsRG9jdW1lbnRJRD0ieG1wLmRpZDo5QjgxRDdBQ0JDQzNFMzExOEIwMzhFMUFFQTBCQ0ZFMiIgeG1wTU06RG9jdW1lbnRJRD0ieG1wLmRpZDo4N0JERjdFMkY2MkYxMUUzQjcxNDhBNTQ3MDlGQzlDNyIgeG1wTU06SW5zdGFuY2VJRD0ieG1wLmlpZDo4N0JERjdFMUY2MkYxMUUzQjcxNDhBNTQ3MDlGQzlDNyIgeG1wOkNyZWF0b3JUb29sPSJBZG9iZSBQaG90b3Nob3AgQ1M1LjEgV2luZG93cyI+IDx4bXBNTTpEZXJpdmVkRnJvbSBzdFJlZjppbnN0YW5jZUlEPSJ4bXAuaWlkOjc5NUFGRTBBMkZGNkUzMTE5RTg1QjIzREE3MjI2OTI3IiBzdFJlZjpkb2N1bWVudElEPSJ4bXAuZGlkOjk3ODFEN0FDQkNDM0UzMTE4QjAzOEUxQUVBMEJDRkUyIi8+IDwvcmRmOkRlc2NyaXB0aW9uPiA8L3JkZjpSREY+IDwveDp4bXBtZXRhPiA8P3hwYWNrZXQgZW5kPSJyIj8+Af/+/fz7+vn49/b19PPy8fDv7u3s6+rp6Ofm5eTj4uHg397d3Nva2djX1tXU09LR0M/OzczLysnIx8bFxMPCwcC/vr28u7q5uLe2tbSzsrGwr66trKuqqainpqWko6KhoJ+enZybmpmYl5aVlJOSkZCPjo2Mi4qJiIeGhYSDgoGAf359fHt6eXh3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'></a></div></td></tr></table><script type=\"text/javascript\">window.NREUM||(NREUM={});NREUM.info={\"beacon\":\"bam.nr-data.net\",\"licenseKey\":\"959fa8fa24\",\"applicationID\":\"19252650\",\"transactionName\":\"MgdRYEMEVhZSWxUPDgtNZkZYSkoAQmcXV08VCkM=\",\"queueTime\":0,\"applicationTime\":122,\"atts\":\"HkBSFgseRRg=\",\"errorBeacon\":\"bam.nr-data.net\",\"agent\":\"\"}</script></body>";

        dcpTappxAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpTappxAdnetwork.getHttpResponseStatusCode(), 200);
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><!DOCTYPE html><html class='httppx' lang='en' xmlns='http://www.w3.org/1999/xhtml'><head><meta name='viewport' content='width=device-width, initial-scale=1, maximum-scale=1, user-scalable=0' /><meta charset='utf-8'/><script type=\"text/javascript\">window.NREUM||(NREUM={}),__nr_require=function(e,t,n){function r(n){if(!t[n]){var o=t[n]={exports:{}};e[n][0].call(o.exports,function(t){var o=e[n][1][t];return r(o||t)},o,o.exports)}return t[n].exports}if(\"function\"==typeof __nr_require)return __nr_require;for(var o=0;o<n.length;o++)r(n[o]);return r}({1:[function(e,t,n){function r(e,t){return function(){o(e,[(new Date).getTime()].concat(a(arguments)),null,t)}}var o=e(\"handle\"),i=e(2),a=e(3);\"undefined\"==typeof window.newrelic&&(newrelic=NREUM);var u=[\"setPageViewName\",\"setCustomAttribute\",\"finished\",\"addToTrace\",\"inlineHit\"],c=[\"addPageAction\"],f=\"api-\";i(u,function(e,t){newrelic[t]=r(f+t,\"api\")}),i(c,function(e,t){newrelic[t]=r(f+t)}),t.exports=newrelic,newrelic.noticeError=function(e){\"string\"==typeof e&&(e=new Error(e)),o(\"err\",[e,(new Date).getTime()])}},{}],2:[function(e,t,n){function r(e,t){var n=[],r=\"\",i=0;for(r in e)o.call(e,r)&&(n[i]=t(r,e[r]),i+=1);return n}var o=Object.prototype.hasOwnProperty;t.exports=r},{}],3:[function(e,t,n){function r(e,t,n){t||(t=0),\"undefined\"==typeof n&&(n=e?e.length:0);for(var r=-1,o=n-t||0,i=Array(0>o?0:o);++r<o;)i[r]=e[t+r];return i}t.exports=r},{}],ee:[function(e,t,n){function r(){}function o(e){function t(e){return e&&e instanceof r?e:e?u(e,a,i):i()}function n(n,r,o){e&&e(n,r,o);for(var i=t(o),a=l(n),u=a.length,c=0;u>c;c++)a[c].apply(i,r);var s=f[g[n]];return s&&s.push([m,n,r,i]),i}function p(e,t){w[e]=l(e).concat(t)}function l(e){return w[e]||[]}function d(e){return s[e]=s[e]||o(n)}function v(e,t){c(e,function(e,n){t=t||\"feature\",g[n]=t,t in f||(f[t]=[])})}var w={},g={},m={on:p,emit:n,get:d,listeners:l,context:t,buffer:v};return m}function i(){return new r}var a=\"nr@context\",u=e(\"gos\"),c=e(2),f={},s={},p=t.exports=o();p.backlog=f},{}],gos:[function(e,t,n){function r(e,t,n){if(o.call(e,t))return e[t];var r=n();if(Object.defineProperty&&Object.keys)try{return Object.defineProperty(e,t,{value:r,writable:!0,enumerable:!1}),r}catch(i){}return e[t]=r,r}var o=Object.prototype.hasOwnProperty;t.exports=r},{}],handle:[function(e,t,n){function r(e,t,n,r){o.buffer([e],r),o.emit(e,t,n)}var o=e(\"ee\").get(\"handle\");t.exports=r,r.ee=o},{}],id:[function(e,t,n){function r(e){var t=typeof e;return!e||\"object\"!==t&&\"function\"!==t?-1:e===window?0:a(e,i,function(){return o++})}var o=1,i=\"nr@id\",a=e(\"gos\");t.exports=r},{}],loader:[function(e,t,n){function r(){if(!w++){var e=v.info=NREUM.info,t=s.getElementsByTagName(\"script\")[0];if(e&&e.licenseKey&&e.applicationID&&t){c(l,function(t,n){e[t]||(e[t]=n)});var n=\"https\"===p.split(\":\")[0]||e.sslForHttp;v.proto=n?\"https://\":\"http://\",u(\"mark\",[\"onload\",a()],null,\"api\");var r=s.createElement(\"script\");r.src=v.proto+e.agent,t.parentNode.insertBefore(r,t)}}}function o(){\"complete\"===s.readyState&&i()}function i(){u(\"mark\",[\"domContent\",a()],null,\"api\")}function a(){return(new Date).getTime()}var u=e(\"handle\"),c=e(2),f=window,s=f.document;NREUM.o={ST:setTimeout,CT:clearTimeout,XHR:f.XMLHttpRequest,REQ:f.Request,EV:f.Event,PR:f.Promise,MO:f.MutationObserver},e(1);var p=\"\"+location,l={beacon:\"bam.nr-data.net\",errorBeacon:\"bam.nr-data.net\",agent:\"js-agent.newrelic.com/nr-943.min.js\"},d=window.XMLHttpRequest&&XMLHttpRequest.prototype&&XMLHttpRequest.prototype.addEventListener&&!/CriOS/.test(navigator.userAgent),v=t.exports={offset:a(),origin:p,features:{},xhrWrappable:d};s.addEventListener?(s.addEventListener(\"DOMContentLoaded\",i,!1),f.addEventListener(\"load\",r,!1)):(s.attachEvent(\"onreadystatechange\",o),f.attachEvent(\"onload\",r)),u(\"mark\",[\"firstbyte\",a()],null,\"api\");var w=0},{}]},{},[\"loader\"]);</script><title>:tappx</title><style type='text/css'>html.httppx, body.bdtppx, table.tbltppx, tr.trtppx, td.tdtppx {  -webkit-touch-callout: none; overflow:hidden; border: 0; margin: 0; padding: 0; text-align: center; vertical-align: middle; color: white !important; } html.httppx { margin: -1px !important; background-color: transparent !important; } html.httppx, body.bdtppx, table.tbltppx, tr.trtppx, td.tdtppx { height: 100%; width: 100%; }</style></head><body class=\"bdtppx\" style=\"background-color:black\"><table class='tbltppx' style=\"background-color:black\"><tr class='trtppx'><td class='tdtppx'><div><a href='http://www.tappx.com' target=\"_blank\">\t<img src='data:image/gif;base64,R0lGODlhQAEyAPf/AMPDxfe/t91HLbu8vb2+wMXGx40ZBLKztuFVPsRcZNo5HeTt7dsAA64iBtU/RhoXG0dESPr6+sWFjtLS1Ot+baOjpr6/wfDw8PMAE7W2uc4DHLm6va+tr7i5vPb298HCw6utr7CxtJ2eorS1uOgBHMvMzfedp+/v8KbFxc3O0CMhJcnKy93e3/T09MjJyvR6g3R0dtXW12dnauQAFrO0thsYHYWDhs/P0McoCLy9v+3t7pGSlNra24IKHfn+/ra3ug0KD9jZ2vLy8ucQIvAADt4FCK6wsufn6PuKld/f4be4u6y1uOXl5o+Pkam6u6yztvPz9MbHyeIAC80pCOnp6eHh4gIBAtfY2dXV1qq/wN/f4Nvb3JucnU9OUTg1OgcaHfX29uMFF+7u7+vr6xUSF+Xo6POsoI2NkKy4u5YEFdfX2NDR0l9fYszNzsDBwwEOEqyusPBpdQYZG52doe2LepmanWAQHg8ZHa2vsS4uMa4FHCwpLgkGC/nMxbGztQUFB+RjTvCYi/zm4/3y8PWzqOhxXfrZ1PKlme6LfPSuouyDcuRjTfCYiul5Zv39/f7+/to5HNQrCfj4+P39/uHj4/z8/K+xs/Hx8uLi493d3vn5+aiqrLGxtObm5+rq6+/v78nLzPn5+txDJ+nr69TV1tPU1fz08+Lk5ODh4rOztu3v79nZ2uPl5drb3Ojo6cpWQjoWHvj5+S4rMO9ZZq+xtJSVlvCUhapfUj49QF1dX/2iqd3X2fHy8lYHFvjg3UY4Pdzd3ugABP319DcfI6+xs/vW0fnIwP76+eocJubj5L2hpv79/dGQmKtMOsvLy9xxe/Xr6fQAGubGwv3p5vv7+/X19dcxEfvb1/HLxezQzPOEjv7z8ZiYmzYEC97e3+p8aa+3uvnNxrq4urC8v+sEDA8NERIOE+2GdbO0t8MuPfXz9K/Cw+/i4H9+gOzs7ZSUlZWWmbW2utvc3bi4vMVCKtZGLf6TmjEWHKyxs7GxteRgSJtNP9UrCf///9UrC////yH/C05FVFNDQVBFMi4wAwEAAAAh/wtYTVAgRGF0YVhNUDw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYxIDY0LjE0MDk0OSwgMjAxMC8xMi8wNy0xMDo1NzowMSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iIHhtbG5zOnN0UmVmPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VSZWYjIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtcE1NOk9yaWdpbmFsRG9jdW1lbnRJRD0ieG1wLmRpZDo5QjgxRDdBQ0JDQzNFMzExOEIwMzhFMUFFQTBCQ0ZFMiIgeG1wTU06RG9jdW1lbnRJRD0ieG1wLmRpZDo4N0JERjdFMkY2MkYxMUUzQjcxNDhBNTQ3MDlGQzlDNyIgeG1wTU06SW5zdGFuY2VJRD0ieG1wLmlpZDo4N0JERjdFMUY2MkYxMUUzQjcxNDhBNTQ3MDlGQzlDNyIgeG1wOkNyZWF0b3JUb29sPSJBZG9iZSBQaG90b3Nob3AgQ1M1LjEgV2luZG93cyI+IDx4bXBNTTpEZXJpdmVkRnJvbSBzdFJlZjppbnN0YW5jZUlEPSJ4bXAuaWlkOjc5NUFGRTBBMkZGNkUzMTE5RTg1QjIzREE3MjI2OTI3IiBzdFJlZjpkb2N1bWVudElEPSJ4bXAuZGlkOjk3ODFEN0FDQkNDM0UzMTE4QjAzOEUxQUVBMEJDRkUyIi8+IDwvcmRmOkRlc2NyaXB0aW9uPiA8L3JkZjpSREY+IDwveDp4bXBtZXRhPiA8P3hwYWNrZXQgZW5kPSJyIj8+Af/+/fz7+vn49/b19PPy8fDv7u3s6+rp6Ofm5eTj4uHg397d3Nva2djX1tXU09LR0M/OzczLysnIx8bFxMPCwcC/vr28u7q5uLe2tbSzsrGwr66trKuqqainpqWko6KhoJ+enZybmpmYl5aVlJOSkZCPjo2Mi4qJiIeGhYSDgoGAf359fHt6eXh3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'></a></div></td></tr></table><script type=\"text/javascript\">window.NREUM||(NREUM={});NREUM.info={\"beacon\":\"bam.nr-data.net\",\"licenseKey\":\"959fa8fa24\",\"applicationID\":\"19252650\",\"transactionName\":\"MgdRYEMEVhZSWxUPDgtNZkZYSkoAQmcXV08VCkM=\",\"queueTime\":0,\"applicationTime\":122,\"atts\":\"HkBSFgseRRg=\",\"errorBeacon\":\"bam.nr-data.net\",\"agent\":\"\"}</script></body><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
                dcpTappxAdnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPTappxGetName() throws Exception {
        assertEquals(dcpTappxAdnetwork.getName(), "tappxDCP");
    }
}