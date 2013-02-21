package com.inmobi.adserve.channels.server;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.util.DebugLogger;

public class RequestParser {

  public static JSONObject extractParams(Map<String, List<String>> params, DebugLogger logger) throws Exception {
    return extractParams(params, "args", logger);
  }

  // Extracting params.
  public static JSONObject extractParams(Map<String, List<String>> params, String jsonKey, DebugLogger logger)
      throws Exception, JSONException {
    JSONObject jObject = null;
    if(!params.isEmpty()) {
      for (Entry<String, List<String>> p : params.entrySet()) {
        String key = p.getKey();
        List<String> vals = p.getValue();
        for (String val : vals) {
          if(key.equalsIgnoreCase(jsonKey)) {
            jObject = new JSONObject(val);
          }
        }
      }
    }
    return jObject;
  }

  public static SASRequestParameters parseRequestParameters(JSONObject jObject, DebugLogger logger) {
    SASRequestParameters params = new SASRequestParameters();
    logger.debug("inside parameter parser");
    if(null == jObject) {
      logger.debug("Returning null as jObject is null.");
      return null;
    }
    params.allParametersJson = jObject.toString();
    params.remoteHostIp = stringify(jObject, "w-s-carrier", logger);
    params.userAgent = stringify(jObject, "rq-x-inmobi-phone-useragent", logger);
    if(null == params.userAgent) {
      params.userAgent = stringify(jObject, "rq-h-user-agent", logger);
    }
    params.locSrc = stringify(jObject, "loc-src", logger);
    params.latLong = stringify(jObject, "latlong", logger);
    params.siteId = stringify(jObject, "rq-mk-siteid", logger);
    params.source = stringify(jObject, "source", logger);
    params.country = parseArray(jObject, "carrier", 2);
    params.countryStr = parseArray(jObject, "carrier", 1);
    params.area = parseArray(jObject, "carrier", 4);
    params.slot = stringify(jObject, "slot-served", logger);
    params.rqMkSlot = stringify(jObject, "rq-mk-ad-slot", logger);
    params.sdkVersion = stringify(jObject, "sdk-version", logger);
    params.siteType = stringify(jObject, "site-type", logger);
    params.adcode = stringify(jObject, "adcode", logger);
    params.platformOsId = jObject.optInt("os-id", -1);
    if(params.siteType != null) {
      params.siteType = params.siteType.toUpperCase();
    }
    params.categories = getCategory(jObject, logger, "category");
    params.newCategories = getCategory(jObject, logger, "new-category");
    params.rqIframe = stringify(jObject, "rq-iframe", logger);
    params.rFormat = stringify(jObject, "r-format", logger);
    params.rqMkAdcount = stringify(jObject, "rq-mk-adcount", logger);
    params.tid = stringify(jObject, "tid", logger);
    params.tp = stringify(jObject, "tp", logger);
    params.allowBannerAds = jObject.opt("site-allowBanner") == null ? true
        : (Boolean) (jObject.opt("site-allowBanner"));
    params.siteFloor = jObject.opt("site-floor") == null ? 0.0 : Double.parseDouble(jObject.opt("site-floor")
        .toString());
    try {
      params.siteSegmentId = jObject.getInt("sel-seg-id");
      logger.debug("Site segment id is", params.siteSegmentId.toString());
    } catch (JSONException e) {
     logger.debug("Site segment id is not present in the request");
    }
    params.ipFileVersion = jObject.optInt("rq-ip-file-ver", 1);
    if(logger.isDebugEnabled()) {
      logger.debug("country obtained is " + params.country);
      logger.debug("site floor is " + params.siteFloor);
      logger.debug("osId is " + params.platformOsId);
    }
    params.uidParams = stringify(jObject, "u-id-params", logger);
    params = getUserParams(params, jObject, logger);
    params = getUserIdParams(params, jObject, logger);
    try {
      JSONArray siteInfo = jObject.getJSONArray("site");
      if(siteInfo != null && siteInfo.length() > 0) {
        params.siteIncId = siteInfo.getLong(0);
      }
    } catch (JSONException exception) {
      logger.debug("site object not found in request");
      params.siteIncId = 0;
    }
    try {
      params.handset = jObject.getJSONArray("handset");
    } catch (JSONException e) {
      logger.debug("Handset array not found");
    }
    try {
      params.carrier = jObject.getJSONArray("carrier");
    } catch (JSONException e) {
      logger.debug("carrier array not found");
    }
    if(null == params.uid || params.uid.isEmpty()) {
      params.uid = stringify(jObject, "u-id", logger);
    }
    params.osId = jObject.optInt("os-id", -1);
    params.isRichMedia = jObject.optBoolean("rich-media",false);
    logger.debug("successfully parsed params");
    return params;
  }

