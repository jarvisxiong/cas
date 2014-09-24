package com.inmobi.adserve.channels.adnetworks.wapstart;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Data
@JsonInclude(Include.NON_NULL)
public class WapStartAdrequest {
	private Impression impression;
	private Site site;
	private Device device;
	private User user;

}
@Data
@JsonInclude(Include.NON_NULL)
class Segment{
	private String name;
	private String value;

}

@Data
@JsonInclude(Include.NON_NULL)
class WapstartData{
	private String name;
	private Segment segment;

}

@Data
@JsonInclude(Include.NON_NULL)
class User{
	private String uid;
	private int yob;
	private int gender;
	private WapstartData data;
}

@Data
@JsonInclude(Include.NON_NULL)
class Geo{
	private String lat;
	private String lon;
	private String country;
}

@Data
@JsonInclude(Include.NON_NULL)
class Device{
	private String ip;
	private String ua;
	private String adid;
	private String idfa;
	private String ifa;
	private String android_id;
	private String imei;
	private Geo geo;
}

@Data
@JsonInclude(Include.NON_NULL)
class Publisher{
	private long id;
	private String name;
	private String url;
}

@Data
@JsonInclude(Include.NON_NULL)
class Site{
	private int id;
	private int ctype;
	private Publisher publisher;
}

@Data
@JsonInclude(Include.NON_NULL)
class Banner{
	private int h;
	private int w;
	private int api;
	private int btype;
}

@Data
@JsonInclude(Include.NON_NULL)
class Impression{
	private Banner[] banner;
}
