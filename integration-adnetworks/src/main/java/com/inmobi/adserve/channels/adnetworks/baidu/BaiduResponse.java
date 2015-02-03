package com.inmobi.adserve.channels.adnetworks.baidu;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Data
@JsonInclude(Include.NON_NULL)
public class BaiduResponse {
    private String request_id;
    private Ad ads;
    private int error_code;
    
}

@Data
@JsonInclude(Include.NON_NULL)
class Ad {
	private String html_snippet;
}



