package com.inmobi.template.deserializer;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.inmobi.template.context.App;
import com.inmobi.template.context.DataMap;
import com.inmobi.template.context.Icon;
import com.inmobi.template.context.Screenshot;
import com.inmobi.template.interfaces.Context;

public class AppDeserializer implements JsonDeserializer<Context> {

	@Override
	public Context deserialize(JsonElement json, Type typeOf, JsonDeserializationContext context) throws JsonParseException {
		JsonObject jsonObj = json.getAsJsonObject();
		String     title = null;
        if(jsonObj.get("title")!=null){		
        	title = jsonObj.get("title").getAsString();
        }
		//String     version = jsonObj.get("version").getAsString();
        String 	   desc = null;
        if(jsonObj.get("description")!=null){
        	 desc  = jsonObj.get("description").getAsString();
        }
		//String 	   actionText  = jsonObj.get("actiontext").getAsString();
        String 	   actionLink = null;
        if(jsonObj.get("actionlink")!=null){
        	actionLink  = jsonObj.get("actionlink").getAsString();
        }
		//String 	   cta_install  = jsonObj.get("actiontext").getAsString();
		JsonElement  idElement   = jsonObj.get("uid");
		String id ="";
		if(idElement!=null){
			id = idElement.getAsString();
		}
		
		JsonElement imgElement = jsonObj.get("image");
		Screenshot imgs[] = null;
		if(imgElement!=null){
			 imgs = new Screenshot[]{ context.deserialize(imgElement, Screenshot.class)};
		}
		
		JsonElement dataElement = jsonObj.get("data");
		DataMap dataMap = null;
		if(dataElement!=null){
			dataMap = context.deserialize(dataElement, DataMap.class);
		}
		
		JsonElement	   iconElement = jsonObj.get("iconurl");
		Icon     icons[] = null;
		if(iconElement!=null){
			String iconurl = iconElement.getAsString();
			Icon.Builder icon = Icon.newBuilder();
			icon.setH(300);
			icon.setW(300);
			icon.setUrl(iconurl);
			icons = new Icon[]{(Icon)icon.build()};
			
		}
		//Icon     icons[] = new Icon[]{context.deserialize(iconElement, Icon.class)};
		
		
		App.Builder app = App.newBuilder();
		app.setDesc(desc);
		app.setTitle(title);
		app.setOpeningLandingUrl(actionLink);
		if(icons!=null){
			app.setIcons(Arrays.asList(icons));
		}
		app.setId(id);
		app.setPixelUrls(getUrls(jsonObj.get("pixelurl")));
		app.setClickUrls(getUrls(jsonObj.get("clickurl")));
		if(imgs!=null){
			app.setScreenshots(Arrays.asList(imgs));
		}
		
		if(dataMap!=null){
			app.setDownloads(dataMap.getDownloads());
			app.setRating(dataMap.getRating());
			app.setRating_count(dataMap.getRating_count());
		}
		
		
		return app.build();
	}
	
	
	private List<String> getUrls(JsonElement jsonElement){
		List<String> urlList = null;
		if(jsonElement!=null){
			JsonArray array = jsonElement.getAsJsonArray();
			
			if(array!=null){
				urlList = Lists.newArrayList();
				Iterator<JsonElement> itr = array.iterator();
				while(itr.hasNext()){
					String url = itr.next().getAsString();
					urlList.add(url);
				}
			}
		}
		return urlList;
	}

}
