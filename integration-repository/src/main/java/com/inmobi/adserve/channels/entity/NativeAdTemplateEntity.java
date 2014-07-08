package com.inmobi.adserve.channels.entity;

import lombok.Getter;
import lombok.Setter;

import com.inmobi.phoenix.batteries.data.IdentifiableEntity;

@Getter
public final class NativeAdTemplateEntity implements IdentifiableEntity<String>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -648051414378424341L;
	private String      siteId;
    private long        nativeAdId;

	private NativeAdTemplateEntity(Builder builder){
		this.siteId = builder.siteId;
		this.nativeAdId=builder.nativeAdId;
	}
	
	@Override
	public String getJSON() {
		return null;
	}

	@Override
	public String getId() {
		return this.siteId;
	}
	
	
	public static Builder newBuilder() {
        return new Builder();
    }
	
	public String getKey(){
		return this.siteId+"-"+this.nativeAdId;
	}
	
	
	@Setter
	public static class Builder {
	    private String      siteId;
	    private long        nativeAdId;
	    
	    public NativeAdTemplateEntity build() {
	        return new NativeAdTemplateEntity(this);
	    }
	}

}




