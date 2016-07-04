package com.inmobi.adserve.contracts.microsoft.request;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Created by deepak.jha on 3/22/16.
 */
@lombok.Data
public class Imp {
    private String id;
    private String time;
    private String displaymanager;
    private String displaymanagerver;
    @SerializedName("native")
    private Native nat ;
    private Map<String,String> auth;
    private Map<String,String> ext;
}
