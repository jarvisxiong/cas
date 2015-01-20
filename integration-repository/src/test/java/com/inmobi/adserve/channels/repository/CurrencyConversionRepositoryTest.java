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

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.channels.entity.CurrencyConversionEntity;
import com.inmobi.adserve.channels.util.Utils.TestUtils.improvedTimestamp;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;

/**
 * Created by anshul.soni on 30/12/14.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({NullAsZeroResultSetRow.class, ResultSetRow.class, CurrencyConversionRepository.class})
public class CurrencyConversionRepositoryTest {

    @Test
    public void testBuildObjectFromRow() throws Exception {
        final String currencyId = "INR";
        final improvedTimestamp modifiedOn = new improvedTimestamp(1395426600000L);

        final Double conversionRate = 40.12;
        final Timestamp startDate = new improvedTimestamp(1395340200000L);
        final Timestamp endDate = new improvedTimestamp(253377657000000L);
        final String expectedLogOutput =
                "Found Currency Conversion Entity : CurrencyConversionEntity(id=INR, currencyId=INR, conversionRate=40.12, startDate=2014-03-20 18:30:00.0, endDate=9999-03-21 18:30:00.0, modifiedOn=2014-03-21 18:30:00.0)";

        final NullAsZeroResultSetRow mockNullAsZeroResultSetRow = createMock(NullAsZeroResultSetRow.class);
        Logger mockLogger = createMock(Logger.class);

        expect(mockLogger.isDebugEnabled()).andReturn(true).anyTimes();
        mockLogger.debug(expectedLogOutput);
        expectLastCall().anyTimes();

        expect(mockNullAsZeroResultSetRow.getString("currency_id")).andReturn(currencyId).times(1);
        expect(mockNullAsZeroResultSetRow.getDouble("conversion_rate")).andReturn(conversionRate).times(1);
        expect(mockNullAsZeroResultSetRow.getTimestamp("start_date")).andReturn(startDate).times(1);
        expect(mockNullAsZeroResultSetRow.getTimestamp("end_date")).andReturn(endDate).times(1);
        expect(mockNullAsZeroResultSetRow.getTimestamp("modified_on")).andReturn(modifiedOn).times(1);
        expectNew(NullAsZeroResultSetRow.class, new Class[] {ResultSetRow.class}, null).andReturn(
                mockNullAsZeroResultSetRow).times(1);

        replayAll();

        final CurrencyConversionRepository tested = new CurrencyConversionRepository();
        MemberModifier.field(CurrencyConversionRepository.class, "logger").set(tested, mockLogger);

        final DBEntity<CurrencyConversionEntity, String> entity = tested.buildObjectFromRow(null);
        final CurrencyConversionEntity output = entity.getObject();

        assertThat(output.getCurrencyId(), is(equalTo(currencyId)));
        assertThat(output.getConversionRate(), is(equalTo(conversionRate)));
        assertThat(output.getStartDate(), is(equalTo(startDate)));
        assertThat(output.getEndDate(), is(equalTo(endDate)));
        assertThat(output.getModifiedOn(), is(equalTo((Timestamp) modifiedOn)));

        verifyAll();
    }

    @Test
    public void testIsObjectToBeDeleted() throws Exception {
        final CurrencyConversionRepository tested = new CurrencyConversionRepository();
        assertThat(tested.isObjectToBeDeleted(null), is(equalTo(false)));
    }

    @Test
    public void testGetHashIndexKeyBuilder() throws Exception {
        final CurrencyConversionRepository tested = new CurrencyConversionRepository();
        assertThat(tested.getHashIndexKeyBuilder(null), is(equalTo(null)));
    }

    @Test
    public void testQueryUniqueResult() throws Exception {
        final CurrencyConversionRepository tested = new CurrencyConversionRepository();
        assertThat(tested.queryUniqueResult(null), is(equalTo(null)));
    }
}
