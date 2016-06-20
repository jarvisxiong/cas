package com.inmobi.adserve.channels.repository;

import static com.inmobi.adserve.channels.entity.NativeAdTemplateEntity.MOVIEBOARD_REQUIRED_JPATH_KEYS;
import static com.inmobi.adserve.channels.entity.NativeAdTemplateEntity.VAST_KEY;
import static com.inmobi.adserve.channels.entity.NativeAdTemplateEntity.TemplateClass.MOVIEBOARD;
import static com.inmobi.adserve.channels.entity.NativeAdTemplateEntity.TemplateClass.STATIC;
import static com.inmobi.adserve.channels.entity.NativeAdTemplateEntity.TemplateClass.VAST;
import static com.inmobi.adserve.channels.repository.NativeConstraints.LAYOUT_FEED;
import static com.inmobi.adserve.channels.repository.NativeConstraints.LAYOUT_ICON;
import static com.inmobi.adserve.channels.repository.NativeConstraints.LAYOUT_STREAM;
import static com.inmobi.adserve.channels.util.InspectorStrings.REPO_STAT_KEY;
import static com.inmobi.adserve.channels.util.InspectorStrings.TOTAL_MOVIEBOARD_TEMPLATES;
import static com.inmobi.adserve.channels.util.InspectorStrings.TOTAL_NATIVE_VIDEO_TEMPLATES;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TDeserializer;

import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity;
import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity.TemplateClass;
import com.inmobi.adserve.channels.query.NativeAdTemplateQuery;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.contracts.misc.NativeAdContentUILayoutType;
import com.inmobi.adtemplate.platform.AdTemplate;
import com.inmobi.adtemplate.platform.AdTemplateDemandConstraints;
import com.inmobi.adtemplate.platform.CardDetail;
import com.inmobi.adtemplate.platform.MultiCardConstraints;
import com.inmobi.phoenix.batteries.data.AbstractStatsMaintainingDBRepository;
import com.inmobi.phoenix.batteries.data.DBEntity;
import com.inmobi.phoenix.batteries.data.EntityError;
import com.inmobi.phoenix.batteries.data.HashIndexKeyBuilder;
import com.inmobi.phoenix.batteries.data.rdbmsrow.NullAsZeroResultSetRow;
import com.inmobi.phoenix.batteries.data.rdbmsrow.ResultSetRow;
import com.inmobi.phoenix.data.RepositoryManager;
import com.inmobi.phoenix.data.RepositoryQuery;
import com.inmobi.phoenix.exception.RepositoryException;
import com.inmobi.template.formatter.TemplateManager;

