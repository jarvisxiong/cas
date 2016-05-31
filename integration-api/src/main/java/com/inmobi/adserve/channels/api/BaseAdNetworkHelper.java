/**
 * Copyright (c) 2015. InMobi, All Rights Reserved.
 */
package com.inmobi.adserve.channels.api;

import static com.inmobi.adserve.channels.util.config.GlobalConstant.CPC;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.util.CategoryList;
import com.inmobi.adserve.channels.util.IABCategoriesMap;

import io.netty.util.CharsetUtil;

/**
 * @author ritwik.kumar
 *
 */
public class BaseAdNetworkHelper {
    private static final Logger LOG = LoggerFactory.getLogger(BaseAdNetworkHelper.class);

    /**
     * @param siteIncId
     * @param adGroupIncId
     * @return
     */
    public static String getBlindedSiteId(final long siteIncId, final long adGroupIncId) {
        return new UUID(adGroupIncId, siteIncId).toString();
    }

    /**
     * 
     * @param entity
     * @return
     */
    public static final Boolean getPricingModel(final ChannelSegmentEntity entity) {
        boolean isCpc = false;
        if (null != entity.getPricingModel() && CPC.equalsIgnoreCase(entity.getPricingModel())) {
            isCpc = true;
        }
        return isCpc;
    }

    /**
     * Generates blinded site uuid from siteIncId. For a given site Id, the generated blinded SiteId will always be
     * same.
     * <p/>
     * NOTE: RTB uses a different logic where the blinded SiteId is a function of siteIncId+AdGroupIncId.
     */
    public static String getBlindedSiteId(final long siteIncId) {
        final byte[] byteArr = ByteBuffer.allocate(8).putLong(siteIncId).array();
        return UUID.nameUUIDFromBytes(byteArr).toString();
    }

   /**
    * 
    * @param seperator
    * @param isAllRequired
    * @param isIABCategory
    * @param sasParams
    * @param entity
    * @return
    */
    protected static String getCategories(final char seperator, final boolean isAllRequired, final boolean isIABCategory,
            final SASRequestParameters sasParams, final ChannelSegmentEntity entity) {
        final StringBuilder sb = new StringBuilder();
        Long[] segmentCategories = null;
        boolean allTags = false;
        if (entity != null) {
            segmentCategories = entity.getCategoryTaxonomy();
            allTags = entity.isAllTags();
        }
        if (allTags) {
            if (isIABCategory) {
                return getValueFromListAsString(IABCategoriesMap.getIABCategories(sasParams.getCategories()),
                        seperator);

            } else if (null != sasParams.getCategories()) {
                for (int index = 0; index < sasParams.getCategories().size(); index++) {
                    final String category = CategoryList.getCategory(sasParams.getCategories().get(index).intValue());
                    appendCategories(sb, category, seperator);
                    if (!isAllRequired) {
                        break;
                    }
                }
            }
        } else {
            for (int index = 0; index < sasParams.getCategories().size(); index++) {
                String category = null;
                final int cat = sasParams.getCategories().get(index).intValue();
                for (int i = 0; i < segmentCategories.length; i++) {
                    if (cat == segmentCategories[i]) {
                        if (isIABCategory) {
                            category = getValueFromListAsString(IABCategoriesMap.getIABCategories(segmentCategories[i]),
                                    seperator);
                        } else {
                            category = CategoryList.getCategory(cat);
                        }
                        appendCategories(sb, category, seperator);
                    }
                }
                if (!isAllRequired && null != category) {
                    break;
                }
            }
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
            return sb.toString();
        }
        if (isIABCategory) {
            return "IAB24";
        }
        return "miscellenous";
    }

    /**
     * @param sb
     * @param category
     */
    private static void appendCategories(final StringBuilder sb, final String category, final char seperator) {
        if (category != null) {
            sb.append(category).append(seperator);
        }
    }

    /**
     * 
     * @param list
     * @return
     */
    protected String getValueFromListAsString(final List<String> list) {
        return getValueFromListAsString(list, ',');
    }

    /**
     * 
     * @param list
     * @param seperatar
     * @return
     */
    protected static String getValueFromListAsString(final List<String> list, final char seperatar) {
        if (list.isEmpty()) {
            return StringUtils.EMPTY;
        }
        final StringBuilder s = new StringBuilder(list.get(0));
        for (int i = 1; i < list.size(); i++) {
            s.append(seperatar).append(list.get(i));
        }
        return s.toString();
    }
    
    /**
     * 
     * @param message
     * @param hashingType
     * @return
     */
    public static String getHashedValue(final String message, final String hashingType) {
        try {
            final MessageDigest md = MessageDigest.getInstance(hashingType);
            final byte[] array = md.digest(message.getBytes(CharsetUtil.UTF_8));
            final StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString(array[i] & 0xFF | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (final java.security.NoSuchAlgorithmException e) {
            LOG.debug("exception raised in BaseAdNetwork {}", e);
        }
        return null;
    }


}
