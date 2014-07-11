package com.inmobi.adserve.channels.api.natives;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity;
import com.inmobi.adserve.channels.repository.NativeConstrains;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.casthrift.rtb.Native;

public class NativeBuilderImpl implements NativeBuilder{
	
	private NativeAdTemplateEntity templateEntity;
	private Native nativeObj;
	
	
	@Inject
	public NativeBuilderImpl(@Assisted NativeAdTemplateEntity templateEntity){
		nativeObj = new Native();
		this.templateEntity = templateEntity;
		
	}
	
	@Override
	public Native build(){
		nativeObj.setMandatory(NativeConstrains.getMandatoryList(templateEntity.getMandatoryKey()));
		nativeObj.setImage(NativeConstrains.getImage(templateEntity.getImageKey()));
		
		return nativeObj;
	}
	
	

}
