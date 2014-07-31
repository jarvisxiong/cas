package com.inmobi.adserve.channels.repository;

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

public class NativeAdTemplateRepository extends
		AbstractStatsMaintainingDBRepository<NativeAdTemplateEntity, String> implements RepositoryManager {
	
	private String abc = "#set($social = $tool.jpath($first, \"imNative.creative\").get(\"social\")) #if ($tool.isNonNull($social)) #set($store = $social.get(\"appstore\")) #end ## Subtitle shouldn't be more than 100 characters. #set($store = $social.get(\"appstore\")) #if($tool.isNonNull($store)) #set($downloads=$store.get(\"downloads\")) #if ($tool.isNotEmpty($downloads)) #if ($downloads.contains(\"<\")) #set($playerNum = $downloads.replaceAll(\"<\", \"\").trim()) #elseif ($downloads.contains(\">\")) #set($playerNum = $downloads.replaceAll(\">\", \"\").trim()) #elseif($downloads.contains(\"-\")) #set($idx = $downloads.indexOf(\"-\") + 1) #set($playerNum = $downloads.substring($idx).trim()) #else #set($playerNum = $downloads) #end #set($playerNum = $playerNum.replaceAll(\",\", \"\")) #else #set($playerNum = \"\") #end #else #set($playerNum = \"\") #end ## description #set($subtitle = $tool.jpath($first, \"imNative.creative.description.text\").replaceAll(\"\\s+\", \" \")) #if ($subtitle.length() > 100) #set($subtitle = \"$subtitle.substring(0, 97)...\") #end ## icon #set ($icons = $tool.jpath($first, \"imNative.creative.icon\")) #foreach( $icon in $icons) #set ($width = $icon.get(\"width\")) #if ($width == 150) #set ($sel_icon = $icon) #break #end #if ($width >150 && $width <= 300) #if ($tool.isNonNull($h_icon)) #set ($h_icon = $icon) #end #elseif ($width > 75 && $width <= 150) #if (!$tool.isNonNull($m_icon)) #set ($m_icon = $icon) #end #elseif($width > 37 && $width <= 75) #if (!$tool.isNonNull($l_icon)) #set ($l_icon = $icon) #end #end #if ($h_icon && $m_icon && $l_icon) #break #end #end #if ($tool.isNonNull($m_icon)) #set ($sel_icon = $m_icon) #elseif ($tool.isNonNull($h_icon)) #set ($sel_icon = $h_icon) #elseif ($l_icon) #set ($sel_icon = $l_icon) #else ## No matching icon, pick first one. #set ($sel_icon = $icons.get(0)) #end ## screenshot #set ($screenshots = $tool.jpath($first, \"imNative.creative.image\")) #foreach( $ss in $screenshots) #set ($width = $ss.get(\"width\")) #set ($height = $ss.get(\"height\")) #set ($aspectratio = $math.toDouble($math.div($math.toDouble($math.floor($math.mul($math.div($width, $height), 100))), 100))) #set($same_ar_screenshots = []) #if ($aspectratio == 1.91) #if ($width==600 || $width==1200) #set ($sel_ss = $ss) #break #end #set($temp_screenshot = $same_ar_screenshots.add($ss)) #if ($width > 1200 && $width <= 1200) #if (!$tool.isNonNull($h_screenshot)) #set ($h_screenshot = $ss) #end #elseif ($width > 600 && $width <= 1200) #if (!$tool.isNonNull($m_screenshot)) #set ($m_screenshot = $ss) #end #elseif ($width > 300 && $width <= 600) #if (!$tool.isNonNull($l_screenshot)) #set ($l_screenshot = $ss) #end #end #if ($h_screenshot && $m_screenshot && $l_screenshot) #break #end #end #end ##If exact match didn't happen then go for resolution checks. #if (!$tool.isNonNull($sel_ss)) #if ($tool.isNonNull($m_screenshot)) #set ($sel_ss = $m_screenshot) #elseif ($tool.isNonNull($l_screenshot)) #set ($sel_ss = $l_screenshot) #elseif ($tool.isNonNull($h_screenshot)) #set ($sel_ss = $h_screenshot) #else #set ($sel_ss = $same_ar_screenshots.get(0)) #end #end ## get appId #if($tool.isNonNull($store)) #set($appId=$store.get(\"uacId\")) #end #if(!$tool.isNonNull($appId)) #set($appId=\"12345\") #end ## get the rating #if($tool.isNonNull($store)) #set($rating=$store.get(\"rating\")) #end #set($x_icon={}) #set($x_icon.w=$sel_icon.width) #set($x_icon.h=$sel_icon.height) #set($x_icon.url=$sel_icon.url) #set($x_ss={}) #set($x_ss.w=$sel_ss.width) #set($x_ss.h=$sel_ss.height) #set($x_ss.url=$sel_ss.url) #set ($String = \"\") ## Define json map #set($pubContentMap = { \"uid\" : $String.valueOf($appId), \"title\" : $tool.jpath($first, \"imNative.creative.headline.text\"), \"subtitle\" : $subtitle, \"click_url\" : $first.openingLandingUrl, \"app_url\" : \"\", \"icon_xhdpi\" : $x_icon, \"image_xhdpi\" : $x_ss, \"star_rating\" : $String.valueOf($rating), \"players_num\": $playerNum, \"imp_id\" : $first.adImpressionId, \"cta_install\" : $tool.jpath($first, \"imNative.creative.action\").get(\"text\") }) #set($pubContent = $tool.jsonEncode($pubContentMap)) $tool.nativeAd($first, $pubContent)";	
	
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
           
            
            List<String> keys = adTemplate.getDemandConstraints().getJsonPath();
            if(keys ==null){
            	throw new Exception("No mandatory field found.");
            }
            
            Iterator<String> itr = keys.iterator();
            while(itr.hasNext()){
            	String key = itr.next();
            	if(NativeConstrains.isImageKey(key)){
            		builder.setImageKey(key);
            	}else if(NativeConstrains.isMandatoryKey(key)){
            		builder.setMandatoryKey(key);
            	}
            }
            
            NativeAdTemplateEntity templateEntity = builder.build();
            if(templateEntity.getMandatoryKey()!=null){
            	if("c17df33c32074522aa83d87d6f1cada1".equals(templateEntity.getSiteId())) {
            		TemplateManager.getInstance().addToTemplateCache(templateEntity.getSiteId(), abc);
            	}else{
            		TemplateManager.getInstance().addToTemplateCache(templateEntity.getSiteId(), adTemplate.getDetails().getContent());
            	}
            	 logger.debug("Adding site id  "+siteId+" and nativeId "+nativeAdId+" to NativeAdTemplateRespository");
            }else{
            	logger.warn("SiteId["+siteId+"]["+nativeAdId+"] doesn't have valid mandatory field thus not adding to Template Cache.");
            }
            
           
            return new DBEntity<NativeAdTemplateEntity, String>(templateEntity, null);
        }
        catch (Exception e) {
            logger.error("Error in resultset row", e);
            return new DBEntity<NativeAdTemplateEntity, String>(new EntityError<String>(null, "ERROR_IN_READING_NATIVE_TEMPLATE"),null);
        }
	}
	
	

	@Override
	public boolean isObjectToBeDeleted(NativeAdTemplateEntity entity) {
		  if (entity.getId() == null || entity.getMandatoryKey() == null) {
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
