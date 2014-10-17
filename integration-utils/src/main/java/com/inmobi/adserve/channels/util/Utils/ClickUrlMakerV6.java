package com.inmobi.adserve.channels.util.Utils;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * 
 * @author devi.chand@inmobi.com
 */

@ToString
public class ClickUrlMakerV6 {

  private static final Logger LOG = LoggerFactory.getLogger(ClickUrlMakerV6.class);


  private static final String DEFAULT_UDID_VALUE = "x";
  private static final String URLPATHSEP = "/";
  private static final String URLVERSIONINITSTR = "/C";
  private static final String URLMARKERCPC = "/t";
  private static final String URLMARKERCPM = "/b";
  private static final String DEFAULTIMSDK = "-1";
  private static final String CLICK = "1";
  private static final String BEACON = "0";
  private static final String IS_TEST = "1";
  private static final String IS_NOT_TEST = "0";
  private static final String RTB_SUPPLY = "rtb";
  private static final String NON_RTB_SUPPLY = "nw";
  private static final Long CLICK_URL_HASHING_SECRET_KEY_VERSION = (long) 1;
  private static final String CLICK_URL_HASHING_SECRET_KEY_VERSION_BASE_36 =
      getIdBase36(CLICK_URL_HASHING_SECRET_KEY_VERSION);
  private static final Long CLICK_URL_HASHING_SECRET_KEY_TEST_MODE_VERSION = (long) 2;
  private static final String CLICK_URL_HASHING_SECRET_KEY_TEST_MODE_VERSION_BASE_36 =
      getIdBase36(CLICK_URL_HASHING_SECRET_KEY_TEST_MODE_VERSION);
  @Getter
  private String beaconUrl;
  @Getter
  private String clickUrl;

  private final Map<String, String> udIdVal; // Uppercase
  private final String testCryptoSecretKey;
  private final String cryptoSecretKey;
  private final String rmBeaconURLPrefix;
  private final String imageBeaconURLPrefix;
  private final Integer segmentId;
  private final String clickURLPrefix;
  private final String imSdk;
  private final String impressionId;
  private final boolean isRmAd;
  private final Boolean isBillableDemog;
  private final boolean isCPC;
  private final Long siteIncId;
  private final Long handsetInternalId;
  private final Long ipFileVersion;
  private final int carrierId;
  private final int countryId;
  private final String gender;
  private final int age;
  private final int location;
  private final boolean testMode;
  private final boolean isBeaconEnabledOnSite;
  private final boolean imageBeaconFlag;
  private final String tierInfo;
  private final boolean isTestRequest;
  private final String latlonval;
  private final boolean isRtbSite;
  private final String creativeId;
  private final String budgetBucketId;
  private final String dst;

