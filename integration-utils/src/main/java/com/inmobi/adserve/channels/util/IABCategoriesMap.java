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
        // Mapping updated on 27-June-2016
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
        categoriesToIABMapping.put(12L, new String[] {"IAB9-30"});
        categoriesToIABMapping.put(13L, new String[] {"IAB9-30"});
        categoriesToIABMapping.put(14L, new String[] {"IAB9-30"});
        categoriesToIABMapping.put(15L, new String[] {"IAB9-5"});
        categoriesToIABMapping.put(16L, new String[] {"IAB9-7"});
        categoriesToIABMapping.put(17L, new String[] {"IAB9-30"});
        categoriesToIABMapping.put(18L, new String[] {"IAB9-30"});
        categoriesToIABMapping.put(19L, new String[] {"IAB9-30", "IAB5"});
        categoriesToIABMapping.put(20L, new String[] {"IAB9-30", "IAB6"});
        categoriesToIABMapping.put(21L, new String[] {"IAB9-30", "IAB6-5"});
        categoriesToIABMapping.put(22L, new String[] {"IAB1-6"});
        categoriesToIABMapping.put(23L, new String[] {"IAB9-5"});
        categoriesToIABMapping.put(24L, new String[] {"IAB9-30"});
        categoriesToIABMapping.put(25L, new String[] {"IAB9-30", "IAB9-25"});
        categoriesToIABMapping.put(26L, new String[] {"IAB9-30", "IAB9-25"});
        categoriesToIABMapping.put(27L, new String[] {"IAB17"});
        categoriesToIABMapping.put(28L, new String[] {"IAB9-30"});
        categoriesToIABMapping.put(29L, new String[] {"IAB9-30", "IAB5"});
        categoriesToIABMapping.put(30L, new String[] {"IAB9-30", "IAB5"});
        categoriesToIABMapping.put(31L, new String[] {"IAB7"});
        categoriesToIABMapping.put(32L, new String[] {"IAB18"});
        categoriesToIABMapping.put(33L, new String[] {"IAB1", "IAB1-5"});
        categoriesToIABMapping.put(34L, new String[] {"IAB3-3", "IAB7"});
        categoriesToIABMapping.put(35L, new String[] {"IAB1", "IAB1-6"});
        categoriesToIABMapping.put(36L, new String[] {"IAB12"});
        categoriesToIABMapping.put(37L, new String[] {"IAB9", "IAB9-23", "IAB9-2"});
        categoriesToIABMapping.put(38L, new String[] {"IAB2"});
        categoriesToIABMapping.put(39L, new String[] {"IAB14-7"});
        categoriesToIABMapping.put(40L, new String[] {"IAB13", "IAB3", "IAB5-15"});
        categoriesToIABMapping.put(41L, new String[] {"IAB6-5", "IAB6-8", "IAB20-26", "IAB5"});
        categoriesToIABMapping.put(42L, new String[] {"IAB18-6", "IAB19-18"});
        categoriesToIABMapping.put(43L, new String[] {"IAB8"});
        categoriesToIABMapping.put(44L, new String[] {"IAB9"});
        categoriesToIABMapping.put(45L, new String[] {"IAB19-29", "IAB10-1"});
        categoriesToIABMapping.put(46L, new String[] {"IAB10-2", "IAB1", "IAB19-29"});
        categoriesToIABMapping.put(47L, new String[] {"IAB18"});
        categoriesToIABMapping.put(48L, new String[] {"IAB7"});
        categoriesToIABMapping.put(49L, new String[] {"IAB5-3"});
        categoriesToIABMapping.put(50L, new String[] {"IAB10", "IAB21"});
        categoriesToIABMapping.put(51L, new String[] {"IAB12"});
        categoriesToIABMapping.put(52L, new String[] {"IAB7-31", "IAB9"});
        categoriesToIABMapping.put(53L, new String[] {"IAB1-6", "IAB1-5"});
        categoriesToIABMapping.put(54L, new String[] {"IAB11", "IAB12"});
        categoriesToIABMapping.put(55L, new String[] {"IAB10"});
        categoriesToIABMapping.put(56L, new String[] {"IAB6"});
        categoriesToIABMapping.put(57L, new String[] {"IAB16"});
        categoriesToIABMapping.put(58L, new String[] {"IAB3"});
        categoriesToIABMapping.put(59L, new String[] {"IAB12"});
        categoriesToIABMapping.put(60L, new String[] {"IAB15"});
        categoriesToIABMapping.put(61L, new String[] {"IAB17"});
        categoriesToIABMapping.put(62L, new String[] {"IAB6-6", "IAB14-6"});
        categoriesToIABMapping.put(63L, new String[] {"IAB20"});
        categoriesToIABMapping.put(64L, new String[] {"IAB7-45", "IAB9"});
        categoriesToIABMapping.put(65L, new String[] {"IAB19"});
        categoriesToIABMapping.put(66L, new String[] {"IAB19"});
        categoriesToIABMapping.put(67L, new String[] {"IAB22"});
        categoriesToIABMapping.put(68L, new String[] {"IAB19-29", "IAB9"});
        categoriesToIABMapping.put(69L, new String[] {"IAB3-4", "IAB19"});
        categoriesToIABMapping.put(70L, new String[] {"IAB17"});
        categoriesToIABMapping.put(71L, new String[] {"IAB19"});
        categoriesToIABMapping.put(72L, new String[] {"IAB20"});
        categoriesToIABMapping.put(73L, new String[] {"IAB19"});
        categoriesToIABMapping.put(74L, new String[] {"IAB15-10"});
        categoriesToIABMapping.put(FAMILY_SAFE_BLOCK_CATEGORIES,
                new String[] {"IAB11", "IAB11-1", "IAB11-2", "IAB11-3", "IAB11-4", "IAB11-5", "IAB12", "IAB12-1",
                        "IAB12-2", "IAB12-3", "IAB13-5", "IAB13-7", "IAB14-1", "IAB14-2", "IAB14-3", "IAB15-5",
                        "IAB17-18", "IAB23-10", "IAB23-2", "IAB23-9", "IAB25", "IAB25-1", "IAB25-2", "IAB25-3",
                        "IAB25-4", "IAB25-5", "IAB25-7", "IAB26", "IAB26-1", "IAB26-2", "IAB26-3", "IAB26-4", "IAB5-2",
                        "IAB6-7", "IAB7", "IAB7-10", "IAB7-11", "IAB7-12", "IAB7-13", "IAB7-14", "IAB7-16", "IAB7-18",
                        "IAB7-19", "IAB7-2", "IAB7-20", "IAB7-21", "IAB7-22", "IAB7-24", "IAB7-25", "IAB7-27",
                        "IAB7-28", "IAB7-29", "IAB7-3", "IAB7-30", "IAB7-31", "IAB7-34", "IAB7-36", "IAB7-37",
                        "IAB7-38", "IAB7-39", "IAB7-4", "IAB7-40", "IAB7-41", "IAB7-44", "IAB7-45", "IAB7-5", "IAB7-6",
                        "IAB7-8", "IAB7-9", "IAB8-5", "IAB9-9", "IAB19-3"});
        categoriesToIABMapping.put(PERFORMANCE_BLOCK_CATEGORIES,
                new String[] {"IAB11-1", "IAB11-2", "IAB14-3", "IAB23-2", "IAB25-2", "IAB25-3", "IAB25-4", "IAB25-5",
                        "IAB26", "IAB26-1", "IAB26-2", "IAB26-3", "IAB26-4", "IAB5-2", "IAB6-7", "IAB7-10", "IAB7-11",
                        "IAB7-12", "IAB7-13", "IAB7-14", "IAB7-16", "IAB7-18", "IAB7-19", "IAB7-2", "IAB7-20",
                        "IAB7-21", "IAB7-22", "IAB7-24", "IAB7-25", "IAB7-27", "IAB7-28", "IAB7-29", "IAB7-3",
                        "IAB7-30", "IAB7-31", "IAB7-34", "IAB7-36", "IAB7-37", "IAB7-38", "IAB7-39", "IAB7-40",
                        "IAB7-44", "IAB7-45", "IAB7-5", "IAB7-8", "IAB7-9", "IAB8-5", "IAB19-3"});


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

        // Mapping updated on 27-June-2016
        uacCatToIABMapping.put("action", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("actionaction", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("actionaction & adventure", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("adventure", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("adventureadventure", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("app_wallpaper", new String[] {"IAB19-2"});
        uacCatToIABMapping.put("app_widgets", new String[] {"IAB19-6"});
        uacCatToIABMapping.put("application", new String[] {"IAB19-6"});
        uacCatToIABMapping.put("apps", new String[] {"IAB19-6"});
        uacCatToIABMapping.put("arcade", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("arcade & action", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("arcadeaction & adventure", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("arts & photography", new String[] {"IAB1"});
        uacCatToIABMapping.put("automotive", new String[] {"IAB2"});
        uacCatToIABMapping.put("board", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("brain", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("brain & puzzle", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("brides & weddings", new String[] {"IAB14-7"});
        uacCatToIABMapping.put("business & investing", new String[] {"IAB13", "IAB13-7"});
        uacCatToIABMapping.put("business & personal finance", new String[] {"IAB13"});
        uacCatToIABMapping.put("card", new String[] {"IAB9-7"});
        uacCatToIABMapping.put("cardcard", new String[] {"IAB9-7"});
        uacCatToIABMapping.put("cards", new String[] {"IAB9-7"});
        uacCatToIABMapping.put("cards & casino", new String[] {"IAB9-7"});
        uacCatToIABMapping.put("casino", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("casual", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("casualaction & adventure", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("casualcasual", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("children's magazines", new String[] {"IAB5"});
        uacCatToIABMapping.put("computers & internet", new String[] {"IAB19"});
        uacCatToIABMapping.put("cooking", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("crafts & hobbies", new String[] {"IAB9-2", "IAB9"});
        uacCatToIABMapping.put("developer tools", new String[] {"IAB19"});
        uacCatToIABMapping.put("dice", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("educational", new String[] {"IAB5"});
        uacCatToIABMapping.put("educationaleducation", new String[] {"IAB5"});
        uacCatToIABMapping.put("electronics & audio", new String[] {"IAB19-29"});
        uacCatToIABMapping.put("entertainmentcreativity", new String[] {"IAB19-29"});
        uacCatToIABMapping.put("family", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("fashion & style", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("game_wallpaper", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("game_widgets", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("graphics & design", new String[] {"IAB9-2", "IAB19"});
        uacCatToIABMapping.put("health", new String[] {"IAB7"});
        uacCatToIABMapping.put("history", new String[] {"IAB5"});
        uacCatToIABMapping.put("home & garden", new String[] {"IAB10"});
        uacCatToIABMapping.put("kids", new String[] {"IAB6"});
        uacCatToIABMapping.put("literary magazines & journals", new String[] {"IAB12", "IAB1-1"});
        uacCatToIABMapping.put("magazines & newspapers", new String[] {"IAB12", "IAB1-1"});
        uacCatToIABMapping.put("media & videomusic & video", new String[] {"IAB19-29", "IAB1-5"});
        uacCatToIABMapping.put("men's interest", new String[] {"IAB9"});
        uacCatToIABMapping.put("mind & body", new String[] {"IAB7"});
        uacCatToIABMapping.put("movies & music", new String[] {"IAB19-29", "IAB1-5", "IAB1-6"});
        uacCatToIABMapping.put("mzgenre.action", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("mzgenre.adventure", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("mzgenre.apps.food_drink", new String[] {"IAB8"});
        uacCatToIABMapping.put("mzgenre.arcade", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("mzgenre.board", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("mzgenre.dice", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("mzgenre.education", new String[] {"IAB5"});
        uacCatToIABMapping.put("mzgenre.educational", new String[] {"IAB5"});
        uacCatToIABMapping.put("mzgenre.entertainment", new String[] {"IAB19-29"});
        uacCatToIABMapping.put("mzgenre.family", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("mzgenre.games", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("mzgenre.lifestyle", new String[] {"IAB9"});
        uacCatToIABMapping.put("mzgenre.medical", new String[] {"IAB7"});
        uacCatToIABMapping.put("mzgenre.photography", new String[] {"IAB9-23"});
        uacCatToIABMapping.put("mzgenre.productivity", new String[] {"IAB19"});
        uacCatToIABMapping.put("mzgenre.puzzle", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("mzgenre.roleplaying", new String[] {"IAB9-30", "IAB9-25"});
        uacCatToIABMapping.put("mzgenre.simulation", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("newsstand", new String[] {"IAB12"});
        uacCatToIABMapping.put("outdoors & nature", new String[] {"IAB10"});
        uacCatToIABMapping.put("parenting & family", new String[] {"IAB6"});
        uacCatToIABMapping.put("pets", new String[] {"IAB16"});
        uacCatToIABMapping.put("podcasts", new String[] {"IAB19-29"});
        uacCatToIABMapping.put("professional & trade", new String[] {"IAB3"});
        uacCatToIABMapping.put("puzzle", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("racing", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("racingaction & adventure", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("regional news", new String[] {"IAB12"});
        uacCatToIABMapping.put("role playing", new String[] {"IAB9-30", "IAB9-25"});
        uacCatToIABMapping.put("role playingaction & adventure", new String[] {"IAB9-30", "IAB9-25"});
        uacCatToIABMapping.put("role playingrole playing", new String[] {"IAB9-30", "IAB9-25"});
        uacCatToIABMapping.put("role-playing", new String[] {"IAB9-30", "IAB9-25"});
        uacCatToIABMapping.put("science", new String[] {"IAB15"});
        uacCatToIABMapping.put("simulation", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("sports & leisure", new String[] {"IAB17", "IAB19-29"});
        uacCatToIABMapping.put("sports games", new String[] {"IAB17"});
        uacCatToIABMapping.put("strategie", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("strategy", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("teens", new String[] {"IAB14-6"});
        uacCatToIABMapping.put("transport", new String[] {"IAB20"});
        uacCatToIABMapping.put("travel & regional", new String[] {"IAB20"});
        uacCatToIABMapping.put("trivia", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("women's interest", new String[] {"IAB9"});
        uacCatToIABMapping.put("word", new String[] {"IAB9-30"});
        uacCatToIABMapping.put("wordeducation", new String[] {"IAB9-30", "IAB5"});
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
        final Set<String> iabCategoriesSet = new HashSet<>();
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
