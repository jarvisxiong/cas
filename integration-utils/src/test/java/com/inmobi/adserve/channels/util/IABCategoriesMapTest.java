package com.inmobi.adserve.channels.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class IABCategoriesMapTest {

    @Test
    public void testGetIABCategories() throws Exception {
        final Long category = IABCategoriesMap.FAMILY_SAFE_BLOCK_CATEGORIES;
        final List<String> expected =
                new ArrayList<>(Arrays.asList(new String[] {"IAB11", "IAB11-1", "IAB11-2", "IAB11-3", "IAB11-4",
                        "IAB11-5", "IAB12", "IAB12-1", "IAB12-2", "IAB12-3", "IAB13-5", "IAB13-7", "IAB14-1",
                        "IAB14-2", "IAB14-3", "IAB15-5", "IAB17-18", "IAB23-10", "IAB23-2", "IAB23-9", "IAB25",
                        "IAB25-1", "IAB25-2", "IAB25-3", "IAB25-4", "IAB25-5", "IAB25-7", "IAB26", "IAB26-1",
                        "IAB26-2", "IAB26-3", "IAB26-4", "IAB5-2", "IAB6-7", "IAB7", "IAB7-10", "IAB7-11", "IAB7-12",
                        "IAB7-13", "IAB7-14", "IAB7-16", "IAB7-18", "IAB7-19", "IAB7-2", "IAB7-20", "IAB7-21",
                        "IAB7-22", "IAB7-24", "IAB7-25", "IAB7-27", "IAB7-28", "IAB7-29", "IAB7-3", "IAB7-30",
                        "IAB7-31", "IAB7-34", "IAB7-36", "IAB7-37", "IAB7-38", "IAB7-39", "IAB7-4", "IAB7-40",
                        "IAB7-41", "IAB7-44", "IAB7-45", "IAB7-5", "IAB7-6", "IAB7-8", "IAB7-9", "IAB8-5", "IAB9-9",
                        "IAB19-3"}));
        assertThat(IABCategoriesMap.getIABCategories(category), is(equalTo(expected)));
    }

    @Test
    public void testGetIABCategoriesNull() throws Exception {
        final Long category = -1L;
        final List<String> expected = new ArrayList<String>();
        assertThat(IABCategoriesMap.getIABCategories(category), is(equalTo(expected)));
    }

    @Test
    public void testGetIABUACCategoriesFail() throws Exception {
        final List<String> expected = new ArrayList<String>();
        assertThat(IABCategoriesMap.getIABCategoriesFromUAC("xyz"), is(equalTo(expected)));
        assertThat(IABCategoriesMap.getIABCategoriesFromUAC(expected), is(equalTo(expected)));
    }

    @Test
    public void testGetIABUACCategoriesCaseInsensitive() throws Exception {
        assertThat(IABCategoriesMap.getIABCategoriesFromUAC("games"),
                is(IABCategoriesMap.getIABCategoriesFromUAC("Games")));
    }

    @Test
    public void testGetIABCategoriesSet() throws Exception {
        final List<Long> category =
                Arrays.asList(IABCategoriesMap.FAMILY_SAFE_BLOCK_CATEGORIES,
                        IABCategoriesMap.PERFORMANCE_BLOCK_CATEGORIES);

        final Set<String> iabCategoriesSet = new HashSet<String>();
        for (int i = 0; i < category.size(); ++i) {
            iabCategoriesSet.addAll(IABCategoriesMap.getIABCategories(category.get(i)));
        }
        final List<String> expected = new ArrayList<>(iabCategoriesSet);
        assertThat(IABCategoriesMap.getIABCategories(category), is(equalTo(expected)));
    }


}
