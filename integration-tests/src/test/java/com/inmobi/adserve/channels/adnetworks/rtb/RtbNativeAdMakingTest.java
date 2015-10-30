package com.inmobi.adserve.channels.adnetworks.rtb;


import static com.inmobi.adserve.channels.util.config.GlobalConstant.CPM;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.mockStaticNice;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.resetAll;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.commons.configuration.Configuration;
import org.apache.velocity.tools.generic.MathTool;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.support.membermodification.MemberMatcher;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.adpool.RequestedAdType;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.IPRepository;
import com.inmobi.adserve.channels.api.NativeResponseMaker;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.Utils.TestUtils;
import com.inmobi.adserve.channels.util.demand.enums.SecondaryAdFormatConstraints;
import com.inmobi.casthrift.rtb.App;
import com.inmobi.casthrift.rtb.Bid;
import com.inmobi.casthrift.rtb.BidRequest;
import com.inmobi.casthrift.rtb.BidResponse;
import com.inmobi.casthrift.rtb.SeatBid;
import com.inmobi.template.config.DefaultConfiguration;
import com.inmobi.template.config.DefaultGsonDeserializerConfiguration;
import com.inmobi.template.formatter.TemplateDecorator;
import com.inmobi.template.formatter.TemplateManager;
import com.inmobi.template.formatter.TemplateParser;
import com.inmobi.template.gson.GsonManager;
import com.inmobi.template.interfaces.TemplateConfiguration;
import com.inmobi.template.tool.ToolsImpl;

