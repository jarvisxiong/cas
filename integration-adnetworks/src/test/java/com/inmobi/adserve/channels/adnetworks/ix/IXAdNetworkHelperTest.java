package com.inmobi.adserve.channels.adnetworks.ix;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.IXBlocklistEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.types.IXBlocklistKeyType;
import com.inmobi.adserve.channels.types.IXBlocklistType;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.inmobi.template.interfaces.TemplateConfiguration;
import com.inmobi.template.module.TemplateModule;

public class IXAdNetworkHelperTest {
    private static RepositoryHelper mockRepositoryHelper;

    private static final String siteId = "siteId";
    private static final Long siteIncId = 1L;
    private static final Long countryId = 2L;
    private static String countryIdString;

    private static SASRequestParameters sasParams;
    private static SASRequestParameters sasParamsFS;

    private static RepositoryHelper repositoryHelperWithOnlySiteEntries;
    private static RepositoryHelper repositoryHelperWithOnlyCountryEntries;
    private static RepositoryHelper repositoryHelperWithBothSiteAndCountryEntries;
    private static RepositoryHelper repositoryHelperWithNoEntries;
    private static RepositoryHelper repositoryHelperWithEmptyEntries;
    private static RepositoryHelper repositoryHelperWithQuerySlotSizeMapRepository;

    private static IXBlocklistEntity siteAdvertiserBlocklistEntity;
    private static IXBlocklistEntity siteIndustryBlocklistEntity;
    private static IXBlocklistEntity siteCreativeAttributesBlocklistEntity;
    private static IXBlocklistEntity countryAdvertiserBlocklistEntity;
    private static IXBlocklistEntity countryIndustryBlocklistEntity;
    private static IXBlocklistEntity countryCreativeAttributesBlocklistEntity;

    private static IXBlocklistEntity emptySiteAdvertiserBlocklistEntity;
    private static IXBlocklistEntity emptySiteIndustryBlocklistEntity;
    private static IXBlocklistEntity emptySiteCreativeAttributesBlocklistEntity;

    @Inject
    private static TemplateConfiguration templateConfiguration;


    private static final String clickUrl = "http://some/click/url/";
    private static final String beaconUrl = "http://some/beacon/url/";
    private static final String winurl = "http://some/win/url/";
    private static final String vast_adm = "<VAST></VAST>";
    private static String ARRAY_PREFIX = "[\"";
    private static String ARRAY_SUFFIX = "\"]";
    private static String EMPTY_ARRAY = "[]";
    private static final String BILLING_BEACON_SUFFIX = "?b=${WIN_BID}${DEAL_GET_PARAM}";

    private static final String adMarkUp_part1 = "\"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<VAST xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"vast.xsd\" version=\"2.0\">\n"
            + " <Ad id=\"299060245\">\n";

