package com.inmobi.adserve.channels.util;

/**
 * Interface for inmobi cities to IAB cities mapping.
 * 
 * @author Devi Chand(devi.chand@inmobi.com)
 */
public interface IABCitiesInterface {

  /**
   * Maps inmobi cities to IAB cities.
   * 
   * @param City .
   * @return IAB city.
   */
  String getIABCity(final String city);
}
