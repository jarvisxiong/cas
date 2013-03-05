package com.inmobi.adserve.channels.server;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.configuration.Configuration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.api.SASRequestParameters;

public class ClickUrlMaker {

  public static class TrackingUrls {
    private String clickUrl;
    private String beaconUrl;

    public TrackingUrls(String clickUrl, String beaconUrl) {
      this.clickUrl = clickUrl;
      this.beaconUrl = beaconUrl;
    }

    public String getClickUrl() {
      return clickUrl;
    }

    public TrackingUrls setClickUrl(String clickUrl) {
      this.clickUrl = clickUrl;
      return this;
    }

    public String getBeaconUrl() {
      return beaconUrl;
    }

    public TrackingUrls setBeaconUrl(String beaconUrl) {
      this.beaconUrl = beaconUrl;
      return this;
    }

  }

  private static String URLPATHSEP = "/";
  private static String URLVERSIONINITSTR = "/4";
  private static String URLMARKERCPM = "/b";
  private static String URLMARKERCPC = "/t";
  private static String PARAMETER = "?ds=1";
  private static String BEACON_PARAMETER = "?ds=1&event=beacon";
  private Configuration config;
  private JSONObject jObject;
  private SASRequestParameters params;
  private DebugLogger logger;
  private String twoPieceRm = "-1";
  private Mac mac = null;
  private SecretKeySpec sk = null;

  public ClickUrlMaker(Configuration config, JSONObject jObject, SASRequestParameters params, DebugLogger logger) {
    this.config = config;
    this.jObject = jObject;
    this.params = params;
    this.logger = logger;
  }

  public TrackingUrls getClickUrl(String pricingModel) throws Exception {
    logger.debug("inside getClickurl");
    StringBuilder adUrlSuffix = new StringBuilder();
    adUrlSuffix.append(URLVERSIONINITSTR);
    if(pricingModel.equals("CPM"))
      adUrlSuffix.append(URLMARKERCPM);
    else
      adUrlSuffix.append(URLMARKERCPC);

    adUrlSuffix.append(appendSeparator(getSiteIdBase36()));
    adUrlSuffix.append(appendSeparator(getHandsetIdBase36()));
    adUrlSuffix.append(appendSeparator(Long.toString(config.getInt("clickmaker.ipFileVersion"), 36)));
    adUrlSuffix.append(appendSeparator(getCarrierCountryIdBase36()));
    adUrlSuffix.append(appendSeparator(getCarrierIdBase36()));

    adUrlSuffix.append(appendSeparator(getGender()));
    adUrlSuffix.append(appendSeparator(getAge()));
    adUrlSuffix.append(appendSeparator("0"));
    adUrlSuffix.append(appendSeparator("0"));
    adUrlSuffix.append(appendSeparator(getUid()));

    adUrlSuffix.append(appendSeparator(params.getImpressionId()));
    adUrlSuffix.append(appendSeparator(twoPieceRm));
    adUrlSuffix.append(appendSeparator(config.getString("clickmaker.clickURLHashingSecretKeyVersion")));

    String clickUrlHash = cryptoHashGenerator(adUrlSuffix.toString(), config.getString("clickmaker.key.1.type"), config.getString("clickmaker.key.1.value"));
    if(null == clickUrlHash)
      return null;
    adUrlSuffix.append(appendSeparator(clickUrlHash));
    String clickUrl = config.getString("clickmaker.clickURLPrefix") + adUrlSuffix + PARAMETER;
    String beaconUrl = config.getString("clickmaker.beaconURLPrefix") + adUrlSuffix + BEACON_PARAMETER;
    return new TrackingUrls(clickUrl, beaconUrl);
  }

  private String getSiteIdBase36() {
    try {
      JSONArray site = jObject.getJSONArray("site");
      logger.debug("Getting site id inside click url maker");
      if(site.length() > 0)
        return (Long.toString(site.getLong(0), 36));
      else
        return "0";
    } catch (JSONException exception) {
      logger.error("site id should not be null " + exception.getMessage());
      return "0";
    }
  }

  private String getHandsetIdBase36() {
    try {
      JSONArray handset = jObject.getJSONArray("handset");
      logger.debug("Getting handset id inside click url maker");
      if(handset.length() > 0)
        return (Long.toString(handset.getLong(0), 36));
      else
        return "0";
    } catch (JSONException exception) {
      logger.debug("handset missing in the request param");
      return "0";
    }

  }

  private String getCarrierCountryIdBase36() {
    try {
      JSONArray carrier = jObject.getJSONArray("carrier");
      logger.debug("Getting country id inside click url maker");
      if(carrier.length() > 1)
        return (Long.toString(carrier.getLong(1), 36));
      else
        return "0";
    } catch (JSONException exception) {
      logger.debug("country info missing in the request params");
      return "0";
    }
  }

  private String getCarrierIdBase36() {
    try {
      JSONArray carrier = jObject.getJSONArray("carrier");
      logger.debug("Getting carrier id inside click url maker");
      if(carrier.length() > 0)
        return (Long.toString(carrier.getLong(0), 36));
      else
        return "0";
    } catch (JSONException exception) {
      logger.debug("carrier info missing in the request params");
      return "0";
    }
  }

  private String getGender() {
    logger.debug("getting gender");
    if(params.getGender() == null)
      return "u";
    else
      return (params.getGender());
  }

  private String getAge() {
    logger.debug("getting age");
    if(params.getAge() == null || !params.getAge().matches("\\d+"))
      return "0";
    int age = Integer.parseInt(params.getAge());
    return (Long.toString(age, 36));
  }

  private String getUid() {
    logger.debug("getting uid");
    if(params.getUid() == null)
      return "x";
    return params.getUid();
  }

  public String cryptoHashGenerator(String url, String keyType, String secretKey) {
    if(mac == null) {
      try {
        sk = new SecretKeySpec(secretKey.getBytes(), "HmacMD5");
        mac = Mac.getInstance("HmacMD5");
        mac.init(sk);
      } catch (NoSuchAlgorithmException e) {
        logger.error("Got no such algorithm exception");
        return null;
      } catch (InvalidKeyException e) {
        logger.error("got invalid key exception");
        return null;
      }
    }
    CRC32 crc = new CRC32();
    crc.update(mac.doFinal(url.getBytes()));
    logger.debug("got the hash inside crypto hash generator");
    return Long.toHexString(crc.getValue());
  }

  private String appendSeparator(String parameter) {
    return (URLPATHSEP + parameter);
  }
}
