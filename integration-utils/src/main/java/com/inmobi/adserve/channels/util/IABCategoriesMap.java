package com.inmobi.adserve.channels.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class IABCategoriesMap implements IABCategoriesInterface {

    private static HashMap<Long, String[]> categoriesToIABMapping       = new HashMap<Long, String[]>();

    public static Long                     FAMILY_SAFE_BLOCK_CATEGORIES = 10000l;
    public static Long                     PERFORMANCE_BLOCK_CATEGORIES = 10001l;
    static {

        categoriesToIABMapping.put(1l, new String[] { "IAB24" });
        categoriesToIABMapping.put(2l, new String[] { "IAB1-1", "IAB5" });
        categoriesToIABMapping.put(3l, new String[] { "IAB4", "IAB19-15", "IAB3", "IAB5-15" });
        categoriesToIABMapping.put(4l, new String[] { "IAB22" });
        categoriesToIABMapping.put(5l, new String[] { "IAB9-11" });
        categoriesToIABMapping.put(6l, new String[] { "IAB5-8" });
        categoriesToIABMapping.put(7l, new String[] { "IAB5" });
        categoriesToIABMapping.put(8l, new String[] { "IAB1", "IAB10-2", "IAB19-29" });
        categoriesToIABMapping.put(9l, new String[] { "IAB4-3", "IAB13" });
        categoriesToIABMapping.put(10l, new String[] { "IAB8" });
        categoriesToIABMapping.put(11l, new String[] { "IAB9-30" });
        categoriesToIABMapping.put(12l, new String[] { "IAB10-2", "IAB1", "IAB19-29" });
        categoriesToIABMapping.put(13l, new String[] { "IAB20-1" });
        categoriesToIABMapping.put(14l, new String[] { "IAB10-2", "IAB1", "IAB19-29" });
        categoriesToIABMapping.put(15l, new String[] { "IAB10-2", "IAB1", "IAB19-29" });
        categoriesToIABMapping.put(16l, new String[] { "IAB9-7" });
        categoriesToIABMapping.put(17l, new String[] { "IAB10-2", "IAB1", "IAB19-29" });
        categoriesToIABMapping.put(18l, new String[] { "IAB10-2", "IAB1", "IAB19-29" });
        categoriesToIABMapping.put(19l, new String[] { "IAB9-22", "IAB5" });
        categoriesToIABMapping.put(20l, new String[] { "IAB6" });
        categoriesToIABMapping.put(21l, new String[] { "IAB6-5", "IAB6-8", "IAB20-26" });
        categoriesToIABMapping.put(22l, new String[] { "IAB1-6" });
        categoriesToIABMapping.put(23l, new String[] { "IAB9-5" });
        categoriesToIABMapping.put(24l, new String[] { "IAB10-2", "IAB1", "IAB19-29" });
        categoriesToIABMapping.put(25l, new String[] { "IAB9-25" });
        categoriesToIABMapping.put(26l, new String[] { "IAB10-2", "IAB1", "IAB19-29" });
        categoriesToIABMapping.put(27l, new String[] { "IAB17" });
        categoriesToIABMapping.put(28l, new String[] { "IAB9-26" });
        categoriesToIABMapping.put(29l, new String[] { "IAB10-2", "IAB1", "IAB19-29" });
        categoriesToIABMapping.put(30l, new String[] { "IAB10-2", "IAB1", "IAB19-29" });
        categoriesToIABMapping.put(31l, new String[] { "IAB7" });
        categoriesToIABMapping.put(32l, new String[] { "IAB18" });
        categoriesToIABMapping.put(33l, new String[] { "IAB1-5" });
        categoriesToIABMapping.put(34l, new String[] { "IAB3-3", "IAB7" });
        categoriesToIABMapping.put(35l, new String[] { "IAB1-6" });
        categoriesToIABMapping.put(36l, new String[] { "IAB12" });
        categoriesToIABMapping.put(37l, new String[] { "IAB1-3", "IAB9-24", "IAB5-3" });
        categoriesToIABMapping.put(38l, new String[] { "IAB2" });
        categoriesToIABMapping.put(39l, new String[] { "IAB12" });
        categoriesToIABMapping.put(40l, new String[] { "IAB3", "IAB5-15" });
        categoriesToIABMapping.put(41l, new String[] { "IAB6-5", "IAB6-8", "IAB20-26" });
        categoriesToIABMapping.put(42l, new String[] { "IAB18-6", "IAB19-18" });
        categoriesToIABMapping.put(43l, new String[] { "IAB8" });
        categoriesToIABMapping.put(44l, new String[] { "IAB9" });
        categoriesToIABMapping.put(45l, new String[] { "IAB12" });
        categoriesToIABMapping.put(46l, new String[] { "IAB10-2", "IAB1", "IAB19-29" });
        categoriesToIABMapping.put(47l, new String[] { "IAB18" });
        categoriesToIABMapping.put(48l, new String[] { "IAB7" });
        categoriesToIABMapping.put(49l, new String[] { "IAB5-3" });
        categoriesToIABMapping.put(50l, new String[] { "IAB10", "IAB21" });
        categoriesToIABMapping.put(51l, new String[] { "IAB12" });
        categoriesToIABMapping.put(52l, new String[] { "IAB7-31", "IAB9" });
        categoriesToIABMapping.put(53l, new String[] { "IAB1-6", "IAB1-5" });
        categoriesToIABMapping.put(54l, new String[] { "IAB11" });
        categoriesToIABMapping.put(55l, new String[] { "IAB12" });
        categoriesToIABMapping.put(56l, new String[] { "IAB6" });
        categoriesToIABMapping.put(57l, new String[] { "IAB16" });
        categoriesToIABMapping.put(58l, new String[] { "IAB12" });
        categoriesToIABMapping.put(59l, new String[] { "IAB12" });
        categoriesToIABMapping.put(60l, new String[] { "IAB15" });
        categoriesToIABMapping.put(61l, new String[] { "IAB17" });
        categoriesToIABMapping.put(62l, new String[] { "IAB6-6", "IAB14-6" });
        categoriesToIABMapping.put(63l, new String[] { "IAB20" });
        categoriesToIABMapping.put(64l, new String[] { "IAB7-45", "IAB9" });
        categoriesToIABMapping.put(65l, new String[] { "IAB19" });
        categoriesToIABMapping.put(66l, new String[] { "IAB19" });
        categoriesToIABMapping.put(67l, new String[] { "IAB22" });
        categoriesToIABMapping.put(68l, new String[] { "IAB14-6" });
        categoriesToIABMapping.put(69l, new String[] { "IAB3-4", "IAB19" });
        categoriesToIABMapping.put(70l, new String[] { "IAB17" });
        categoriesToIABMapping.put(71l, new String[] { "IAB19" });
        categoriesToIABMapping.put(72l, new String[] { "IAB20" });
        categoriesToIABMapping.put(73l, new String[] { "IAB1-6", "IAB17" });
        categoriesToIABMapping.put(74l, new String[] { "IAB15-10" });
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