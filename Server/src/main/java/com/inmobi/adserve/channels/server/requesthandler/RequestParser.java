package com.inmobi.adserve.channels.server.requesthandler;

import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.util.DebugLogger;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.*;


public class RequestParser {

    public static JSONObject extractParams(Map<String, List<String>> params) throws Exception {
        return extractParams(params, "args");
    }

    // Extracting params.
    public static JSONObject extractParams(Map<String, List<String>> params, String jsonKey) throws JSONException,
            UnsupportedEncodingException {
        if (!params.isEmpty()) {
            List<String> values = params.get(jsonKey);
            if (CollectionUtils.isNotEmpty(values)) {
                String stringVal = values.iterator().next();
                return new JSONObject(URLDecoder.decode(stringVal, "UTF-8"));
            }
        }
        return null;
    }

    public static void parseRequestParameters(JSONObject jObject, SASRequestParameters params,
            CasInternalRequestParameters casInternalRequestParameters, DebugLogger logger) {
        logger.debug("Inside parameter parser");
        if (null == jObject) {
            logger.error("Returning null as jObject is null.");
            params = null;
            return;
        }
        params.setAllParametersJson(jObject.toString());
        int dst = jObject.optInt("dst", 2);
        Set<Integer> accountSegments = getAcoountSegments(jObject, logger);
        boolean isResponseOnlyFromDcp = jObject.optBoolean("isResponseOnlyFromDcp", false);
        logger.debug("dst type is", dst, "isResponseOnlyFromDcp ", isResponseOnlyFromDcp, "and account segments are",
            accountSegments);
        params.setDst(dst);
        params.setResponseOnlyFromDcp(isResponseOnlyFromDcp);
        params.setAccountSegment(accountSegments);
        params.setRemoteHostIp(stringify(jObject, "w-s-carrier", logger));
        params.setUserAgent(stringify(jObject, "rqXInmobiPhoneUseragent", logger));
        if (null == params.getUserAgent()) {
            params.setUserAgent(stringify(jObject, "rqHUserAgent", logger));
        }
        params.setLocSrc(stringify(jObject, "loc-src", logger));
        params.setLatLong(stringify(jObject, "latlong", logger));
        params.setSiteId(stringify(jObject, "rqMkSiteid", logger));
        params.setSource(stringify(jObject, "source", logger));
        params.setCountry(parseArray(jObject, "carrier", 2));
        params.setCountryStr(parseArray(jObject, "carrier", 1));
        params.setArea(parseArray(jObject, "carrier", 4));
        params.setSlot(stringify(jObject, "slot-served", logger));
        params.setRqMkSlot(stringify(jObject, "rqMkAdSlot", logger));
        String sdkVersion = stringify(jObject, "sdk-version", logger);
        if (StringUtils.isBlank(sdkVersion) || "null".equalsIgnoreCase(sdkVersion)) {
            sdkVersion = null;
        }
        params.setSdkVersion(sdkVersion);
        params.setSiteType(stringify(jObject, "site-type", logger));
        params.setAdcode(stringify(jObject, "adcode", logger));
        if (params.getSiteType() != null) {
            params.setSiteType(params.getSiteType().toUpperCase());
        }
        params.setCategories(getCategory(jObject, logger, "new-category"));
        params.setRqIframe(stringify(jObject, "rqIframe", logger));
        params.setRFormat(stringify(jObject, "r-format", logger));
        params.setRqMkAdcount(stringify(jObject, "rqMkAdcount", logger));
        params.setTid(stringify(jObject, "tid", logger));
        params.setTp(stringify(jObject, "tp", logger));

        params.setAllowBannerAds(jObject.optBoolean("site-allowBanner", true));
        params.setSiteFloor(jObject.optDouble("site-floor", 0.0));
        params.setSiteSegmentId(jObject.optInt("sel-seg-id", 0));
        params.setModelId(jObject.optInt("model-id", 0));
        logger.debug("Site segment id is", params.getSiteSegmentId(), "and model id is", params.getModelId());
        params.setIpFileVersion(jObject.optInt("rqIpFileVer", 1));
        logger.debug("country obtained is", params.getCountry());
        logger.debug("site floor is", params.getSiteFloor());
        logger.debug("osId is", params.getOsId());
        params.setUidParams(stringify(jObject, "raw-uid", logger));
        setUserIdParams(casInternalRequestParameters, jObject, logger);
        params = getUserParams(params, jObject, logger);
        try {
            JSONArray siteInfo = jObject.getJSONArray("site");
            if (siteInfo != null && siteInfo.length() > 0) {
                params.setSiteIncId(siteInfo.getLong(0));
            }
        }
        catch (JSONException exception) {
            logger.error("site object not found in request");
            params.setSiteIncId(0);
        }
        
        try {
            JSONArray jsonArray = jObject.getJSONArray("handset");
            params.setHandsetInternalId(Long.parseLong(jsonArray.get(0).toString()));
        }
        catch (JSONException e) {
            logger.error("Handset array not found");
        }
        try {
            params.setCarrier(jObject.getJSONArray("carrier"));
        }
        catch (JSONException e) {
            logger.error("carrier array not found");
        }
        params.setOsId(jObject.optInt("os-id", -1));
        params.setRichMedia(jObject.optBoolean("rich-media", false));
        params.setRqAdType(stringify(jObject, "rqAdtype", logger));
        params.setAppUrl(stringify(jObject, "site-url", logger));
        logger.debug("successfully parsed params");
    }

