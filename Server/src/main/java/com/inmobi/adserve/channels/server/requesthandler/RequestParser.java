package com.inmobi.adserve.channels.server.requesthandler;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;


@Singleton
public class RequestParser {
  private static final Logger LOG = LoggerFactory.getLogger(RequestParser.class);
  private final Provider<Marker> traceMarkerProvider;

  @Inject
  public RequestParser(final Provider<Marker> traceMarkerProvider) {
    this.traceMarkerProvider = traceMarkerProvider;
  }

  public JSONObject extractParams(final Map<String, List<String>> params) throws Exception {
    return extractParams(params, "args");
  }

  // Extracting params.
  public JSONObject extractParams(final Map<String, List<String>> params, final String jsonKey) throws JSONException,
      UnsupportedEncodingException {
    if (!params.isEmpty()) {
      final List<String> values = params.get(jsonKey);
      if (CollectionUtils.isNotEmpty(values)) {
        final String stringVal = values.iterator().next();
        return new JSONObject(stringVal);
      }
    }
    return null;
  }

  public void parseRequestParameters(final JSONObject jObject, SASRequestParameters params,
      final CasInternalRequestParameters casInternalRequestParameters) {
    final Marker traceMarker = traceMarkerProvider.get();

    LOG.debug(traceMarker, "Inside parameter parser");
    if (null == jObject) {
      LOG.debug(traceMarker, "Returning null as jObject is null.");
      params = null;
      return;
    }
    params.setAllParametersJson(jObject.toString());
    final int dst = jObject.optInt("dst", 2);
    final Set<Integer> accountSegments = getAcoountSegments(jObject);
    final boolean isResponseOnlyFromDcp = jObject.optBoolean("isResponseOnlyFromDcp", false);
    LOG.debug(traceMarker, "dst type is {} isResponseOnlyFromDcp  {} and account segments are {}", dst,
        isResponseOnlyFromDcp, accountSegments);
    params.setDst(dst);
    params.setResponseOnlyFromDcp(isResponseOnlyFromDcp);
    params.setAccountSegment(accountSegments);
    params.setRemoteHostIp(stringify(jObject, "w-s-carrier"));
    params.setUserAgent(stringify(jObject, "rqXInmobiPhoneUseragent"));
    if (null == params.getUserAgent()) {
      params.setUserAgent(stringify(jObject, "rqHUserAgent"));
    }
    params.setLocSrc(stringify(jObject, "loc-src"));
    params.setLatLong(stringify(jObject, "latlong"));
    params.setSiteId(stringify(jObject, "rqMkSiteid"));
    params.setSource(stringify(jObject, "source"));
    params.setCarrierId(Integer.parseInt(parseArray(jObject, "carrier", 0)));
    params.setCountryId(Long.parseLong(parseArray(jObject, "carrier", 1)));
    params.setCountryCode(parseArray(jObject, "carrier", 2));
    try {
      params.setCity(Integer.parseInt(parseArray(jObject, "carrier", 3)));
    } catch (final NumberFormatException e) {
      LOG.debug(traceMarker, "City not found in request");
    }
    try {
      params.setState(Integer.parseInt(parseArray(jObject, "carrier", 4)));
    } catch (final NumberFormatException e) {
      LOG.debug(traceMarker, "State not found in request");
    }
    final String slot = stringify(jObject, "slot-served");
    if (StringUtils.isNotEmpty(slot)) {
      params.setSlot(Short.parseShort(slot));
    }
    final String rqMkSlot = stringify(jObject, "rqMkAdSlot");
    if (StringUtils.isNotEmpty(rqMkSlot)) {
      params.setRqMkSlot(Arrays.asList(Short.parseShort(rqMkSlot)));
    }
    String sdkVersion = stringify(jObject, "sdk-version");
    if (StringUtils.isBlank(sdkVersion) || "null".equalsIgnoreCase(sdkVersion)) {
      sdkVersion = null;
    }
    params.setSdkVersion(sdkVersion);
    params.setSiteType(stringify(jObject, "site-type"));
    params.setAdcode(stringify(jObject, "adcode"));
    if (params.getSiteType() != null) {
      params.setSiteType(params.getSiteType().toUpperCase());
    }
    params.setCategories(getCategory(jObject, "new-category"));
    params.setRqIframe(stringify(jObject, "rqIframe"));
    params.setRFormat(stringify(jObject, "r-format"));
    String adCountStr = stringify(jObject, "rqMkAdcount");
    adCountStr = StringUtils.isEmpty(adCountStr) ? "1" : adCountStr;
    params.setRqMkAdcount(Short.parseShort(adCountStr));
    params.setTid(stringify(jObject, "tid"));

    params.setAllowBannerAds(jObject.optBoolean("site-allowBanner", true));
    params.setSiteFloor(jObject.optDouble("site-floor", 0.0));
    params.setSiteSegmentId(jObject.optInt("sel-seg-id", 0));
    params.setModelId(jObject.optInt("model-id", 0));
    LOG.debug(traceMarker, "Site segment id is {} and model id is {}", params.getSiteSegmentId(), params.getModelId());
    params.setIpFileVersion(jObject.optInt("rqIpFileVer", 7));
    LOG.debug(traceMarker, "country obtained is {}", params.getCountryCode());
    LOG.debug(traceMarker, "site floor is {}", params.getSiteFloor());
    LOG.debug(traceMarker, "osId is {}", params.getOsId());
    params.setUidParams(stringify(jObject, "raw-uid"));
    setUserIdParams(casInternalRequestParameters, jObject);
    params = getUserParams(params, jObject);
    try {
      final JSONArray siteInfo = jObject.getJSONArray("site");
      if (siteInfo != null && siteInfo.length() > 0) {
        params.setSiteIncId(siteInfo.getLong(0));
      }
    } catch (final JSONException exception) {
      LOG.debug(traceMarker, "site object not found in request, {}", exception);
      params.setSiteIncId(0);
    }
    try {
      final JSONArray jsonArray = jObject.getJSONArray("handset");
      params.setHandsetInternalId(Long.parseLong(jsonArray.get(0).toString()));
    } catch (final JSONException e) {
      LOG.debug(traceMarker, "Handset array not found, {}", e);
    }
    params.setOsId(jObject.optInt("os-id", -1));
    params.setRichMedia(jObject.optBoolean("rich-media", false));
    params.setRqAdType(stringify(jObject, "rqAdtype"));
    params.setAppUrl(stringify(jObject, "site-url"));
    LOG.debug(traceMarker, "successfully parsed params : {}", params);
  }

