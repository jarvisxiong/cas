package com.inmobi.adserve.channels.api;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;


@Data
public class SASRequestParameters {

    private String       allParametersJson;
    private String       remoteHostIp;
    private String       userAgent;

    private String       source;               // WAP/APP
    private String       age;
    private String       gender;
    //private String       uid;
    private String       locSrc;               // wifi/ip
    private String       postalCode;
    // User location parameters
    private String       latLong;
    private String       country;
    private String       countryStr;
    private String       userLocation;
    private String       impressionId;
    private String       clurl;
    private String       siteId;
    private String       slot;
    private String       host;
    // TODO: Convert this to ENUM.
    private String       siteType;
    private String       sdkVersion;
    private long         siteIncId;
    private long         adIncId;
    private String       adcode;
    // Site parameters
    private List<Long>   categories;
    private Double       siteFloor      = 0.0d;
    private Boolean      allowBannerAds = true;
    private Integer      siteSegmentId;
    // Uid parameters
    private String       uidParams;
    private Map<String, String>     tUidParams;

    private String       rqIframe;
    private String       rFormat;

    private int          osId;
    private String       rqMkAdcount;
    private String       tid;
    private String       tp;
    private long         handsetInternalId;
    private int          carrierId;
    private String       city;
    private String       area;
    private String       rqMkSlot;
    private Integer      ipFileVersion;
    private boolean      isRichMedia;
    private String       rqAdType;
    private String       imaiBaseUrl;
    private String       appUrl;
    private int          modelId;

    private int          dst;                  // This will describe the type of request dcp or rtbd
    private Set<Integer> accountSegment;       // This will tell from which all type of segments you can fill for
                                                // example dso brancd, dso performance etc.
    private boolean      isResponseOnlyFromDcp;

    public SASRequestParameters() {
        // Do Nothing.
    }

    public enum HandSetOS {
        Others(1),
        Linux_Smartphone_OS(2),
        Android(3),
        Nokia_OS(4),
        iPhone_OS(5),
        RIM_OS(6),
        MTK_Nucleus_OS(7),
        Symbian_OS(8),
        Windows_Mobile_OS(9),
        Palm_OS(10),
        Bada_OS(11),
        webOS(12),
        Windows_Phone_OS(13),
        Rex_Qualcomm_OS(14),
        Hiptop_OS(15),
        MeeGo(16),
        RIM_Tablet_OS(17),
        Desktop(18),
        Windows_CE(19),
        Windows_RT(20);

        private final int id;

        HandSetOS(int id) {
            this.id = id;
        }

        public int getValue() {
            return id;
        }
    }
}
