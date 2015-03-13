package com.inmobi.adserve.channels.util.Utils;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class ImpressionIdGeneratorTest {
    private static ImpressionIdGenerator impressionIdGenerator;

    @BeforeClass
    public static void setUp() {
        short hostIdCode = (short) 5;
        byte dataCenterIdCode = 1;
        // Not using the static init method for ImpressionIdGenerator
        impressionIdGenerator = new ImpressionIdGenerator(hostIdCode, dataCenterIdCode);
    }

    /**
     * Generates 3 impression ids: id$1, id$2(generated from id$1 by resetting the int key)
     * and id$3(generated from id$2 by resetting the int key with the old int key)
     * Test checks that id$1 and id$3 are identical
     */
    @Test
    public void testResetWilburyIntKeyAndBack() {
        long old_adId = 75L;
        long new_adId = 175L;

        String id$1 = impressionIdGenerator.getImpressionId(old_adId);
        String id$2 = impressionIdGenerator.resetWilburyIntKey(id$1, (int) new_adId).toString();
        String id$3 = impressionIdGenerator.resetWilburyIntKey(id$2, (int) old_adId).toString();

        assertThat(id$1, is(equalTo(id$3)));
    }

    /**
     * Generates 4 impression ids: id$1, id$2(generated from id$1 by resetting the int key with key$1),
     * id$3(generated from id$2 by resetting the int key with key$2) and
     * id$4(generated from id$1 by resetting the int key with key$2)
     * Test checks that id$3 and id$4 are identical
     */
    @Test
    public void testResetWilburyIntKeyMultiplePaths() {
        long key$0 = 75L;
        long key$1 = 175L;
        long key$2 = 275L;

        String id$1 = impressionIdGenerator.getImpressionId(key$0);
        String id$2 = impressionIdGenerator.resetWilburyIntKey(id$1, (int) key$1).toString();
        String id$3 = impressionIdGenerator.resetWilburyIntKey(id$2, (int) key$2).toString();
        String id$4 = impressionIdGenerator.resetWilburyIntKey(id$1, (int) key$2).toString();

        assertThat(id$3, is(equalTo(id$4)));
    }

    /**
     * Runs a series of parameterised tests which generate 3 impression ids:
     * id$1(generated using key[0],
     * id$2(generated from id$1 by resetting the int key with key[1]
     * id$3(generated from id$2 by resetting the int key with the old int key, i.e, key[0])
     *
     * Test checks that id$1 and id$3 are identical in all cases where Key[1] is at or beyond the integer boundary.
     * This is to ensure that the Wilbury library is working as intended.
     */
    @Test
    public void testResetWilburyIntKeyIntegerCornerCases() {
        long[][] keys = {
                {75, Integer.MAX_VALUE},
                {75, Integer.MAX_VALUE+1},
                {75, Integer.MIN_VALUE},
                {75, Integer.MIN_VALUE-1},
                {75, -1L}, // Key[1] has all bits set
                {75, 0L} // Key[1] has all bits unset
        };

        String id$1, id$2, id$3;
        for (long[] key : keys) {
            id$1 = impressionIdGenerator.getImpressionId(key[0]);
            id$2 = impressionIdGenerator.resetWilburyIntKey(id$1, (int) key[1]).toString();
            id$3 = impressionIdGenerator.resetWilburyIntKey(id$2, (int) key[0]).toString();
            assertThat(id$1, is(equalTo(id$3)));
        }
    }

}