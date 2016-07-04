package com.inmobi.adserve.contracts.microsoft.request;

import lombok.Data;

import java.util.Map;

/**
 * Created by deepak.jha on 3/22/16.
 */
@Data
public class Publisher {
    private String id;
    private String name;
    private Map<String,String> ext;
}
