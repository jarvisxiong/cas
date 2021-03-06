package com.inmobi.adserve.contracts.ix.response;

import java.util.List;

import com.inmobi.adserve.contracts.ix.common.CommonExtension;
import com.inmobi.adserve.contracts.common.response.nativead.AdmObject;
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
public final class Bid {
    @NonNull @Required private String id;
    @NonNull @Required private String impid;
    @NonNull @Required private Double price;
    private String aqid;    // Not in official spec - No Need for making this required, since we have AbstractAuctionFilter
    private int estimated;
    private String dealid;
    private int pmptier;
    private String nurl;
    private String adm;
    private String crid;
    private AdmObject admobject;
    private List<String> adomain;
    private int h;
    private int w;
    private double adjustbid;   // Not in official spec
    private CommonExtension ext;
}
