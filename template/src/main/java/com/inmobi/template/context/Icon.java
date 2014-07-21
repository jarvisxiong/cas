package com.inmobi.template.context;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.inmobi.template.interfaces.Context;

@Getter
@ToString
public class Icon extends AbstractContext{
	
	private int 	w;
	private int 	width;
	private int 	height;
	private int 	h;
	private String  url;
	
	private Icon(Builder builder){
		this.w = builder.w;
		this.h = builder.h;
		this.height = builder.h;
		this.width = builder.w;
		this.url = builder.url;
		setValues(params);
	}

	@Override
	 void setValues(Map<String, Object> params) {
		params.put(KeyConstants.WIDTH, this.w);
		params.put(KeyConstants.HEIGHT, this.h);
		params.put(KeyConstants.URL, this.url);
	}
	
	public static Builder newBuilder(){
		return new Builder();
	}
	
	@Setter
	public static class Builder{
		
		private int 	w;
		private int 	h;
		private String  url;
		
		public Context build(){
			return new Icon(this);
		}
		
	}

}
