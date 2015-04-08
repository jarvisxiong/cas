package com.inmobi.adserve.channels.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class IABCategoriesMap {
    public static final Long FAMILY_SAFE_BLOCK_CATEGORIES = 10000L;
    public static final Long PERFORMANCE_BLOCK_CATEGORIES = 10001L;
    private static Map<Long, String[]> categoriesToIABMapping = new HashMap<>();
    private static Map<String, String[]> uacCatToIABMapping = new HashMap<>();

    private IABCategoriesMap() {}

    static {
        categoriesToIABMapping.put(1L, new String[] {"IAB24"});
        categoriesToIABMapping.put(2L, new String[] {"IAB1-1", "IAB5"});
        categoriesToIABMapping.put(3L, new String[] {"IAB4", "IAB19-15", "IAB3", "IAB5-15"});
        categoriesToIABMapping.put(4L, new String[] {"IAB22"});
        categoriesToIABMapping.put(5L, new String[] {"IAB9-11"});
        categoriesToIABMapping.put(6L, new String[] {"IAB5-8"});
        categoriesToIABMapping.put(7L, new String[] {"IAB5"});
        categoriesToIABMapping.put(8L, new String[] {"IAB1", "IAB10-2", "IAB19-29"});
        categoriesToIABMapping.put(9L, new String[] {"IAB4-3", "IAB13"});
        categoriesToIABMapping.put(10L, new String[] {"IAB8"});
        categoriesToIABMapping.put(11L, new String[] {"IAB9-30"});
        categoriesToIABMapping.put(12L, new String[] {"IAB10-2", "IAB1", "IAB19-29"});
        categoriesToIABMapping.put(13L, new String[] {"IAB20-1"});
        categoriesToIABMapping.put(14L, new String[] {"IAB10-2", "IAB1", "IAB19-29"});
        categoriesToIABMapping.put(15L, new String[] {"IAB10-2", "IAB1", "IAB19-29"});
        categoriesToIABMapping.put(16L, new String[] {"IAB9-7"});
        categoriesToIABMapping.put(17L, new String[] {"IAB10-2", "IAB1", "IAB19-29"});
        categoriesToIABMapping.put(18L, new String[] {"IAB10-2", "IAB1", "IAB19-29"});
        categoriesToIABMapping.put(19L, new String[] {"IAB9-22", "IAB5"});
        categoriesToIABMapping.put(20L, new String[] {"IAB6"});
        categoriesToIABMapping.put(21L, new String[] {"IAB6-5", "IAB6-8", "IAB20-26"});
        categoriesToIABMapping.put(22L, new String[] {"IAB1-6"});
        categoriesToIABMapping.put(23L, new String[] {"IAB9-5"});
        categoriesToIABMapping.put(24L, new String[] {"IAB10-2", "IAB1", "IAB19-29"});
        categoriesToIABMapping.put(25L, new String[] {"IAB9-25"});
        categoriesToIABMapping.put(26L, new String[] {"IAB10-2", "IAB1", "IAB19-29"});
        categoriesToIABMapping.put(27L, new String[] {"IAB17"});
        categoriesToIABMapping.put(28L, new String[] {"IAB9-26"});
        categoriesToIABMapping.put(29L, new String[] {"IAB10-2", "IAB1", "IAB19-29"});
        categoriesToIABMapping.put(30L, new String[] {"IAB10-2", "IAB1", "IAB19-29"});
        categoriesToIABMapping.put(31L, new String[] {"IAB7"});
        categoriesToIABMapping.put(32L, new String[] {"IAB18"});
        categoriesToIABMapping.put(33L, new String[] {"IAB1-5"});
        categoriesToIABMapping.put(34L, new String[] {"IAB3-3", "IAB7"});
        categoriesToIABMapping.put(35L, new String[] {"IAB1-6"});
        categoriesToIABMapping.put(36L, new String[] {"IAB12"});
        categoriesToIABMapping.put(37L, new String[] {"IAB1-3", "IAB9-24", "IAB5-3"});
        categoriesToIABMapping.put(38L, new String[] {"IAB2"});
        categoriesToIABMapping.put(39L, new String[] {"IAB12"});
        categoriesToIABMapping.put(40L, new String[] {"IAB3", "IAB5-15"});
        categoriesToIABMapping.put(41L, new String[] {"IAB6-5", "IAB6-8", "IAB20-26"});
        categoriesToIABMapping.put(42L, new String[] {"IAB18-6", "IAB19-18"});
        categoriesToIABMapping.put(43L, new String[] {"IAB8"});
        categoriesToIABMapping.put(44L, new String[] {"IAB9"});
        categoriesToIABMapping.put(45L, new String[] {"IAB12"});
        categoriesToIABMapping.put(46L, new String[] {"IAB10-2", "IAB1", "IAB19-29"});
        categoriesToIABMapping.put(47L, new String[] {"IAB18"});
        categoriesToIABMapping.put(48L, new String[] {"IAB7"});
        categoriesToIABMapping.put(49L, new String[] {"IAB5-3"});
        categoriesToIABMapping.put(50L, new String[] {"IAB10", "IAB21"});
        categoriesToIABMapping.put(51L, new String[] {"IAB12"});
        categoriesToIABMapping.put(52L, new String[] {"IAB7-31", "IAB9"});
        categoriesToIABMapping.put(53L, new String[] {"IAB1-6", "IAB1-5"});
        categoriesToIABMapping.put(54L, new String[] {"IAB11"});
        categoriesToIABMapping.put(55L, new String[] {"IAB12"});
        categoriesToIABMapping.put(56L, new String[] {"IAB6"});
        categoriesToIABMapping.put(57L, new String[] {"IAB16"});
        categoriesToIABMapping.put(58L, new String[] {"IAB12"});
        categoriesToIABMapping.put(59L, new String[] {"IAB12"});
        categoriesToIABMapping.put(60L, new String[] {"IAB15"});
        categoriesToIABMapping.put(61L, new String[] {"IAB17"});
        categoriesToIABMapping.put(62L, new String[] {"IAB6-6", "IAB14-6"});
        categoriesToIABMapping.put(63L, new String[] {"IAB20"});
        categoriesToIABMapping.put(64L, new String[] {"IAB7-45", "IAB9"});
        categoriesToIABMapping.put(65L, new String[] {"IAB19"});
        categoriesToIABMapping.put(66L, new String[] {"IAB19"});
        categoriesToIABMapping.put(67L, new String[] {"IAB22"});
        categoriesToIABMapping.put(68L, new String[] {"IAB14-6"});
        categoriesToIABMapping.put(69L, new String[] {"IAB3-4", "IAB19"});
        categoriesToIABMapping.put(70L, new String[] {"IAB17"});
        categoriesToIABMapping.put(71L, new String[] {"IAB19"});
        categoriesToIABMapping.put(72L, new String[] {"IAB20"});
        categoriesToIABMapping.put(73L, new String[] {"IAB1-6", "IAB17"});
        categoriesToIABMapping.put(74L, new String[] {"IAB15-10"});
        categoriesToIABMapping.put(FAMILY_SAFE_BLOCK_CATEGORIES, new String[] {"IAB11", "IAB11-1", "IAB11-2",
                "IAB11-3", "IAB11-4", "IAB11-5", "IAB12", "IAB12-1", "IAB12-2", "IAB12-3", "IAB13-5", "IAB13-7",
                "IAB14-1", "IAB14-2", "IAB14-3", "IAB15-5", "IAB17-18", "IAB23-10", "IAB23-2", "IAB23-9", "IAB25",
                "IAB25-1", "IAB25-2", "IAB25-3", "IAB25-4", "IAB25-5", "IAB25-7", "IAB26", "IAB26-1", "IAB26-2",
                "IAB26-3", "IAB26-4", "IAB5-2", "IAB6-7", "IAB7", "IAB7-10", "IAB7-11", "IAB7-12", "IAB7-13",
                "IAB7-14", "IAB7-16", "IAB7-18", "IAB7-19", "IAB7-2", "IAB7-20", "IAB7-21", "IAB7-22", "IAB7-24",
                "IAB7-25", "IAB7-27", "IAB7-28", "IAB7-29", "IAB7-3", "IAB7-30", "IAB7-31", "IAB7-34", "IAB7-36",
                "IAB7-37", "IAB7-38", "IAB7-39", "IAB7-4", "IAB7-40", "IAB7-41", "IAB7-44", "IAB7-45", "IAB7-5",
                "IAB7-6", "IAB7-8", "IAB7-9", "IAB8-5", "IAB9-9", "IAB19-3"});
        categoriesToIABMapping.put(PERFORMANCE_BLOCK_CATEGORIES, new String[] {"IAB11-1", "IAB11-2", "IAB14-3",
                "IAB23-2", "IAB25-2", "IAB25-3", "IAB25-4", "IAB25-5", "IAB26", "IAB26-1", "IAB26-2", "IAB26-3",
                "IAB26-4", "IAB5-2", "IAB6-7", "IAB7-10", "IAB7-11", "IAB7-12", "IAB7-13", "IAB7-14", "IAB7-16",
                "IAB7-18", "IAB7-19", "IAB7-2", "IAB7-20", "IAB7-21", "IAB7-22", "IAB7-24", "IAB7-25", "IAB7-27",
                "IAB7-28", "IAB7-29", "IAB7-3", "IAB7-30", "IAB7-31", "IAB7-34", "IAB7-36", "IAB7-37", "IAB7-38",
                "IAB7-39", "IAB7-40", "IAB7-44", "IAB7-45", "IAB7-5", "IAB7-8", "IAB7-9", "IAB8-5", "IAB19-3"});


        // Android
        uacCatToIABMapping.put("books & reference", new String[] {"IAB1", "IAB1-1"});
        uacCatToIABMapping.put("business", new String[] {"IAB3"});
        uacCatToIABMapping.put("comics", new String[] {"IAB9", "IAB9-11"});
        uacCatToIABMapping.put("communication", new String[] {"IAB24"});
        uacCatToIABMapping.put("education", new String[] {"IAB5"});
        uacCatToIABMapping.put("entertainment", new String[] {"IAB1"});
        uacCatToIABMapping.put("finance", new String[] {"IAB13"});
        uacCatToIABMapping.put("games", new String[] {"IAB9", "IAB9-30"});
        uacCatToIABMapping.put("health & fitness", new String[] {"IAB7"});
        uacCatToIABMapping.put("libraries & demo", new String[] {"IAB19"});
        uacCatToIABMapping.put("lifestyle", new String[] {"IAB14"});
        uacCatToIABMapping.put("live wallpaper", new String[] {"IAB9"});
        uacCatToIABMapping.put("media & video", new String[] {"IAB1"});
        uacCatToIABMapping.put("medical", new String[] {"IAB7"});
        uacCatToIABMapping.put("music & audio", new String[] {"IAB1"});
        uacCatToIABMapping.put("news & magazines", new String[] {"IAB12"});
        uacCatToIABMapping.put("personalization", new String[] {"IAB19"});
        uacCatToIABMapping.put("photography", new String[] {"IAB9", "IAB9-23"});
        uacCatToIABMapping.put("productivity", new String[] {"IAB19"});
        uacCatToIABMapping.put("shopping", new String[] {"IAB22"});
        uacCatToIABMapping.put("social", new String[] {"IAB24"});
        uacCatToIABMapping.put("sports", new String[] {"IAB17"});
        uacCatToIABMapping.put("tools", new String[] {"IAB19"});
        uacCatToIABMapping.put("transportation", new String[] {"IAB20"});
        uacCatToIABMapping.put("travel & local", new String[] {"IAB20"});
        uacCatToIABMapping.put("weather", new String[] {"IAB12"});
        uacCatToIABMapping.put("widgets", new String[] {"IAB19"});

        // IOS
        uacCatToIABMapping.put("books", new String[] {"IAB1", "IAB1-1"});
        uacCatToIABMapping.put("business", new String[] {"IAB3"});
        uacCatToIABMapping.put("catalogues", new String[] {"IAB22"});
        uacCatToIABMapping.put("education", new String[] {"IAB5"});
        uacCatToIABMapping.put("entertainment", new String[] {"IAB1"});
        uacCatToIABMapping.put("finance", new String[] {"IAB13"});
        uacCatToIABMapping.put("food & drink", new String[] {"IAB8"});
        uacCatToIABMapping.put("games", new String[] {"IAB9", "IAB9-30"});
        uacCatToIABMapping.put("health & fitness", new String[] {"IAB7"});
        uacCatToIABMapping.put("lifestyle", new String[] {"IAB14"});
        uacCatToIABMapping.put("medical", new String[] {"IAB7"});
        uacCatToIABMapping.put("music", new String[] {"IAB1"});
        uacCatToIABMapping.put("navigation", new String[] {"IAB20"});
        uacCatToIABMapping.put("news", new String[] {"IAB12"});
        uacCatToIABMapping.put("newstand", new String[] {"IAB12"});
        uacCatToIABMapping.put("photo & video", new String[] {"IAB9", "IAB9-23"});
        uacCatToIABMapping.put("productivity", new String[] {"IAB3"});
        uacCatToIABMapping.put("reference", new String[] {"IAB24"});
        uacCatToIABMapping.put("social networking", new String[] {"IAB24"});
        uacCatToIABMapping.put("sports", new String[] {"IAB17"});
        uacCatToIABMapping.put("travel", new String[] {"IAB20"});
        uacCatToIABMapping.put("utilities", new String[] {"IAB3"});
        uacCatToIABMapping.put("weather", new String[] {"IAB15", "IAB15-10"});

        // Game extras
        uacCatToIABMapping.put("game", new String[] {"IAB9", "IAB9-30"});
        uacCatToIABMapping.put("game_racing", new String[] {"IAB9", "IAB9-30"});
        uacCatToIABMapping.put("game_adventure", new String[] {"IAB9", "IAB9-30"});
        uacCatToIABMapping.put("game_strategy", new String[] {"IAB9", "IAB9-30"});
        uacCatToIABMapping.put("game_trivia", new String[] {"IAB9", "IAB9-30"});
        uacCatToIABMapping.put("game_arcade", new String[] {"IAB9", "IAB9-30"});
        uacCatToIABMapping.put("game_sports", new String[] {"IAB9", "IAB9-30"});
        uacCatToIABMapping.put("game_board", new String[] {"IAB9", "IAB9-30"});
        uacCatToIABMapping.put("game_word", new String[] {"IAB9", "IAB9-30"});
        uacCatToIABMapping.put("game_simulation", new String[] {"IAB9", "IAB9-30"});
        uacCatToIABMapping.put("game_role_playing", new String[] {"IAB9", "IAB9-30"});
        uacCatToIABMapping.put("game_casino", new String[] {"IAB9", "IAB9-30"});
        uacCatToIABMapping.put("game_action", new String[] {"IAB9", "IAB9-30"});
        uacCatToIABMapping.put("game_family", new String[] {"IAB9", "IAB9-30"});
        uacCatToIABMapping.put("game_educational", new String[] {"IAB9", "IAB9-30"});
        uacCatToIABMapping.put("game_casual", new String[] {"IAB9", "IAB9-30"});
        uacCatToIABMapping.put("game_music", new String[] {"IAB9", "IAB9-30"});
        uacCatToIABMapping.put("game_puzzle", new String[] {"IAB9", "IAB9-30"});
        uacCatToIABMapping.put("game_card", new String[] {"IAB9", "IAB9-30"});
        uacCatToIABMapping.put("sports_games", new String[] {"IAB9", "IAB9-30"});

        // Other Extras
        uacCatToIABMapping.put("music_and_audio", new String[] {"IAB1"});
        uacCatToIABMapping.put("health_and_fitness", new String[] {"IAB7"});
        uacCatToIABMapping.put("book", new String[] {"IAB1", "IAB1-1"});
        uacCatToIABMapping.put("books_and_reference", new String[] {"IAB1", "IAB1-1"});
        uacCatToIABMapping.put("catalogs", new String[] {"IAB22"});
        uacCatToIABMapping.put("news_and_magazines", new String[] {"IAB12"});
        uacCatToIABMapping.put("news & politics", new String[] {"IAB12"});
        uacCatToIABMapping.put("travel_and_local", new String[] {"IAB20"});
        uacCatToIABMapping.put("libraries_and_demo", new String[] {"IAB19"});
        uacCatToIABMapping.put("media_and_video", new String[] {"IAB1"});

        /*
         * 
        "APPLICATION";220
        "Action & Adventure";1
        "Blues";1
        "CARDS";453
        "Developer Tools";1
        "ARCADE";1345
        "BRAIN";1517
        "Graphics & Design";2
        "RACING";547
        "CASUAL";1408
        "apps";11
         */
    }

    /**
     * Maps inmobi categories to IAB categories.
     * 
     * @param List of new inmobi categories.
     * @return List of IAB Categories code.
     */
    public static List<String> getIABCategories(final Long category) {
        final String[] categories = categoriesToIABMapping.get(category);
        if (null != categories) {
            return new ArrayList<String>(Arrays.asList(categories));
        } else {
            return new ArrayList<String>();
        }
    }

    /**
     * Maps inmobi categories to IAB categories.
     * 
     * @param categories
     * @return
     */
    public static List<String> getIABCategories(final List<Long> categories) {
        final Set<String> iabCategoriesSet= new HashSet<>();
        if (categories != null) {
            for (final Long cat : categories) {
                if (null != categoriesToIABMapping.get(cat)) {
                    iabCategoriesSet.addAll(Arrays.asList(categoriesToIABMapping.get(cat)));
                }
            }
        }
        return new ArrayList<>(iabCategoriesSet);
    }

    /**
     * Maps UAC categories to IAB categories.
     * 
     * @param uacCategory
     * @return
     */
    public static List<String> getIABCategoriesFromUAC(final String uacCategory) {
        if (uacCategory != null) {
            final String[] categories = uacCatToIABMapping.get(uacCategory.toLowerCase());
            if (null != categories) {
                return new ArrayList<String>(Arrays.asList(categories));
            }
        }
        return new ArrayList<String>();
    }

    /**
     * Maps UAC categories to IAB categories.
     * 
     * @param uacCategories
     * @return
     */
    public static List<String> getIABCategoriesFromUAC(final List<String> uacCategories) {
        final Set<String> iabCategoriesSet = new HashSet<String>();
        if (uacCategories != null) {
            for (final String cat : uacCategories) {
                if (cat != null) {
                    final String[] categories = uacCatToIABMapping.get(cat.toLowerCase());
                    if (null != categories) {
                        iabCategoriesSet.addAll(Arrays.asList(categories));
                    }
                }
            }
        }
        return new ArrayList<>(iabCategoriesSet);
    }
}
