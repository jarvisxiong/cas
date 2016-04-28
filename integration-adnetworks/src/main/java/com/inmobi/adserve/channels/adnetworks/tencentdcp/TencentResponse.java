package com.inmobi.adserve.channels.adnetworks.tencentdcp;

import lombok.Data;

/**
 * Created by thushara.v on 3/10/16.
 */
@Data
public class TencentResponse {
    private String res_url;
    private String pv_url;
    private String cv_url;
    private String download_url;
    private String click_3p;
    private String app_name;
    private String description;
    private int width;
    private int height;
}
