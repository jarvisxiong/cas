package com.inmobi.adserve.channels.util;

/**
 * Interface to look up IAB countries.
 * 
 * @author Devi Chand(devi.chand@inmobi.com)
 */
public interface IABCountriesInterface
{

    /**
     * Maps new inmobi categories to IAB countries.
     * 
     * @param List
     *            of inmobi countries.
     * @return List of IAB countries.
     */
    String getIabCountry(String country);
}
