package com.inmobi.adserve.channels.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import java.awt.Dimension;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(PowerMockRunner.class)
public class SlotSizeMappingTest {

    @Test
    public void testGetDimension() throws Exception {
        Long slot = 29L;
        Dimension expectedDimensions = new Dimension(250, 250);
        SlotSizeMapping.init();
        assertThat(SlotSizeMapping.getDimension(slot), is(equalTo(expectedDimensions)));
    }

    @Test
    public void testIsIXSupportedSlot() throws Exception {
        short slot = 21;
        Boolean expected = true;
        SlotSizeMapping.init();
        assertThat(SlotSizeMapping.isIXSupportedSlot(slot), is(equalTo(expected)));
    }

    @Test
    public void testIsIXSupportedSlotFalse() throws Exception {
        short slot = -1;
        Boolean expected = false;
        SlotSizeMapping.init();
        assertThat(SlotSizeMapping.isIXSupportedSlot(slot), is(equalTo(expected)));
    }

    @Test
    public void testGetIXMappedSlotId() throws Exception {
        short slot = 29;
        Integer expectedMappedValue = 14;
        SlotSizeMapping.init();
        assertThat(SlotSizeMapping.getIXMappedSlotId(slot), is(equalTo(expectedMappedValue)));
    }
}