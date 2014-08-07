package com.inmobi.adserve.channels.api.attribute;

import java.util.ArrayList;
import java.util.List;

public class MandatoryNativeAttributeType implements NativeAttributeType<List<Integer>> {
	
	public static enum MandatoryType {
		ICON,
		MEDIA,
		HEADLINE,
		DESCRIPTION
	}
	
	static List<Integer> attrs = new ArrayList<Integer>(MandatoryType.values().length);
	static{
		attrs.add(MandatoryType.ICON.ordinal());
		attrs.add(MandatoryType.MEDIA.ordinal());
		attrs.add(MandatoryType.HEADLINE.ordinal());
		attrs.add(MandatoryType.DESCRIPTION.ordinal());
	}

	@Override
	public List<Integer> getAttributes() {
		return attrs;
	}


	
}
