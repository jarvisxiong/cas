package com.inmobi.adserve.channels.repository;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import com.inmobi.adserve.channels.entity.CreativeEntity;
import com.inmobi.adserve.channels.query.CreativeQuery;
import com.inmobi.adserve.channels.types.CreativeExposure;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;

import java.sql.Timestamp;

/**
 * Created by anshul.soni on 05/01/15.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({NullAsZeroResultSetRow.class, ResultSetRow.class, CreativeRepository.class})
public class CreativeRepositoryTest {
    @Test
    public void testBuildObjectFromRow() throws Exception {
        final Timestamp modifiedOn = new Timestamp(1234L);
        final String advertiserId = "094c438f2ce24f8588552afe9ba6a89d";
        final String creativeId = "11062-916085f4cfed2e0d2d6a9ba43e704db3";
        final String exposureLevelSelfServe = "SELF-SERVE";
        final String exposureLevelRejected = "REJECTED";
        final CreativeExposure exposureLevelInEntitySelfServe = CreativeExposure.SELF_SERVE;
        final CreativeExposure exposureLevelInEntityRejected = CreativeExposure.REJECTED;
        final String imageUrl =
                "http://static1.adinch.com/media/bannerspub/2014/09/05/c377e42003e5dfea160eac0a83cc4713.gif";
        final String expectedLogOutputSelfServe =
                "Adding creative entity : CreativeEntity(advertiserId=094c438f2ce24f8588552afe9ba6a89d, creativeId=11062-916085f4cfed2e0d2d6a9ba43e704db3, exposureLevel=SELF_SERVE, imageUrl=http://static1.adinch.com/media/bannerspub/2014/09/05/c377e42003e5dfea160eac0a83cc4713.gif)";
        final String expectedLogOutputRejected =
                "Adding creative entity : CreativeEntity(advertiserId=094c438f2ce24f8588552afe9ba6a89d, creativeId=11062-916085f4cfed2e0d2d6a9ba43e704db3, exposureLevel=REJECTED, imageUrl=http://static1.adinch.com/media/bannerspub/2014/09/05/c377e42003e5dfea160eac0a83cc4713.gif)";

        final NullAsZeroResultSetRow mockNullAsZeroResultSetRow = createMock(NullAsZeroResultSetRow.class);

        Logger mockLogger = createMock(Logger.class);

        expect(mockLogger.isDebugEnabled()).andReturn(true).anyTimes();
        mockLogger.debug(expectedLogOutputSelfServe);
        expectLastCall().times(1);
        mockLogger.debug(expectedLogOutputRejected);
        expectLastCall().times(1);

        expect(mockNullAsZeroResultSetRow.getString("advertiser_id")).andReturn(advertiserId).times(2);
        expect(mockNullAsZeroResultSetRow.getString("creative_id")).andReturn(creativeId).times(2);
        expect(mockNullAsZeroResultSetRow.getString("exposure_level")).andReturn(exposureLevelSelfServe).times(1).andReturn(exposureLevelRejected).times(1);
        expect(mockNullAsZeroResultSetRow.getString("sample_url")).andReturn(imageUrl).times(2);
        expect(mockNullAsZeroResultSetRow.getTimestamp("modified_on")).andReturn(modifiedOn).times(2);
        expectNew(NullAsZeroResultSetRow.class, new Class[] {ResultSetRow.class}, null).andReturn(
                mockNullAsZeroResultSetRow).times(2);

        replayAll();

        final CreativeRepository tested = new CreativeRepository();
        MemberModifier.field(CreativeRepository.class, "logger").set(tested, mockLogger);
        final DBEntity<CreativeEntity, CreativeQuery> entity = tested.buildObjectFromRow(null);
        final CreativeEntity output = entity.getObject();

        assertThat(output.getAdvertiserId(), is(equalTo(advertiserId)));
        assertThat(output.getCreativeId(), is(equalTo(creativeId)));
        assertThat(output.getExposureLevel(), is(equalTo(exposureLevelInEntitySelfServe)));
        assertThat(output.getImageUrl(), is(equalTo(imageUrl)));

        final CreativeRepository tested2 = new CreativeRepository();
        MemberModifier.field(CreativeRepository.class, "logger").set(tested2, mockLogger);
        final DBEntity<CreativeEntity, CreativeQuery> entity2 = tested2.buildObjectFromRow(null);
        final CreativeEntity output2 = entity2.getObject();

        assertThat(output2.getAdvertiserId(), is(equalTo(advertiserId)));
        assertThat(output2.getCreativeId(), is(equalTo(creativeId)));
        assertThat(output2.getExposureLevel(), is(equalTo(exposureLevelInEntityRejected)));
        assertThat(output2.getImageUrl(), is(equalTo(imageUrl)));

        verifyAll();
    }

    @Test
    public void testIsObjectToBeDeleted() throws Exception {
        final CreativeRepository tested = new CreativeRepository();
        assertThat(tested.isObjectToBeDeleted(null), is(equalTo(false)));
    }

    @Test
    public void testGetHashIndexKeyBuilder() throws Exception {
        final CreativeRepository tested = new CreativeRepository();
        assertThat(tested.getHashIndexKeyBuilder(null), is(equalTo(null)));
    }
}
