package com.inmobi.adserve.channels.api.natives;

import com.inmobi.casthrift.rtb.Native;

public interface NativeBuilder {

    /**
     * 
     * @return
     */
    Native build();
    
    /**
     * 
     * @return
     */
    com.inmobi.adserve.contracts.ix.request.nativead.Native buildNativeIX();

}
