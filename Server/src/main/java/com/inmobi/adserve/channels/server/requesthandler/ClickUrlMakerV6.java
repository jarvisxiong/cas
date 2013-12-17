package com.inmobi.adserve.channels.server.requesthandler;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;


public class ClickUrlMakerV6 {
    private static final Logger LOG                                           = LoggerFactory
                                                                                      .getLogger(ClickUrlMakerV6.class);

    private static final String DEFAULT_UDID_VALUE                            = "x";
    private static final String URLPATHSEP                                    = "/";
    private static final String URLVERSIONINITSTR                             = "/6";
    private static final String URLMARKERCPC                                  = "/t";
    private static final String URLMARKERCPM                                  = "/b";
    private static final String DEFAULTIMSDK                                  = "-1";
    private final Set<String>   unhashable;
    private static Long         clickURLHashingSecretKeyVersion               = (long) 1;
    private static String       clickURLHashingSecretKeyVersionBase36         = Long
                                                                                      .toString(
                                                                                          ClickUrlMakerV6.clickURLHashingSecretKeyVersion,
                                                                                          36);
    private static Long         clickURLHashingSecretKeyTestModeVersion       = (long) 2;
    private static String       clickURLHashingSecretKeyTestModeVersionBase36 = Long
                                                                                      .toString(
                                                                                          ClickUrlMakerV6.clickURLHashingSecretKeyTestModeVersion,
                                                                                          36);

    public ClickUrlMakerV6(final Set<String> unhashable) {
        this.unhashable = unhashable;
    }

    private String testCryptoSecretKey = "qqq";

    public void setTestCryptoSecretKey(final String testCryptoSecretKey) {
        this.testCryptoSecretKey = testCryptoSecretKey;
    }

    private String cryptoSecretKey = "SystemManagerScottTiger";

    public void setCryptoSecretKey(final String cryptoSecretKey) {
        this.cryptoSecretKey = cryptoSecretKey;
    }

    private String rmBeaconURLPrefix;

    public void setRmBeaconURLPrefix(final String rmBeaconURLPrefix) {
        this.rmBeaconURLPrefix = rmBeaconURLPrefix;
    }

    private String imageBeaconURLPrefix;

    public void setImageBeaconURLPrefix(final String imageBeaconURLPrefix) {
        this.imageBeaconURLPrefix = imageBeaconURLPrefix;
    }

    private Integer segmentId;

    public void setSegmentId(final Integer segmentId) {
        this.segmentId = segmentId;
    }

    private String beaconUrl;

