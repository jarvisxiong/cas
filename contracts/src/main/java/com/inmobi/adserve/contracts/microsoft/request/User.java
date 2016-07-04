package com.inmobi.adserve.contracts.microsoft.request;

import lombok.Data;

import java.util.List;

/**
 * Created by deepak.jha on 3/22/16.
 */
@Data
public class User {
    private String yob;
    private String gender;
    private List<String> keywords;
}
