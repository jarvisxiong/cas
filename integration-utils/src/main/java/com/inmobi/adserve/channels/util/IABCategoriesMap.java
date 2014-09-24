package com.inmobi.adserve.channels.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class IABCategoriesMap implements IABCategoriesInterface {

    private static HashMap<Long, String[]> categoriesToIABMapping       = new HashMap<Long, String[]>();

    public static final Long                     FAMILY_SAFE_BLOCK_CATEGORIES = 10000L;
    public static final Long                     PERFORMANCE_BLOCK_CATEGORIES = 10001L;
    static {

        categoriesToIABMapping.put(1L, new String[] { "IAB24" });
        categoriesToIABMapping.put(2L, new String[] { "IAB1-1", "IAB5" });
        categoriesToIABMapping.put(3L, new String[] { "IAB4", "IAB19-15", "IAB3", "IAB5-15" });
        categoriesToIABMapping.put(4L, new String[] { "IAB22" });
        categoriesToIABMapping.put(5L, new String[] { "IAB9-11" });
        categoriesToIABMapping.put(6L, new String[] { "IAB5-8" });
        categoriesToIABMapping.put(7L, new String[] { "IAB5" });
        categoriesToIABMapping.put(8L, new String[] { "IAB1", "IAB10-2", "IAB19-29" });
        categoriesToIABMapping.put(9L, new String[] { "IAB4-3", "IAB13" });
        categoriesToIABMapping.put(10L, new String[] { "IAB8" });
        categoriesToIABMapping.put(11L, new String[] { "IAB9-30" });
        categoriesToIABMapping.put(12L, new String[] { "IAB10-2", "IAB1", "IAB19-29" });
        categoriesToIABMapping.put(13L, new String[] { "IAB20-1" });
        categoriesToIABMapping.put(14L, new String[] { "IAB10-2", "IAB1", "IAB19-29" });
        categoriesToIABMapping.put(15L, new String[] { "IAB10-2", "IAB1", "IAB19-29" });
        categoriesToIABMapping.put(16L, new String[] { "IAB9-7" });
        categoriesToIABMapping.put(17L, new String[] { "IAB10-2", "IAB1", "IAB19-29" });
        categoriesToIABMapping.put(18L, new String[] { "IAB10-2", "IAB1", "IAB19-29" });
        categoriesToIABMapping.put(19L, new String[] { "IAB9-22", "IAB5" });
        categoriesToIABMapping.put(20L, new String[] { "IAB6" });
        categoriesToIABMapping.put(21L, new String[] { "IAB6-5", "IAB6-8", "IAB20-26" });
        categoriesToIABMapping.put(22L, new String[] { "IAB1-6" });
        categoriesToIABMapping.put(23L, new String[] { "IAB9-5" });
        categoriesToIABMapping.put(24L, new String[] { "IAB10-2", "IAB1", "IAB19-29" });
        categoriesToIABMapping.put(25L, new String[] { "IAB9-25" });
        categoriesToIABMapping.put(26L, new String[] { "IAB10-2", "IAB1", "IAB19-29" });
        categoriesToIABMapping.put(27L, new String[] { "IAB17" });
        categoriesToIABMapping.put(28L, new String[] { "IAB9-26" });
        categoriesToIABMapping.put(29L, new String[] { "IAB10-2", "IAB1", "IAB19-29" });
        categoriesToIABMapping.put(30L, new String[] { "IAB10-2", "IAB1", "IAB19-29" });
        categoriesToIABMapping.put(31L, new String[] { "IAB7" });
        categoriesToIABMapping.put(32L, new String[] { "IAB18" });
        categoriesToIABMapping.put(33L, new String[] { "IAB1-5" });
        categoriesToIABMapping.put(34L, new String[] { "IAB3-3", "IAB7" });
        categoriesToIABMapping.put(35L, new String[] { "IAB1-6" });
        categoriesToIABMapping.put(36L, new String[] { "IAB12" });
        categoriesToIABMapping.put(37L, new String[] { "IAB1-3", "IAB9-24", "IAB5-3" });
        categoriesToIABMapping.put(38L, new String[] { "IAB2" });
        categoriesToIABMapping.put(39L, new String[] { "IAB12" });
        categoriesToIABMapping.put(40L, new String[] { "IAB3", "IAB5-15" });
        categoriesToIABMapping.put(41L, new String[] { "IAB6-5", "IAB6-8", "IAB20-26" });
        categoriesToIABMapping.put(42L, new String[] { "IAB18-6", "IAB19-18" });
        categoriesToIABMapping.put(43L, new String[] { "IAB8" });
        categoriesToIABMapping.put(44L, new String[] { "IAB9" });
        categoriesToIABMapping.put(45L, new String[] { "IAB12" });
        categoriesToIABMapping.put(46L, new String[] { "IAB10-2", "IAB1", "IAB19-29" });
        categoriesToIABMapping.put(47L, new String[] { "IAB18" });
        categoriesToIABMapping.put(48L, new String[] { "IAB7" });
        categoriesToIABMapping.put(49L, new String[] { "IAB5-3" });
        categoriesToIABMapping.put(50L, new String[] { "IAB10", "IAB21" });
        categoriesToIABMapping.put(51L, new String[] { "IAB12" });
        categoriesToIABMapping.put(52L, new String[] { "IAB7-31", "IAB9" });
        categoriesToIABMapping.put(53L, new String[] { "IAB1-6", "IAB1-5" });
        categoriesToIABMapping.put(54L, new String[] { "IAB11" });
        categoriesToIABMapping.put(55L, new String[] { "IAB12" });
        categoriesToIABMapping.put(56L, new String[] { "IAB6" });
        categoriesToIABMapping.put(57L, new String[] { "IAB16" });
        categoriesToIABMapping.put(58L, new String[] { "IAB12" });
        categoriesToIABMapping.put(59L, new String[] { "IAB12" });
        categoriesToIABMapping.put(60L, new String[] { "IAB15" });
        categoriesToIABMapping.put(61L, new String[] { "IAB17" });
        categoriesToIABMapping.put(62L, new String[] { "IAB6-6", "IAB14-6" });
        categoriesToIABMapping.put(63L, new String[] { "IAB20" });
        categoriesToIABMapping.put(64L, new String[] { "IAB7-45", "IAB9" });
        categoriesToIABMapping.put(65L, new String[] { "IAB19" });
        categoriesToIABMapping.put(66L, new String[] { "IAB19" });
        categoriesToIABMapping.put(67L, new String[] { "IAB22" });
        categoriesToIABMapping.put(68L, new String[] { "IAB14-6" });
        categoriesToIABMapping.put(69L, new String[] { "IAB3-4", "IAB19" });
        categoriesToIABMapping.put(70L, new String[] { "IAB17" });
        categoriesToIABMapping.put(71L, new String[] { "IAB19" });
        categoriesToIABMapping.put(72L, new String[] { "IAB20" });
        categoriesToIABMapping.put(73L, new String[] { "IAB1-6", "IAB17" });
        categoriesToIABMapping.put(74L, new String[] { "IAB15-10" });
        categoriesToIABMapping.put(FAMILY_SAFE_BLOCK_CATEGORIES, new String[] { "IAB11", "IAB11-1", "IAB11-2",
            "IAB11-3", "IAB11-4", "IAB11-5", "IAB12", "IAB12-1", "IAB12-2", "IAB12-3", "IAB13-5", "IAB13-7", "IAB14-1",
            "IAB14-2", "IAB14-3", "IAB15-5", "IAB17-18", "IAB23-10", "IAB23-2", "IAB23-9", "IAB25", "IAB25-1",
            "IAB25-2", "IAB25-3", "IAB25-4", "IAB25-5", "IAB25-7", "IAB26", "IAB26-1", "IAB26-2", "IAB26-3", "IAB26-4",
            "IAB5-2", "IAB6-7", "IAB7", "IAB7-10", "IAB7-11", "IAB7-12", "IAB7-13", "IAB7-14", "IAB7-16", "IAB7-18",
            "IAB7-19", "IAB7-2", "IAB7-20", "IAB7-21", "IAB7-22", "IAB7-24", "IAB7-25", "IAB7-27", "IAB7-28",
            "IAB7-29", "IAB7-3", "IAB7-30", "IAB7-31", "IAB7-34", "IAB7-36", "IAB7-37", "IAB7-38", "IAB7-39", "IAB7-4",
            "IAB7-40", "IAB7-41", "IAB7-44", "IAB7-45", "IAB7-5", "IAB7-6", "IAB7-8", "IAB7-9", "IAB8-5", "IAB9-9","IAB19-3" });
        categoriesToIABMapping.put(PERFORMANCE_BLOCK_CATEGORIES, new String[] { "IAB11-1", "IAB11-2", "IAB14-3",
            "IAB23-2", "IAB25-2", "IAB25-3", "IAB25-4", "IAB25-5", "IAB26", "IAB26-1", "IAB26-2", "IAB26-3", "IAB26-4",
            "IAB5-2", "IAB6-7", "IAB7-10", "IAB7-11", "IAB7-12", "IAB7-13", "IAB7-14", "IAB7-16", "IAB7-18", "IAB7-19",
            "IAB7-2", "IAB7-20", "IAB7-21", "IAB7-22", "IAB7-24", "IAB7-25", "IAB7-27", "IAB7-28", "IAB7-29", "IAB7-3",
            "IAB7-30", "IAB7-31", "IAB7-34", "IAB7-36", "IAB7-37", "IAB7-38", "IAB7-39", "IAB7-40", "IAB7-44",
            "IAB7-45", "IAB7-5", "IAB7-8", "IAB7-9", "IAB8-5","IAB19-3" });
    }

    @Override
    public List<String> getIABCategories(Long category) {
        return new ArrayList<String>(Arrays.asList(categoriesToIABMapping.get(category)));
    }

    @Override
    public List<String> getIABCategories(List<Long> categories) {
        Set<String> iabCategoriesSet = new HashSet<String>();
        for (Long cat : categories) {
            if (null != categoriesToIABMapping.get(cat)) {
                iabCategoriesSet.addAll(Arrays.asList(categoriesToIABMapping.get(cat)));
            }
        }
        List<String> iabCategoriesList = new ArrayList<String>();
        iabCategoriesList.addAll(iabCategoriesSet);
        return iabCategoriesList;
    }
}