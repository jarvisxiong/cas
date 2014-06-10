package com.inmobi.adserve.channels.api;

import java.util.List;

import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;

import quicktime.std.image.DSequence;

import com.inmobi.casthrift.rtb.Bid;
import com.inmobi.casthrift.rtb.BidResponse;
import com.inmobi.casthrift.rtb.Native;
import com.inmobi.casthrift.rtb.NativeResponse;
import com.inmobi.casthrift.rtb.NativeResponseData;

public class NativeTemplateFormatterImpl implements NativeTemplateFormatter {
	
	//TODO: remove it. Just added for tango for MVP.
	private String pubContent = "{"
			+ "\"uid\":\"$UID\","
			+ "\"title\":\"$TITLE\","
			+ "\"subtitle\":\"$SUBTITLE\","
			+ "\"click_url\":\"$CLICK_URL\","
			+ "\"app_url\":\"\","
			+ "\"icon_xhdpi\":{"
				+ "\"w\":300,"
				+ "\"h\":300,"
				+ "\"url\":\"ICON_URL\""
				+ "},"
		    + "\"image_xhdpi\":{"
		    	+ "\"w\":1200,"
		    	+ "\"h\":627,"
		    	+ "\"url\":\"$IMAGE_URL\""
		    	+ "},"
		    + "\"star_rating\":\"$RATING\","
		    + "\"players_num\":\"\","
		    + "\"imp_id\":\"$IMP_ID\","
		    + "\"cta_install\":\"Install\"}";

	@Override
	public String getFormatterValue(String template, BidResponse response) throws TException {
		Bid bid = response.getSeatbid().get(0).getBid().get(0);
		String adm = bid.getAdm();
		
		NativeResponse natResponse = new NativeResponse();
		TDeserializer deserializer = new TDeserializer();
		deserializer.deserialize(natResponse, adm.getBytes());
		
		Native nat = response.getExt().getNativeObject();
		
		//from adm thrift get the follow values
				/*
				 * 1. title <->headline
				 * 2. subtitle <->description
				 * 3. clickurl
				 */
		
		String title = natResponse.getTitle();
		String subtitle = natResponse.getDescription();
		String clickUrl = natResponse.getClickurl().get(0);
		String iconUrl = natResponse.getIconurl();
		
		
		String impId = bid.getImpid();
		long imageWidth = 	nat.getImage().width;// Image should have height field too.
		//long imageheight = nat.getImage().height
		int suggestList = nat.getSuggestedSize();
		if(suggestList == 1){
		  List<NativeResponseData> data = natResponse.getData();
		}
		
		
		
		return null;
	}

}
