package com.inmobi.adserve.channels.server.requesthandler;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStaticNice;
import static org.powermock.api.easymock.PowerMock.replayAll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.auction.AuctionEngine;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.Utils.ImpressionIdGenerator;
import com.inmobi.casthrift.DemandSourceType;

@RunWith(PowerMockRunner.class)
@PrepareForTest({InspectorStats.class, Logging.class})
public class ResponseSenderTest {

    @BeforeClass
    public static void setUp() {
        final short hostIdCode = (short) 5;
        final byte dataCenterIdCode = 1;
        ImpressionIdGenerator.init(hostIdCode, dataCenterIdCode);
    }

    @Test
    public void testGetResponseFormat() throws Exception {
        SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);

        expect(mockSASRequestParameters.getRFormat()).andReturn(null).times(2).andReturn("axml").times(1)
                .andReturn("xhtml").times(1).andReturn("html").times(1).andReturn("imai").times(1).andReturn("native")
                .times(1).andReturn("jsAdCode").times(1);

        expect(mockSASRequestParameters.getAdcode()).andReturn("JS").times(1).andReturn(null).anyTimes();

        expect(mockSASRequestParameters.getRqIframe()).andReturn("<Iframe Code>").times(1).andReturn(null).anyTimes();

        replayAll();

        ResponseSender responseSender = new ResponseSender();

        responseSender.setSasParams(null);
        assertThat(responseSender.getResponseFormat(), is(equalTo(ResponseFormat.HTML)));

        responseSender.setSasParams(mockSASRequestParameters);
        assertThat(responseSender.getResponseFormat(), is(equalTo(ResponseFormat.JS_AD_CODE)));
        assertThat(responseSender.getResponseFormat(), is(equalTo(ResponseFormat.HTML)));
        assertThat(responseSender.getResponseFormat(), is(equalTo(ResponseFormat.XHTML)));
        assertThat(responseSender.getResponseFormat(), is(equalTo(ResponseFormat.XHTML)));
        assertThat(responseSender.getResponseFormat(), is(equalTo(ResponseFormat.HTML)));
        assertThat(responseSender.getResponseFormat(), is(equalTo(ResponseFormat.IMAI)));
        assertThat(responseSender.getResponseFormat(), is(equalTo(ResponseFormat.NATIVE)));
        assertThat(responseSender.getResponseFormat(), is(equalTo(ResponseFormat.JS_AD_CODE)));
    }

    @Test
    public void testWriteLogsFailure() throws Exception {
        mockStaticNice(Logging.class);
        mockStaticNice(InspectorStats.class);
        final SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);
        final AuctionEngine mockAuctionEngine = createMock(AuctionEngine.class);

        List<ChannelSegment> list = new ArrayList<>();
        expect(mockAuctionEngine.getUnfilteredChannelSegmentList()).andReturn(list).times(2).andReturn(null).times(1);
        expect(mockSASRequestParameters.getDst()).andReturn(DemandSourceType.DCP.getValue()).times(1)
                .andReturn(DemandSourceType.IX.getValue()).times(1);
        replayAll();

        ResponseSender responseSender = new ResponseSender();
        responseSender.setSasParams(null);
        responseSender.writeLogs();

        MemberModifier.field(ResponseSender.class, "auctionEngine").set(responseSender, mockAuctionEngine);
        responseSender.setSasParams(mockSASRequestParameters);

        list.add(null);
        responseSender.setRankList(list);
        responseSender.writeLogs();
        responseSender.setRankList(null);
        responseSender.writeLogs();
        responseSender.setRankList(list);
        responseSender.writeLogs();
    }

    @Test
    public void testWriteLogsSuccess() throws Exception {
        mockStaticNice(Logging.class);
        mockStaticNice(InspectorStats.class);
        SASRequestParameters mockSASRequestParameters = createMock(SASRequestParameters.class);
        AuctionEngine mockAuctionEngine = createMock(AuctionEngine.class);

        expect(mockAuctionEngine.getUnfilteredChannelSegmentList()).andReturn(new ArrayList<ChannelSegment>()).times(1)
                .andReturn(null).times(1);
        expect(mockAuctionEngine.getAuctionResponse()).andReturn(getDummyChannelSegment()).times(2).andReturn(null)
                .times(1);
        expect(mockSASRequestParameters.getDst()).andReturn(DemandSourceType.IX.getValue()).times(3)
                .andReturn(DemandSourceType.DCP.getValue()).times(3);
        replayAll();

        ResponseSender responseSender = new ResponseSender();
        responseSender.casInternalRequestParameters = new CasInternalRequestParameters();
        responseSender.casInternalRequestParameters.setAuctionBidFloor(1.5);

        MemberModifier.field(ResponseSender.class, "auctionEngine").set(responseSender, mockAuctionEngine);
        responseSender.setSasParams(mockSASRequestParameters);

        responseSender.setRankList(null);
        responseSender.writeLogs();
        responseSender.setRankList(Arrays.asList(getDummyChannelSegment()));
        responseSender.writeLogs();

    }

    private ChannelSegment getDummyChannelSegment() {
        return new ChannelSegment(null, null, null, null, null, null, 0.0);
    }

}