    private static final String inlineOpenTag = "  <InLine>\n";
    private static final String wrapperOpenTag = "  <Wrapper>\n";
    private static final String adMarkUp_part2 =
            "   <AdSystem>DBM</AdSystem>\n"
                    + "   <AdTitle>In-Stream Video</AdTitle>\n";
    private static final String errorTag =
            "   <Error><![CDATA[https://bid.g.doubleclick.net/xbbe/notify/rubicon?creative_id=16839126&usl_id=1181397&errorcode=[ERRORCODE]&d=APEucNVXoGKpR8JU4VGGuQsNBMSP2R4wXuhdPuHjl8BVEBavU7k0thNvWYak6z6aaaB4_d8AFgjUxXjDUhd5PBe7kz8_8U-Sron7C0xLvbMdKAsOEDImCcClCOySKsDHN0ea0xfTgQu2wOi90qG1_Nowx_ea7GZTDQ_LetU4omwWFMxZXymVHIYE6d8nqm35shZTPycvuimc8sUlP66oRhEb8ZauYrNhRvbDudUmreChBAnVgxOxDs9VPeQX6rFXqCs0rGvQlQFlBbj1MfKoeEThfQhTyPwz2bbhyRrt56j48z208Ru2PjI]]></Error>\n";
    private static final String impressionTag =
            "   <Impression><![CDATA[https://bid.g.doubleclick.net/xbbe/pixel?d=CJcBEP7pFBjW44MIIAE&v=APEucNX1ybOr1eT9xwNqEq-ieldOvmZR0E_cjceLanIbyjRc8eiAh86EWNcwiEhlK8VFHBpoyNuP]]></Impression>\n";
    private static final String adMarkUp_part3 =
            "   <Creatives>\n"
                    + "    <Creative id=\"67382425\" sequence=\"1\">\n"
                    + "     <Linear>\n"
                    + "      <Duration>00:00:30</Duration>\n";
    private static final String trackingEventsTag =
            "      <TrackingEvents>\n"
                    + "       <Tracking event=\"start\"><![CDATA[https://ad.doubleclick.net/ddm/activity/dc_oe=ChMI64zYxt3TyQIVy42PCh2nlgZoEAAYACCZ2ZAg;met=1;ecn1=1;etm1=0;eid1=11;]]></Tracking>\n"
                    + "       <Tracking event=\"firstQuartile\"><![CDATA[https://ad.doubleclick.net/ddm/activity/dc_oe=ChMI64zYxt3TyQIVy42PCh2nlgZoEAAYACCZ2ZAg;met=1;ecn1=1;etm1=0;eid1=960584;]]></Tracking>\n"
                    + "       <Tracking event=\"midpoint\"><![CDATA[https://ad.doubleclick.net/ddm/activity/dc_oe=ChMI64zYxt3TyQIVy42PCh2nlgZoEAAYACCZ2ZAg;met=1;ecn1=1;etm1=0;eid1=18;]]></Tracking>\n"
                    + "       <Tracking event=\"thirdQuartile\"><![CDATA[https://ad.doubleclick.net/ddm/activity/dc_oe=ChMI64zYxt3TyQIVy42PCh2nlgZoEAAYACCZ2ZAg;met=1;ecn1=1;etm1=0;eid1=960585;]]></Tracking>\n"
                    + "       <Tracking event=\"complete\"><![CDATA[https://ad.doubleclick.net/ddm/activity/dc_oe=ChMI64zYxt3TyQIVy42PCh2nlgZoEAAYACCZ2ZAg;met=1;ecn1=1;etm1=0;eid1=13;]]></Tracking>\n"
                    + "       <Tracking event=\"mute\"><![CDATA[https://ad.doubleclick.net/ddm/activity/dc_oe=ChMI64zYxt3TyQIVy42PCh2nlgZoEAAYACCZ2ZAg;met=1;ecn1=1;etm1=0;eid1=16;]]></Tracking>\n"
                    + "       <Tracking event=\"unmute\"><![CDATA[https://ad.doubleclick.net/ddm/activity/dc_oe=ChMI64zYxt3TyQIVy42PCh2nlgZoEAAYACCZ2ZAg;met=1;ecn1=1;etm1=0;eid1=149645;]]></Tracking>\n"
                    + "       <Tracking event=\"pause\"><![CDATA[https://ad.doubleclick.net/ddm/activity/dc_oe=ChMI64zYxt3TyQIVy42PCh2nlgZoEAAYACCZ2ZAg;met=1;ecn1=1;etm1=0;eid1=15;]]></Tracking>\n"
                    + "       <Tracking event=\"fullscreen\"><![CDATA[https://ad.doubleclick.net/ddm/activity/dc_oe=ChMI64zYxt3TyQIVy42PCh2nlgZoEAAYACCZ2ZAg;met=1;ecn1=1;etm1=0;eid1=19;]]></Tracking>\n"
                    + "      </TrackingEvents>\n";
    private static final String adMarkUp_part4 =
            "      <AdParameters><![CDATA[&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt; &lt;VAST xmlns:xsi=&quot;http://www.w3.org/2001/XMLSchema-instance&quot; xsi:noNamespaceSchemaLocation=&quot;vast.xsd&quot; version=&quot;2.0&quot;&gt;  &lt;Ad id=&quot;&quot;&gt;   &lt;Wrapper&gt;    &lt;AdSystem&gt;&lt;/AdSystem&gt;    &lt;VASTAdTagURI&gt;&lt;![CDATA[https://x4.vindicosuite.com/?lc=420877-883716&amp;t=x&amp;plid=v_PublisherInsertMacroOrValueHere_v&amp;ts=[timestamp]]]&gt;&lt;/VASTAdTagURI&gt;    &lt;Error&gt;&lt;![CDATA[]]&gt;&lt;/Error&gt;    &lt;Impression&gt;&lt;![CDATA[https://s0.2mdn.net/dot.gif]]&gt;&lt;/Impression&gt;    &lt;Creatives&gt;     &lt;Creative sequence=&quot;1&quot;&gt;      &lt;Linear&gt;       &lt;Duration&gt;00:00:30&lt;/Duration&gt;       &lt;TrackingEvents&gt;        &lt;Tracking event=&quot;start&quot;&gt;&lt;![CDATA[https://ad.doubleclick.net/ddm/activity/dc_oe=ChMI64zYxt3TyQIVy42PCh2nlgZoEAAYACCZ2ZAg;av=1;acvw=[VIEWABILITY];dc_rfl=[URL_SIGNALS];ecn1=0;etm1=0;eid1=210001;]]&gt;&lt;/Tracking&gt;        &lt;Tracking event=&quot;firstQuartile&quot;&gt;&lt;![CDATA[https://ad.doubleclick.net/ddm/activity/dc_oe=ChMI64zYxt3TyQIVy42PCh2nlgZoEAAYACCZ2ZAg;av=1;acvw=[VIEWABILITY];ecn1=0;etm1=0;eid1=210002;]]&gt;&lt;/Tracking&gt;        &lt;Tracking event=&quot;midpoint&quot;&gt;&lt;![CDATA[https://ad.doubleclick.net/ddm/activity/dc_oe=ChMI64zYxt3TyQIVy42PCh2nlgZoEAAYACCZ2ZAg;av=1;acvw=[VIEWABILITY];ecn1=0;etm1=0;eid1=210003;]]&gt;&lt;/Tracking&gt;        &lt;Tracking event=&quot;thirdQuartile&quot;&gt;&lt;![CDATA[https://ad.doubleclick.net/ddm/activity/dc_oe=ChMI64zYxt3TyQIVy42PCh2nlgZoEAAYACCZ2ZAg;av=1;acvw=[VIEWABILITY];ecn1=0;etm1=0;eid1=210004;]]&gt;&lt;/Tracking&gt;        &lt;Tracking event=&quot;complete&quot;&gt;&lt;![CDATA[https://ad.doubleclick.net/ddm/activity/dc_oe=ChMI64zYxt3TyQIVy42PCh2nlgZoEAAYACCZ2ZAg;av=1;acvw=[VIEWABILITY];ecn1=0;etm1=0;eid1=210005;]]&gt;&lt;/Tracking&gt;        &lt;Tracking event=&quot;mute&quot;&gt;&lt;![CDATA[https://ad.doubleclick.net/ddm/activity/dc_oe=ChMI64zYxt3TyQIVy42PCh2nlgZoEAAYACCZ2ZAg;av=1;acvw=[VIEWABILITY];ecn1=0;etm1=0;eid1=210006;]]&gt;&lt;/Tracking&gt;        &lt;Tracking event=&quot;unmute&quot;&gt;&lt;![CDATA[https://ad.doubleclick.net/ddm/activity/dc_oe=ChMI64zYxt3TyQIVy42PCh2nlgZoEAAYACCZ2ZAg;av=1;acvw=[VIEWABILITY];ecn1=0;etm1=0;eid1=210007;]]&gt;&lt;/Tracking&gt;        &lt;Tracking event=&quot;pause&quot;&gt;&lt;![CDATA[https://ad.doubleclick.net/ddm/activity/dc_oe=ChMI64zYxt3TyQIVy42PCh2nlgZoEAAYACCZ2ZAg;av=1;acvw=[VIEWABILITY];ecn1=0;etm1=0;eid1=210008;]]&gt;&lt;/Tracking&gt;        &lt;Tracking event=&quot;fullscreen&quot;&gt;&lt;![CDATA[https://ad.doubleclick.net/ddm/activity/dc_oe=ChMI64zYxt3TyQIVy42PCh2nlgZoEAAYACCZ2ZAg;av=1;acvw=[VIEWABILITY];ecn1=0;etm1=0;eid1=210009;]]&gt;&lt;/Tracking&gt;       &lt;/TrackingEvents&gt;       &lt;VideoClicks&gt;        &lt;ClickThrough&gt;&lt;![CDATA[https://adclick.g.doubleclick.net/pcs/click?xai=AKAOjsuJr8W9S-YevlPn3nxI6Tx0Ie5x6EwoCqOY8-ux1pwteLk3GECkeif8-F9Ymd6WNx_6FUgvJLCbLPfjh2w0ekPyL72N0yYpJwJsy_OwQQ&amp;sig=Cg0ArKJSzBLOeFgW0p5dEAE&amp;urlfix=1&amp;adurl=http://www.currys.co.uk]]&gt;&lt;/ClickThrough&gt;       &lt;/VideoClicks&gt;      &lt;/Linear&gt;     &lt;/Creative&gt;    &lt;/Creatives&gt;    &lt;Extensions&gt;     &lt;Extension type=&quot;dart&quot;&gt;&lt;AdServingData&gt;  &lt;DeliveryData&gt;   &lt;GeoData&gt;&lt;![CDATA[ct=IN&st=&city=7259&dma=0&zp=&bw=4]]&gt;&lt;/GeoData&gt;  &lt;/DeliveryData&gt; &lt;/AdServingData&gt; &lt;/Extension&gt;     &lt;Extension type=&quot;activeview&quot;&gt;&lt;CustomTracking&gt;  &lt;Tracking event=&quot;viewable_impression&quot;&gt;&lt;![CDATA[https://pagead2.googlesyndication.com/activeview?id=lidarv&amp;acvw=[VIEWABILITY]&amp;avi=BNLaiIbdqVqv4CsubvgSnrZrABgAAAAAQATgB4AQCiAXf7fgDoAY6]]&gt;&lt;/Tracking&gt;  &lt;Tracking event=&quot;measurable_impression&quot;&gt;&lt;![CDATA[https://pagead2.googlesyndication.com/activeview?id=lidarv&amp;acvw=[VIEWABILITY]&amp;avi=BNLaiIbdqVqv4CsubvgSnrZrABgAAAAAQATgB4AQCiAXf7fgDoAY6&amp;avm=1]]&gt;&lt;/Tracking&gt;  &lt;Tracking event=&quot;viewable_impression&quot;&gt;&lt;![CDATA[https://ad.doubleclick.net/ddm/activity/dc_oe=ChMI64zYxt3TyQIVy42PCh2nlgZoEAAYACCZ2ZAg;av=1;acvw=[VIEWABILITY];ecn1=0;etm1=0;eid1=200000;]]&gt;&lt;/Tracking&gt; &lt;/CustomTracking&gt; &lt;/Extension&gt;     &lt;Extension type=&quot;geo&quot;&gt;&lt;Country&gt;IN&lt;/Country&gt; &lt;Bandwidth&gt;4&lt;/Bandwidth&gt; &lt;BandwidthKbps&gt;16340&lt;/BandwidthKbps&gt; &lt;/Extension&gt;    &lt;/Extensions&gt;   &lt;/Wrapper&gt;  &lt;/Ad&gt; &lt;/VAST&gt; ]]>"
                    + "</AdParameters>\n";
    private static final String videoClicks =
            "      <VideoClicks>\n" + "       <ClickThrough><![CDATA[http://www.currys.co.uk]]></ClickThrough>\n"
                    + "       <ClickTracking><![CDATA[https://adclick.g.doubleclick.net/pcs/click?xai=AKAOjsuJr8W9S-YevlPn3nxI6Tx0Ie5x6EwoCqOY8-ux1pwteLk3GECkeif8-F9Ymd6WNx_6FUgvJLCbLPfjh2w0ekPyL72N0yYpJwJsy_OwQQ&sig=Cg0ArKJSzBLOeFgW0p5dEAE&urlfix=1&adurl=]]></ClickTracking>\n"
                    + "      </VideoClicks>\n";
    private static final String mediaFiles =
            "      <MediaFiles>\n"
                    + "       <MediaFile delivery=\"progressive\" width=\"640\" height=\"480\" type=\"application/x-shockwave-flash\" apiFramework=\"VPAID\"><![CDATA[https://imasdk.googleapis.com/flash/sdkloader/vpaid2video.swf?adTagUrl=embedded&embedAdsResponse=1]]></MediaFile>\n"
                    + "      </MediaFiles>\n";
    private static final String vastAdTagUrl =
            "<VASTAdTagURL>\n"
            + "<![CDATA[http://www.secondaryadserver.com/ad/tag/parameters?time=1234567]]>\n"
            + "</VASTAdTagURL>\n";
    private static final String adMarkUp_part5 =
            "     </Linear>\n"
                    + "    </Creative>\n"
                    + "   </Creatives>\n"
                    + "   <Extensions>\n"
                    + "    <Extension type=\"dart\">"
                    + "      <AdServingData>\n"
                    + "       <DeliveryData>\n"
                    + "         <GeoData><![CDATA[ct=IN&st=&city=7259&dma=0&zp=&bw=4]]></GeoData>\n"
                    + "       </DeliveryData>\n"
                    + "      </AdServingData>\n"
                    + "    </Extension>\n"
                    + "    <Extension type=\"geo\"><Country>IN</Country>\n"
                    + "      <Bandwidth>4</Bandwidth>\n"
                    + "      <BandwidthKbps>16340</BandwidthKbps>\n"
                    + "    </Extension>\n"
                    + "   </Extensions>\n";
    private static final String inlineCloseTag =
            "  </InLine>\n";
    private static final String wrapperCloseTag =
            "  </Wrapper>\n";
    private static final String adMarkUp_part6 =
            " </Ad>\n"
                    + "</VAST>\n";

