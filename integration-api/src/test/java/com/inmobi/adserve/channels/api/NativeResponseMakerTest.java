package com.inmobi.adserve.channels.api;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.createNiceMock;
import static org.powermock.api.easymock.PowerMock.replayAll;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.tools.generic.MathTool;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

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

        return templateConfiguration;
    }

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

}
