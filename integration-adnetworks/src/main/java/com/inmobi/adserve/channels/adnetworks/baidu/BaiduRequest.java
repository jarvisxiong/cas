package com.inmobi.adserve.channels.adnetworks.baidu;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Created by thushara on 11/1/15.
 */

@Data
@JsonInclude(Include.NON_NULL)
public class BaiduRequest {
    private String request_id;
    private Version version;
    private String pid;
    private int carrier;
    private String lbs;
    private String ua;
    private Device device;
    private App app;
    private AdSlots adslots;
    private Network network;
    private Vendor vendor;
    private Gps gps;
}

@Data
@JsonInclude(Include.NON_NULL)
class AdUnit{
    private String id;
    private String size;

}


@Data
@JsonInclude(Include.NON_NULL)
class Device{
    private Udid udid;
  // private String platform;
    private Version os_version;
    private String brand;
 //   private String model;
    private String vendor;
}

@Data
@JsonInclude(Include.NON_NULL)
class App{
    private String name;
    private String id;
    private String category;
}

@Data
@JsonInclude(Include.NON_NULL)
class Version{
	private int major;
    private int minor;
}

@Data
@JsonInclude(Include.NON_NULL)
class Size{
    private int width;
    private int height;
}

@Data
@JsonInclude(Include.NON_NULL)
class Buyer{
    private int id;
    private int minor;
}

@Data
@JsonInclude(Include.NON_NULL)
class AdSlots {
    private String id;
    private Size size;
}

@Data
@JsonInclude(Include.NON_NULL)
class Network{
    private String ipv4;
    private Type type;
}

@Data
@JsonInclude(Include.NON_NULL)
class Udid{
    private String idfa;
}


@JsonInclude(Include.NON_NULL)
enum Type{
    WIFI,NEW_TYPE
}

@Data
@JsonInclude(Include.NON_NULL)
class Vendor{
    private String vendor;
}

@Data
@JsonInclude(Include.NON_NULL)
class Gps{
	private Type type;
	private Double latitude;
	private Double longitude;
}



