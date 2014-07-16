package com.inmobi.adserve.channels.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.inmobi.casthrift.rtb.Image;

public class NativeConstrains {
	
	public static final int Icon = 0;
	public static final int Media = 1;
	public static final int Headline = 2;
	public static final int Description = 3;
	
	
	public enum Mandatory{
		
		ICON(Icon,"icon"),
		MEDIA(Media,"Media"),
		HEADLINE(Headline,"Headline"),
		DESCRIPTION(Description,"Description");
		
		private final int index;
		private String name;
		private Mandatory(int index,String name){
			this.index = index;
			this.name= name;
		}
		
		public String getName(){
			return this.name;
		}
		
		public int getIndex(){
			return this.index;
		}
	}
	
	public static final String layoutConstraint_3 = "layoutConstraint.3";
	public static final String layoutConstraint_2 = "layoutConstraint.2";
	public static final String layoutConstraint_1 = "layoutConstraint.1";
	
	public static final String inmTag_a083 = "inmTag.a083";
	public static final String inmTag_a067 = "inmTag.a067";
	public static final String inmTag_a056 = "inmTag.a056";
	public static final String inmTag_a12 = "inmTag.a12";
	public static final String inmTag_a15 = "inmTag.a15";
	public static final String inmTag_a177 = "inmTag.a177";
	public static final String inmTag_a191 = "inmTag.a191";
	public static final String inmTag_a64  = "inmTag.a64";
	public static final String inmTag_a808 = "inmTag.a808";
	
	
	private static Map<String, List<Integer>> mandatoryMap = new HashMap<>();
	private static Map<String, Image> 		  imageMap = new HashMap<>();
	
	static{
		mandatoryMap.put(layoutConstraint_1, 
				Lists.asList(Mandatory.ICON.getIndex(),
						new Integer[]{Mandatory.HEADLINE.getIndex()}));
		mandatoryMap.put(layoutConstraint_2, 
				Lists.asList(Mandatory.ICON.getIndex(),
						new Integer[]{Mandatory.HEADLINE.getIndex(),Mandatory.DESCRIPTION.getIndex()}));
		mandatoryMap.put(layoutConstraint_3, 
				Lists.asList(Mandatory.ICON.getIndex(),
						new Integer[]{Mandatory.HEADLINE.getIndex(),Mandatory.MEDIA.getIndex()}));
		
		imageMap.put(inmTag_a056, getImage(0.56,320,720));
		imageMap.put(inmTag_a067, getImage(0.67,320,800));
		imageMap.put(inmTag_a083, getImage(0.83,250,250));
		imageMap.put(inmTag_a12, getImage(1.2,300,300));
		imageMap.put(inmTag_a15, getImage(1.5,480,1200));
		imageMap.put(inmTag_a177, getImage(1.77,568,1280));
		imageMap.put(inmTag_a191, getImage(1.91,600,1200));
		imageMap.put(inmTag_a64, getImage(6.4,320,320));
		imageMap.put(inmTag_a808, getImage(12,728,728));
		
		
	}
	
	
	private static Image getImage(double ar,int minW, int maxW){
		Image img = new Image();
		img.setAspectratio(ar);
		img.setMaxwidth(maxW);
		img.setMinwidth(minW);
		return img;
	}
	
	public static boolean isMandatoryKey(String key){
		return mandatoryMap.containsKey(key);
	}
	
	public static boolean isImageKey(String key){
		return imageMap.containsKey(key);
	}

	
	public static List<Integer> getMandatoryList(String key){
		return mandatoryMap.get(key);
	}
	
	public static Image getImage(String key){
		Image img = imageMap.get(key);
		if(img!=null){
			return img.deepCopy();
		}
		return img;
	}
	

}
