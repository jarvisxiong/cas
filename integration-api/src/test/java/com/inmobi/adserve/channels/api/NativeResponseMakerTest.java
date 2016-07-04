package com.inmobi.adserve.channels.api;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.createNiceMock;
import static org.powermock.api.easymock.PowerMock.replayAll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.tools.generic.ListTool;
import org.apache.velocity.tools.generic.MathTool;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;

import com.inmobi.adserve.contracts.rtb.response.Bid;
import com.inmobi.adserve.contracts.rtb.response.BidResponse;
import com.inmobi.adserve.contracts.rtb.response.SeatBid;
import com.inmobi.template.config.DefaultConfiguration;
import com.inmobi.template.context.App;
import com.inmobi.template.formatter.TemplateDecorator;
import com.inmobi.template.formatter.TemplateParser;
import com.inmobi.template.gson.GsonManager;
import com.inmobi.template.interfaces.TemplateConfiguration;
import com.inmobi.template.tool.ToolsImpl;

@RunWith(PowerMockRunner.class)
@PrepareForTest({App.class, BidResponse.class, SeatBid.class, Bid.class})
public class NativeResponseMakerTest {
    private static NativeResponseMaker nativeResponseMaker;

    private static BidResponse getBidResponseMock(final String adm, final String nUrl) {
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

    private static TemplateConfiguration getTemplateConfiguration() throws Exception {
        final DefaultConfiguration templateConfiguration = new DefaultConfiguration();
        final GsonManager gsonManager = new GsonManager();
        templateConfiguration.setGsonManager(gsonManager);
        templateConfiguration.setTool(new ToolsImpl(gsonManager));
        templateConfiguration.setMathTool(new MathTool());
        templateConfiguration.setListTool(new ListTool());

        return templateConfiguration;
    }

    @BeforeTest
    @BeforeClass
    public static void setUp() throws Exception {
        final TemplateConfiguration templateConfiguration = getTemplateConfiguration();

        final TemplateDecorator templateDecorator = new TemplateDecorator();
        templateDecorator.addContextFile("/contextCode.vm");

        nativeResponseMaker = new NativeResponseMaker(new TemplateParser(templateConfiguration), templateDecorator,
                templateConfiguration);
    }

    @Test
    public void testGetTrackingCodeNullCase() {
        getBidResponseMock("", null);
        final Map<String, String> params = new HashMap<>();
        final App mockApp = createNiceMock(App.class);

        replayAll();

        final String expectedOutput = "";
        final String actualOutput = nativeResponseMaker.getTrackingCode(params, mockApp);

        assertThat(actualOutput, is(equalTo(expectedOutput)));
    }

    @Test
    public void testGetTrackingCodeExceptionThrown() {
        new BidResponse("id", Arrays.asList(new SeatBid(Arrays.asList(new Bid()))));
        final Map<String, String> params = new HashMap<>();
        final App mockApp = createNiceMock(App.class);

        replayAll();

        final String expectedOutput = "";
        final String actualOutput = nativeResponseMaker.getTrackingCode(params, mockApp);

        assertThat(actualOutput, is(equalTo(expectedOutput)));
    }

    @Test
    public void testGetTrackingCodeNurlPresent() {
        final Map<String, String> params = new HashMap<>();
        params.put("nUrl", "nUrl");
        final App mockApp = createNiceMock(App.class);

        replayAll();

        final String expectedOutput = "<img src=\\\"nUrl\\\" style=\\\"display:none;\\\" />";
        final String actualOutput = nativeResponseMaker.getTrackingCode(params, mockApp);

        assertThat(actualOutput, is(equalTo(expectedOutput)));
    }

    @Test
    public void testGetTrackingCodeWinUrlAlsoPresent() {
        final Map<String, String> params = new HashMap<>();
        final App mockApp = createNiceMock(App.class);
        params.put("nUrl", "nUrl");
        params.put("winUrl", "winUrl");
        replayAll();

        final String expectedOutput =
                "<img src=\\\"nUrl\\\" style=\\\"display:none;\\\" /><img src=\\\"winUrl\\\" style=\\\"display:none;\\\" />";
        final String actualOutput = nativeResponseMaker.getTrackingCode(params, mockApp);

        assertThat(actualOutput, is(equalTo(expectedOutput)));
    }

    @Test
    public void testGetTrackingCodePixelUrlsAlsoPresent() {
        final Map<String, String> params = new HashMap<>();
        final App mockApp = createMock(App.class);

        expect(mockApp.getPixelUrls()).andReturn(Arrays.asList("www.pixelUrl1.com", "www.pixelUrl2.com")).anyTimes();
        replayAll();

        params.put("nUrl", "nUrl");
        params.put("winUrl", "winUrl");

        final String expectedOutput =
                "<img src=\\\"nUrl\\\" style=\\\"display:none;\\\" /><img src=\\\"winUrl\\\" style=\\\"display:none;\\\" /><img src=\\\"www.pixelUrl1.com\\\" style=\\\"display:none;\\\" /><img src=\\\"www.pixelUrl2.com\\\" style=\\\"display:none;\\\" />";
        final String actualOutput = nativeResponseMaker.getTrackingCode(params, mockApp);
        assertThat(actualOutput, is(equalTo(expectedOutput)));
    }

    @Test
    public void testTrackingEventNoJs() {
        final Map<String, String> params = new HashMap<>();
        final App mockApp = createMock(App.class);
        expect(mockApp.getPixelUrls()).andReturn(null).anyTimes();
        expect(mockApp.getClickUrls()).andReturn(Arrays.asList("www.clickUrl1.com", "www.InmobiClickUrl1.com")).anyTimes();

        replayAll();

        params.put("nUrl", "nUrl");
        params.put("winUrl", "winUrl");

        final Map<Integer, Map<String, List<String>>> actualOutput = nativeResponseMaker.getEventTracking(mockApp, params);
        System.out.println(actualOutput);
    }

    @DataProvider(name = "Data for NoJs EventTracking")
    public Object[][] eventTrackingNoJsDataProvider() {
        final List<String> pixelUrlArray1 = new ArrayList<>();
        pixelUrlArray1.add("www.pixelUrl1.com");
        pixelUrlArray1.add("www.pixelUrl2.com");

        final List<String> clickUrlArray1 = new ArrayList<>();
        clickUrlArray1.add("www.clickUrl1.com");
        clickUrlArray1.add( "www.clickUrl2.com");

        final String nUrl = "nurl";
        final String winUrl = "winUrl";

        return new Object[][] {
            {"AllPresentV1", pixelUrlArray1, clickUrlArray1, nUrl, winUrl},
            {"Winurl", null, null, null, winUrl},
            {"PixelAndWinUrl", pixelUrlArray1, null, null, winUrl},
            {"ClickAndWinUrl", null, clickUrlArray1, null, winUrl},
            {"AllPresentV2", pixelUrlArray1, clickUrlArray1, nUrl, winUrl},
            {"NurlAndWinUrl", null, null, nUrl, winUrl},
            {"PixelNurlAndWinUrl", pixelUrlArray1, null, nUrl, winUrl},
            {"ClickNurlAndWinUrl", null, clickUrlArray1, nUrl, winUrl},
        };
    }

    @org.testng.annotations.Test(dataProvider = "Data for NoJs EventTracking")
    public void testEventTrackingNoJs(final String testName, List<String> pixelUrlArray,
        final List<String> clickUrlArray, final String nUrl, final String winUrl) {
        if (null == pixelUrlArray && (StringUtils.isNotBlank(nUrl) || (StringUtils.isNotBlank(winUrl)))) {
            pixelUrlArray = new ArrayList<>();
        }
        final Map<String, String> params = new HashMap<>();
        final com.inmobi.template.context.App.Builder appBuilder = com.inmobi.template.context.App.newBuilder();
        final com.inmobi.template.context.App.Builder contextBuilder = com.inmobi.template.context.App.newBuilder();
        contextBuilder.setClickUrls(clickUrlArray);
        contextBuilder.setPixelUrls(pixelUrlArray);
        com.inmobi.template.context.App app = (App) contextBuilder.build();
        params.put("nUrl", nUrl);
        params.put("winUrl", winUrl);
        final Map<Integer, Map<String, List<String>>> actualOutput = nativeResponseMaker.getEventTracking(app, params);

        if (StringUtils.isNotBlank(nUrl)) {
            pixelUrlArray.add(nUrl);
        }
        if (StringUtils.isNotBlank(winUrl)) {
            pixelUrlArray.add(winUrl);
        }
        if (CollectionUtils.isNotEmpty(pixelUrlArray)) {
            assertEquals(actualOutput.get(TrackerUIInteraction.RENDER.getValue()).get("urls"), pixelUrlArray);
            assertEquals(actualOutput.get(18).get("urls"), pixelUrlArray);
        } else {
            assertTrue(CollectionUtils.isEmpty(actualOutput.get(TrackerUIInteraction.RENDER.getValue()).get("urls")));
            assertTrue(CollectionUtils.isEmpty(actualOutput.get(18).get("urls")));
        }
        if (CollectionUtils.isNotEmpty(clickUrlArray)) {
            assertEquals(actualOutput.get(TrackerUIInteraction.CLICK.getValue()).get("urls"), clickUrlArray);
            assertEquals(actualOutput.get(8).get("urls"), clickUrlArray);
        } else {
            assertTrue(CollectionUtils.isEmpty(actualOutput.get(TrackerUIInteraction.CLICK.getValue()).get("urls")));
            assertTrue(CollectionUtils.isEmpty(actualOutput.get(8).get("urls")));
        }
    }

}