    public String getBeaconUrl(final Map<String, String> getParams) {
        if (null == beaconUrl) {
            return beaconUrl;
        }
        if (getParams.isEmpty()) {
            return beaconUrl;
        }
        else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("?");
            int i = 1;
            for (Map.Entry<String, String> entry : getParams.entrySet()) {
                if (i < getParams.size()) {
                    stringBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                }
                else {
                    stringBuilder.append(entry.getKey()).append("=").append(entry.getValue());
                }
                i++;
            }
            return beaconUrl + stringBuilder.toString();
        }
    }

    private String clickUrl;

    public String getClickUrl(final Map<String, String> getParams) {
        if (null == clickUrl) {
            return clickUrl;
        }
        if (getParams.isEmpty()) {
            LOG.debug("clickUrl is {}", clickUrl);
            return clickUrl;
        }
        else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("?");
            int i = 1;
            for (Map.Entry<String, String> entry : getParams.entrySet()) {
                if (i < getParams.size()) {
                    stringBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                }
                else {
                    stringBuilder.append(entry.getKey()).append("=").append(entry.getValue());
                }
                i++;
            }
            return clickUrl + stringBuilder.toString();
        }
    }

    private String clickURLPrefix;

    public void setClickURLPrefix(final String clickURLPrefix) {
        this.clickURLPrefix = clickURLPrefix;
    }

    private String imSdk;

    public void setImSdk(final String imSdk) {
        this.imSdk = imSdk;
    }

    private String impressionId;

    public void setImpressionId(final String impressionId) {
        this.impressionId = impressionId;
    }

    private boolean isRmAd; // isBeaconingEventValue

    public void setRmAd(final boolean isRmAd) {
        this.isRmAd = isRmAd;
    }

    private Boolean isBillableDemog;

    public void setIsBillableDemog(final Boolean isBillableDemog) {
        this.isBillableDemog = isBillableDemog;
    }

    private boolean isCPC;

    public void setCPC(final boolean isCPC) {
        this.isCPC = isCPC;
    }

    private Long siteIncId;

    public void setSiteIncId(final Long siteIncId) {
        this.siteIncId = siteIncId;
    }

    private Long handsetInternalId;

    public void setHandsetInternalId(final Long handsetInternalId) {
        this.handsetInternalId = handsetInternalId;
    }

    private Long ipFileVersion;

    public void setIpFileVersion(final Long ipFileVersion) {
        this.ipFileVersion = ipFileVersion;
    }

    private Map<String, String> uidMapUpperCase;

    public void setUdIdVal(final Map<String, String> uidMapUpperCase) {
        this.uidMapUpperCase = uidMapUpperCase;
    }

    private int carrierId;

    public void setCarrierId(final int carrierId) {
        this.carrierId = carrierId;
    }

    private int countryId;

    public void setCountryId(final int countryId) {
        this.countryId = countryId;
    }

    private String gender;

    public void setGender(final String gender) {
        this.gender = gender;
    }

    private int age;

    public void setAge(final int age) {
        this.age = age;
    }

    private int location;

    public void setLocation(final int location) {
        this.location = location;
    }

    private boolean testMode = false;

    public void setTestMode(final boolean testMode) {
        this.testMode = testMode;
    }

    private boolean isBeaconEnabledOnSite;

    public void setBeaconEnabledOnSite(final boolean isBeaconEnabledOnSite) {
        this.isBeaconEnabledOnSite = isBeaconEnabledOnSite;
    }

    private boolean imageBeaconFlag = false;

    public void setImageBeaconFlag(final boolean imageBeaconFlag) {
        this.imageBeaconFlag = imageBeaconFlag;
    }

    public void createClickUrls() {
        StringBuilder adUrlSuffix = new StringBuilder(100);
        // 1st URL component: url format version info
        adUrlSuffix.append(URLVERSIONINITSTR);
        // 2nd URL component: CPC/CPM information
        if (isCPC) {
            adUrlSuffix.append(URLMARKERCPC);
        }
        else {
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
        adUrlSuffix.append(appendSeparator(getCountryIdBase36(countryId)));
        // 7th URL Component: ccid
        adUrlSuffix.append(appendSeparator(getCarrierIdBase36(carrierId, countryId)));
        // 8th URL Component: gender
        if (StringUtils.isEmpty(gender)) {
            gender = "u";
        }
        adUrlSuffix.append(appendSeparator(gender));
        // 9th URL Component: age
        if (age < 0) {
            age = 0;
        }
        adUrlSuffix.append(appendSeparator(Long.toString(age, 36)));
        // 10th URL Component: location
        adUrlSuffix.append(appendSeparator(Long.toString(location, 36)));
        // 11th URL Component: Billable Click
        String billable;
        if (null == isBillableDemog) {
            isBillableDemog = true;
        }
        if (isBillableDemog) {
            billable = "1";
        }
        else {
            billable = "0";
        }
        adUrlSuffix.append(appendSeparator(billable));
        // 12th URL Component: udid or odin1. Based on PI311
        if (null == uidMapUpperCase) {
            LOG.debug("uidMapUpperCase is null so returning");
            return;
        }
        if (uidMapUpperCase.isEmpty()) {
            adUrlSuffix.append(appendSeparator(DEFAULT_UDID_VALUE));
        }
        else {
            adUrlSuffix.append(appendSeparator(getEncodedJson(uidMapUpperCase)));
        }
        // impression id
        if (null == impressionId) {
            LOG.debug("impressionId is null so returning");
            return;
        }
        adUrlSuffix.append(appendSeparator(impressionId));
        // 14th URL Component : imsdk (specially for two piece rm will have sdk
        // version or 0 for web and -1 for other ad formats)
        if (!StringUtils.isEmpty(imSdk)) {
            adUrlSuffix.append(appendSeparator(imSdk));
        }
        else {
            adUrlSuffix.append(appendSeparator(DEFAULTIMSDK));
        }
        // 15th segmentId
        if (null != segmentId) {
            adUrlSuffix.append(appendSeparator(Integer.toString(segmentId, 36)));
        }
        else {
            adUrlSuffix.append(appendSeparator(Integer.toString(0)));
        }
        // 16th and 17th URL Component: hash key version and url hash
        if (testMode) {
            adUrlSuffix.append(appendSeparator(ClickUrlMakerV6.clickURLHashingSecretKeyTestModeVersionBase36));
            CryptoHashGenerator cryptoHashGenerator = new CryptoHashGenerator(testCryptoSecretKey);
            String clickUrlHash = cryptoHashGenerator.generateHash(adUrlSuffix.toString());
            adUrlSuffix.append(appendSeparator(clickUrlHash));

        }
        else {
            adUrlSuffix.append(appendSeparator(ClickUrlMakerV6.clickURLHashingSecretKeyVersionBase36));
            CryptoHashGenerator cryptoHashGenerator = new CryptoHashGenerator(cryptoSecretKey);
            String clickUrlHash = cryptoHashGenerator.generateHash(adUrlSuffix.toString());
            adUrlSuffix.append(appendSeparator(clickUrlHash));
        }
        if (null != clickURLPrefix) {
            clickUrl = this.clickURLPrefix + adUrlSuffix.toString();
        }
        else {
            LOG.debug("clickURLPrefix is not set so sending clickUrl null");
        }

        if (isRmAd) {
            // Incase of rm ads, we need to set both beacon url to indicate rendering
            // and click url to capture the click event.
            LOG.debug("Valid beaconing event. Hence need to have both click and beacon URL");
            if (null != rmBeaconURLPrefix) {
                beaconUrl = this.rmBeaconURLPrefix + adUrlSuffix.toString();
            }
            else {
                LOG.debug("rmBeaconURLPrefix is not set so beacon url is null");
            }
            return;
        }

        if (this.imageBeaconFlag || isBeaconEnabledOnSite) {
            LOG.debug("Beacon is enabled for this request. Sending beacon url");
            if (null != imageBeaconURLPrefix) {
                beaconUrl = this.imageBeaconURLPrefix + adUrlSuffix.toString();
            }
            else {
                LOG.debug("ImageBeaconUrlPrefix is not set so beacon url is null");
            }
        }
        return;
    }

    public String getIdBase36(final Long id) {
        return Long.toString(id, 36);
    }

    public String getCountryIdBase36(final int countryId) {
        return Long.toString(countryId, 36);
    }

    public String getCarrierIdBase36(final int carrierId, final int countryId) {
        return Long.toString(carrierId != 0 ? carrierId : -countryId, 36);
    }

    public String getHashedValue(final String value, final String key) {
        String hashedUidValue = value;
        if (!(unhashable.contains(key) || 32 == value.length())) {
            byte[] uidStoredBytes = value.getBytes();
            try {
                MessageDigest hashingAlgorithmMd5 = MessageDigest.getInstance("MD5");
                hashingAlgorithmMd5.reset();
                hashingAlgorithmMd5.update(uidStoredBytes);
                byte uidMessageDigest[] = hashingAlgorithmMd5.digest();

                StringBuilder hexString = new StringBuilder();
                for (int i = 0; i < uidMessageDigest.length; i++) {
                    String hex = Integer.toHexString(0xFF & uidMessageDigest[i]);
                    if (hex.length() == 1) {
                        hexString.append('0');
                    }
                    hexString.append(hex);
                }
                hashedUidValue = hexString.toString();
            }
            catch (NoSuchAlgorithmException nsae) {
                return DEFAULT_UDID_VALUE;
            }
        }
        return hashedUidValue;
    }

    public String getEncodedJson(final Map<String, String> clickUidMap) {
        Gson gson = new Gson();
        String temp = gson.toJson(clickUidMap);
        byte[] unEncoded = temp.getBytes();
        String encoded = new String(Base64.encodeBase64(unEncoded));
        temp = encoded.replaceAll("\\+", "-").replaceAll("\\/", "_").replaceAll("=", "~");
        return temp;
    }

    private String appendSeparator(final String parameter) {
        return (URLPATHSEP + parameter);
    }
}