    private static final String impression = "<Impression><![CDATA[http://some/beacon/url/?m=18]]></Impression>";
    private static final String error = "<Error><![CDATA[http://some/beacon/url/?m=99&action=vast-error&label=[ERRORCODE]]]></Error>";
    private static final String start = "<Tracking event=\"start\"><![CDATA[http://some/beacon/url/?m=10]]></Tracking>";
    private static final String billing = "<Tracking event=\"start\"><![CDATA[http://some/beacon/url/?b=${WIN_BID}${DEAL_GET_PARAM}]]></Tracking>";
    private static final String firstQuartile = "<Tracking event=\"firstQuartile\"><![CDATA[http://some/beacon/url/?m=12&q=1&mid=video]]></Tracking>";
    private static final String midPoint = "<Tracking event=\"midpoint\"><![CDATA[http://some/beacon/url/?m=12&q=2&mid=video]]></Tracking>";
    private static final String thirdQuartile = "<Tracking event=\"thirdQuartile\"><![CDATA[http://some/beacon/url/?m=12&q=3&mid=video]]></Tracking>";
    private static final String complete = "<Tracking event=\"complete\"><![CDATA[http://some/beacon/url/?m=13&mid=video]]></Tracking>";
    private static final String clickTracking = "<ClickTracking><![CDATA[http://some/click/url/]]></ClickTracking>";
    private static final String beaconclickTracking = "<ClickTracking><![CDATA[http://some/beacon/url/?m=8]]></ClickTracking>";
    private static final String videoClicksTag = "<VideoClicks>";
    private static final String trackingEventsTagCheck = "<TrackingEvents>";
    private static final short processedSlotId = 9;

