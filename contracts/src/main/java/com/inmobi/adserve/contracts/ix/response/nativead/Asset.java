package com.inmobi.adserve.contracts.ix.response.nativead;

import com.inmobi.adserve.contracts.ix.common.CommonExtension;
import com.inmobi.template.gson.DefaultValue;
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
public class Asset {
    @Required private Integer id;
    @DefaultValue(0) private Integer required;
    private Title title;
    private Image img;
    private Video video;
    private Data data;
    private Link link;
    private CommonExtension ext;
}
