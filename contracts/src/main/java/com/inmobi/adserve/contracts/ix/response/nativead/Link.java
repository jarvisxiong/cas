package com.inmobi.adserve.contracts.ix.response.nativead;

import java.util.List;

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
public class Link {
    @Required private String url;
    private List<String> clicktrackers;
    private String fallback;
    private CommonExtension ext;
}