    @BeforeTest
    @BeforeClass
    public static void setUp() {
        try {
            Formatter.init();
        } catch (Exception e) {
        }
        countryIdString = String.valueOf(countryId);

        sasParams = new SASRequestParameters();
        sasParams.setSiteId(siteId);
        sasParams.setSiteIncId(siteIncId);
        sasParams.setCountryId(countryId);
        sasParams.setSiteContentType(ContentType.PERFORMANCE);

        sasParamsFS = new SASRequestParameters();
        sasParamsFS.setSiteId(siteId);
        sasParamsFS.setSiteIncId(siteIncId);
        sasParamsFS.setCountryId(countryId);
        sasParamsFS.setSiteContentType(ContentType.FAMILY_SAFE);


        siteAdvertiserBlocklistEntity = new IXBlocklistEntity("s-a", null, null, null, 1, null);
        siteIndustryBlocklistEntity = new IXBlocklistEntity("s-i", null, null, null, 1, null);
        siteCreativeAttributesBlocklistEntity = new IXBlocklistEntity("s-c", null, null, null, 1, null);
        countryAdvertiserBlocklistEntity = new IXBlocklistEntity("c-a", null, null, null, 1, null);
        countryIndustryBlocklistEntity = new IXBlocklistEntity("c-i", null, null, null, 1, null);
        countryCreativeAttributesBlocklistEntity = new IXBlocklistEntity("c-c", null, null, null, 1, null);
        emptySiteAdvertiserBlocklistEntity = new IXBlocklistEntity("should not be present", null, null, null, 0, null);
        emptySiteIndustryBlocklistEntity = new IXBlocklistEntity("should not be present", null, null, null, 0, null);
        emptySiteCreativeAttributesBlocklistEntity = new IXBlocklistEntity("should not be present", null, null, null,
                0, null);

        repositoryHelperWithOnlySiteEntries = createNiceMock(RepositoryHelper.class);
        repositoryHelperWithOnlyCountryEntries = createNiceMock(RepositoryHelper.class);
        repositoryHelperWithBothSiteAndCountryEntries = createNiceMock(RepositoryHelper.class);
        repositoryHelperWithNoEntries = createNiceMock(RepositoryHelper.class);
        repositoryHelperWithEmptyEntries = createNiceMock(RepositoryHelper.class);
        repositoryHelperWithQuerySlotSizeMapRepository = createNiceMock(RepositoryHelper.class);

        mockRepositoryHelper = createMock(RepositoryHelper.class);
        expect(repositoryHelperWithOnlySiteEntries.queryIXBlocklistRepository(siteId, IXBlocklistKeyType.SITE,
                IXBlocklistType.ADVERTISERS)).andReturn(siteAdvertiserBlocklistEntity).anyTimes();
        expect(repositoryHelperWithOnlySiteEntries.queryIXBlocklistRepository(siteId, IXBlocklistKeyType.SITE,
                IXBlocklistType.INDUSTRY_IDS)).andReturn(siteIndustryBlocklistEntity).anyTimes();
        expect(repositoryHelperWithOnlySiteEntries.queryIXBlocklistRepository(siteId, IXBlocklistKeyType.SITE,
                IXBlocklistType.CREATIVE_ATTRIBUTE_IDS)).andReturn(siteCreativeAttributesBlocklistEntity).anyTimes();

        expect(repositoryHelperWithOnlyCountryEntries.queryIXBlocklistRepository(countryIdString,
                IXBlocklistKeyType.COUNTRY, IXBlocklistType.ADVERTISERS)).andReturn(countryAdvertiserBlocklistEntity)
                        .anyTimes();
        expect(repositoryHelperWithOnlyCountryEntries.queryIXBlocklistRepository(countryIdString,
                IXBlocklistKeyType.COUNTRY, IXBlocklistType.INDUSTRY_IDS)).andReturn(countryIndustryBlocklistEntity)
                        .anyTimes();
        expect(repositoryHelperWithOnlyCountryEntries.queryIXBlocklistRepository(countryIdString,
                IXBlocklistKeyType.COUNTRY, IXBlocklistType.CREATIVE_ATTRIBUTE_IDS))
                        .andReturn(countryCreativeAttributesBlocklistEntity).anyTimes();

        expect(repositoryHelperWithBothSiteAndCountryEntries.queryIXBlocklistRepository(siteId, IXBlocklistKeyType.SITE,
                IXBlocklistType.ADVERTISERS)).andReturn(siteAdvertiserBlocklistEntity).anyTimes();
        expect(repositoryHelperWithBothSiteAndCountryEntries.queryIXBlocklistRepository(siteId, IXBlocklistKeyType.SITE,
                IXBlocklistType.INDUSTRY_IDS)).andReturn(siteIndustryBlocklistEntity).anyTimes();
        expect(repositoryHelperWithBothSiteAndCountryEntries.queryIXBlocklistRepository(siteId, IXBlocklistKeyType.SITE,
                IXBlocklistType.CREATIVE_ATTRIBUTE_IDS)).andReturn(siteCreativeAttributesBlocklistEntity).anyTimes();
        expect(repositoryHelperWithBothSiteAndCountryEntries.queryIXBlocklistRepository(countryIdString,
                IXBlocklistKeyType.COUNTRY, IXBlocklistType.ADVERTISERS)).andReturn(countryAdvertiserBlocklistEntity)
                        .anyTimes();
        expect(repositoryHelperWithBothSiteAndCountryEntries.queryIXBlocklistRepository(countryIdString,
                IXBlocklistKeyType.COUNTRY, IXBlocklistType.INDUSTRY_IDS)).andReturn(countryIndustryBlocklistEntity)
                        .anyTimes();
        expect(repositoryHelperWithBothSiteAndCountryEntries.queryIXBlocklistRepository(countryIdString,
                IXBlocklistKeyType.COUNTRY, IXBlocklistType.CREATIVE_ATTRIBUTE_IDS))
                        .andReturn(countryCreativeAttributesBlocklistEntity).anyTimes();

        expect(repositoryHelperWithEmptyEntries.queryIXBlocklistRepository(siteId, IXBlocklistKeyType.SITE,
                IXBlocklistType.ADVERTISERS)).andReturn(emptySiteAdvertiserBlocklistEntity).anyTimes();
        expect(repositoryHelperWithEmptyEntries.queryIXBlocklistRepository(siteId, IXBlocklistKeyType.SITE,
                IXBlocklistType.INDUSTRY_IDS)).andReturn(emptySiteIndustryBlocklistEntity).anyTimes();
        expect(repositoryHelperWithEmptyEntries.queryIXBlocklistRepository(siteId, IXBlocklistKeyType.SITE,
                IXBlocklistType.CREATIVE_ATTRIBUTE_IDS)).andReturn(emptySiteCreativeAttributesBlocklistEntity)
                        .anyTimes();

        replay(repositoryHelperWithOnlySiteEntries, repositoryHelperWithOnlyCountryEntries,
                repositoryHelperWithBothSiteAndCountryEntries, repositoryHelperWithNoEntries,
                repositoryHelperWithEmptyEntries, mockRepositoryHelper, repositoryHelperWithQuerySlotSizeMapRepository);
    }

