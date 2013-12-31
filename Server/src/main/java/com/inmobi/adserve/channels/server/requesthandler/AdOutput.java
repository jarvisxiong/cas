package com.inmobi.adserve.channels.server.requesthandler;

import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.creativetool.api.AdCreative;
import com.inmobi.adserve.creativetool.api.AdMakerFactory;
import com.inmobi.adserve.creativetool.api.AdRequest;
import com.inmobi.phoenix.entity.adgroup.AdGroup;
import com.inmobi.phoenix.entity.advertisement.CreativeEntity;
import com.inmobi.phoenix.entity.advertisement.creative.ContentProvider;
import com.inmobi.phoenix.entity.advertisement.creative.CreativeContentType;
import com.inmobi.phoenix.entity.advertisement.creative.CreativeType;
import com.inmobi.phoenix.exception.InitializationException;
import com.inmobi.phoenix.exception.RepositoryException;
import lombok.Data;
import org.apache.commons.configuration.ConfigurationException;

import java.awt.*;

public class AdOutput {
    private static AdMakerFactory adMakerFactory;
    
    public static void  init(final DebugLogger logger) {
        try {
            adMakerFactory = new AdMakerFactory();
        } catch (InitializationException e) {
            logger.debug("Failed in Initialization", e);
        } catch (ConfigurationException e) {
            logger.debug("Failed in Configuration", e);
        } catch (RepositoryException e) {
            logger.debug("Failed in Repository", e);
        }        
    }
    
    @Data
    public static class Builder {
        Long slot;
        String beaconUrl;
        String clickUrl;
        AdGroup.MarketPlace marketPlace;
        AdRequest.RequestFormat requestFormat;
        AdRequest.Os os;
        CreativeType creativeType;
        CreativeContentType creativeContentType;
        ContentProvider contentProvider;  
    }
    
    
    
    public static byte [] makeAd(Builder builder) {
        CreativeEntity creativeEntity = new CreativeEntity();
        creativeEntity.setCreativeType(builder.getCreativeType());
        creativeEntity.setCreativeContentType(builder.getCreativeContentType());
        creativeEntity.setContentProvider(builder.getContentProvider());
        
        creativeEntity.setSlotId(builder.getSlot().intValue());
        Dimension dimension = SlotSizeMapping.getDimension(builder.getSlot());
        int creativeHeight = new Double(dimension.getHeight()).intValue();
        int creativeWidth = new Double(dimension.getWidth()).intValue();
        creativeEntity.setActualHeight(creativeHeight);
        creativeEntity.setActualWidth(creativeWidth);
        
        //TODO Landing page is not available
        AdCreative adCreative = new AdCreative(creativeEntity, "",
                builder.getBeaconUrl(), builder.getClickUrl(), false, true, true,
                builder.getMarketPlace(), false);
        AdRequest adRequest = new AdRequest(builder.getRequestFormat(), builder.getOs(),
                builder.getSlot().intValue(), creativeHeight, creativeWidth);
        return adMakerFactory.getAdMaker().makeAd(adRequest, adCreative);
    } 
}
