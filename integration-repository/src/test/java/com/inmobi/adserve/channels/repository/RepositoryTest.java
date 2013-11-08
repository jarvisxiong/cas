package com.inmobi.adserve.channels.repository;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class RepositoryTest
{
    ChannelAdGroupRepository channelAdGroupRepository;
    PricingEngineRepository  pricingEngineRepository;

    @BeforeMethod
    public void setUp() throws IOException
    {
        channelAdGroupRepository = new ChannelAdGroupRepository();
        pricingEngineRepository = new PricingEngineRepository();
    }

    @Test
    public void testParseManufacturingIds()
    {
        String modelTargetingJson = "{\"manuf\": [{\"id\":8,\"modelIds\":[7473],\"incl\":true}]}";
        assertEquals(1, channelAdGroupRepository.parseManufacturingIds(modelTargetingJson).size());
        assertEquals(7473, channelAdGroupRepository.parseManufacturingIds(modelTargetingJson).get(0).intValue());
    }

    @Test
    public void testGetSupplyToDemandMap()
    {
        String json = "{\"0\":[\"0\",\"1\"],\"1\":[\"0\",\"1\",\"2\"]}";
        Map<String, Set<String>> map = pricingEngineRepository.getSupplyToDemandMap(json);
        assertEquals(true, map.get("0").contains("1"));
        assertEquals(true, map.get("1").contains("2"));
    }
}