    @DataProvider(name = "BlocklistDataProvider")
    public Object[][] blocklistDataProvider() {
        return new Object[][] {
                {"testOnlySiteBlocklists", sasParams, repositoryHelperWithOnlySiteEntries,
                        ImmutableList.of("blk1", "s-a", "s-i", "s-c")},
                {"testOnlyCountryBlocklists", sasParams, repositoryHelperWithOnlyCountryEntries,
                        ImmutableList.of("blk1", "c-a", "c-i", "c-c")},
                {"testBothSiteAndCountryBlocklists", sasParams, repositoryHelperWithBothSiteAndCountryEntries,
                        ImmutableList.of("blk1", "s-a", "s-i", "s-c")},
                {"testOnlyGlobalBlocklistsPERF", sasParams, repositoryHelperWithNoEntries,
                        ImmutableList.of("blk1", "InMobiPERFAdv", "InMobiPERFInd", "InMobiPERFCre")},
                {"testOnlyGlobalBlocklistsFS", sasParamsFS, repositoryHelperWithNoEntries,
                        ImmutableList.of("blk1", "InMobiFSAdv", "InMobiFSInd", "InMobiFSCre")},
                {"testEmptySiteBlocklists", sasParams, repositoryHelperWithEmptyEntries, ImmutableList.of("blk1")},};
    }

