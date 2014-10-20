package com.inmobi.adserve.channels.util;

import java.util.List;


/**
 * Interface to look up IAB categories.
 * 
 * @author Devi Chand(devi.chand@inmobi.com)
 */
public interface IABCategoriesInterface {

	/**
	 * Maps new inmobi categories to IAB categories.
	 * 
	 * @param List of new inmobi categories.
	 * @return List of IAB Categories code.
	 */
	List<String> getIABCategories(final List<Long> categories);

	List<String> getIABCategories(final Long category);

}
