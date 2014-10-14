package com.inmobi.adserve.channels.util;

import java.util.HashMap;
import java.util.Map;


public class CategoryList {
    private static Map<Integer, String> categoryList                 = new HashMap<Integer, String>();
    private static int                  FAMILY_SAFE_BLOCK_CATEGORIES = 10000;
    private static int                  PERFORMANCE_BLOCK_CATEGORIES = 10001;

    static {
        categoryList.put(1, "Aggregator");
        categoryList.put(2, "Books & Reference");
        categoryList.put(3, "Business");
        categoryList.put(4, "Catalogs");
        categoryList.put(5, "Comics");
        categoryList.put(6, "Communication");
        categoryList.put(7, "Education");
        categoryList.put(8, "Entertainment");
        categoryList.put(9, "Finance");
        categoryList.put(10, "Food & Drink");
        categoryList.put(11, "Games");
        categoryList.put(12, "Action");
        categoryList.put(13, "Adventure");
        categoryList.put(14, "Arcade");
        categoryList.put(15, "Board");
        categoryList.put(16, "Card");
        categoryList.put(17, "Casino");
        categoryList.put(18, "Dice");
        categoryList.put(19, "Educational");
        categoryList.put(20, "Family");
        categoryList.put(21, "Kids");
        categoryList.put(22, "Music");
        categoryList.put(23, "Puzzle");
        categoryList.put(24, "Racing");
        categoryList.put(25, "Role Playing");
        categoryList.put(26, "Simulation");
        categoryList.put(27, "Sports");
        categoryList.put(28, "Strategy");
        categoryList.put(29, "Trivia");
        categoryList.put(30, "Word");
        categoryList.put(31, "Health & Fitness");
        categoryList.put(32, "Lifestyle");
        categoryList.put(33, "Media & Video");
        categoryList.put(34, "Medical");
        categoryList.put(35, "Music & Audio");
        categoryList.put(36, "News & Magazines");
        categoryList.put(37, "Arts & Photography");
        categoryList.put(38, "Automotive");
        categoryList.put(39, "Brides & Weddings");
        categoryList.put(40, "Business & Investing");
        categoryList.put(41, "Children's Magazines");
        categoryList.put(42, "Computers & Internet");
        categoryList.put(43, "Cooking, Food & Drink");
        categoryList.put(44, "Crafts & Hobbies");
        categoryList.put(45, "Electronics & Audio");
        categoryList.put(46, "Entertainment");
        categoryList.put(47, "Fashion & Style");
        categoryList.put(48, "Health, Mind & Body");
        categoryList.put(49, "History");
        categoryList.put(50, "Home & Garden");
        categoryList.put(51, "Literary Magazines & Journals");
        categoryList.put(52, "Men's Interest");
        categoryList.put(53, "Movies & Music");
        categoryList.put(54, "News & Politics");
        categoryList.put(55, "Outdoors & Nature");
        categoryList.put(56, "Parenting & Family");
        categoryList.put(57, "Pets");
        categoryList.put(58, "Professional & Trade");
        categoryList.put(59, "Regional News");
        categoryList.put(60, "Science");
        categoryList.put(61, "Sports & Leisure");
        categoryList.put(62, "Teens");
        categoryList.put(63, "Travel & Regional");
        categoryList.put(64, "Women's Interest");
        categoryList.put(65, "Personalization");
        categoryList.put(66, "Productivity");
        categoryList.put(67, "Shopping");
        categoryList.put(68, "Social Networking");
        categoryList.put(69, "Software Libraries and Demos");
        categoryList.put(70, "Sports");
        categoryList.put(71, "Tools");
        categoryList.put(72, "Travel & Local");
        categoryList.put(73, "WAP Portals");
        categoryList.put(74, "Weather");
        // TODO: Government spelling wrong
        categoryList
                .put(
                    FAMILY_SAFE_BLOCK_CATEGORIES,
                    "Law,Govermentt & Politics,Immigration,Legal Issues,U.S. Government Resources,Politics,Commentary,News,International News,National News,Local News,Hedge Fund,Investing,Dating,Divorce Support,Gay Life,Paranormal Phenomena,Hunting/Shooting,Pagan/Wiccan,Atheism/Agnosticism,Latter-Day Saints,Non-Standard Content,Unmoderated UGC,Extreme Graphic/Explicit Violence,Pornography,Profane Content,Hate Content,Incentivized,Illegal Content,Illegal Content,Warez,Spyware/Malware,Copyright Infringement,Adult Education,Pregnancy,Health & Fitness,Brain Tumor,Cancer,Cholesterol,Chronic Fatigue Syndrome,Chronic Pain,Deafness,Depression,Dermatology,A.D.D.,Diabetes,Epilepsy,GERD/Acid Reflux,Heart Disease,Herbs for Health,IBS/Crohn's Disease,Incest/Abuse Support,Incontinence,AIDS/HIV,Infertility,Men's Health,Panic/Anxiety Disorders,Physical Therapy,Psychology/Psychiatry,Senor Health,Sexuality,Allergies,Sleep Disorders,Smoking Cessation,Weight Loss,Women's Health,Alternative Medicine,Arthritis,Autism/PDD,Bipolar Disorder,Cocktails/Beer,Cigars");
        categoryList
                .put(
                    PERFORMANCE_BLOCK_CATEGORIES,
                    "Immigration,Legal Issues,Gay Life,Atheism/Agnosticism,Extreme Graphic/Explicit Violence,Pornography,Profane Content,Hate Content,Illegal Content,Warez,Spyware/Malware,Copyright Infringement,Adult Education,Pregnancy,Brain Tumor,Cancer,Cholesterol,Chronic Fatigue Syndrome,Chronic Pain,Deafness,Depression,Dermatology,A.D.D.,Diabetes,Epilepsy,GERD/Acid Reflux,Heart Disease,Herbs for Health,IBS/Crohn's Disease,Incest/Abuse Support,Incontinence,AIDS/HIV,Infertility,Men's Health,Panic/Anxiety Disorders,Physical Therapy,Psychology/Psychiatry,Senor Health,Sexuality,Sleep Disorders,Weight Loss,Women's Health,Alternative Medicine,Autism/PDD,Bipolar Disorder,Cocktails/Beer");
    }

    public static String getCategory(int index) {
        return categoryList.get(index);
    }

    public static String getBlockedCategoryForFamilySafe() {
        return categoryList.get(FAMILY_SAFE_BLOCK_CATEGORIES);
    }

    public static String getBlockedCategoryForPerformance() {
        return categoryList.get(PERFORMANCE_BLOCK_CATEGORIES);
    }
}