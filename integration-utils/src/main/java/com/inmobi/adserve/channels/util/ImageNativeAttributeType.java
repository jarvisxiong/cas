package com.inmobi.adserve.channels.util;

import com.inmobi.casthrift.rtb.Image;

public class ImageNativeAttributeType implements NativeAttributeType<Image> {

	
	@Override
	public Image getAttributes() {
		Image image = new Image();
		image.setAspectratio(1.91);
		image.setMaxwidth(1200);
		image.setMinwidth(600);
		
		return image;
	}

}
