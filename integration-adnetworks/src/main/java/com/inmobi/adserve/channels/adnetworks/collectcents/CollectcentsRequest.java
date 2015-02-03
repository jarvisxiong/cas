package com.inmobi.adserve.channels.adnetworks.collectcents;

import java.util.List;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


@Data
@JsonInclude(Include.NON_NULL)
public class CollectcentsRequest {
	private String responseformat;
	private List<MainRequest> main;
    
    private Site site;
    private Device device;
    private User user;
 
}

@Data
@JsonInclude(Include.NON_NULL)
class MainRequest{
	private String pubid;
    private int ads;
    private String adtype;
    private String response;
    private Banner banner;
}

@Data
@JsonInclude(Include.NON_NULL)
class Banner{
    private short adsize;
    private String pos;

}


@Data
@JsonInclude(Include.NON_NULL)
class Device{
    private String os;
    private String ip;
    private String deviceid;
    private String ua;
    private String conntype;
    private Geo geo;
}

@Data 
@JsonInclude(Include.NON_NULL)
class Geo{
	private String geolat;
	private String geolong;
}

@Data
@JsonInclude(Include.NON_DEFAULT)
class User{
	private int yob;
	private String gender;
}

@Data
@JsonInclude(Include.NON_NULL) 
class Site{
    private String rated;
    private String category;
    private String id; 
}