/**
 * Created by ishanbhatnagar on 12/12/14. Adheres to the following Native contract: InMobi OpenRTB v2.x Extension for
 * Native Ads v1.1
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({RtbAdNetwork.class, NativeAdTemplateEntity.class, InspectorStats.class, Formatter.class})
public class RtbNativeAdMakingTest {
    private static final String LAYOUT_CONSTRAINT_3 = "layoutConstraint.3"; // Stream
    private static final String LAYOUT_CONSTRAINT_2 = "layoutConstraint.2"; // Feed
    private static final String LAYOUT_CONSTRAINT_1 = "layoutConstraint.1"; // Icon
    private static final String INM_TAG_A056 = "inmTag.a056";
    private static RtbAdNetwork rtbAdNetwork;
    private static SASRequestParameters mockSASRequestParameters;
    private static ChannelSegmentEntity mockChannelSegmentEntity;
    private static Configuration mockConfig;
    private static BidRequest mockBidRequest;
    private static App mockApp;

    private static String advertiserName = "advertiserName";
    private static long placementId = 94L;
    private static String impressionId = "impressionId";
    private static String externalSiteKey = "externalSiteKey";
    private static String nUrl = "responseNurl";
    private static String namespace = "namespace";
    private static Long adgroupIncId = 123L;
    private static Long siteIncId = 456L;

    // templateGuId = 6435704039925181184
    private static String tangoTemplateContent = "## Subtitle shouldn''t be more than 100 characters.\n"
        + "#set($subtitle = $tool.jpath($first, \"imNative.creative.description\")."
        + "get(\"text\").replaceAll(\"\\s+\", \" \"))\n" + "#if ($subtitle.length() > 100)\n"
        + "#set($subtitle = \"$subtitle.substring(0, 97)...\")\n" + "#end\n"
        + "#set ($icons = $tool.jpath($first, \"imNative.creative\").get(\"icon\"))\n" + "#foreach( $icon in $icons)\n"
        + "#set ($width = $icon.get(\"width\"))\n" + "## Try to match the exact width.\n" + "#if ($width == 75)\n"
        + "#set ($sel_icon = $icon)\n" + "#break\n" + "#end\n" + "#if ($width >150 && $width <= 300)\n"
        + "#if (!$tool.isNonNull($h_icon))\n" + "#set ($h_icon = $icon)\n" + "#end\n"
        + "#elseif ($width > 75 && $width <= 150)\n" + "#if (!$tool.isNonNull($m_icon))\n" + "#set ($m_icon = $icon)\n"
        + "#end\n" + "#elseif($width > 37 && $width <= 75)\n" + "#if (!$tool.isNonNull($l_icon))\n"
        + "#set ($l_icon = $icon)\n" + "#end\n" + "#end\n" + "#if ($h_icon && $m_icon && $l_icon)\n" + "#break\n"
        + "#end\n" + "#end\n" + "#if ($tool.isNonNull($l_icon))\n" + "#set ($sel_icon = $l_icon)\n"
        + "#elseif ($tool.isNonNull($m_icon))\n" + "#set ($sel_icon = $m_icon)\n" + "#elseif ($h_icon)\n"
        + "#set ($sel_icon = $h_icon)\n" + "#else\n" + "## No matching icon, pick first one.\n"
        + "#set ($sel_icon = $icons.get(0))\n" + "#end\n"
        + "#set ($screenshots = $tool.jpath($first, \"imNative.creative\").get(\"image\"))\n"
        + "#foreach( $screenshot in $screenshots)\n" + "#set ($width = $screenshot.get(\"width\"))\n"
        + "#set ($height = $screenshot.get(\"height\"))\n"
        + "#set ($ar = $math.toDouble($math.div($math.toDouble($math.floor($math.mul($math.div($width, $height), 100))), 100)))\n"
        + "#set($same_ar_screenshots = [])\n" + "#if ($ar == 1.5)\n" + "#if ($width == 480)\n"
        + "#set ($sel_screenshot = $screenshot)\n" + "#break\n" + "#end\n"
        + "#set($temp_screenshot = $same_ar_screenshots.add($screenshot))\n" + "#if ($width > 960 && $width <= 1200)\n"
        + "#if (!$tool.isNonNull($h_screenshot))\n" + "#set ($h_screenshot = $screenshot)\n" + "#end\n"
        + "#elseif ($width > 480 && $width <= 960)\n" + "#if (!$tool.isNonNull($m_screenshot))\n"
        + "#set ($m_screenshot = $screenshot)\n" + "#end\n" + "#elseif ($width > 240 && $width <= 480)\n"
        + "#if (!$tool.isNonNull($l_screenshot))\n" + "#set ($l_screenshot = $screenshot)\n" + "#end\n" + "#end\n"
        + "#if ($h_screenshot && $m_screenshot && $l_screenshot)\n" + "#break\n" + "#end\n" + "#end\n" + "#end\n"
        + "##If exact match didn't happen then go for resolution checks.\n"
        + "#if (!$tool.isNonNull($sel_screenshot))\n" + "#if ($tool.isNonNull($m_screenshot))\n"
        + "#set ($sel_screenshot = $m_screenshot)\n" + "#elseif ($tool.isNonNull($h_screenshot))\n"
        + "#set ($sel_screenshot = $h_screenshot)\n" + "#elseif ($tool.isNonNull($l_screenshot))\n"
        + "#set ($sel_screenshot = $l_screenshot)\n" + "#else\n"
        + "#set ($sel_screenshot = $same_ar_screenshots.get(0))\n" + "#end\n" + "#end\n" + "#set($x_icon={})\n"
        + "#set($x_icon.w=$sel_icon.width)\n" + "#set($x_icon.h=$sel_icon.height)\n"
        + "#set($x_icon.url=$sel_icon.url)\n" + "#set($x_ss={})\n" + "#set($x_ss.w=$sel_screenshot.width)\n"
        + "#set($x_ss.h=$sel_screenshot.height)\n" + "#set($x_ss.url=$sel_screenshot.url)\n" + "#set ($String = \"\")\n"
        + "## Define json map\n" + "#set($pubContentMap = {\n"
        + "  \"title\" : $tool.jpath($first, \"imNative.creative.headline\").get(\"text\"),\n"
        + "  \"subtitle\" : $subtitle,\n" + "  \"landing_url\" : $first.openingLandingUrl,\n"
        + "  \"icon_90X90\" : $x_icon,\n" + "  \"image_576X630\" : $x_ss,\n"
        + "  \"rating\" : $tool.jpath($first, \"imNative.creative.social.appstore.rating\")})\n"
        + "#set($pubContent = $tool.jsonEncode($pubContentMap))\n" + "$tool.nativeAd($first, $pubContent)";

    private static void setUpMocks() {
        mockSASRequestParameters = createMock(SASRequestParameters.class);
        mockChannelSegmentEntity = createMock(ChannelSegmentEntity.class);
        mockConfig = createMock(Configuration.class);
        mockBidRequest = createMock(BidRequest.class);
        mockApp = createMock(App.class);

        expect(mockSASRequestParameters.getPlacementId()).andReturn(placementId).anyTimes();
        expect(mockSASRequestParameters.getImpressionId()).andReturn(impressionId).anyTimes();
        expect(mockSASRequestParameters.getSiteIncId()).andReturn(siteIncId).anyTimes();
        expect(mockSASRequestParameters.getSource()).andReturn("APP").anyTimes();
        expect(mockSASRequestParameters.getRFormat()).andReturn("native").anyTimes();
        expect(mockSASRequestParameters.getRequestedAdType()).andReturn(RequestedAdType.NATIVE).anyTimes();
        expect(mockSASRequestParameters.getDst()).andReturn(8).anyTimes();
        expect(mockChannelSegmentEntity.getExternalSiteKey()).andReturn(externalSiteKey).anyTimes();
        expect(mockChannelSegmentEntity.getAdgroupIncId()).andReturn(adgroupIncId).anyTimes();
        expect(mockChannelSegmentEntity.getPricingModel()).andReturn(CPM).anyTimes();
        expect(mockChannelSegmentEntity.getDst()).andReturn(2).anyTimes();
        expect(mockChannelSegmentEntity.getSecondaryAdFormatConstraints())
            .andReturn(SecondaryAdFormatConstraints.STATIC).anyTimes();
        expect(mockConfig.getString(advertiserName + ".advertiserId")).andReturn("advertiserId").anyTimes();
        expect(mockConfig.getString(advertiserName + ".urlArg")).andReturn("").anyTimes();
        expect(mockConfig.getString(advertiserName + ".rtbVer", "2.0")).andReturn("2.0").anyTimes();
        expect(mockConfig.getString(advertiserName + ".wnUrlback")).andReturn("").anyTimes();
        expect(mockConfig.getString(advertiserName + ".rtbMethod")).andReturn("").anyTimes();
        expect(mockConfig.getBoolean(advertiserName + ".siteBlinded")).andReturn(true).anyTimes();
        expect(mockConfig.getBoolean(advertiserName + ".isWnRequired")).andReturn(false).anyTimes();
        expect(mockConfig.getBoolean(advertiserName + ".htmlSupported", true)).andReturn(true).anyTimes();
        expect(mockConfig.getBoolean(advertiserName + ".nativeSupported", false)).andReturn(false).anyTimes();
        expect(mockConfig.getBoolean(advertiserName + ".bannerVideoSupported", false)).andReturn(false).anyTimes();
        expect(mockConfig.getString(advertiserName + ".currency", "USD")).andReturn("USD").anyTimes();
        expect(mockBidRequest.getApp()).andReturn(mockApp).anyTimes();
        expect(mockApp.getId()).andReturn(String.valueOf(placementId)).anyTimes();
        replayAll();
    }

    private static TemplateConfiguration getTemplateConfiguration() throws Exception {
        final DefaultConfiguration templateConfiguration = new DefaultConfiguration();

        final GsonManager gsonManager = new GsonManager(new DefaultGsonDeserializerConfiguration());

        templateConfiguration.setGsonManager(gsonManager);
        templateConfiguration.setTool(new ToolsImpl(gsonManager));
        templateConfiguration.setMathTool(new MathTool());

        return templateConfiguration;
    }

    private static void setUpRtbAdapter() throws Exception {
        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        final IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        rtbAdNetwork = new RtbAdNetwork(mockConfig, null, null, null, null, advertiserName, false);
        final Method[] methodsToSupress = new Method[] {RtbAdNetwork.class.getDeclaredMethod("configureParameters")};
        MemberModifier.suppress(methodsToSupress);

        final TemplateConfiguration templateConfiguration = getTemplateConfiguration();
        TemplateDecorator templateDecorator = new TemplateDecorator();
        templateDecorator.addContextFile("/contextCode.vm");

        MemberMatcher.field(RtbAdNetwork.class, "nativeResponseMaker")
            .set(rtbAdNetwork, new NativeResponseMaker(new TemplateParser(templateConfiguration), templateDecorator, templateConfiguration));
        rtbAdNetwork.setHost("http://localhost:8800/getBid");
        rtbAdNetwork.configureParameters(mockSASRequestParameters, null, mockChannelSegmentEntity, 0, null);
        rtbAdNetwork.setBidRequest(mockBidRequest);
    }

    private static void setUpMockTemplate() throws Exception {
        TemplateManager.getInstance().addToTemplateCache(String.valueOf(placementId), tangoTemplateContent);
    }

    @BeforeClass
    public static void setUp() throws Exception {
        resetAll();
        setUpMocks();
        setUpRtbAdapter();
        setUpMockTemplate();
    }

    private static BidResponse getBidResponseMock(final String adm) {
        final BidResponse mockBidResponse = createMock(BidResponse.class);
        final SeatBid mockSeatBid = createMock(SeatBid.class);
        final Bid mockBid = createMock(Bid.class);

        expect(mockBidResponse.getSeatbid()).andReturn(Arrays.asList(mockSeatBid)).anyTimes();
        expect(mockSeatBid.getBid()).andReturn(Arrays.asList(mockBid)).anyTimes();
        expect(mockBid.getAdm()).andReturn(adm).anyTimes();
        expect(mockBid.getNurl()).andReturn(nUrl).anyTimes();

        replayAll();
        return mockBidResponse;
    }

    @Test
    public void testNativePrerequisitesNotMet() throws Exception {
        mockStaticNice(InspectorStats.class);
        final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        final NativeAdTemplateEntity mockNativeAdTemplateEntity = createMock(NativeAdTemplateEntity.class);

        expect(mockRepositoryHelper.queryNativeAdTemplateRepository(placementId)).andReturn(null).times(1)
            .andReturn(mockNativeAdTemplateEntity).times(1);

        replayAll();
        MemberMatcher.field(RtbAdNetwork.class, "repositoryHelper").set(rtbAdNetwork, mockRepositoryHelper);

        // #1: TemplateEntity is null
        rtbAdNetwork.nativeAdBuilding();
        assertThat(rtbAdNetwork.getResponseContent(), is(equalTo("")));
        assertThat(rtbAdNetwork.getAdStatus(), is(equalTo("NO_AD")));

        // #2: BidResponse is null
        rtbAdNetwork.setBidResponse(getBidResponseMock(""));
        rtbAdNetwork.nativeAdBuilding();
        assertThat(rtbAdNetwork.getResponseContent(), is(equalTo("")));
        assertThat(rtbAdNetwork.getAdStatus(), is(equalTo("NO_AD")));
    }

    @Test
    public void testAppDeserialisationFailure() throws Exception {
        mockStaticNice(InspectorStats.class);
        final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        final NativeAdTemplateEntity mockNativeAdTemplateEntity = createMock(NativeAdTemplateEntity.class);

        expect(mockRepositoryHelper.queryNativeAdTemplateRepository(placementId)).andReturn(mockNativeAdTemplateEntity)
            .anyTimes();

        replayAll();
        MemberMatcher.field(RtbAdNetwork.class, "repositoryHelper").set(rtbAdNetwork, mockRepositoryHelper);

        // #1: Ad Markup is not a valid JSON
        rtbAdNetwork.setBidResponse(getBidResponseMock("NotAJson"));
        rtbAdNetwork.nativeAdBuilding();
        assertThat(rtbAdNetwork.getResponseContent(), is(equalTo("")));
        assertThat(rtbAdNetwork.getAdStatus(), is(equalTo("NO_AD")));

        // #2: Ad Markup fails in Data Deserialisation: Downloads value must be an integer
        String adMarkup = "{\"version\": \"1.0\",\"iconurl\": \"www.inmobi.com\","
            + "\"title\": \"Hello World\",\"description\": \"I am a description\","
            + "\"image\": {\"imageurl\": \"http://demo.image.com\",\"w\": 320,\"h\": 980},"
            + "\"actiontext\": \"Action text\",\"actionlink\": \"actionlink.inmobi.com\","
            + "\"pixelurl\": [\"http://rendered.action1\",\"http://rendered.action2\"],"
            + "\"callout\": 0,\"data\": [{\"label\": 0,\"value\": \"3.5\",\"seq\": 0}]}";
        rtbAdNetwork.setBidResponse(getBidResponseMock(adMarkup));
        rtbAdNetwork.nativeAdBuilding();
        assertThat(rtbAdNetwork.getResponseContent(), is(equalTo("")));
        assertThat(rtbAdNetwork.getAdStatus(), is(equalTo("NO_AD")));

        // #3: Ad Markup fails in Data Deserialisation: Ratings count must be an integer
        adMarkup = "{\"version\": \"1.0\",\"iconurl\": \"www.inmobi.com\","
            + "\"title\": \"Hello World\",\"description\": \"I am a description\","
            + "\"image\": {\"imageurl\": \"http://demo.image.com\",\"w\": 320,\"h\": 980},"
            + "\"actiontext\": \"Action text\",\"actionlink\": \"actionlink.inmobi.com\","
            + "\"pixelurl\": [\"http://rendered.action1\",\"http://rendered.action2\"],"
            + "\"callout\": 0,\"data\": [{\"label\": 2,\"value\": \"300.5\",\"seq\": 0}]}";
        rtbAdNetwork.setBidResponse(getBidResponseMock(adMarkup));
        rtbAdNetwork.nativeAdBuilding();
        assertThat(rtbAdNetwork.getResponseContent(), is(equalTo("")));
        assertThat(rtbAdNetwork.getAdStatus(), is(equalTo("NO_AD")));
    }

    @Test
    public void testAppValidation() throws Exception {
        mockStaticNice(InspectorStats.class);
        final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        final NativeAdTemplateEntity mockNativeAdTemplateEntity = createMock(NativeAdTemplateEntity.class);

        expect(mockRepositoryHelper.queryNativeAdTemplateRepository(placementId)).andReturn(mockNativeAdTemplateEntity)
            .anyTimes();
        expect(mockNativeAdTemplateEntity.getMandatoryKey()).andReturn(LAYOUT_CONSTRAINT_1).times(3)
            .andReturn(LAYOUT_CONSTRAINT_2).times(3).andReturn(LAYOUT_CONSTRAINT_3).times(3);
        expect(mockNativeAdTemplateEntity.getImageKey()).andReturn(INM_TAG_A056).times(9);

        replayAll();
        MemberMatcher.field(RtbAdNetwork.class, "repositoryHelper").set(rtbAdNetwork, mockRepositoryHelper);

        final String[] adMarkup =
            new String[] {"{title:\"Title\",iconurl:\"http://www.IconUrl.com\",image:{w:319,h:320,imageurl=\"http://www.ImageUrl.com\"}}", "{iconurl:\"http://www.IconUrl.com\",image:{w:320,h:320,imageurl=\"http://www.ImageUrl.com\"}}", "{title:\"Title\",image:{w:320,h:320,imageurl=\"http://www.ImageUrl.com\"}}", "{description:\"Description\",iconurl:\"http://www.IconUrl.com\",image:{w:319,h:320,imageurl=\"http://www.ImageUrl.com\"}}", "{title:\"Title\",description:\"Description\",image:{w:319,h:320,imageurl=\"http://www.ImageUrl.com\"}}", "{title:\"Title\",iconurl:\"http://www.IconUrl.com\",image:{w:319,h:320,imageurl=\"http://www.ImageUrl.com\"}}", "{iconurl:\"http://www.IconUrl.com\",image:{w:319,h:320,imageurl=\"http://www.ImageUrl.com\"}}", "{title:\"Title\",image:{w:319,h:320,imageurl=\"http://www.ImageUrl.com\"}}", "{title:\"Title\",iconurl:\"http://www.IconUrl.com\"}"};

        for (int i = 0; i < 9; ++i) {
            rtbAdNetwork.setBidResponse(getBidResponseMock(adMarkup[i]));
            rtbAdNetwork.nativeAdBuilding();
            assertThat(rtbAdNetwork.getResponseContent(), is(equalTo("")));
            assertThat(rtbAdNetwork.getAdStatus(), is(equalTo("NO_AD")));
        }
    }

    // Standard Native Positive Test Case
    public void testNativeResponseContentVariation1() throws Exception {
        mockStatic(Formatter.class);
        mockStaticNice(InspectorStats.class);
        final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        final NativeAdTemplateEntity mockNativeAdTemplateEntity = createMock(NativeAdTemplateEntity.class);

        expect(Formatter.getRTBDNamespace()).andReturn(namespace).anyTimes();
        expect(mockRepositoryHelper.queryNativeAdTemplateRepository(placementId)).andReturn(mockNativeAdTemplateEntity)
            .anyTimes();
        expect(mockNativeAdTemplateEntity.getMandatoryKey()).andReturn(LAYOUT_CONSTRAINT_3).anyTimes();
        expect(mockNativeAdTemplateEntity.getImageKey()).andReturn(INM_TAG_A056).anyTimes();

        replayAll();
        MemberMatcher.field(RtbAdNetwork.class, "repositoryHelper").set(rtbAdNetwork, mockRepositoryHelper);
        MemberModifier.field(RtbAdNetwork.class, "nurl").set(rtbAdNetwork, nUrl);

        final String expectedResponseContent =
            "{\"pubContent\":\"eyJ0aXRsZSI6IkhlbGxvIFdvcmxkIiwic3VidGl0bGUiOiJJIGFtIGEgZGVzY3JpcHRpb24iLCJsYW5kaW5"
                + "nX3VybCI6ImFjdGlvbmxpbmsuaW5tb2JpLmNvbSIsImljb25fOTBYOTAiOnsidyI6MzAwLCJoIjozMDAsInVybCI6In"
                + "d3dy5pbm1vYmkuY29tIn0sImltYWdlXzU3Nlg2MzAiOnsidyI6NDgwLCJoIjozMjAsInVybCI6Imh0dHA6Ly9kZW1vL"
                + "mltYWdlLmNvbSJ9fQ\\u003d\\u003d\",\"contextCode\":\"\\u003cscript type\\u003d\\\"text/javascri"
                + "pt\\\" src\\u003d\\\"mraid.js\\\"\\u003e\\u003c/script\\u003e\\n\\u003cdiv style\\u003d\\\"display:n"
                + "one; position:absolute;\\\" id\\u003d\\\"namespaceclickTarget\\\"\\u003e\\u003c/div\\u003e\\n\\u003c"
                + "script type\\u003d\\\"text/javascript\\\"\\u003e\\n(function() {\\nvar e\\u003dencodeURIComponent,"
                + "f\\u003dwindow,h\\u003ddocument,k\\u003d\\u0027appendChild\\u0027,n\\u003d\\u0027createElement"
                + "\\u0027,p\\u003d\\u0027setAttribute\\u0027,q\\u003d\\u0027\\u0027,r\\u003d\\u0027\\u0026\\u0027,s"
                + "\\u003d\\u00270\\u0027,t\\u003d\\u00272\\u0027,u\\u003d\\u0027\\u003d\\u0027,v\\u003d\\u0027?m"
                + "\\u003d\\u0027,w\\u003d\\u0027Events\\u0027,x\\u003d\\u0027_blank\\u0027,y\\u003d\\u0027a\\u0027,z"
                + "\\u003d\\u0027click\\u0027,A\\u003d\\u0027clickCallback\\u0027,B\\u003d\\u0027clickTarget\\u0027,C"
                + "\\u003d\\u0027error\\u0027,D\\u003d\\u0027event\\u0027,E\\u003d\\u0027function\\u0027,F\\u003d"
                + "\\u0027height\\u0027,G\\u003d\\u0027href\\u0027,H\\u003d\\u0027iatSendClick\\u0027,I\\u003d"
                + "\\u0027iframe\\u0027,J\\u003d\\u0027img\\u0027,K\\u003d\\u0027impressionCallback\\u0027,L\\u003d"
                + "\\u0027onclick\\u0027,M\\u003d\\u0027openLandingPage\\u0027,N\\u003d\\u0027recordEvent\\u0027,O"
                + "\\u003d\\u0027seamless\\u0027,P\\u003d\\u0027src\\u0027,Q\\u003d\\u0027target\\u0027,R\\u003d"
                + "\\u0027width\\u0027;f.inmobi\\u003df.inmobi||{};\\nfunction S(a){\\nthis.g\\u003da.lp;this.h"
                + "\\u003da.lps;this.c\\u003da.ct;this.d\\u003da.tc;this.e\\u003da.bcu;this.a\\u003da.ns;this.i"
                + "\\u003da.ws;a\\u003dthis.a;var c\\u003dthis;\\nf[a+M]\\u003dfunction(){\\nvar a\\u003dS.b(c.g),b"
                + "\\u003df.mraid;\\u0027undefined\\u0027!\\u003d\\u003dtypeof b\\u0026\\u0026\\u0027undefined\\u0027!"
                + "\\u003d\\u003dtypeof b.openExternal?b.openExternal(a):(a\\u003dS.b(c.h),b\\u003dh[n](y),b[p](Q,x),"
                + "b[p](G,a),h.body[k](b),S.f(b))};\\nf[a+A]\\u003dfunction(a){\\nT(c,a)};f[a+K]\\u003dfunction(){U(c)}"
                + ";\\nf[a+N]\\u003dfunction(a,b){V(c,a,b)}}f.inmobi.Bolt\\u003dS;\\nS.f\\u003dfunction(a){\\nif(typeof"
                + " a.click\\u003d\\u003dE)a.click.call(a);\\nelse if(a.fireEvent)a.fireEvent(L);\\nelse if(a.dispatch"
                + "Event){\\nvar c\\u003dh.createEvent(w);c.initEvent(z,!1,!0);a.dispatchEvent(c)}};\\nS.b\\u003dfuncti"
                + "on(a){\\nreturn a.replace(/\\\\\\\\$TS/g,q+(new Date).getTime())};\\nfunction W(a,c){\\nvar d\\u003"
                + "dh.getElementById(a.a+B),b\\u003dh[n](I);b[p](P,c);b[p](O,O);b[p](F,s);b[p](R,t);d[k](b)}\\nfunctio"
                + "n T(a,c){\\nvar d\\u003df[a.a+H];d\\u0026\\u0026d();for(var d\\u003da.c.length,b\\u003d0;b\\u003cd;b"
                + "++)W(a,S.b(a.c[b]));a.i\\u0026\\u0026(c\\u003dc||eval(D),\\u0027undefined\\u0027!\\u003d\\u003dtypeo"
                + "f c\\u0026\\u0026(d\\u003dvoid 0!\\u003dc.touches?c.touches[0]:c,f.external.notify(JSON.stringify({j"
                + ":d.clientX,k:d.clientY}))))}function U(a){if(null!\\u003da.d)try{var c\\u003dh.getElementById(a.a+B)"
                + ",d\\u003da.d,b\\u003dh[n](I);b[p](O,O);b[p](F,s);b[p](R,t);c[k](b);var g\\u003db.contentWindow;g"
                + "\\u0026\\u0026g.document.write(d)}catch(m){}}\\nfunction V(a,c,d){\\nfunction b(c,d,g){if(!(0"
                + "\\u003e\\u003dg)){\\nvar m\\u003dh.getElementById(a.a+B),l\\u003dh[n](J);l[p](P,c);l[p](F,s);l[p](R,"
                + "t);void 0!\\u003dl.addEventListener\\u0026\\u0026l.addEventListener(C,function(){f.setTimeout(functi"
                + "on(){3E5\\u003cd\\u0026\\u0026(d\\u003d3E5);b(c,2*d,g-1)},d*Math.random())},!1);m[k](l)}}var g\\u003"
                + "da.e,g\\u003dg+(v+c);if(d)for(var m in d)g+\\u003dr+e(m)+u+e(d[m]);b(g,1E3,5);18\\u003d\\u003dc"
                + "\\u0026\\u0026U(a);8\\u003d\\u003dc\\u0026\\u0026T(a,null)};})();\\nnew window.inmobi.Bolt({\\n\\\""
                + "lp\\\":\\\"actionlink.inmobi.com\\\",\\n\\\"lps\\\":\\\"actionlink.inmobi.com\\\",\\n\\\"ct\\\":"
                + "[\\\"http://click.action1\\\",\\\"http://click.action2\\\"],\\n\\\"bcu\\\":\\\"www.dummyBeacon.inmob"
                + "i.com\\\",\\n\\\"tc\\\":\\\"\\u003cimg src\\u003d\\\\\\\"responseNurl\\\\\\\" style\\u003d\\\\\\\"di"
                + "splay:none;\\\\\\\" /\\u003e\\u003cimg src\\u003d\\\\\\\"www.dummyBeacon.inmobi.com?b\\u003d${WIN_BI"
                + "D}\\\\\\\" style\\u003d\\\\\\\"display:none;\\\\\\\" /\\u003e\\u003cimg src\\u003d\\\\\\\"http://ren"
                + "dered.action1\\\\\\\" style\\u003d\\\\\\\"display:none;\\\\\\\" /\\u003e\\u003cimg src\\u003d"
                + "\\\\\\\"http://rendered.action2\\\\\\\" style\\u003d\\\\\\\"display:none;\\\\\\\" /\\u003e\\\",\\n"
                + "\\\"ws\\\":false,\\n\\\"ns\\\":\\\"namespace\\\"});\\n(function() {var b\\u003dwindow,c\\u003d"
                + "\\u0027handleClick\\u0027,d\\u003d\\u0027handleTouchEnd\\u0027,f\\u003d\\u0027handleTouchStart"
                + "\\u0027;b.inmobi\\u003db.inmobi||{};var g\\u003db.inmobi;function h(a,e){return function(l){e.call(a"
                + ",l)}}function k(a,e){this.b\\u003de;this.a\\u003dthis.c\\u003d!1;b[a+c]\\u003dh(this,this.click);b[a"
                + "+f]\\u003dh(this,this.start);b[a+d]\\u003dh(this,this.end)}k.prototype.click\\u003dfunction(){this."
                + "c||this.b()};k.prototype.start\\u003dfunction(a){this.a\\u003dthis.c\\u003d!0;a\\u0026\\u0026a.preve"
                + "ntDefault()};k.prototype.end\\u003dfunction(){this.a\\u0026\\u0026(this.a\\u003d!1,this.b())};g.OldT"
                + "ap\\u003dk;})(); new window.inmobi.OldTap(\\\"namespace\\\", function() {  window[\\u0027namespaceop"
                + "enLandingPage\\u0027]();  window[\\u0027namespaceclickCallback\\u0027]();});\\u003c/script\\u003e\","
                + "\"namespace\":\"namespace\"}";

        rtbAdNetwork.setBidResponse(getBidResponseMock(TestUtils.SampleStrings.rtbNativeAdMarkup));
        rtbAdNetwork.nativeAdBuilding();
        assertThat(rtbAdNetwork.getResponseContent(), is(equalTo(expectedResponseContent)));
    }

    // Native Positive Test Case with Chinese Text
    public void testNativeResponseContentVariation2() throws Exception {
        mockStatic(Formatter.class);
        mockStaticNice(InspectorStats.class);
        final RepositoryHelper mockRepositoryHelper = createMock(RepositoryHelper.class);
        final NativeAdTemplateEntity mockNativeAdTemplateEntity = createMock(NativeAdTemplateEntity.class);

        expect(Formatter.getRTBDNamespace()).andReturn(namespace).anyTimes();
        expect(mockRepositoryHelper.queryNativeAdTemplateRepository(placementId)).andReturn(mockNativeAdTemplateEntity)
            .anyTimes();
        expect(mockNativeAdTemplateEntity.getMandatoryKey()).andReturn(LAYOUT_CONSTRAINT_3).anyTimes();
        expect(mockNativeAdTemplateEntity.getImageKey()).andReturn(INM_TAG_A056).anyTimes();

        replayAll();
        MemberMatcher.field(RtbAdNetwork.class, "repositoryHelper").set(rtbAdNetwork, mockRepositoryHelper);
        MemberModifier.field(RtbAdNetwork.class, "nurl").set(rtbAdNetwork, nUrl);

        final String expectedResponseContent =
            "{\"pubContent\":\"eyJ0aXRsZSI6IuWqmuS4ieWbvS3ljbPml7bmiJjmlpfljaHniYwiLCJzdWJ0aXRsZSI6IuaMh+WwluaOqOW"
                + "hlO+8jOWqmuWwhuaXoOWPjO+8geWIm+aWsOaAp+exu2RvdGHljaHniYzmiYvmuLjjgIrlqprkuInlm73jgIvluKbkva"
                + "DmiJjotbfmnaXvvIEiLCJsYW5kaW5nX3VybCI6ImFjdGlvbmxpbmsuaW5tb2JpLmNvbSIsImljb25fOTBYOTAiOnsid"
                + "yI6MzAwLCJoIjozMDAsInVybCI6Ind3dy5pbm1vYmkuY29tIn0sImltYWdlXzU3Nlg2MzAiOnsidyI6NDgwLCJoIjoz"
                + "MjAsInVybCI6Imh0dHA6Ly9kZW1vLmltYWdlLmNvbSJ9fQ\\u003d\\u003d\",\"contextCode\":\"\\u003cscript type"
                + "\\u003d\\\"text/javascri"
                + "pt\\\" src\\u003d\\\"mraid.js\\\"\\u003e\\u003c/script\\u003e\\n\\u003cdiv style\\u003d\\\"display:n"
                + "one; position:absolute;\\\" id\\u003d\\\"namespaceclickTarget\\\"\\u003e\\u003c/div\\u003e\\n\\u003c"
                + "script type\\u003d\\\"text/javascript\\\"\\u003e\\n(function() {\\nvar e\\u003dencodeURIComponent,"
                + "f\\u003dwindow,h\\u003ddocument,k\\u003d\\u0027appendChild\\u0027,n\\u003d\\u0027createElement"
                + "\\u0027,p\\u003d\\u0027setAttribute\\u0027,q\\u003d\\u0027\\u0027,r\\u003d\\u0027\\u0026\\u0027,s"
                + "\\u003d\\u00270\\u0027,t\\u003d\\u00272\\u0027,u\\u003d\\u0027\\u003d\\u0027,v\\u003d\\u0027?m"
                + "\\u003d\\u0027,w\\u003d\\u0027Events\\u0027,x\\u003d\\u0027_blank\\u0027,y\\u003d\\u0027a\\u0027,z"
                + "\\u003d\\u0027click\\u0027,A\\u003d\\u0027clickCallback\\u0027,B\\u003d\\u0027clickTarget\\u0027,C"
                + "\\u003d\\u0027error\\u0027,D\\u003d\\u0027event\\u0027,E\\u003d\\u0027function\\u0027,F\\u003d"
                + "\\u0027height\\u0027,G\\u003d\\u0027href\\u0027,H\\u003d\\u0027iatSendClick\\u0027,I\\u003d"
                + "\\u0027iframe\\u0027,J\\u003d\\u0027img\\u0027,K\\u003d\\u0027impressionCallback\\u0027,L\\u003d"
                + "\\u0027onclick\\u0027,M\\u003d\\u0027openLandingPage\\u0027,N\\u003d\\u0027recordEvent\\u0027,O"
                + "\\u003d\\u0027seamless\\u0027,P\\u003d\\u0027src\\u0027,Q\\u003d\\u0027target\\u0027,R\\u003d"
                + "\\u0027width\\u0027;f.inmobi\\u003df.inmobi||{};\\nfunction S(a){\\nthis.g\\u003da.lp;this.h"
                + "\\u003da.lps;this.c\\u003da.ct;this.d\\u003da.tc;this.e\\u003da.bcu;this.a\\u003da.ns;this.i"
                + "\\u003da.ws;a\\u003dthis.a;var c\\u003dthis;\\nf[a+M]\\u003dfunction(){\\nvar a\\u003dS.b(c.g),b"
                + "\\u003df.mraid;\\u0027undefined\\u0027!\\u003d\\u003dtypeof b\\u0026\\u0026\\u0027undefined\\u0027!"
                + "\\u003d\\u003dtypeof b.openExternal?b.openExternal(a):(a\\u003dS.b(c.h),b\\u003dh[n](y),b[p](Q,x),"
                + "b[p](G,a),h.body[k](b),S.f(b))};\\nf[a+A]\\u003dfunction(a){\\nT(c,a)};f[a+K]\\u003dfunction(){U(c)}"
                + ";\\nf[a+N]\\u003dfunction(a,b){V(c,a,b)}}f.inmobi.Bolt\\u003dS;\\nS.f\\u003dfunction(a){\\nif(typeof"
                + " a.click\\u003d\\u003dE)a.click.call(a);\\nelse if(a.fireEvent)a.fireEvent(L);\\nelse if(a.dispatch"
                + "Event){\\nvar c\\u003dh.createEvent(w);c.initEvent(z,!1,!0);a.dispatchEvent(c)}};\\nS.b\\u003dfuncti"
                + "on(a){\\nreturn a.replace(/\\\\\\\\$TS/g,q+(new Date).getTime())};\\nfunction W(a,c){\\nvar d\\u003"
                + "dh.getElementById(a.a+B),b\\u003dh[n](I);b[p](P,c);b[p](O,O);b[p](F,s);b[p](R,t);d[k](b)}\\nfunctio"
                + "n T(a,c){\\nvar d\\u003df[a.a+H];d\\u0026\\u0026d();for(var d\\u003da.c.length,b\\u003d0;b\\u003cd;b"
                + "++)W(a,S.b(a.c[b]));a.i\\u0026\\u0026(c\\u003dc||eval(D),\\u0027undefined\\u0027!\\u003d\\u003dtypeo"
                + "f c\\u0026\\u0026(d\\u003dvoid 0!\\u003dc.touches?c.touches[0]:c,f.external.notify(JSON.stringify({j"
                + ":d.clientX,k:d.clientY}))))}function U(a){if(null!\\u003da.d)try{var c\\u003dh.getElementById(a.a+B)"
                + ",d\\u003da.d,b\\u003dh[n](I);b[p](O,O);b[p](F,s);b[p](R,t);c[k](b);var g\\u003db.contentWindow;g"
                + "\\u0026\\u0026g.document.write(d)}catch(m){}}\\nfunction V(a,c,d){\\nfunction b(c,d,g){if(!(0"
                + "\\u003e\\u003dg)){\\nvar m\\u003dh.getElementById(a.a+B),l\\u003dh[n](J);l[p](P,c);l[p](F,s);l[p](R,"
                + "t);void 0!\\u003dl.addEventListener\\u0026\\u0026l.addEventListener(C,function(){f.setTimeout(functi"
                + "on(){3E5\\u003cd\\u0026\\u0026(d\\u003d3E5);b(c,2*d,g-1)},d*Math.random())},!1);m[k](l)}}var g\\u003"
                + "da.e,g\\u003dg+(v+c);if(d)for(var m in d)g+\\u003dr+e(m)+u+e(d[m]);b(g,1E3,5);18\\u003d\\u003dc"
                + "\\u0026\\u0026U(a);8\\u003d\\u003dc\\u0026\\u0026T(a,null)};})();\\nnew window.inmobi.Bolt({\\n\\\""
                + "lp\\\":\\\"actionlink.inmobi.com\\\",\\n\\\"lps\\\":\\\"actionlink.inmobi.com\\\",\\n\\\"ct\\\":"
                + "[\\\"http://click.action1\\\",\\\"http://click.action2\\\"],\\n\\\"bcu\\\":\\\"www.dummyBeacon.inmob"
                + "i.com\\\",\\n\\\"tc\\\":\\\"\\u003cimg src\\u003d\\\\\\\"responseNurl\\\\\\\" style\\u003d\\\\\\\"di"
                + "splay:none;\\\\\\\" /\\u003e\\u003cimg src\\u003d\\\\\\\"www.dummyBeacon.inmobi.com?b\\u003d${WIN_BI"
                + "D}\\\\\\\" style\\u003d\\\\\\\"display:none;\\\\\\\" /\\u003e\\u003cimg src\\u003d\\\\\\\"http://ren"
                + "dered.action1\\\\\\\" style\\u003d\\\\\\\"display:none;\\\\\\\" /\\u003e\\u003cimg src\\u003d"
                + "\\\\\\\"http://rendered.action2\\\\\\\" style\\u003d\\\\\\\"display:none;\\\\\\\" /\\u003e\\\",\\n"
                + "\\\"ws\\\":false,\\n\\\"ns\\\":\\\"namespace\\\"});\\n(function() {var b\\u003dwindow,c\\u003d"
                + "\\u0027handleClick\\u0027,d\\u003d\\u0027handleTouchEnd\\u0027,f\\u003d\\u0027handleTouchStart"
                + "\\u0027;b.inmobi\\u003db.inmobi||{};var g\\u003db.inmobi;function h(a,e){return function(l){e.call(a"
                + ",l)}}function k(a,e){this.b\\u003de;this.a\\u003dthis.c\\u003d!1;b[a+c]\\u003dh(this,this.click);b[a"
                + "+f]\\u003dh(this,this.start);b[a+d]\\u003dh(this,this.end)}k.prototype.click\\u003dfunction(){this."
                + "c||this.b()};k.prototype.start\\u003dfunction(a){this.a\\u003dthis.c\\u003d!0;a\\u0026\\u0026a.preve"
                + "ntDefault()};k.prototype.end\\u003dfunction(){this.a\\u0026\\u0026(this.a\\u003d!1,this.b())};g.OldT"
                + "ap\\u003dk;})(); new window.inmobi.OldTap(\\\"namespace\\\", function() {  window[\\u0027namespaceop"
                + "enLandingPage\\u0027]();  window[\\u0027namespaceclickCallback\\u0027]();});\\u003c/script\\u003e\","
                + "\"namespace\":\"namespace\"}";

        rtbAdNetwork.setBidResponse(getBidResponseMock(TestUtils.SampleStrings.rtbNativeAdMarkupWithChineseText));
        rtbAdNetwork.nativeAdBuilding();
        assertThat(rtbAdNetwork.getResponseContent(), is(equalTo(expectedResponseContent)));
    }

}
