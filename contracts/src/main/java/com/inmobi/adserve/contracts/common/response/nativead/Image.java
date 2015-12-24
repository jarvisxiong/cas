package com.inmobi.adserve.contracts.common.response.nativead;

import com.inmobi.adserve.contracts.ix.common.CommonExtension;
import com.inmobi.template.gson.GsonContract;
import com.inmobi.template.gson.Required;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author ritwik.kumar
 */
@Getter
@Setter
@NoArgsConstructor
@GsonContract
public class Image {
    @Required private String url;
    private Integer w;
    private Integer h;
    private CommonExtension ext;
}