  public String stringify(final JSONObject jObject, final String field) {
    final Marker traceMarker = traceMarkerProvider.get();
    String fieldValue = "";
    try {
      final Object fieldValueObject = jObject.get(field);
      if (null != fieldValueObject) {
        fieldValue = fieldValueObject.toString();
      }
    } catch (final JSONException e) {
      LOG.debug(traceMarker, "exception {} while Stringifying {} from JSON", e, field);
      return null;
    }
    LOG.debug(traceMarker, "Retrived from json {} = {}", field, fieldValue);
    return fieldValue;
  }

  public String parseArray(final JSONObject jObject, final String param, final int index) {
    final Marker traceMarker = traceMarkerProvider.get();
    if (null == jObject) {
      return null;
    }
    try {
      final JSONArray jArray = jObject.getJSONArray(param);
      if (null == jArray) {
        return null;
      } else {
        return jArray.getString(index);
      }
    } catch (final JSONException e) {
      LOG.debug(traceMarker, "exception {} while parsing field at index {} from {}", e, index, jObject);
      return null;
    }
  }

  public List<Long> getCategory(final JSONObject jObject, final String oldORnew) {
    final Marker traceMarker = traceMarkerProvider.get();
    try {
      final JSONArray categories = jObject.getJSONArray(oldORnew);
      final Long[] category = new Long[categories.length()];
      for (int index = 0; index < categories.length(); index++) {
        category[index] = categories.getLong(index);
      }
      return Arrays.asList(category);
    } catch (final JSONException e) {
      LOG.debug(traceMarker, "error while reading category array {}", e);
      return null;
    }
  }

  public Set<Integer> getAcoountSegments(final JSONObject jObject) {
    final Marker traceMarker = traceMarkerProvider.get();
    try {
      final JSONArray segments = jObject.optJSONArray("segments");
      final HashSet<Integer> accountSegments = new HashSet<Integer>();
      for (int index = 0; segments != null && index < segments.length(); index++) {
        accountSegments.add(segments.getInt(index));
      }
      return accountSegments;
    } catch (final JSONException e) {
      LOG.debug(traceMarker, "error while reading account segments array {}", e);
      return new HashSet<Integer>();
    }
  }