public class NativeAdTemplateRepository
        extends AbstractStatsMaintainingDBRepository<NativeAdTemplateEntity, NativeAdTemplateQuery>
        implements
            RepositoryManager {

    @Override
    public DBEntity<NativeAdTemplateEntity, NativeAdTemplateQuery> buildObjectFromRow(final ResultSetRow resultSetRow)
            throws RepositoryException {
        final NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        final Long placementId = row.getLong("placement_id");
        final Timestamp modifiedOn = row.getTimestamp("modified_on");
        TemplateClass templateClass = STATIC;
        String errorMsg = "ERROR_IN_READING_NATIVE_TEMPLATE";
        try {
            final Long templateId = row.getLong("native_template_id");
            final Integer uiLayoutId = (Integer) row.getObject("ui_layout_id");
            // final String contentJson = row.getString("content_json");
            final String binaryTemplate = row.getString("binary_template");
            if (StringUtils.isEmpty(binaryTemplate)) {
                throw new RepositoryException("No binary template found.");
            }

            final NativeAdTemplateEntity.Builder builder = NativeAdTemplateEntity.newBuilder();
            builder.placementId(placementId);
            builder.modifiedOn(modifiedOn);
            builder.templateId(templateId);

            // Deserialize ad Template Binary
            final TDeserializer deserializer = new TDeserializer();
            final AdTemplate adTemplate = new com.inmobi.adtemplate.platform.AdTemplate();
            deserializer.deserialize(adTemplate, Base64.decodeBase64(binaryTemplate));

            // Get fields from ad Template Binary
            List<String> demandJpath = null;
            if (adTemplate.isMultiCardTemplate() && checkIfMovieBoardTemplate(adTemplate.getMultiCardConstraints())) {
                templateClass = MOVIEBOARD;
                InspectorStats.incrementStatCount(REPO_STAT_KEY, TOTAL_MOVIEBOARD_TEMPLATES);
            } else {
                final AdTemplateDemandConstraints adTemplateDemandConstraints = adTemplate.getDemandConstraints();
                demandJpath = null != adTemplateDemandConstraints ? adTemplateDemandConstraints.getJsonPath() : null;
            }

            if (demandJpath == null && templateClass != MOVIEBOARD) {
                throw new RepositoryException("No jsonpath found for non MOVIEBOARD template");
            }

            String mandatoryKey = null, imageKey = null;
            if (demandJpath != null) {
                for (final String key : demandJpath) {
                    if (NativeConstraints.isImageKey(key)) {
                        builder.imageKey(key);
                        imageKey = key;
                    } else if (NativeConstraints.isMandatoryKey(key)) {
                        builder.mandatoryKey(key);
                        mandatoryKey = key;
                    } else if (VAST_KEY.equalsIgnoreCase(key)) {
                        templateClass = VAST;
                        InspectorStats.incrementStatCount(REPO_STAT_KEY, TOTAL_NATIVE_VIDEO_TEMPLATES);
                    }
                }
            }

            if (STATIC == templateClass) {
                final boolean isImageReqButNull = NativeConstraints.isImageRequired(mandatoryKey) && imageKey == null;
                if (mandatoryKey == null || isImageReqButNull) {
                    errorMsg = "No mandatory/image Key for STATIC templateClass. JPath is " + demandJpath.toString();
                    throw new RepositoryException(errorMsg);
                }
                final NativeAdContentUILayoutType type = getnativeUILayout(uiLayoutId, mandatoryKey);
                if (type == null) {
                    throw new RepositoryException(String.format(
                            "Native UI Layout is null for uiLayoutId=%s, mandatoryKey=%s", uiLayoutId, mandatoryKey));
                }
                builder.nativeUILayout(type);
            }
            builder.templateClass(templateClass);
            // Add template Content
            final String templateContent = adTemplate.getDetails().getContent();
            if (StringUtils.isEmpty(templateContent)) {
                errorMsg = "No template content for templateId ->" + templateId;
                throw new RepositoryException(errorMsg);
            }
            builder.template(templateContent);
            // Add Template to VM Cache
            final TemplateManager templateMgr = TemplateManager.getInstance();
            templateMgr.addToTemplateCache(templateId, templateContent);
            logger.debug(String.format("Adding templateId %d to NativeAdTemplateRepository", templateId));
            // Fetching the template from cache to check its validity
            templateMgr.getTemplate(templateId);
            // Build NativeAdTemplateEntity
            final NativeAdTemplateEntity templateEntity = builder.build();
            return new DBEntity<>(templateEntity, modifiedOn);
        } catch (final Exception e) {
            logger.error("Error in resultset row", e);
            return new DBEntity<>(new EntityError<>(new NativeAdTemplateQuery(placementId, templateClass), errorMsg),
                    modifiedOn);
        }
    }

    /**
     *
     * @param uiLayoutId
     * @param mandatoryKey
     * @return
     */
    private NativeAdContentUILayoutType getnativeUILayout(final Integer uiLayoutId, final String mandatoryKey) {
        NativeAdContentUILayoutType toReturn = null;
        if (null != uiLayoutId) {
            try {
                // builder.nativeUILayout(NativeAdContentUILayoutType.findByValue(uiLayoutId));
                toReturn = NativeAdContentUILayoutType.findByValue(uiLayoutId);
            } catch (final IllegalArgumentException ignored) {
                // Ignored
            }
        }
        if (null == toReturn) {
            switch (mandatoryKey) {
                case LAYOUT_ICON:
                    toReturn = NativeAdContentUILayoutType.NEWS_FEED;
                    break;
                case LAYOUT_FEED:
                    toReturn = NativeAdContentUILayoutType.NEWS_FEED;
                    break;
                case LAYOUT_STREAM:
                    toReturn = NativeAdContentUILayoutType.CONTENT_STREAM;
                    break;
                // default is already taken care.
            }
        }
        return toReturn;
    }

    /**
     *
     * @param multiCardConstraintsMap
     * @return
     */
    private boolean checkIfMovieBoardTemplate(final Map<Integer, MultiCardConstraints> multiCardConstraintsMap) {
        if (null == multiCardConstraintsMap) {
            return false;
        }

        boolean movieBoardTemplate = true;
        for (final MultiCardConstraints multiCardConstraints : multiCardConstraintsMap.values()) {
            for (final CardDetail cardDetail : multiCardConstraints.getCardDetails().values()) {

                boolean cardHasMovieBoardJpaths = false;
                if (cardDetail.isSetDemandConstraints()) {
                    final AdTemplateDemandConstraints cardDemandConstraints = cardDetail.getDemandConstraints();
                    if (cardDemandConstraints.isSetJsonPath() && cardDemandConstraints.getJsonPath().size() > 0) {
                        cardHasMovieBoardJpaths = true;
                        for (final String requiredJPaths : MOVIEBOARD_REQUIRED_JPATH_KEYS) {
                            if (!cardDemandConstraints.getJsonPath().contains(requiredJPaths)) {
                                cardHasMovieBoardJpaths = false;
                                break;
                            }
                        }
                    }
                }
                movieBoardTemplate = movieBoardTemplate && cardHasMovieBoardJpaths;
            }
        }

        return movieBoardTemplate;
    }


    @Override
    public boolean isObjectToBeDeleted(final NativeAdTemplateEntity entity) {
        return false;
    }

    @Override
    public HashIndexKeyBuilder<NativeAdTemplateEntity> getHashIndexKeyBuilder(final String className) {
        return null;
    }

    @Override
    public NativeAdTemplateEntity queryUniqueResult(final RepositoryQuery q) throws RepositoryException {
        return null;
    }

}
