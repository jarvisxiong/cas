package com.inmobi.template.context;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.inmobi.template.interfaces.Context;

@ToString
@Getter
public class DataMap extends AbstractContext {
	
	
	private int downloads;
	private String rating;
	private int rating_count;
	
	private DataMap(Builder builder){
		this.downloads = builder.downloads;
		this.rating = builder.rating;
		this.rating_count = builder.rating_count;
		setValues(params);
	}
	

	@Override
	void setValues(Map<String, Object> params) {
		
		params.put("downloads", this.downloads);
		params.put("rating",this.rating);
		params.put("rating_count", this.rating_count);
	}
	
	public static Builder newBuilder(){
		return new Builder();
	}
	
	
	public static class Builder{
		@Setter private int downloads;
		@Setter private String rating;
		@Setter private int rating_count;
		
		public Context build(){
			return new DataMap(this);
		}
		
		
	}

}
