package com.inmobi.adserve.channels.server.beans;

import lombok.Data;

import com.inmobi.adserve.channels.entity.PricingEngineEntity;


/**
 * @author abhishek.parwal
 * 
 */
@Data
public class CasContext {

	private int sumOfSiteImpressions;
	private PricingEngineEntity pricingEngineEntity;

}
