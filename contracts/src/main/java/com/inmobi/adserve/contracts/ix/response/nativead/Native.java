package com.inmobi.adserve.contracts.ix.response.nativead;

import java.util.List;

import com.inmobi.adserve.contracts.ix.common.CommonExtension;
import com.inmobi.template.gson.DefaultValue;
import com.inmobi.template.gson.GsonContract;
import com.inmobi.template.gson.Required;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


/**
 * @author ritwik.kumar
 */
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@GsonContract
public final class Native {
    @DefaultValue(1) private Integer ver;
    @NonNull @Required private List<Asset> assets;
    @NonNull @Required private Link link;
    private List<String> imptrackers;
    private String jstracker;
    private CommonExtension ext;
}
