package com.inmobi.template.interfaces;

import java.util.List;


public abstract class Tools {
	
	
	public abstract Object jpath(Context context, String key);

    public abstract String jpathStr(Context context, String key); 
    
    
    public boolean isNonNull(Object value) {
        return value != null;
    }
    
    public abstract String jsonEncode(Object json);    
    

    public abstract String nativeAd(Context context, String pubContent) ;
    
    public boolean newAsyncMode(Boolean abTestingMode, List<String> list, String value) {
      return true;
    }
    
    public Object boltObject(){

    	
    	
    	
    	return null;
    }
    

}
