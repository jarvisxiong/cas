package com.inmobi.adserve.channels.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class IABCountriesMapTest {

    @Test
    public void testGetIabCountryNull() throws Exception {
        assertThat(IABCountriesMap.getIabCountry(null), is(equalTo(null)));
    }

    @Test
    public void testGetIabCountryFound() throws Exception {
        final String country = "GS";
        final String expectedIabMapping = "SGS";

        assertThat(IABCountriesMap.getIabCountry(country), is(equalTo(expectedIabMapping)));
    }

    @Test
    public void testGetIabCountryNotFound() throws Exception {
        final String country = "Sealand";
        final String expectedIabMapping = null;

        assertThat(IABCountriesMap.getIabCountry(country), is(equalTo(expectedIabMapping)));
    }

    @Test
    public void testGetIabCountryLowerCase() throws Exception {
        final String country = "gs";
        final String expectedIabMapping = "SGS";

        assertThat(IABCountriesMap.getIabCountry(country), is(equalTo(expectedIabMapping)));
    }
}
