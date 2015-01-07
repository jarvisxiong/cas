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

import com.inmobi.casthrift.rtb.Bid;
import com.inmobi.casthrift.rtb.BidResponse;
import com.inmobi.casthrift.rtb.SeatBid;
import com.inmobi.template.config.DefaultConfiguration;
import com.inmobi.template.config.DefaultDeserializerConfiguration;
import com.inmobi.template.context.App;
import com.inmobi.template.formatter.TemplateDecorator;
import com.inmobi.template.formatter.TemplateParser;
import com.inmobi.template.gson.GsonManager;
import com.inmobi.template.interfaces.TemplateConfiguration;
import com.inmobi.template.tool.ToolsImpl;

@RunWith(PowerMockRunner.class)
@PrepareForTest({App.class})
public class NativeResponseMakerTest {
    private static NativeResponseMaker nativeResponseMaker;

    private static BidResponse getBidResponseMock(String adm, String nUrl) {
        BidResponse mockBidResponse = createMock(BidResponse.class);
        SeatBid mockSeatBid = createMock(SeatBid.class);
        Bid mockBid = createMock(Bid.class);

        expect(mockBidResponse.getSeatbid()).andReturn(Arrays.asList(mockSeatBid)).anyTimes();
        expect(mockSeatBid.getBid()).andReturn(Arrays.asList(mockBid)).anyTimes();
        expect(mockBid.getAdm()).andReturn(adm).anyTimes();
        expect(mockBid.getNurl()).andReturn(nUrl).anyTimes();

        replayAll();
        return mockBidResponse;
    }

    private static TemplateConfiguration getTemplateConfiguration() throws Exception{
        DefaultConfiguration templateConfiguration = new DefaultConfiguration();

        GsonManager gsonManager = new GsonManager();
        gsonManager.setDeserializer(new DefaultDeserializerConfiguration());

        TemplateDecorator templateDecorator = new TemplateDecorator();
        templateDecorator.addContextFile("/contextCode.vm");

        templateConfiguration.setGsonManager(gsonManager);
        templateConfiguration.setTool(new ToolsImpl());
        templateConfiguration.setMathTool(new MathTool());
        templateConfiguration.setTemplateDecorator(templateDecorator);

        return templateConfiguration;
    }

    @BeforeClass
    public static void setUp() throws Exception{
        TemplateConfiguration templateConfiguration = getTemplateConfiguration();
        nativeResponseMaker = new NativeResponseMaker(new TemplateParser(templateConfiguration), templateConfiguration);
    }

    @Test
    public void testGetTrackingCodeNullCase() {
        BidResponse mockBidResponse = getBidResponseMock("", null);
        Map<String, String> params = new HashMap<>();
        App mockApp = createNiceMock(App.class);

        replayAll();

        String expectedOutput = "";
        String actualOutput = nativeResponseMaker.getTrackingCode(mockBidResponse, params, mockApp);

        assertThat(actualOutput, is(equalTo(expectedOutput)));
    }

    @Test
    public void testGetTrackingCodeExceptionThrown() {
        BidResponse mockBidResponse = new BidResponse("id", null);
        Map<String, String> params = new HashMap<>();
        App mockApp = createNiceMock(App.class);

        replayAll();

        String expectedOutput = "";
        String actualOutput = nativeResponseMaker.getTrackingCode(mockBidResponse, params, mockApp);

        assertThat(actualOutput, is(equalTo(expectedOutput)));
    }

    @Test
    public void testGetTrackingCodeNurlPresent() {
        BidResponse mockBidResponse = getBidResponseMock(null, "nUrl");
        Map<String, String> params = new HashMap<>();
        App mockApp = createNiceMock(App.class);

        replayAll();

        String expectedOutput = "<img src=\\\"nUrl\\\" style=\\\"display:none;\\\" />";
        String actualOutput = nativeResponseMaker.getTrackingCode(mockBidResponse, params, mockApp);

        assertThat(actualOutput, is(equalTo(expectedOutput)));
    }

    @Test
    public void testGetTrackingCodeWinUrlAlsoPresent() {
        BidResponse mockBidResponse = getBidResponseMock(null, "nUrl");
        Map<String, String> params = new HashMap<>();
        App mockApp = createNiceMock(App.class);

        params.put("winUrl", "winUrl");
        replayAll();

        String expectedOutput = "<img src=\\\"nUrl\\\" style=\\\"display:none;\\\" /><img src=\\\"winUrl\\\" style=\\\"display:none;\\\" />";
        String actualOutput = nativeResponseMaker.getTrackingCode(mockBidResponse, params, mockApp);

        assertThat(actualOutput, is(equalTo(expectedOutput)));
    }

    @Test
    public void testGetTrackingCodePixelUrlsAlsoPresent() {
        BidResponse mockBidResponse = getBidResponseMock(null, "nUrl");
        Map<String, String> params = new HashMap<>();
        App mockApp = createMock(App.class);

        expect(mockApp.getPixelUrls()).andReturn(Arrays.asList("www.pixelUrl1.com", "www.pixelUrl2.com")).anyTimes();
        replayAll();

        params.put("winUrl", "winUrl");

        String expectedOutput = "<img src=\\\"nUrl\\\" style=\\\"display:none;\\\" /><img src=\\\"winUrl\\\" style=\\\"display:none;\\\" /><img src=\\\"www.pixelUrl1.com\\\" style=\\\"display:none;\\\" /><img src=\\\"www.pixelUrl2.com\\\" style=\\\"display:none;\\\" />";
        String actualOutput = nativeResponseMaker.getTrackingCode(mockBidResponse, params, mockApp);
        assertThat(actualOutput, is(equalTo(expectedOutput)));
    }

}
