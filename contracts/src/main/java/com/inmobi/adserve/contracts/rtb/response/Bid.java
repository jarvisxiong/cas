package com.inmobi.adserve.contracts.rtb.response;

import java.util.List;

import com.inmobi.adserve.contracts.common.response.nativead.AdmObject;
import com.inmobi.template.gson.GsonContract;
import com.inmobi.template.gson.Required;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Created by avinash.kumar on 12/14/15.
 */
@Data
@NoArgsConstructor
@GsonContract
public final class Bid {
    @NonNull @Required private String id;
    @NonNull @Required private String impid;
    @NonNull @Required private Double price;
    private String adid;
    private String nurl;
    private String adm;
    private AdmObject admobject;
    private List<String> adomain;
    private String iurl;
    private String cid;
    private String crid;
    private List<Integer> attr;
    private String dealid;
    private BidExtensions ext;
}