  // Get user specific params
  public SASRequestParameters getUserParams(SASRequestParameters parameter, final JSONObject jObject) {
    final Marker traceMarker = traceMarkerProvider.get();
    LOG.debug(traceMarker, "inside parsing user params");
    try {
      final JSONObject userMap = (JSONObject) jObject.get("uparams");
      if (null != stringify(userMap, "u-age")) {
        parameter.setAge(Short.valueOf(stringify(userMap, "u-age")));
      }
      if (null != stringify(userMap, "u-gender")) {
        parameter.setGender(stringify(userMap, "u-gender"));
      }
      if (null != stringify(userMap, "u-postalcode")) {
        parameter.setPostalCode(Integer.parseInt(stringify(userMap, "u-postalcode")));
      }
      parameter = encodeParams(parameter, userMap);
    } catch (final JSONException exception) {
      LOG.debug(traceMarker, "json exception in parsing u params {}", exception);
    } catch (final NumberFormatException e) {
      LOG.debug(traceMarker, "number format exception in u params {}", e);
    }
    return parameter;
  }

  private SASRequestParameters encodeParams(final SASRequestParameters parameter, final JSONObject userMap)
      throws JSONException {
    final Marker traceMarker = traceMarkerProvider.get();
    final String utf8 = "UTF-8";
    try {
      if (null != parameter.getAge()) {
        parameter.setAge(Short.valueOf(URLEncoder.encode(String.valueOf(parameter.getAge()), utf8)));
      }
      if (null != parameter.getGender()) {
        parameter.setGender(URLEncoder.encode(parameter.getGender(), utf8));
      }
      if (null != parameter.getPostalCode()) {
        parameter.setPostalCode(Integer.valueOf(URLEncoder.encode(String.valueOf(parameter.getPostalCode()), utf8)));
      }
      String[] advertiserList = null;
      if (userMap.get("u-adapter") != null) {
        advertiserList = ((String) userMap.get("u-adapter")).split(",");
      }
      final Set<String> advertiserSet = new HashSet<String>();
      if (advertiserList != null) {
        Collections.addAll(advertiserSet, advertiserList);
        parameter.setUAdapters(advertiserSet);
      }
    } catch (final UnsupportedEncodingException e) {
      LOG.debug(traceMarker, "Error in encoding u params {}", e);
    }
    return parameter;
  }

  // Get user id params
  public void setUserIdParams(final CasInternalRequestParameters parameter, final JSONObject jObject) {
    final Marker traceMarker = traceMarkerProvider.get();
    if (null == jObject) {
      return;
    }
    try {
      final JSONObject userIdMap = (JSONObject) jObject.get("raw-uid");
      if (null == userIdMap) {
        return;
      }
      final String uid = stringify(userIdMap, "u-id");
      parameter.setUid(StringUtils.isNotBlank(uid) ? uid : stringify(userIdMap, "UDID"));
      if (StringUtils.isNotBlank(parameter.getUid()) && parameter.getUid().length() != 32) {
        parameter.setUid(MD5(parameter.getUid()));
      }
      parameter.setUidO1(stringify(userIdMap, "O1"));
      parameter.setUidMd5(stringify(userIdMap, "UM5"));
      parameter.setUidIFA(stringify(userIdMap, "IDA"));
      parameter.setGpid(stringify(userIdMap, "GPID"));
      parameter.setUidSO1(stringify(userIdMap, "SO1"));
      parameter.setUidIFV(stringify(userIdMap, "IDV"));
      parameter.setUidIDUS1(stringify(userIdMap, "IDUS1"));
      parameter.setUidADT(stringify(userIdMap, "u-id-adt"));
      parameter.setUuidFromUidCookie(stringify(userIdMap, "imuc__5"));
      parameter.setUidWC(stringify(userIdMap, "WC"));
    } catch (final JSONException exception) {
      LOG.debug(traceMarker, "Error in extracting userid params, {}", exception);
    }
  }

  public String MD5(final String md5) {
    try {
      final MessageDigest md = MessageDigest.getInstance("MD5");
      final byte[] array = md.digest(md5.getBytes());
      final StringBuffer sb = new StringBuffer();
      for (final byte anArray : array) {
        sb.append(Integer.toHexString(anArray & 0xFF | 0x100).substring(1, 3));
      }
      return sb.toString();
    } catch (final java.security.NoSuchAlgorithmException ignored) {
      LOG.debug("Exception {} raised with String {} in MD5", ignored, md5);
    }
    return null;
  }

}
