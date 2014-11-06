package com.inmobi.adserve.channels.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class CategoryListTest {

    @Test
    public void testGetCategory() throws Exception {
        final int index = 66;
        final String expected = "Productivity";
        assertThat(CategoryList.getCategory(index), is(equalTo(expected)));
    }

    @Test
    public void testGetCategoryNull() throws Exception {
        final int index = -1;
        final String expected = null;
        assertThat(CategoryList.getCategory(index), is(equalTo(expected)));
    }

    @Test
    public void testGetBlockedCategoryForFamilySafe() throws Exception {
        final int index = 10000;
        final String expected =
                "Law,Government & Politics,Immigration,Legal Issues,U.S. Government Resources,Politics,Commentary,News,International News,National News,Local News,Hedge Fund,Investing,Dating,Divorce Support,Gay Life,Paranormal Phenomena,Hunting/Shooting,Pagan/Wiccan,Atheism/Agnosticism,Latter-Day Saints,Non-Standard Content,Unmoderated UGC,Extreme Graphic/Explicit Violence,Pornography,Profane Content,Hate Content,Incentivized,Illegal Content,Illegal Content,Warez,Spyware/Malware,Copyright Infringement,Adult Education,Pregnancy,Health & Fitness,Brain Tumor,Cancer,Cholesterol,Chronic Fatigue Syndrome,Chronic Pain,Deafness,Depression,Dermatology,A.D.D.,Diabetes,Epilepsy,GERD/Acid Reflux,Heart Disease,Herbs for Health,IBS/Crohn's Disease,Incest/Abuse Support,Incontinence,AIDS/HIV,Infertility,Men's Health,Panic/Anxiety Disorders,Physical Therapy,Psychology/Psychiatry,Senor Health,Sexuality,Allergies,Sleep Disorders,Smoking Cessation,Weight Loss,Women's Health,Alternative Medicine,Arthritis,Autism/PDD,Bipolar Disorder,Cocktails/Beer,Cigars";

        assertThat(CategoryList.getCategory(index), is(equalTo(expected)));
    }

    @Test
    public void testGetBlockedCategoryForPerformance() throws Exception {
        final int index = 10001;
        final String expected =
                "Immigration,Legal Issues,Gay Life,Atheism/Agnosticism,Extreme Graphic/Explicit Violence,Pornography,Profane Content,Hate Content,Illegal Content,Warez,Spyware/Malware,Copyright Infringement,Adult Education,Pregnancy,Brain Tumor,Cancer,Cholesterol,Chronic Fatigue Syndrome,Chronic Pain,Deafness,Depression,Dermatology,A.D.D.,Diabetes,Epilepsy,GERD/Acid Reflux,Heart Disease,Herbs for Health,IBS/Crohn's Disease,Incest/Abuse Support,Incontinence,AIDS/HIV,Infertility,Men's Health,Panic/Anxiety Disorders,Physical Therapy,Psychology/Psychiatry,Senor Health,Sexuality,Sleep Disorders,Weight Loss,Women's Health,Alternative Medicine,Autism/PDD,Bipolar Disorder,Cocktails/Beer";

        assertThat(CategoryList.getCategory(index), is(equalTo(expected)));
    }
}
