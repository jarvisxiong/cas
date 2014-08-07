package com.inmobi.template.context;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.inmobi.template.interfaces.Context;


@ToString
@Getter
public final class Screenshot extends AbstractContext{
	
	
	 private int 	 w;
	 private int 	 h;
	 private int 	 width;
	 private int 	 height;
	 private String  ar;
	 private String  url;
	
	
	
	private Screenshot(Builder builder){
		this.w=builder.w;
		this.h= builder.h;
		this.width = this.w;
		this.height = this.h;
		this.url=builder.url;
		this.ar=builder.ar;
		setValues(params);
	}
	
	@Override
	public void setValues(Map<String, Object> params) {
		params.put("w", w);
		params.put("h", h);
		params.put("width", w);
		params.put("height", h);
		params.put("ar", ar);
		params.put("url", url);
	}
	
	public static Builder newBuilder(){
		return new Builder();
	}
	
	
	public static class Builder{
		
		@Setter private int 	w;
		@Setter private int 	h;
		@Setter private String  ar;
		@Setter private String  url;
		
		public Context build(){
			return new Screenshot(this);
			
		}
		
	}


}