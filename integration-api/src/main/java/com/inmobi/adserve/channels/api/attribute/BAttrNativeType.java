package com.inmobi.adserve.channels.api.attribute;

import java.util.ArrayList;
import java.util.List;

public class BAttrNativeType implements NativeAttributeType<List<Integer>> {
	
	List<Integer> battr = new ArrayList<>(4);
	
	public BAttrNativeType(){
		battr.add(7);
    	battr.add(9);
    	battr.add(10);
    	battr.add(14);
	}

	@Override
	public List<Integer> getAttributes() {
		return battr;
	}

}
