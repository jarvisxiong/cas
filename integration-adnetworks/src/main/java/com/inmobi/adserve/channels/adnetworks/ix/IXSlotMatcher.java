/**
 * Copyright (c) 2015. InMobi, All Rights Reserved.
 */
package com.inmobi.adserve.channels.adnetworks.ix;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.entity.CAUMetadataEntity;
import com.inmobi.adserve.channels.entity.CAUMetadataEntity.CreativeDim;
import com.inmobi.adserve.channels.entity.CAUMetadataEntity.Dimension;
import com.inmobi.adserve.channels.repository.RepositoryHelper;

import lombok.Getter;

/**
 * @author ritwik.kumar
 *
 */
public class IXSlotMatcher {
    private static final Logger LOG = LoggerFactory.getLogger(IXSlotMatcher.class);
    private final RepositoryHelper repositoryHelper;
    @Getter
    private Integer matchedRPSlotId = null;
    @Getter
    private CAUMetadataEntity matchedCau = null;
    @Getter
    private java.awt.Dimension matchedRPDimension = null;
    @Getter
    private Long matchedCAUId = null;

    /**
     *
     * @param repositoryHelper
     */
    public IXSlotMatcher(final RepositoryHelper repositoryHelper) {
        super();
        this.repositoryHelper = repositoryHelper;
    }

    /**
     *
     * Get the first matching Rubicon size for the given sets of Custom Ad Units. Take size and tolerance into
     * consideration.
     *
     * @param cauSet
     * @return
     */
    public Integer getMatchingSlotForCAU(final Set<Long> cauSet) {
        LOG.debug("Inside getMatchingSlotForCAU, CAU Set ->{}", cauSet);
        // Iterate over all Rubicon sizes
        for (final Integer rpSlot : SlotSizeMapping.RP_SLOT_DIMENSION.keySet()) {
            final java.awt.Dimension rpDim = SlotSizeMapping.RP_SLOT_DIMENSION.get(rpSlot);

            // Check all CAU, if they fall within criteria
            for (final Long cauId : cauSet) {
                final CAUMetadataEntity cauEntity = repositoryHelper.queryCauMetaDataRepository(cauId);
                if (cauEntity == null) {
                    LOG.error("No CAU entity for id {}", cauId);
                    continue;
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Trying to match rp dim{} with cau entity {}", rpDim, cauEntity.toString());
                }

                final CreativeDim creativeDim = cauEntity.getConstraint().getCreative_dim();
                final Dimension cauMaxDim = creativeDim.getMax();
                final Dimension cauMinDim = creativeDim.getMin();
                final Double cauAR = creativeDim.getAspectRatio();
                final Double tolerance = creativeDim.getTolerance();

                // RP Height and width should be within Max CAU Height and Width. And difference in Aspect Ratio should
                // be within tolerance level
                final boolean lessThanMax =
                        cauMaxDim.getH() >= rpDim.getHeight() && cauMaxDim.getW() >= rpDim.getWidth();
                final boolean moreThanMin =
                        cauMinDim.getH() <= rpDim.getHeight() && cauMinDim.getW() <= rpDim.getWidth();
                if (lessThanMax && moreThanMin) {
                    final Double rpAr = rpDim.getWidth() / rpDim.getHeight();
                    if (Math.abs(cauAR - rpAr) <= tolerance) {
                        matchedCAUId = cauId;
                        matchedCau = cauEntity;
                        matchedRPSlotId = rpSlot;
                        matchedRPDimension = rpDim;
                        break;
                    }
                }
            }

        }
        if (matchedRPSlotId == null) {
            LOG.info("No matching slot found for CAU Set {}", cauSet);
        }
        LOG.debug("matchedRPSlotId {}", matchedRPSlotId);
        LOG.debug("matchedCAUId {}", matchedCAUId);
        LOG.debug("matchedCau {}", matchedCau);
        LOG.debug("matchedRPDimension {}", matchedRPDimension);
        return matchedRPSlotId;
    }
}
