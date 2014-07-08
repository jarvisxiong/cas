package com.inmobi.template.context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class AppManager {
	
	private static Gson gson = null;
	static GsonBuilder gsonBuilder = new GsonBuilder();
	static{
		gson = gsonBuilder.create();
	}
	
	public static App getAppFromString(String appStr){
		return gson.fromJson(appStr, App.class);
	}
	
	
	
	public static void main(String args[]){
		
		String adm = "{"
				+ "\"uid\":\"\","
				+ "\"title\":\"AppLovin\","
				+ "\"subtitle\":\"AppLovin, Your customers aren't targets, or personas, they're people.\","
				+ "\"click_url\":\"http://a.applovin.com/redirect?clcode=3!7283.1403763299!1eBApfg9DiRku4p9jpReauuqOeDX-MhPszWW7djk1vGz5f1NPyE5J0xF26F77hRCtqIWevzdO9qMVHThJepSDBhaoHvJkxmHFBqf3igP5UMydnbemHzmqZ4BdIssykwYUHh5MBm5EIpqj397e3JP6HpnA8SIJg-dvPX_zfGFw7fD3VxtjdAtjsMbkiUPNgaa0cat1D8Y1qf30_vuKczSFRHVZH_kM8hIo9AtN2ZPAExzK4GVtLv0ftRnw_ka1jFttupmqsS_RF-XxmbQJZTlQa-zTlq90uTYamOtOqs5c5PqcsZGp9uXYOYxkxKitq18V9TYNI4W0-ONmr8ziMcBqg**\","
				+ "\"app_url\":\"\","
				+ "\"icon_xhdpi\":{"
						+ "\"w\":1200,"
						+ "\"h\":627,"
						+ "\"url\":\"http://applovin-assets.s3.amazonaws.com/applovin_logo_80x80.png\""
						+ "},"
				+ "\"image_xhdpi\":{"
						+ "\"w\":1200,"
						+ "\"h\":627,"
						+ "\"url\":\"http://applovin-assets.s3.amazonaws.com/applovin_logo_1200x627.jpg\""
						+ "},"
				+ "\"star_rating\":\"\","
				+ "\"players_num\":\"\","
				+ "\"imp_id\":\"DDSSBFGH8765\","
				+ "\"cta_install\":\"Visit Us\""
				+ "}";
		
		
		
		
	}

}
