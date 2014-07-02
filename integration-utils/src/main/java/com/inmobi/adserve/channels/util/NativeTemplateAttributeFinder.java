package com.inmobi.adserve.channels.util;

public class NativeTemplateAttributeFinder {
	
	public <T> T findAttribute(NativeAttributeType<T> type){
		
		return type.getAttributes();
	}

}
