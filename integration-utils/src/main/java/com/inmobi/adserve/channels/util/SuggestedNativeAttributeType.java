package com.inmobi.adserve.channels.util;

import java.util.ArrayList;
import java.util.List;

public class SuggestedNativeAttributeType implements NativeAttributeType<List<Integer>> {

	List<Integer> suggested = new ArrayList<>();
	
	public SuggestedNativeAttributeType(){
		suggested.add(0);
	}
	
	@Override
	public List<Integer> getAttributes() {
		return suggested;
	}

}
