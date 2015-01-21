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

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.channels.entity.PricingEngineEntity;
import com.inmobi.adserve.channels.query.PricingEngineQuery;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;

/**
 * Created by anshul.soni on 30/12/14.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({NullAsZeroResultSetRow.class, ResultSetRow.class, PricingEngineRepository.class})
public class PricingEngineRepositoryTest {

    @Test
    public void testBuildObjectFromRow() throws Exception {
        final Integer countryId = 253;
        final Integer osId = 3;
        final Double rtbdFloor = 0.15;
        final Double dcpFloor = 0.1;
        final String supplyDemandJson =
                "{'0':['0','1'],'1':['0','1','2'],'2':['0','1','2','3'],'3':['0','1','2','3','4'],'4':['0','1','2','3','4','5'],'5':['0','1','2','3','4','5','6'],'6':['0','1','2','3','4','5','6','7'],'7':['0','1','2','3','4','5','6','7','8'],'8':['0','1','2','3','4','5','6','7','8','9'],'9':['0','1','2','3','4','5','6','7','8','9']}";
        final Timestamp modifiedOn = new Timestamp(1353954600000L);
        final String expectedLogOutput =
                "Adding pricing entity : PricingEngineEntity(countryId=253, osId=3, rtbFloor=0.15, dcpFloor=0.1, supplyToDemandMap={3=[3, 2, 1, 0, 4], 2=[3, 2, 1, 0], 1=[2, 1, 0], 0=[1, 0], 7=[3, 2, 1, 0, 7, 6, 5, 4, 8], 6=[3, 2, 1, 0, 7, 6, 5, 4], 5=[3, 2, 1, 0, 6, 5, 4], 4=[3, 2, 1, 0, 5, 4], 9=[3, 2, 1, 0, 7, 6, 5, 4, 9, 8], 8=[3, 2, 1, 0, 7, 6, 5, 4, 9, 8]})";
        final Map<String, Set<String>> supplyToDemandMap = new HashMap<String, Set<String>>();
        supplyToDemandMap.put("0", new HashSet<String>(Arrays.asList("0", "1")));
        supplyToDemandMap.put("1", new HashSet<String>(Arrays.asList("0", "1", "2")));
        supplyToDemandMap.put("2", new HashSet<String>(Arrays.asList("0", "1", "2", "3")));
        supplyToDemandMap.put("3", new HashSet<String>(Arrays.asList("0", "1", "2", "3", "4")));
        supplyToDemandMap.put("4", new HashSet<String>(Arrays.asList("0", "1", "2", "3", "4", "5")));
        supplyToDemandMap.put("5", new HashSet<String>(Arrays.asList("0", "1", "2", "3", "4", "5", "6")));
        supplyToDemandMap.put("6", new HashSet<String>(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7")));
        supplyToDemandMap.put("7", new HashSet<String>(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8")));
        supplyToDemandMap.put("8", new HashSet<String>(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")));
        supplyToDemandMap.put("9", new HashSet<String>(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")));
        final NullAsZeroResultSetRow mockNullAsZeroResultSetRow = createMock(NullAsZeroResultSetRow.class);

        Logger mockLogger = createMock(Logger.class);

        expect(mockLogger.isDebugEnabled()).andReturn(true).anyTimes();
        mockLogger.debug(expectedLogOutput);
        expectLastCall().anyTimes();

        expect(mockNullAsZeroResultSetRow.getInt("country_id")).andReturn(countryId).times(1);
        expect(mockNullAsZeroResultSetRow.getInt("os_id")).andReturn(osId).times(1);
        expect(mockNullAsZeroResultSetRow.getDouble("rtb_floor")).andReturn(rtbdFloor).times(1);
        expect(mockNullAsZeroResultSetRow.getDouble("dcp_floor")).andReturn(dcpFloor).times(1);
        expect(mockNullAsZeroResultSetRow.getString("supply_demand_json")).andReturn(supplyDemandJson).times(1);
        expect(mockNullAsZeroResultSetRow.getTimestamp("modified_on")).andReturn(modifiedOn).times(1);
        expectNew(NullAsZeroResultSetRow.class, new Class[] {ResultSetRow.class}, null).andReturn(
                mockNullAsZeroResultSetRow).times(1);

        replayAll();

        final PricingEngineRepository tested = new PricingEngineRepository();
        MemberModifier.field(PricingEngineRepository.class, "logger").set(tested, mockLogger);
        final DBEntity<PricingEngineEntity, PricingEngineQuery> entity = tested.buildObjectFromRow(null);
        final PricingEngineEntity output = entity.getObject();

        assertThat(output.getCountryId(), is(equalTo(countryId)));
        assertThat(output.getOsId(), is(equalTo(osId)));
        assertThat(output.getDcpFloor(), is(equalTo(dcpFloor)));
        assertThat(output.getRtbFloor(), is(equalTo(rtbdFloor)));
        assertThat(output.getSupplyToDemandMap(), is(equalTo(supplyToDemandMap)));

        verifyAll();
    }

    @Test
    public void testgetSupplyToDemandMap() throws Exception {
        final String supplyDemandJson =
                "{'0':['0','1'],'1':['0','1','2'],'2':['0','1','2','3'],'3':['0','1','2','3','4'],'4':['0','1','2','3','4','5'],'5':['0','1','2','3','4','5','6'],'6':['0','1','2','3','4','5','6','7'],'7':['0','1','2','3','4','5','6','7','8'],'8':['0','1','2','3','4','5','6','7','8','9'],'9':['0','1','2','3','4','5','6','7','8','9']}";
        final Map<String, Set<String>> supplyToDemandMap = new HashMap<String, Set<String>>();
        supplyToDemandMap.put("0", new HashSet<String>(Arrays.asList("0", "1")));
        supplyToDemandMap.put("1", new HashSet<String>(Arrays.asList("0", "1", "2")));
        supplyToDemandMap.put("2", new HashSet<String>(Arrays.asList("0", "1", "2", "3")));
        supplyToDemandMap.put("3", new HashSet<String>(Arrays.asList("0", "1", "2", "3", "4")));
        supplyToDemandMap.put("4", new HashSet<String>(Arrays.asList("0", "1", "2", "3", "4", "5")));
        supplyToDemandMap.put("5", new HashSet<String>(Arrays.asList("0", "1", "2", "3", "4", "5", "6")));
        supplyToDemandMap.put("6", new HashSet<String>(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7")));
        supplyToDemandMap.put("7", new HashSet<String>(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8")));
        supplyToDemandMap.put("8", new HashSet<String>(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")));
        supplyToDemandMap.put("9", new HashSet<String>(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")));

        final PricingEngineRepository tested = new PricingEngineRepository();

        Map<String, Set<String>> resultingSupplyDemandJsonMap = tested.getSupplyToDemandMap(supplyDemandJson);
        assertThat(resultingSupplyDemandJsonMap, is(equalTo(supplyToDemandMap)));
        verifyAll();
    }

    @Test
    public void testGetHashIndexKeyBuilder() throws Exception {
        final PricingEngineRepository tested = new PricingEngineRepository();
        assertThat(tested.getHashIndexKeyBuilder(null), is(equalTo(null)));
    }

    @Test
    public void testisObjectToBeDeleted() throws Exception {
        final PricingEngineRepository tested = new PricingEngineRepository();

        final PricingEngineEntity dummy1 = createMock(PricingEngineEntity.class);

        expect(dummy1.getDcpFloor()).andReturn(0.0).anyTimes();
        expect(dummy1.getRtbFloor()).andReturn(0.0).anyTimes();

        final PricingEngineEntity dummy2 = createMock(PricingEngineEntity.class);
        expect(dummy2.getDcpFloor()).andReturn(0.0).anyTimes();
        expect(dummy2.getRtbFloor()).andReturn(1.2).anyTimes();
        replayAll();

        final PricingEngineEntity dummy3 = createMock(PricingEngineEntity.class);
        expect(dummy3.getDcpFloor()).andReturn(1.2).anyTimes();
        expect(dummy3.getRtbFloor()).andReturn(0.0).anyTimes();
        replayAll();

        final PricingEngineEntity dummy4 = createMock(PricingEngineEntity.class);
        expect(dummy4.getDcpFloor()).andReturn(1.2).anyTimes();
        expect(dummy4.getRtbFloor()).andReturn(1.2).anyTimes();
        replayAll();

        assertThat(tested.isObjectToBeDeleted(dummy1), is(equalTo(true)));
        assertThat(tested.isObjectToBeDeleted(dummy2), is(equalTo(false)));
        assertThat(tested.isObjectToBeDeleted(dummy3), is(equalTo(false)));
        assertThat(tested.isObjectToBeDeleted(dummy4), is(equalTo(false)));
        verifyAll();
    }
}
