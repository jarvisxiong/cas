package com.inmobi.template.context;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.inmobi.template.interfaces.Context;


@Getter
@ToString
public final class App extends AbstractContext{
	
	private String title;
	private String desc;
	private List<Icon> icons;
	private List<Screenshot> screenshots;
	private String id;
	private String openingLandingUrl;
	private String rating;
	private int rating_count;
	private int downloads;
	
	
	private App(Builder builder){
		this.title = builder.title;
		this.desc = builder.desc;
		this.icons = builder.icons;
		this.screenshots = builder.screenshots;
		this.id = builder.id;
		this.rating = builder.rating;
		this.rating_count = builder.rating_count;
		this.downloads = builder.downloads;
		this.openingLandingUrl = builder.openingLandingUrl;
		
		setValues(params);
	}

	@Override
	public void setValues(Map<String, Object> params) {
		params.put(KeyConstants.APP_DESC, this.desc);
		params.put(KeyConstants.APP_ICONS, this.icons);
		params.put(KeyConstants.APP_SCREENSHOTS, this.screenshots);
		params.put(KeyConstants.APP_ID	, this.id);
		params.put(KeyConstants.APP_TITLE, this.title);
		params.put(KeyConstants.APP_RATING, this.rating);
		params.put(KeyConstants.APP_RATING_COUNT, this.rating_count);
		params.put(KeyConstants.APP_DOWNLOADS, this.downloads);
	}
	
	public static Builder newBuilder(){
		return new Builder();
	}
	
	
	public static class Builder{
		
		@Setter private String title;
		@Setter private String desc;
		@Setter private List<Icon> icons;
		@Setter private List<Screenshot> screenshots;
		@Setter private String id;
		@Setter private String openingLandingUrl;
		@Setter private String rating;
		@Setter private int rating_count;
		@Setter private int downloads;
		
		public Context build(){
			return new App(this);
		}
		
	}

}
