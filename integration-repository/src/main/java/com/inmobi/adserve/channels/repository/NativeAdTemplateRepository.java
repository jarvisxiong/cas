package com.inmobi.adserve.channels.repository;

import org.apache.commons.codec.binary.Base64;
import org.apache.thrift.TDeserializer;

import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity;
import com.inmobi.adserve.channels.util.NativeTemplateAttributeFinder;
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

public class NativeAdTemplateRepository extends
		AbstractStatsMaintainingDBRepository<NativeAdTemplateEntity, String> implements RepositoryManager {
	
	
	@Override
	public DBEntity<NativeAdTemplateEntity, String> buildObjectFromRow(
			ResultSetRow resultSetRow) throws RepositoryException {
        NullAsZeroResultSetRow row = new NullAsZeroResultSetRow(resultSetRow);
        try {
            String siteId = row.getString("site_id");
            long nativeAdId = row.getLong("native_ad_id");
            String encodedTemplate = row.getString("binary_template");
            
            byte[] binaryTemplate = Base64.decodeBase64(encodedTemplate);
            
            
            TDeserializer deserializer = new TDeserializer();
            com.inmobi.adtemplate.platform.AdTemplate adTemplate = new com.inmobi.adtemplate.platform.AdTemplate();
            deserializer.deserialize(adTemplate, binaryTemplate);
            
            
            NativeAdTemplateEntity.Builder builder = NativeAdTemplateEntity.newBuilder();
            builder.setSiteId(siteId);
            builder.setNativeAdId(nativeAdId);
            
            NativeAdTemplateEntity templateEntity = builder.build();
            
            TemplateManager.addToTemplateCache(templateEntity.getKey(), adTemplate.getDetails().getContent());
            
            logger.debug("SiteId  = "+siteId+" : nativeAdId = "+nativeAdId+" : template = "+adTemplate.getDetails().getContent());
            
            
            return new DBEntity<NativeAdTemplateEntity, String>(templateEntity, null);
        }
        catch (Exception e) {
            logger.error("Error in resultset row", e);
            return new DBEntity<NativeAdTemplateEntity, String>(new EntityError<String>(null, "ERROR_IN_READING_NATIVE_TEMPLATE"),null);
        }
	}
	
	

	@Override
	public boolean isObjectToBeDeleted(NativeAdTemplateEntity entity) {
		  if (entity.getId() == null || entity.getNativeAdId() <1) {
	            return true;
	        }
		  return false;
	}

	@Override
	public HashIndexKeyBuilder<NativeAdTemplateEntity> getHashIndexKeyBuilder(
			String className) {
		return null;
	}
	
	@Override
	public NativeAdTemplateEntity queryUniqueResult(RepositoryQuery q)
			throws RepositoryException {
		return null;
	}

	
}
