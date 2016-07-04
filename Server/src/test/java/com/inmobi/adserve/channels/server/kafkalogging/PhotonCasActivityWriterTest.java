package com.inmobi.adserve.channels.server.kafkalogging;

import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.replayAll;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.testng.annotations.Test;

import com.inmobi.adserve.adpool.UidType;
import com.inmobi.adserve.channels.adnetworks.ix.IXAdNetwork;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.pmp.DealEntity;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.filters.ChannelSegmentFilterApplierTest;
import com.inmobi.adserve.channels.util.ConfigurationLoader;
import com.inmobi.casthrift.DemandSourceType;
import com.inmobi.messaging.publisher.MessagePublisherFactory;
import com.inmobi.user.photon.datatypes.activity.NestedActivityRecord;

import junit.framework.TestCase;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MessagePublisherFactory.class})
public class PhotonCasActivityWriterTest extends TestCase {

    private static final String CHANNEL_SERVER_CONFIG_FILE = "channel-server.properties";
    private PhotonCasActivityWriter photonCasActivityWriter;

    @Override
    public void setUp() throws ClassNotFoundException, IllegalAccessException, IOException {
        PowerMock.mockStaticNice(MessagePublisherFactory.class);
        replayAll();
        ConfigurationLoader configurationLoder = ConfigurationLoader.getInstance(CHANNEL_SERVER_CONFIG_FILE);
        Configuration serverConfiguration = configurationLoder.getServerConfiguration();
        PhotonCasActivityWriter.init(serverConfiguration, false);
        photonCasActivityWriter = PhotonCasActivityWriter.getInstance();
        System.out.println("initialized");

    }

    @Test
    public void testNestedActivityRecord() {
        System.out.println("started");
        ChannelSegment channelSegment = getChannelSegment();
        SASRequestParameters sasRequestParameters = new SASRequestParameters();
        sasRequestParameters.setTUidParams(getUidMap());
        NestedActivityRecord nestedActivityRecord =
                photonCasActivityWriter.getNestedActivityRecord(channelSegment, sasRequestParameters);
        assertEquals("036541b1-e1c1-4da7-aea2-ca06a9bc98fe", nestedActivityRecord.getUserId());
    }

    @Test
    public void testNestedActivityRecordWithNullChannelSegment() {
        System.out.println("started");
        SASRequestParameters sasRequestParameters = new SASRequestParameters();
        sasRequestParameters.setTUidParams(getUidMap());
        NestedActivityRecord nestedActivityRecord =
                photonCasActivityWriter.getNestedActivityRecord(null, sasRequestParameters);
        assertEquals(null, nestedActivityRecord);
    }

    @Test
    public void testNestedActivityRecordWithNullchannelSegmentEntity() {
        System.out.println("started");
        SASRequestParameters sasRequestParameters = new SASRequestParameters();
        ChannelSegment channelSegment = getChannelSegmentWithNullChannelSegmentEntity();
        sasRequestParameters.setTUidParams(getUidMap());
        NestedActivityRecord nestedActivityRecord =
                photonCasActivityWriter.getNestedActivityRecord(channelSegment, sasRequestParameters);
        assertEquals(null, nestedActivityRecord);
    }

    @Test
    public void testNestedActivityRecordWithNullAdNetworkInterface() {
        System.out.println("started");
        SASRequestParameters sasRequestParameters = new SASRequestParameters();
        ChannelSegment channelSegment = getChannelSegmentWithNullAdNetworkInterface();
        sasRequestParameters.setTUidParams(getUidMap());
        NestedActivityRecord nestedActivityRecord =
                photonCasActivityWriter.getNestedActivityRecord(channelSegment, sasRequestParameters);
        assertEquals(null, nestedActivityRecord);
    }

    private ChannelSegment getChannelSegment() {
        final Long[] rcList = null;
        final Long[] tags = null;
        final Timestamp modified_on = null;
        final Long[] slotIds = null;
        final Integer[] siteRatings = null;
        final ChannelSegmentEntity channelSegmentEntity =
                new ChannelSegmentEntity(ChannelSegmentFilterApplierTest.getChannelSegmentEntityBuilder("advId",
                        "adgroupId", "adId", "channelId", 1, rcList, tags, true, true, "externalSiteKey", modified_on,
                        "campaignId", slotIds, 1, true, "pricingModel", siteRatings, 1, null, false, false, false,
                        false, false, false, false, false, false, false, null, null, 0.0d, null, null, false,
                        new HashSet<String>(), 0));

        final IXAdNetwork ixAdNetwork = createMock(IXAdNetwork.class);
        expect(ixAdNetwork.getAdStatus()).andReturn("AD").anyTimes();
        expect(ixAdNetwork.getImpressionId()).andReturn("a7a09645-0154-1000-eeb3-3eea3ae10066").anyTimes();
        expect(ixAdNetwork.getAdvId()).andReturn("advId").anyTimes();
        expect(ixAdNetwork.getCreativeId()).andReturn("creativeId").anyTimes();
        expect(ixAdNetwork.getDspId()).andReturn("dspId").anyTimes();
        expect(ixAdNetwork.getDst()).andReturn(DemandSourceType.IX).anyTimes();
        expect(ixAdNetwork.getDeal()).andReturn(DealEntity.newBuilder().id("12345").build()).anyTimes();
        replay(ixAdNetwork);

        return new ChannelSegment(channelSegmentEntity, null, null, null, null, ixAdNetwork, 0);

    }

    private ChannelSegment getChannelSegmentWithNullChannelSegmentEntity() {
        final IXAdNetwork ixAdNetwork = createMock(IXAdNetwork.class);
        expect(ixAdNetwork.getAdStatus()).andReturn("AD").anyTimes();
        expect(ixAdNetwork.getImpressionId()).andReturn("a7a09645-0154-1000-eeb3-3eea3ae10066").anyTimes();
        expect(ixAdNetwork.getAdvId()).andReturn("advId").anyTimes();
        expect(ixAdNetwork.getCreativeId()).andReturn("creativeId").anyTimes();
        expect(ixAdNetwork.getDspId()).andReturn("dspId").anyTimes();
        expect(ixAdNetwork.getDst()).andReturn(DemandSourceType.IX).anyTimes();
        replay(ixAdNetwork);

        return new ChannelSegment(null, null, null, null, null, ixAdNetwork, 0);

    }

    private ChannelSegment getChannelSegmentWithNullAdNetworkInterface() {
        final Long[] rcList = null;
        final Long[] tags = null;
        final Timestamp modified_on = null;
        final Long[] slotIds = null;
        final Integer[] siteRatings = null;
        final ChannelSegmentEntity channelSegmentEntity =
                new ChannelSegmentEntity(ChannelSegmentFilterApplierTest.getChannelSegmentEntityBuilder("advId",
                        "adgroupId", "adId", "channelId", 1, rcList, tags, true, true, "externalSiteKey", modified_on,
                        "campaignId", slotIds, 1, true, "pricingModel", siteRatings, 1, null, false, false, false,
                        false, false, false, false, false, false, false, null, null, 0.0d, null, null, false,
                        new HashSet<String>(), 0));

        return new ChannelSegment(channelSegmentEntity, null, null, null, null, null, 0);

    }

    private Map<String, String> getUidMap() {
        Map<String, String> uidMap = new HashMap<String, String>();
        uidMap.put(UidType.GPID.toString(), "036541b1-e1c1-4da7-aea2-ca06a9bc98fe");
        return uidMap;
    }
}
