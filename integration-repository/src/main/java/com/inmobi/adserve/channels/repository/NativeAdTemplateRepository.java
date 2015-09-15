package com.inmobi.adserve.channels.repository;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TDeserializer;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity;
import com.inmobi.adserve.contracts.misc.NativeAdContentUILayoutType;
import com.inmobi.adserve.contracts.misc.contentjson.NativeContentJsonObject;
import com.inmobi.adtemplate.platform.AdTemplate;
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

public class NativeAdTemplateRepository extends AbstractStatsMaintainingDBRepository<NativeAdTemplateEntity, Long>
        implements
            RepositoryManager {
    private final static Gson GSON = new Gson();

    @Override
    public DBEntity<NativeAdTemplateEntity, Long> buildObjectFromRow(final ResultSetRow resultSetRow)
            throws RepositoryException {
        final NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        final long placementId = row.getLong("placement_id");
        final Timestamp modifiedOn = row.getTimestamp("modified_on");

        try {
            final long nativeTemplateId = row.getLong("native_template_id");
            final Integer uiLayoutId = (Integer)row.getObject("ui_layout_id");
            final String contentJson = row.getString("content_json");
            final String encodedTemplate = row.getString("binary_template");

            if (StringUtils.isEmpty(encodedTemplate)) {
                throw new RepositoryException("No binary template found.");
            }
            final byte[] binaryTemplate = Base64.decodeBase64(encodedTemplate);

            final TDeserializer deserializer = new TDeserializer();
            final AdTemplate adTemplate = new com.inmobi.adtemplate.platform.AdTemplate();
            deserializer.deserialize(adTemplate, binaryTemplate);

            final NativeAdTemplateEntity.Builder builder = NativeAdTemplateEntity.newBuilder();
            builder.placementId(placementId);
            builder.modifiedOn(modifiedOn);
            builder.nativeTemplateId(nativeTemplateId);

            if (null != uiLayoutId) {
                try {
                    builder.nativeUILayout(NativeAdContentUILayoutType.findByValue(uiLayoutId));
                } catch (IllegalArgumentException ignored) {
                    // Ignored
                }
            }

            try {
                builder.contentJson(GSON.fromJson(contentJson, NativeContentJsonObject.class));
            } catch (final JsonParseException jpe) {
                // Ignored
            }

            final List<String> keys = adTemplate.getDemandConstraints().getJsonPath();
            if (keys == null) {
                throw new RepositoryException("No keys(mandatory/image) found.");
            }

            final Iterator<String> itr = keys.iterator();
            while (itr.hasNext()) {
                final String key = itr.next();
                if (NativeConstraints.isImageKey(key)) {
                    builder.imageKey(key);
                } else if (NativeConstraints.isMandatoryKey(key)) {
                    builder.mandatoryKey(key);
                }
            }
            // Add template Content
            final String templateContent = adTemplate.getDetails().getContent();
            builder.template(templateContent);

            final NativeAdTemplateEntity templateEntity = builder.build();
            if (templateEntity.getMandatoryKey() != null) {
                TemplateManager.getInstance().addToTemplateCache(String.valueOf(templateEntity.getPlacementId()), templateContent);
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("Adding placement id %d and nativeId %s to NativeAdTemplateRepository",
                            placementId, nativeTemplateId));
                }
            } else {
                logger.info("PlacementId[" + placementId + "][" + nativeTemplateId
                        + "] doesn't have valid mandatory field thus not adding to Template Cache.");
            }
            return new DBEntity<NativeAdTemplateEntity, Long>(templateEntity, modifiedOn);
        } catch (final Exception e) {
            logger.error("Error in resultset row", e);
            return new DBEntity<NativeAdTemplateEntity, Long>(new EntityError<Long>(placementId,
                    "ERROR_IN_READING_NATIVE_TEMPLATE"), modifiedOn);
        }
    }


    @Override
    public boolean isObjectToBeDeleted(final NativeAdTemplateEntity entity) {
        if (entity.getId() == null || entity.getMandatoryKey() == null) {
            return true;
        }
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
