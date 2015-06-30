package com.inmobi.adserve.channels.adnetworks.taboola;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * Created by thushara.v on 04/06/15.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaboolaResponse {
    private String session;
    private String id;
    private NativeJson[] list;
}
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
class Thumbnail{

    private String url;
    private int width;
    private int height;

}
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
class NativeJson{

    private String branding;
    private String type;
    private String name;
    private String[] categories;
    private String duration;
    private String id;
    private String url;
    private Thumbnail[] thumbnail;
    private String description;
}

