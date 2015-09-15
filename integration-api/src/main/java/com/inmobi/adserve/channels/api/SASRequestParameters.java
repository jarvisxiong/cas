package com.inmobi.adserve.channels.api;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.inmobi.adserve.adpool.ConnectionType;
import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.adpool.EncryptionKeys;
import com.inmobi.adserve.adpool.IntegrationDetails;
import com.inmobi.adserve.adpool.RequestedAdType;
import com.inmobi.adserve.channels.entity.SiteEcpmEntity;
import com.inmobi.adserve.channels.entity.WapSiteUACEntity;
import com.inmobi.segment.impl.AdTypeEnum;
import com.inmobi.types.DeviceType;
import com.inmobi.types.LocationSource;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class SASRequestParameters {
    private String allParametersJson;
    private String remoteHostIp;
    private String userAgent;

    private String source; // WAP/APP
    private Short age;
    private String gender;
    private String locSrc; // wifi/ip
    private String postalCode;
    // User location parameters
    private String latLong;
    private String countryCode; // Country Code like US for USA
    private Long countryId; // Integer value for country
    private LocationSource locationSource;
    private Set<Long> geoFenceIds;
    private String impressionId;
    private String clurl;
    private String siteId;
    private Long placementId;
    private ContentType siteContentType;
    private String sdkVersion;
    private long siteIncId;
    private long adIncId;
    private String adcode;
    // Site parameters
    private List<Long> categories;
    private Double siteFloor = 0.0d;
    private Boolean allowBannerAds = true;
    private Integer siteSegmentId;
    private Integer placementSegmentId;

    // Pub Control parameter.
    private List<AdTypeEnum> pubControlSupportedAdTypes;
    private String pubControlPreferencesJson;

    // Uid parameters
    private String uidParams;
    private Map<String, String> tUidParams;

    private String rqIframe;
    private String rFormat;

    private DeviceType deviceType;
    private long modelId;
    private long manufacturerId;
    private String deviceMake;
    private String deviceModel;
    private int osId;
    private Short rqMkAdcount;
    private String tid;
    private long handsetInternalId;
    private Integer carrierId;
    private Integer city;
    private Integer state;
    private List<Short> rqMkSlot;
    private List<Short> processedMkSlot;
    private Integer ipFileVersion;
    private boolean isRichMedia;
    private boolean isRewardedVideo;
    private String imaiBaseUrl;
    private String appUrl;
    private Set<String> uAdapters;

    private int dst; // This will describe the type of request dcp, rtbd or ix
    private Set<Integer> accountSegment;
    private int sst; // 0 for Network
    private String pubId;
    private String osMajorVersion;
    private ConnectionType connectionType;
    private double marketRate;
    private IntegrationDetails integrationDetails;
    private String appBundleId;
    private String normalizedUserId;
    private RequestedAdType requestedAdType;

    private EncryptionKeys encryptionKey;
    private boolean isKeepAlive;
    private Set<Integer> csiTags;
    // UAC enrichment data
    private WapSiteUACEntity wapSiteUACEntity;
    private SiteEcpmEntity siteEcpmEntity;
    private boolean isVideoSupported;
    private String referralUrl;
    private String automationTestId;
    private Set<Long> cauMetadataSet;
    private boolean customTemplatesOnly = false;

    // requestGuid is a unique identifier used for tracking purposes between ump and the SDK. This value was
    // earlier being set by ump only in case of Native Ads so DCP flow was unaffected, but due to response format
    // unification introduced in SDK 500, DCP will be setting this value directly in the wrapped DCP response.
    // note: requestGuid is different from the task id (the unique id between ump and other adpools)
    private String requestGuid;

    public enum HandSetOS {
        OTHERS(1), Linux_Smartphone_OS(2), Android(3), Nokia_OS(4), iOS(5), RIM_OS(6), MTK_Nucleus_OS(7), Symbian_OS(8), Windows_Mobile_OS(
                9), Palm_OS(10), Bada_OS(11), webOS(12), Windows_Phone_OS(13), Rex_Qualcomm_OS(14), Hiptop_OS(15), MeeGo(
                16), RIM_Tablet_OS(17), Desktop(18), Windows_CE(19), Windows_RT(20);

        private final int id;

        HandSetOS(final int id) {
            this.id = id;
        }

        public int getValue() {
            return id;
        }
    }

}
