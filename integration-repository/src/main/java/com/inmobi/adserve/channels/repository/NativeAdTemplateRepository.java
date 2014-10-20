package com.inmobi.adserve.channels.repository;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.thrift.TDeserializer;

import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity;
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

public class NativeAdTemplateRepository extends AbstractStatsMaintainingDBRepository<NativeAdTemplateEntity, String>
        implements
            RepositoryManager {



    @Override
    public DBEntity<NativeAdTemplateEntity, String> buildObjectFromRow(final ResultSetRow resultSetRow)
            throws RepositoryException {
        final NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        final String siteId = row.getString("site_id");
        final Timestamp modifiedOn = row.getTimestamp("modified_on");

        try {
            final long nativeAdId = row.getLong("native_ad_id");
            final String encodedTemplate = row.getString("binary_template");
            final byte[] binaryTemplate = Base64.decodeBase64(encodedTemplate);

            final TDeserializer deserializer = new TDeserializer();
            final com.inmobi.adtemplate.platform.AdTemplate adTemplate =
                    new com.inmobi.adtemplate.platform.AdTemplate();
            deserializer.deserialize(adTemplate, binaryTemplate);

            final NativeAdTemplateEntity.Builder builder = NativeAdTemplateEntity.newBuilder();
            builder.setSiteId(siteId);
            builder.setModifiedOn(modifiedOn);
            builder.setNativeAdId(nativeAdId);

            final List<String> keys = adTemplate.getDemandConstraints().getJsonPath();
            if (keys == null) {
                throw new Exception("No mandatory field found.");
            }

            final Iterator<String> itr = keys.iterator();
            while (itr.hasNext()) {
                final String key = itr.next();
                if (NativeConstrains.isImageKey(key)) {
                    builder.setImageKey(key);
                } else if (NativeConstrains.isMandatoryKey(key)) {
                    builder.setMandatoryKey(key);
                }
            }

            final NativeAdTemplateEntity templateEntity = builder.build();
            if (templateEntity.getMandatoryKey() != null) {
                TemplateManager.getInstance().addToTemplateCache(templateEntity.getSiteId(),
                        adTemplate.getDetails().getContent());
                logger.debug("Adding site id  " + siteId + " and nativeId " + nativeAdId
                        + " to NativeAdTemplateRespository");
            } else {
                logger.warn("SiteId[" + siteId + "][" + nativeAdId
                        + "] doesn't have valid mandatory field thus not adding to Template Cache.");
            }
            return new DBEntity<NativeAdTemplateEntity, String>(templateEntity, modifiedOn);
        } catch (final Exception e) {
            logger.error("Error in resultset row", e);
            return new DBEntity<NativeAdTemplateEntity, String>(new EntityError<String>(siteId,
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