    @Test(dataProvider = "BlocklistDataProvider")
    public void testGetBlocklists(final String testCaseName, final SASRequestParameters sasParams,
            final RepositoryHelper repositoryHelper, final List<String> expectedBlocklists) throws Exception {
        assertEquals(IXAdNetworkHelper.getBlocklists(sasParams, repositoryHelper, null), expectedBlocklists);
    }

    @DataProvider(name = "vastPositiveAdBuildingDataProvider")
    public Object[][] vastPositiveAdBuildingDataProvider() {
        return new Object[][] {
                {"testInmobiAdTrackers_inline_withClickTrackingNode", clickUrl, beaconUrl, adMarkUp_part1+inlineOpenTag+adMarkUp_part2+errorTag+
                        impressionTag+adMarkUp_part3+trackingEventsTag+adMarkUp_part4+videoClicks+mediaFiles+
                        adMarkUp_part5+inlineCloseTag+adMarkUp_part6, impression, error, start, billing, firstQuartile,
                        midPoint, thirdQuartile, complete, clickTracking, beaconclickTracking, videoClicksTag, trackingEventsTagCheck},
                {"testInmobiAdTrackers_inline_withoutClickTrackingNode", clickUrl, beaconUrl, adMarkUp_part1+inlineOpenTag+adMarkUp_part2+errorTag+
                        impressionTag+adMarkUp_part3+trackingEventsTag+adMarkUp_part4+mediaFiles+
                        adMarkUp_part5+inlineCloseTag+adMarkUp_part6, impression, error, start, billing, firstQuartile,
                        midPoint, thirdQuartile, complete, clickTracking, beaconclickTracking, videoClicksTag, trackingEventsTagCheck},
                {"testInmobiAdTrackers_wrapper_withClickTrackingNode", clickUrl, beaconUrl, adMarkUp_part1+wrapperOpenTag+adMarkUp_part2+errorTag+
                        impressionTag+adMarkUp_part3+trackingEventsTag+adMarkUp_part4+videoClicks+vastAdTagUrl+
                        adMarkUp_part5+wrapperCloseTag+adMarkUp_part6, impression, error, start, billing, firstQuartile,
                        midPoint, thirdQuartile, complete, clickTracking, beaconclickTracking, videoClicksTag, trackingEventsTagCheck},
                {"testInmobiAdTrackers_wrapper_withoutClickTrackingNode", clickUrl, beaconUrl, adMarkUp_part1+wrapperOpenTag+adMarkUp_part2+errorTag+
                        impressionTag+adMarkUp_part3+trackingEventsTag+adMarkUp_part4+vastAdTagUrl+
                        adMarkUp_part5+wrapperCloseTag+adMarkUp_part6, impression, error, start, billing, firstQuartile,
                        midPoint, thirdQuartile, complete, clickTracking, beaconclickTracking, videoClicksTag, trackingEventsTagCheck},
        };
    }

