package com.inmobi.adserve.channels.api;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class SlotSizeMappingTest {

    @Test
    public void testIsIXSupportedSlot() throws Exception {
        final short slot = 21;
        final Boolean expected = true;
        assertThat(SlotSizeMapping.isIXSupportedSlot(slot), is(equalTo(expected)));
    }

    @Test
    public void testIsIXSupportedSlotFalse() throws Exception {
        final short slot = -1;
        final Boolean expected = false;
        assertThat(SlotSizeMapping.isIXSupportedSlot(slot), is(equalTo(expected)));
    }

    @Test
    public void testGetIXMappedSlotId() throws Exception {
        final short slot = 29;
        final Integer expectedMappedValue = 14;
        assertThat(SlotSizeMapping.getIXMappedSlotId(slot), is(equalTo(expectedMappedValue)));
    }
}
