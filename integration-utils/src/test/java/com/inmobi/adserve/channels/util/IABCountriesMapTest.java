package com.inmobi.adserve.channels.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(PowerMockRunner.class)
public class IABCountriesMapTest {

    @Test
    public void testGetIabCountryNull() throws Exception {
        IABCountriesMap tested = new IABCountriesMap();
        assertThat(tested.getIabCountry(null), is(equalTo(null)));
    }

    @Test
    public void testGetIabCountryFound() throws Exception {
        String country = "GS";
        String expectedIabMapping = "SGS";

        IABCountriesMap tested = new IABCountriesMap();
        assertThat(tested.getIabCountry(country), is(equalTo(expectedIabMapping)));
    }

    @Test
    public void testGetIabCountryNotFound() throws Exception {
        String country = "Sealand";
        String expectedIabMapping = null;

        IABCountriesMap tested = new IABCountriesMap();
        assertThat(tested.getIabCountry(country), is(equalTo(expectedIabMapping)));
    }

    @Test
    public void testGetIabCountryLowerCase() throws Exception {
        String country = "gs";
        String expectedIabMapping = "SGS";

        IABCountriesMap tested = new IABCountriesMap();
        assertThat(tested.getIabCountry(country), is(equalTo(expectedIabMapping)));
    }
}