  public ClickUrlMakerV6(final Builder builder) {
    udIdVal = builder.udIdVal;
    testCryptoSecretKey = builder.testCryptoSecretKey;
    cryptoSecretKey = builder.cryptoSecretKey;
    rmBeaconURLPrefix = builder.rmBeaconURLPrefix;
    imageBeaconURLPrefix = builder.imageBeaconURLPrefix;
    segmentId = builder.segmentId;
    clickURLPrefix = builder.clickURLPrefix;
    imSdk = builder.imSdk;
    impressionId = builder.impressionId;
    isRmAd = builder.isRmAd;
    isCPC = builder.isCPC;
    siteIncId = builder.siteIncId;
    handsetInternalId = builder.handsetInternalId;
    ipFileVersion = builder.ipFileVersion;
    carrierId = builder.carrierId;
    countryId = builder.countryId;
    location = builder.location;
    testMode = builder.testMode;
    isBeaconEnabledOnSite = builder.isBeaconEnabledOnSite;
    imageBeaconFlag = builder.imageBeaconFlag;
    budgetBucketId = builder.budgetBucketId;
    if (StringUtils.isEmpty(builder.gender)) {
      gender = "u";
    } else {
      gender = builder.gender;
    }
    if (builder.age < 0) {
      age = 0;
    } else {
      age = builder.age;
    }
    if (null == builder.isBillableDemog) {
      isBillableDemog = true;
    } else {
      isBillableDemog = builder.isBillableDemog;
    }
    if (null == builder.tierInfo) {
      tierInfo = "-1";
    } else {
      tierInfo = builder.tierInfo;
    }
    if (StringUtils.isEmpty(builder.latlonval)) {
      latlonval = "x";
    } else {
      latlonval = builder.latlonval;
    }
    if (null == builder.creativeId) {
      creativeId = "0";
    } else {
      creativeId = builder.creativeId;
    }
    if (null == builder.dst) {
      dst = "1";
    } else {
      dst = builder.dst;
    }
    isTestRequest = builder.isTestRequest;
    isRtbSite = builder.isRtbSite;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  @Setter
  public static class Builder {
    private Map<String, String> udIdVal;
    private String testCryptoSecretKey;
    private String cryptoSecretKey;
    private String rmBeaconURLPrefix;
    private String imageBeaconURLPrefix;
    private Integer segmentId;
    private String clickURLPrefix;
    private String imSdk;
    private String impressionId;
    private boolean isRmAd;
    private Boolean isBillableDemog;
    private boolean isCPC;
    private Long siteIncId;
    private Long handsetInternalId;
    private Long ipFileVersion;
    private int carrierId;
    private int countryId;
    private String gender;
    private int age;
    private int location;
    private boolean testMode;
    private boolean isBeaconEnabledOnSite;
    private boolean imageBeaconFlag;
    private String tierInfo;
    private boolean isTestRequest;
    private String latlonval;
    private boolean isRtbSite;
    private String creativeId;
    private String budgetBucketId;
    private String dst;
  }

  public void createClickUrls() {
    final StringBuilder adUrlSuffix = new StringBuilder(100);
    // 1st URL component: url format version info
    adUrlSuffix.append(URLVERSIONINITSTR);
    // 2nd URL component: CPC/CPM information
    if (isCPC) {
      adUrlSuffix.append(URLMARKERCPC);
    } else {
      adUrlSuffix.append(URLMARKERCPM);
    }
    // 3rd URL component: site inc id
    if (null == siteIncId) {
      LOG.debug("Site inc id is null so returning");
      return;
    }
    adUrlSuffix.append(appendSeparator(getIdBase36(siteIncId)));
    // 4th URL Component: handset device id
    if (null == handsetInternalId) {
      LOG.debug("handsetInternaleId is null so returning");
      return;
    }
    adUrlSuffix.append(appendSeparator(getIdBase36(handsetInternalId)));
    // 5th URL Component: ip file version
    if (null == ipFileVersion) {
      LOG.debug("ipFileVersion is null so returning");
      return;
    }
    adUrlSuffix.append(appendSeparator(getIdBase36(ipFileVersion)));
    // 6th URL Component: country id
    adUrlSuffix.append(appendSeparator(getIdBase36(countryId)));
    // 7th URL Component: ccid
    adUrlSuffix.append(appendSeparator(getCarrierIdBase36(carrierId, countryId)));
    // 8th URL Component: gender
    adUrlSuffix.append(appendSeparator(gender));
    // 9th URL Component: age
    adUrlSuffix.append(appendSeparator(getIdBase36(age)));
    // 10th URL Component: location
    adUrlSuffix.append(appendSeparator(getIdBase36(location)));
    // 11th URL Component: Billable Click
    String billable;
    if (isBillableDemog) {
      billable = "1";
    } else {
      billable = "0";
    }
    adUrlSuffix.append(appendSeparator(billable));
    // 12th URL Component: udid or odin1. Based on PI311
    if (null == udIdVal || udIdVal.isEmpty()) {
      LOG.debug("udIdVal is null or empty so using default value");
      adUrlSuffix.append(appendSeparator(DEFAULT_UDID_VALUE));
    } else {
      adUrlSuffix.append(appendSeparator(getEncodedJson(udIdVal)));
    }
    // 13th impression id
    if (null == impressionId) {
      LOG.debug("impressionId is null so returning");
      return;
    }
    adUrlSuffix.append(appendSeparator(impressionId));
    // 14th imsdk, 0 for web and -1 for other ad formats)
    if (!StringUtils.isEmpty(imSdk)) {
      adUrlSuffix.append(appendSeparator(imSdk));
    } else {
      adUrlSuffix.append(appendSeparator(DEFAULTIMSDK));
    }
    // 15th segmentId
    if (null != segmentId) {
      adUrlSuffix.append(appendSeparator(getIdBase36(segmentId)));
    } else {
      adUrlSuffix.append(appendSeparator(Integer.toString(0)));
    }

    // 16the field for tier info
    adUrlSuffix.append(appendSeparator(tierInfo));

    StringBuilder beaconUrlSuffix = new StringBuilder(100);
    // 17th field for event type <click or beacon>
    beaconUrlSuffix = beaconUrlSuffix.append(adUrlSuffix.toString());
    adUrlSuffix.append(appendSeparator(CLICK));
    beaconUrlSuffix.append(appendSeparator(BEACON));

    // 18th field for test mode
    if (isTestRequest) {
      adUrlSuffix.append(appendSeparator(IS_TEST));
      beaconUrlSuffix.append(appendSeparator(IS_TEST));
    } else {
      adUrlSuffix.append(appendSeparator(IS_NOT_TEST));
      beaconUrlSuffix.append(appendSeparator(IS_NOT_TEST));
    }

    // 19th URL Component: request's latlong
    adUrlSuffix.append(appendSeparator(latlonval));
    beaconUrlSuffix.append(appendSeparator(latlonval));

    // 20th URL Component: creative id
    adUrlSuffix.append(appendSeparator(creativeId));
    beaconUrlSuffix.append(appendSeparator(creativeId));

    // 21st URL Component: supply source
    if (isRtbSite) {
      adUrlSuffix.append(appendSeparator(RTB_SUPPLY));
      beaconUrlSuffix.append(appendSeparator(RTB_SUPPLY));
    } else {
      adUrlSuffix.append(appendSeparator(NON_RTB_SUPPLY));
      beaconUrlSuffix.append(appendSeparator(NON_RTB_SUPPLY));
    }

    // 22th budget bucket id
    adUrlSuffix.append(appendSeparator(budgetBucketId));
    beaconUrlSuffix.append(appendSeparator(budgetBucketId));

    // 23th dst
    adUrlSuffix.append(appendSeparator(dst));
    beaconUrlSuffix.append(appendSeparator(dst));

    // 24th and 25th URL Component: hash key version and url hash
    CryptoHashGenerator cryptoHashGenerator;
    if (testMode) {
      adUrlSuffix.append(appendSeparator(ClickUrlMakerV6.CLICK_URL_HASHING_SECRET_KEY_TEST_MODE_VERSION_BASE_36));
      beaconUrlSuffix.append(appendSeparator(ClickUrlMakerV6.CLICK_URL_HASHING_SECRET_KEY_TEST_MODE_VERSION_BASE_36));
      cryptoHashGenerator = new CryptoHashGenerator(testCryptoSecretKey);
    } else {
      adUrlSuffix.append(appendSeparator(ClickUrlMakerV6.CLICK_URL_HASHING_SECRET_KEY_VERSION_BASE_36));
      beaconUrlSuffix.append(appendSeparator(ClickUrlMakerV6.CLICK_URL_HASHING_SECRET_KEY_VERSION_BASE_36));
      cryptoHashGenerator = new CryptoHashGenerator(cryptoSecretKey);
    }
    adUrlSuffix.append(appendSeparator(cryptoHashGenerator.generateHash(adUrlSuffix.toString())));
    beaconUrlSuffix.append(appendSeparator(cryptoHashGenerator.generateHash(beaconUrlSuffix.toString())));
    if (null != clickURLPrefix) {
      clickUrl = clickURLPrefix + adUrlSuffix.toString();
    }

    if (isRmAd) {
      if (null != rmBeaconURLPrefix) {
        beaconUrl = rmBeaconURLPrefix + beaconUrlSuffix.toString();
      }
      return;
    }

    if (imageBeaconFlag || isBeaconEnabledOnSite) {
      if (null != imageBeaconURLPrefix) {
        beaconUrl = imageBeaconURLPrefix + beaconUrlSuffix.toString();
      }
    }
  }

  public static String getIdBase36(final Long id) {
    return Long.toString(id, 36);
  }

  public static String getIdBase36(final int id) {
    return Integer.toString(id, 36);
  }

  public String getCarrierIdBase36(final int carrierId, final int countryId) {
    return Long.toString(carrierId != 0 ? carrierId : -countryId, 36);
  }

  public String getEncodedJson(final Map<String, String> clickUidMap) {
    final Gson gson = new Gson();
    String temp = gson.toJson(clickUidMap);
    final byte[] unEncoded = temp.getBytes();
    final String encoded = new String(Base64.encodeBase64(unEncoded));
    temp = encoded.replaceAll("\\+", "-").replaceAll("\\/", "_").replaceAll("=", "~");
    return temp;
  }

  private String appendSeparator(final String parameter) {
    return URLPATHSEP + parameter;
  }
}