    public static String stringify(JSONObject jObject, String field, DebugLogger logger) {
        String fieldValue = "";
        try {
            Object fieldValueObject = jObject.get(field);
            if (null != fieldValueObject) {
                fieldValue = fieldValueObject.toString();
            }
        }
        catch (JSONException e) {
            return null;
        }
        logger.debug("Retrived from json", field, " = ", fieldValue);
        return fieldValue;
    }

    public static String parseArray(JSONObject jObject, String param, int index) {
        if (null == jObject) {
            return null;
        }
        try {
            JSONArray jArray = jObject.getJSONArray(param);
            if (null == jArray) {
                return null;
            }
            else {
                return (jArray.getString(index));
            }
        }
        catch (JSONException e) {
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
        }
        catch (JSONException e) {
            logger.error("error while reading category array", e.getMessage());
            return null;
        }
    }

    public static Set<Integer> getAcoountSegments(JSONObject jObject, DebugLogger logger) {
        try {
            JSONArray segments = jObject.getJSONArray("segments");
            HashSet<Integer> accountSegments = new HashSet<Integer>();
            for (int index = 0; index < segments.length(); index++) {
                accountSegments.add(segments.getInt(index));
            }
            return accountSegments;
        }
        catch (JSONException e) {
            logger.debug("error while reading account segments array", e.getMessage());
            return new HashSet<Integer>();
        }
    }

    // Get user specific params
    public static SASRequestParameters getUserParams(SASRequestParameters parameter, JSONObject jObject,
            DebugLogger logger) {
        logger.debug("inside parsing user params");
        String utf8 = "UTF-8";
        try {
            JSONObject userMap = (JSONObject) jObject.get("uparams");
            parameter.setAge(stringify(userMap, "u-age", logger));
            parameter.setGender(stringify(userMap, "u-gender", logger));
            parameter.setPostalCode(stringify(userMap, "u-postalcode", logger));
            if (!StringUtils.isEmpty(parameter.getPostalCode())) {
                parameter.setPostalCode(parameter.getPostalCode().replaceAll(" ", ""));
            }
            parameter.setUserLocation(stringify(userMap, "u-location", logger));
            parameter.setGenderOrig(stringify(userMap, "u-gender-orig", logger));
            try {
                if (null != parameter.getAge()) {
                    parameter.setAge(URLEncoder.encode(parameter.getAge(), utf8));
                }
                if (null != parameter.getGender()) {
                    parameter.setGender(URLEncoder.encode(parameter.getGender(), utf8));
                }
                if (null != parameter.getPostalCode()) {
                    parameter.setPostalCode(URLEncoder.encode(parameter.getPostalCode(), utf8));
                }
            }
            catch (UnsupportedEncodingException e) {
                logger.debug("Error in encoding u params", e.getMessage());
            }
        }
        catch (JSONException exception) {
            logger.debug("json exception in parsing u params", exception);
        }
        return parameter;
    }

    // Get user id params
    public static void setUserIdParams(CasInternalRequestParameters parameter, JSONObject jObject, DebugLogger logger) {
        if (null == jObject) {
            return;
        }
        try {
            JSONObject userIdMap = (JSONObject) jObject.get("raw-uid");
            if (null == userIdMap) {
                return;
            }
            String uid = stringify(userIdMap, "u-id", logger);
            parameter.uid = (StringUtils.isNotBlank(uid) ? uid : stringify(userIdMap, "UDID", logger));
            if (StringUtils.isNotBlank(parameter.uid) && parameter.uid.length() != 32) {
                parameter.uid = MD5(parameter.uid);
            }
            parameter.uidO1 = stringify(userIdMap, "O1", logger);
            parameter.uidMd5 = stringify(userIdMap, "UM5", logger);
            parameter.uidIFA = stringify(userIdMap, "IDA", logger);
            parameter.uidSO1 = stringify(userIdMap, "SO1", logger);
            parameter.uidIFV = stringify(userIdMap, "IDV", logger);
            parameter.uidIDUS1 = stringify(userIdMap, "IDUS1", logger);
            parameter.uidADT = stringify(userIdMap, "u-id-adt", logger);
        }
        catch (JSONException exception) {
            logger.debug("Error in extracting userid params");
        }
    }

    public static String MD5(String md5) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (byte anArray : array) {
                sb.append(Integer.toHexString((anArray & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        }
        catch (java.security.NoSuchAlgorithmException ignored) {
        }
        return null;
    }

}
