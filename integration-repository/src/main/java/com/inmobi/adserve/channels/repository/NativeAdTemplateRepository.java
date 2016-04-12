package com.inmobi.adserve.channels.repository;

import static com.inmobi.adserve.channels.entity.NativeAdTemplateEntity.VAST_KEY;
import static com.inmobi.adserve.channels.entity.NativeAdTemplateEntity.TemplateClass.STATIC;
import static com.inmobi.adserve.channels.entity.NativeAdTemplateEntity.TemplateClass.VAST;

import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TDeserializer;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity;
import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity.TemplateClass;
import com.inmobi.adserve.channels.query.NativeAdTemplateQuery;
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

public class NativeAdTemplateRepository
        extends AbstractStatsMaintainingDBRepository<NativeAdTemplateEntity, NativeAdTemplateQuery>
        implements
            RepositoryManager {
    private final static Gson GSON = new Gson();

    @Override
    public DBEntity<NativeAdTemplateEntity, NativeAdTemplateQuery> buildObjectFromRow(final ResultSetRow resultSetRow)
            throws RepositoryException {
        final NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        final Long placementId = row.getLong("placement_id");
        final Timestamp modifiedOn = row.getTimestamp("modified_on");
        TemplateClass templateClass = STATIC;
        try {
            final Long templateId = row.getLong("native_template_id");
            final Integer uiLayoutId = (Integer) row.getObject("ui_layout_id");
            final String contentJson = row.getString("content_json");
            final String binaryTemplate = row.getString("binary_template");
            if (StringUtils.isEmpty(binaryTemplate)) {
                throw new RepositoryException("No binary template found.");
            }

            final NativeAdTemplateEntity.Builder builder = NativeAdTemplateEntity.newBuilder();
            builder.placementId(placementId);
            builder.modifiedOn(modifiedOn);
            builder.templateId(templateId);

            if (null != uiLayoutId) {
                try {
                    builder.nativeUILayout(NativeAdContentUILayoutType.findByValue(uiLayoutId));
                } catch (final IllegalArgumentException ignored) {
                    // Ignored
                }
            }
            try {
                builder.contentJson(GSON.fromJson(contentJson, NativeContentJsonObject.class));
            } catch (final JsonParseException jpe) {
                // Ignored
            }

            // Deserialize ad Template Binary
            final TDeserializer deserializer = new TDeserializer();
            final AdTemplate adTemplate = new com.inmobi.adtemplate.platform.AdTemplate();
            deserializer.deserialize(adTemplate, Base64.decodeBase64(binaryTemplate));
            // Get fields from ad Template Binary
            final List<String> demandJpath = adTemplate.getDemandConstraints().getJsonPath();
            if (demandJpath == null) {
                throw new RepositoryException("No keys, jsonpath found");
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
                    }
                }
            }
            if (STATIC == templateClass) {
                final boolean isImageReqButNull = NativeConstraints.isImageRequired(mandatoryKey) && imageKey == null;
                if (mandatoryKey == null || isImageReqButNull) {
                    throw new RepositoryException("No mandatory/image Key for STATIC templateClass");
                }
            }
            builder.templateClass(templateClass);
            // Add template Content
            final String templateContent = adTemplate.getDetails().getContent();
            if (StringUtils.isEmpty(templateContent)) {
                throw new RepositoryException("No template content for templateId ->" + templateId);
            }
            builder.template(templateContent);
            // Add Template to VM Cache
            TemplateManager templateMgr = TemplateManager.getInstance();
            templateMgr.addToTemplateCache(templateId, templateContent);
            logger.debug(String.format("Adding templateId %d to NativeAdTemplateRepository", templateId));
            // Fetching the template from cache to check its validity
            templateMgr.getTemplate(templateId);
            // Build NativeAdTemplateEntity
            final NativeAdTemplateEntity templateEntity = builder.build();
            return new DBEntity<NativeAdTemplateEntity, NativeAdTemplateQuery>(templateEntity, modifiedOn);
        } catch (final Exception e) {
            logger.error("Error in resultset row", e);
            return new DBEntity<NativeAdTemplateEntity, NativeAdTemplateQuery>(
                    new EntityError<NativeAdTemplateQuery>(new NativeAdTemplateQuery(placementId, templateClass),
                            "ERROR_IN_READING_NATIVE_TEMPLATE"),
                    modifiedOn);
        }
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
