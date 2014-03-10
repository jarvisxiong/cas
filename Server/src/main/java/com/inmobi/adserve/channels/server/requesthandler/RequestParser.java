package com.inmobi.adserve.channels.server.requesthandler;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Arrays;
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
    private static final Logger    LOG = LoggerFactory.getLogger(RequestParser.class);
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
            List<String> values = params.get(jsonKey);
            if (CollectionUtils.isNotEmpty(values)) {
                String stringVal = values.iterator().next();
                return new JSONObject(stringVal);
            }
        }
        return null;
    }

    public void parseRequestParameters(final JSONObject jObject, SASRequestParameters params,
            final CasInternalRequestParameters casInternalRequestParameters) {
        Marker traceMarker = traceMarkerProvider.get();

        LOG.debug(traceMarker, "Inside parameter parser");
        if (null == jObject) {
            LOG.error(traceMarker, "Returning null as jObject is null.");
            params = null;
            return;
        }
        params.setAllParametersJson(jObject.toString());
        int dst = jObject.optInt("dst", 2);
        Set<Integer> accountSegments = getAcoountSegments(jObject);
        boolean isResponseOnlyFromDcp = jObject.optBoolean("isResponseOnlyFromDcp", false);
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
        params.setCountry(parseArray(jObject, "carrier", 2));
        params.setCountryStr(parseArray(jObject, "carrier", 1));
        params.setArea(parseArray(jObject, "carrier", 4));
        params.setSlot(stringify(jObject, "slot-served"));
        params.setRqMkSlot(stringify(jObject, "rqMkAdSlot"));
        String sdkVersion = stringify(jObject, "sdk-version");
        if (StringUtils.isBlank(sdkVersion) || "null".equalsIgnoreCase(sdkVersion)) {
            sdkVersion = null;
        }
        params.setSdkVersion(sdkVersion);
        params.setSiteType(stringify(jObject, "site-type"));
        params.setAdcode(stringify(jObject, "adcode"));
        params.setPlatformOsId(jObject.optInt("os-id", -1));
        if (params.getSiteType() != null) {
            params.setSiteType(params.getSiteType().toUpperCase());
        }
        params.setCategories(getCategory(jObject, "new-category"));
        params.setRqIframe(stringify(jObject, "rqIframe"));
        params.setRFormat(stringify(jObject, "r-format"));
        params.setRqMkAdcount(stringify(jObject, "rqMkAdcount"));
        params.setTid(stringify(jObject, "tid"));
        params.setTp(stringify(jObject, "tp"));

        params.setAllowBannerAds(jObject.optBoolean("site-allowBanner", true));
        params.setSiteFloor(jObject.optDouble("site-floor", 0.0));
        params.setSiteSegmentId(jObject.optInt("sel-seg-id", 0));
        params.setModelId(jObject.optInt("model-id", 0));
        LOG.debug(traceMarker, "Site segment id is {} and model id is {}", params.getSiteSegmentId(),
                params.getModelId());
        params.setIpFileVersion(jObject.optInt("rqIpFileVer", 1));
        LOG.debug(traceMarker, "country obtained is {}", params.getCountry());
        LOG.debug(traceMarker, "site floor is {}", params.getSiteFloor());
        LOG.debug(traceMarker, "osId is {}", params.getPlatformOsId());
        params.setUidParams(stringify(jObject, "raw-uid"));
        setUserIdParams(casInternalRequestParameters, jObject);
        params = getUserParams(params, jObject);
        try {
            JSONArray siteInfo = jObject.getJSONArray("site");
            if (siteInfo != null && siteInfo.length() > 0) {
                params.setSiteIncId(siteInfo.getLong(0));
            }
        }
        catch (JSONException exception) {
            LOG.error(traceMarker, "site object not found in request");
            params.setSiteIncId(0);
        }
        try {
            params.setHandset(jObject.getJSONArray("handset"));
        }
        catch (JSONException e) {
            LOG.error(traceMarker, "Handset array not found");
        }
        try {
            params.setCarrier(jObject.getJSONArray("carrier"));
        }
        catch (JSONException e) {
            LOG.error(traceMarker, "carrier array not found");
        }
        params.setOsId(jObject.optInt("os-id", -1));
        params.setRichMedia(jObject.optBoolean("rich-media", false));
        params.setRqAdType(stringify(jObject, "rqAdtype"));
        params.setAppUrl(stringify(jObject, "site-url"));
        LOG.debug(traceMarker, "successfully parsed params");
    }

    public String stringify(final JSONObject jObject, final String field) {
        Marker traceMarker = traceMarkerProvider.get();

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
        LOG.debug(traceMarker, "Retrived from json {} = {}", field, fieldValue);
        return fieldValue;
    }

    public String parseArray(final JSONObject jObject, final String param, final int index) {
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

    public List<Long> getCategory(final JSONObject jObject, final String oldORnew) {
        Marker traceMarker = traceMarkerProvider.get();

        try {
            JSONArray categories = jObject.getJSONArray(oldORnew);
            Long[] category = new Long[categories.length()];
            for (int index = 0; index < categories.length(); index++) {
                category[index] = categories.getLong(index);
            }
            return Arrays.asList(category);
        }
        catch (JSONException e) {
            LOG.error(traceMarker, "error while reading category array {}", e);
            return null;
        }
    }

    public Set<Integer> getAcoountSegments(final JSONObject jObject) {
        Marker traceMarker = traceMarkerProvider.get();

        try {
            JSONArray segments = jObject.getJSONArray("segments");
            HashSet<Integer> accountSegments = new HashSet<Integer>();
            for (int index = 0; index < segments.length(); index++) {
                accountSegments.add(segments.getInt(index));
            }
            return accountSegments;
        }
        catch (JSONException e) {
            LOG.debug(traceMarker, "error while reading account segments array {}", e);
            return new HashSet<Integer>();
        }
    }

    // Get user specific params
    public SASRequestParameters getUserParams(final SASRequestParameters parameter, final JSONObject jObject) {
        Marker traceMarker = traceMarkerProvider.get();

        LOG.debug(traceMarker, "inside parsing user params");
        String utf8 = "UTF-8";
        try {
            JSONObject userMap = (JSONObject) jObject.get("uparams");
            parameter.setAge(stringify(userMap, "u-age"));
            parameter.setGender(stringify(userMap, "u-gender"));
            parameter.setPostalCode(stringify(userMap, "u-postalcode"));
            if (!StringUtils.isEmpty(parameter.getPostalCode())) {
                parameter.setPostalCode(parameter.getPostalCode().replaceAll(" ", ""));
            }
            parameter.setUserLocation(stringify(userMap, "u-location"));
            parameter.setGenderOrig(stringify(userMap, "u-gender-orig"));
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
                LOG.debug(traceMarker, "Error in encoding u params {}", e);
            }
        }
        catch (JSONException exception) {
            LOG.debug(traceMarker, "json exception in parsing u params {}", exception);
        }
        return parameter;
    }

    // Get user id params
    public void setUserIdParams(final CasInternalRequestParameters parameter, final JSONObject jObject) {
        Marker traceMarker = traceMarkerProvider.get();

        if (null == jObject) {
            return;
        }
        try {
            JSONObject userIdMap = (JSONObject) jObject.get("raw-uid");
            if (null == userIdMap) {
                return;
            }
            String uid = stringify(userIdMap, "u-id");
            parameter.uid = (StringUtils.isNotBlank(uid) ? uid : stringify(userIdMap, "UDID"));
            if (StringUtils.isNotBlank(parameter.uid) && parameter.uid.length() != 32) {
                parameter.uid = MD5(parameter.uid);
            }
            parameter.uidO1 = stringify(userIdMap, "O1");
            parameter.uidMd5 = stringify(userIdMap, "UM5");
            parameter.uidIFA = stringify(userIdMap, "IDA");
            parameter.uidSO1 = stringify(userIdMap, "SO1");
            parameter.uidIFV = stringify(userIdMap, "IDV");
            parameter.uidIDUS1 = stringify(userIdMap, "IDUS1");
            parameter.uidADT = stringify(userIdMap, "u-id-adt");
        }
        catch (JSONException exception) {
            LOG.debug(traceMarker, "Error in extracting userid params");
        }
    }

    public String MD5(final String md5) {
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
