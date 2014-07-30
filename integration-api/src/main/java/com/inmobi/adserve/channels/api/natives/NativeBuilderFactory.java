package com.inmobi.adserve.channels.api.natives;

import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity;



public interface NativeBuilderFactory {
	
	public NativeBuilder create(NativeAdTemplateEntity entiry);

}