  public static String stringify(JSONObject jObject, String field, DebugLogger logger) throws NullPointerException {
    String fieldValue = "";
    try {
      try {
        fieldValue = (String) jObject.get(field);
      } catch (ClassCastException e) {
        fieldValue = jObject.get(field).toString();
      }
    } catch (JSONException e) {
      return null;
    }
    if(logger.isDebugEnabled())
      logger.debug("Retrived from json " + field + " = " + fieldValue);
    return fieldValue;
  }

  public static String parseArray(JSONObject jObject, String param, int index) {
    try {
      JSONArray jArray = jObject.getJSONArray(param);
      return (jArray.getString(index));
    } catch (JSONException e) {
      return null;
    } catch (NullPointerException e) {
      return null;
    }
  }

  public static List<Long> getCategory(JSONObject jObject, DebugLogger logger, String oldORnew) {
    try {
      JSONArray categories = jObject.getJSONArray(oldORnew);
      Long[] category = new Long[categories.length()];
      for (int index = 0; index < categories.length(); index++) {
        category[index] = categories.getLong(index);
      }
      return Arrays.asList(category);
    } catch (JSONException e) {
      logger.error("error while reading category array");
      return null;
    }
  }

  // Get user specific params
  public static SASRequestParameters getUserParams(SASRequestParameters parameter, JSONObject jObject,
      DebugLogger logger) {
    logger.debug("inside parsing user params");
    try {
      JSONObject userMap = (JSONObject) jObject.get("uparams");
      parameter.age = stringify(userMap, "u-age", logger);
      parameter.gender = stringify(userMap, "u-gender", logger);
      parameter.uid = stringify(userMap, "u-id", logger);
      parameter.postalCode = stringify(userMap, "u-postalcode", logger);
      if(!StringUtils.isEmpty(parameter.postalCode))
        parameter.postalCode = parameter.postalCode.replaceAll(" ", "");
      parameter.userLocation = stringify(userMap, "u-location", logger);
      parameter.genderOrig = stringify(userMap, "u-gender-orig", logger);
      if(logger.isDebugEnabled()) {
        logger.debug("uid is " + parameter.uid + ",postalCode is " + parameter.postalCode + ",gender is "
            + parameter.gender);
        logger.debug("age is " + parameter.age + ",location is " + parameter.userLocation + ",genderorig is "
            + parameter.genderOrig);
      }
    } catch (JSONException exception) {
      parameter.age = null;
      parameter.gender = null;
      parameter.uid = null;
      parameter.postalCode = null;
      parameter.userLocation = null;
      parameter.genderOrig = null;
      logger.error("uparams missing in the request");
    } catch (NullPointerException exception) {
      parameter.age = null;
      parameter.gender = null;
      parameter.uid = null;
      parameter.postalCode = null;
      parameter.userLocation = null;
      parameter.genderOrig = null;
      logger.error("uparams missing in the request");
    }
    return parameter;
  }

  // Get user id params
  public static SASRequestParameters getUserIdParams(SASRequestParameters parameter, JSONObject jObject,
      DebugLogger logger) {
    if(logger.isDebugEnabled())
      logger.debug("inside parsing userid params");
    try {
      JSONObject userIdMap = (JSONObject) jObject.get("u-id-params");
      String o1Uid = stringify(userIdMap, "SO1", logger);
      parameter.uidO1 = (o1Uid != null) ? o1Uid : stringify(userIdMap, "O1", logger);
      parameter.uidMd5 = stringify(userIdMap, "UM5", logger);
      parameter.uidIFA = ("iphone".equalsIgnoreCase(parameter.source)) ? stringify(userIdMap, "IDA", logger) : null;

    } catch (JSONException exception) {
      setNullValueForUid(parameter, logger);
    } catch (NullPointerException exception) {
      setNullValueForUid(parameter, logger);
    }
    return parameter;
  }

  private static void setNullValueForUid(SASRequestParameters parameter, DebugLogger logger) {
    parameter.uidO1 = null;
    parameter.uidMd5 = null;
    parameter.uidIFA = null;
    logger.error("uidparams missing in the request");
  }
}