    @Test(dataProvider = "vastPositiveAdBuildingDataProvider")
    public void testVastPositiveAdBuilding(final String testName, final String clickUrl, final String beaconUrl,
            final String adMarkup, final String impression, final String error, final String start,
            final String billing, final String firstQuartile, final String midPoint, final String thirdQuartile,
            final String complete, final String clickTracking, final String beaconClickTracking,
            final String videoClicksTag, final String trackingEventsTagCheck) throws Exception {

        try {
            final String responseContent = IXAdNetworkHelper.pureVastAdBuilding(adMarkup, beaconUrl, clickUrl, false);
            assertTrue(responseContent.contains(impression));
            assertTrue(responseContent.contains(error));
            assertTrue(responseContent.contains(start));
            assertTrue(responseContent.contains(billing));
            assertTrue(responseContent.contains(firstQuartile));
            assertTrue(responseContent.contains(midPoint));
            assertTrue(responseContent.contains(thirdQuartile));
            assertTrue(responseContent.contains(complete));
            assertTrue(responseContent.contains(clickTracking));
            assertTrue(responseContent.contains(beaconClickTracking));
            assertTrue(responseContent.contains(videoClicksTag));
            assertTrue(responseContent.contains(trackingEventsTagCheck));
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    @DataProvider(name = "vastNegativeAdBuildingDataProvider")
    public Object[][] vastNegativeAdBuildingDataProvider() {
        return new Object[][] {
                {"testInmobiAdTrackers_withoutInlineAndWrapper", clickUrl, beaconUrl, adMarkUp_part1+adMarkUp_part2+errorTag+
                        impressionTag+adMarkUp_part3+trackingEventsTag+adMarkUp_part4+videoClicks+mediaFiles+ adMarkUp_part5+adMarkUp_part6},
                {"testInmobiAdTrackers_inline_withoutTrackingEvent", clickUrl, beaconUrl, adMarkUp_part1+inlineOpenTag+adMarkUp_part2+errorTag+
                        impressionTag+adMarkUp_part3+adMarkUp_part4+videoClicks+mediaFiles+adMarkUp_part5+inlineCloseTag+adMarkUp_part6},
                {"testInmobiAdTrackers_wrapper_withoutTrackingEvent", clickUrl, beaconUrl, adMarkUp_part1+wrapperOpenTag+adMarkUp_part2+errorTag+
                        impressionTag+adMarkUp_part3+adMarkUp_part4+videoClicks+vastAdTagUrl+ adMarkUp_part5+wrapperCloseTag+adMarkUp_part6},
                {"testInmobiAdTrackers_without_Inline_Wrapper_And_TrackingEvent", clickUrl, beaconUrl, adMarkUp_part1+adMarkUp_part2+errorTag+
                        impressionTag+adMarkUp_part3+adMarkUp_part4+videoClicks+vastAdTagUrl+ adMarkUp_part5+adMarkUp_part6},
        };
    }

    @Test(dataProvider = "vastNegativeAdBuildingDataProvider")
    public void testVastNegativeAdBuilding(final String testName, final String clickUrl, final String beaconUrl,
            final String adMarkup) throws Exception {
        try {
            IXAdNetworkHelper.pureVastAdBuilding(adMarkup, beaconUrl, clickUrl, false);
            Assert.assertTrue(false);
        } catch (final Exception e) {
            Assert.assertTrue(true);
        }

    }

    @DataProvider(name = "vast template check with array replacement")
    public Object[][] paramVastTemplateCheckWithArrayReplacement() {
        final String viewabilityTracker = "http://third_party_viewability_tracker";
        final String audienceVerificiation = "http://third_party_audience_verification";
        final String thirdPartyImpressionTracker = "http://third_party_impression_tracker";
        final String thirdPartyClickTracker = "http://third_party_click_tracker";
        final String billingUrlTracker = beaconUrl + BILLING_BEACON_SUFFIX;

        Map<String, String> trackerMapWithOutEmptyValue = new HashMap<>();
        trackerMapWithOutEmptyValue.put(VelocityTemplateFieldConstants.VIEWABILITY_TRACKER, viewabilityTracker);
        trackerMapWithOutEmptyValue.put(VelocityTemplateFieldConstants.AUDIENCE_VERIFICATION_TRACKER, audienceVerificiation);
        trackerMapWithOutEmptyValue.put(VelocityTemplateFieldConstants.THIRD_PARTY_IMPRESSION_TRACKER, thirdPartyImpressionTracker);
        trackerMapWithOutEmptyValue.put(VelocityTemplateFieldConstants.THIRD_PARTY_CLICK_TRACKER, thirdPartyClickTracker);
        final String[] trackerArr1a = {billingUrlTracker, audienceVerificiation, thirdPartyImpressionTracker};
        final String expectedOutput1a = getExpectedStr(trackerArr1a);
        final String[] trackerArr1b = {thirdPartyClickTracker};
        final String expectedOutput1b = getExpectedStr(trackerArr1b);

        Map<String, String> trackerMapWithEmptyAndNullValue = new HashMap<>();
        trackerMapWithEmptyAndNullValue.put(VelocityTemplateFieldConstants.VIEWABILITY_TRACKER, "   ");
        trackerMapWithEmptyAndNullValue.put(VelocityTemplateFieldConstants.AUDIENCE_VERIFICATION_TRACKER, "");
        trackerMapWithEmptyAndNullValue.put(VelocityTemplateFieldConstants.THIRD_PARTY_IMPRESSION_TRACKER, "");
        trackerMapWithEmptyAndNullValue.put(VelocityTemplateFieldConstants.THIRD_PARTY_CLICK_TRACKER, null);
        final String[] trackerArr2a = {billingUrlTracker};
        final String expectedOutput2a = getExpectedStr(trackerArr2a);
        final String[] trackerArr2b = {};
        final String expectedOutput2b = getExpectedStr(trackerArr2b);

        Map<String, String> trackerMapWithEmptyNullAndValidValue = new HashMap<>();
        trackerMapWithEmptyNullAndValidValue.put(VelocityTemplateFieldConstants.VIEWABILITY_TRACKER, viewabilityTracker);
        trackerMapWithEmptyNullAndValidValue.put(VelocityTemplateFieldConstants.AUDIENCE_VERIFICATION_TRACKER, "");
        trackerMapWithEmptyNullAndValidValue.put(VelocityTemplateFieldConstants.THIRD_PARTY_IMPRESSION_TRACKER, " ");
        trackerMapWithEmptyNullAndValidValue.put(VelocityTemplateFieldConstants.THIRD_PARTY_CLICK_TRACKER, null);
        final String[] trackerArr3a = {billingUrlTracker};
        final String expectedOutput3a = getExpectedStr(trackerArr3a);
        final String[] trackerArr3b = {};
        final String expectedOutput3b = getExpectedStr(trackerArr3b);

        Map<String, String> emptyTrackerMap = new HashMap<>();
        Map<String, String> expectedTrackerMapNull = new HashMap<>();
        expectedTrackerMapNull.put(VelocityTemplateFieldConstants.VIEWABILITY_TRACKER, null);
        expectedTrackerMapNull.put(VelocityTemplateFieldConstants.AUDIENCE_VERIFICATION_TRACKER, null);
        expectedTrackerMapNull.put(VelocityTemplateFieldConstants.THIRD_PARTY_IMPRESSION_TRACKER, null);
        expectedTrackerMapNull.put(VelocityTemplateFieldConstants.THIRD_PARTY_CLICK_TRACKER, null);
        final String[] trackerArr4a = {billingUrlTracker};
        final String expectedOutput4a = getExpectedStr(trackerArr4a);
        final String[] trackerArr4b = {};
        final String expectedOutput4b = getExpectedStr(trackerArr4b);

        return new Object[][] {
            {"trackerMapWithOutEmptyValue", trackerMapWithOutEmptyValue, false, expectedOutput1a, expectedOutput1b},
            {"trackerMapWithEmptyAndNullValue", trackerMapWithEmptyAndNullValue, false, expectedOutput2a, expectedOutput2b},
            {"trackerMapWithEmptyNullAndValidValue", trackerMapWithEmptyNullAndValidValue, false, expectedOutput3a, expectedOutput3b},
            {"EmptyTrackerMap", emptyTrackerMap, false, expectedOutput4a, expectedOutput4b},
            {"trackerMapWithOutEmptyValue", trackerMapWithOutEmptyValue, true, expectedOutput1a, expectedOutput1b},
            {"trackerMapWithEmptyAndNullValue", trackerMapWithEmptyAndNullValue, true, expectedOutput2a, expectedOutput2b},
            {"trackerMapWithEmptyNullAndValidValue", trackerMapWithEmptyNullAndValidValue, true, expectedOutput3a, expectedOutput3b},
            {"EmptyTrackerMap", emptyTrackerMap, true, expectedOutput4a, expectedOutput4b}
        };
    }

    private String getExpectedStr(final String[] trackerArray) {
        for (int i=0 ; i<trackerArray.length; i++) {
            trackerArray[i] = StringEscapeUtils.escapeJavaScript(trackerArray[i]);
        }
        return trackerArray.length > 0 ? ARRAY_PREFIX + StringUtils.join(trackerArray, "\", \"") + ARRAY_SUFFIX : EMPTY_ARRAY;
    }


    @org.testng.annotations.Test(dataProvider = "vast template check with array replacement")
    public void testVastTemplateCheckWithArrayReplacement(final String testName, final  Map<String, String> trackerMap,
            final boolean isRewarded, final String expectedAudienceImp, final String expectedClick) throws Exception {
        Injector injector = Guice.createInjector(new TemplateModule());
        templateConfiguration = injector.getInstance(TemplateConfiguration.class);
        final String response = IXAdNetworkHelper.videoAdBuilding(templateConfiguration.getTemplateTool(), sasParams,
            repositoryHelperWithQuerySlotSizeMapRepository, processedSlotId, beaconUrl, clickUrl, vast_adm, winurl,
            isRewarded, trackerMap, false);
        System.out.println(response);
        Assert.assertTrue(response.contains(expectedAudienceImp));
        Assert.assertTrue(response.contains(expectedClick));
    }
    @DataProvider(name = "Audience verification Replace Macro")
    public Object[][] paramAudienceVerificationMacroReplacement() {
        final String audienceVerificiation = "http://secure-cert.imrworldwide.com/cgi-bin/m?ci=nlsnci535&am=3&at=view&rt=banner&st=image&ca=nlsn13134&cr=crtve&pc=inmobi_plc0001&ce=inmobi&c9=devid,$USER_ID_SHA256_HASHED&c13=asid,P29D9B460-8979-4826-803F-9C7B137584F5&imp_cb=$IMP_CB&limit_ad_tracking=$LIMIT_AD_TRACKING";
        final String[] deviceId = new String[7];
        deviceId[0] = "ida";
        deviceId[1] = "gpid";
        deviceId[2] = "o1";
        deviceId[3] = "so1";
        deviceId[4] = "um5";
        deviceId[5] = "udid";
        deviceId[6] = "idv";
        final String impCb = "abcd-ufgh-sed";
        final Object obj[][] = new Object[128][5];

        for (int i=0; i<(1<<deviceId.length); i++) {
            final String tmpDeviceId[] = new String[7];
            for (int j=0; j<deviceId.length; j++) {
                if (0 != (i & (1<<j))) {
                    tmpDeviceId[j] = deviceId[j];
                }
            }
            obj[i][0] = "MacroReplace";
            obj[i][1] = audienceVerificiation;
            obj[i][2] = impCb;
            obj[i][3] = (0 == i%2) ? true : false;
            obj[i][4] = tmpDeviceId;
        }
        return obj;
    }

    @org.testng.annotations.Test(dataProvider = "Audience verification Replace Macro")
    public void testAudienceVerificationMacroReplacement(final String testName,String audienceVerification,
        final String impCb, final boolean trackkingAllowed, final String[] deviceId) throws Exception {

        CasInternalRequestParameters casInt = new CasInternalRequestParameters();
        SASRequestParameters sasParam = new SASRequestParameters();

        casInt.setAuctionId(impCb);
        casInt.setTrackingAllowed(trackkingAllowed);
        casInt.setUidIFA(deviceId[0]);
        casInt.setGpid(deviceId[1]);
        casInt.setUidO1(deviceId[2]);
        casInt.setUidSO1(deviceId[3]);
        casInt.setUidMd5(deviceId[4]);
        casInt.setUid(deviceId[5]);
        casInt.setUidIFV(deviceId[6]);

        final MacroData macroData = new MacroData(casInt, sasParam);
        final String response = IXAdNetworkHelper.replaceAudienceVerificationTrackerMacros(audienceVerification, macroData.getMacroMap());
        final String limitAdTracking = trackkingAllowed ? "0" : "1";
        String userId = "";
        for (int i=0; i<deviceId.length; i++){
            if (StringUtils.isNotBlank(deviceId[i])) {
                userId = deviceId[i];
                break;
            }
        }
        final String UserIdSha256Hashed = DigestUtils.sha256Hex(userId);
        Assert.assertTrue(response.contains("devid," + UserIdSha256Hashed));
        Assert.assertTrue(response.contains("imp_cb=" + impCb.replace("-", "")));
        Assert.assertTrue(response.contains("limit_ad_tracking=" + limitAdTracking));

    }

}
