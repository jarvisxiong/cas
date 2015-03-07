package com.inmobi.adserve.contracts.ix.response.nativead;

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
public class Video {
    @Required private String vasttag;
}
