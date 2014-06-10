package com.inmobi.adserve.channels.entity;

import lombok.Setter;

import com.inmobi.adserve.channels.entity.ChannelEntity.Builder;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;

public class NativeAdTemplateEntity implements IdentifiableEntity<String>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -648051414378424341L;
	private String      siteId;
    private long      nativeAdId;
    private String      binary_template;

	private NativeAdTemplateEntity(Builder builder){
		this.siteId = builder.siteId;
		this.nativeAdId=builder.nativeAdId;
		this.binary_template=builder.binary_template;
	}
	
	@Override
	public String getJSON() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getId() {
		return this.siteId;
	}
	
	public String getSiteId() {
		return this.siteId;
	}
	
	public long getNativeTemplateId() {
		return this.nativeAdId;
	}
	
	
	public static Builder newBuilder() {
        return new Builder();
    }
	
	@Setter
	public static class Builder {
	    private String      siteId;
	    private long      nativeAdId;
	    private String      binary_template;
	    
	    public NativeAdTemplateEntity build() {
	        return new NativeAdTemplateEntity(this);
	    }
	}

}




