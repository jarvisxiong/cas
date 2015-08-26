// Copyright (c) 2013. InMobi, All Rights Reserved.

package com.inmobi.template.tool;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.velocity.tools.generic.EscapeTool;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.inmobi.adserve.channels.util.config.GlobalConstant;
import com.inmobi.template.gson.GsonManager;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Tools used for formatting inside velocity template. The strings need to be escaped given the context.
 *
 * @author ritwik.kumar
 *
 */
@Slf4j
public class TemplateTool extends EscapeTool {
    private static final Pattern WHITESPACE_1 = Pattern.compile("(\\s)+");
    private static final Pattern WHITESPACE_2 = Pattern.compile("^\\s|\\s$");
    private static final String continueParam = "continue=";
    private final Gson gson;

    /**
     * 
     * @param gsonManager
     */
    @Inject
    public TemplateTool(final GsonManager gsonManager) {
        gson = gsonManager.getGsonInstance();
    }

    /**
     * 
     * @param str
     * @return
     */
    public String cdata(final String str) {
        final String strReplaced = str.replaceAll("]]>", "]]]]><![CDATA[>");
        return "<![CDATA[" + strReplaced + "]]>";
    }

    /**
     * 
     * @param str
     * @return
     */
    public boolean isNotEmpty(@Nullable final String str) {
        return StringUtils.isNotEmpty(str);
    }

    /**
     * 
     * @param clickUrl
     * @param appendMacro
     * @return
     */
    public String encodeUrlParam(final String clickUrl, final String appendMacro) {
        final String param = addUrlParams(clickUrl, continueParam + appendMacro);
        try {
            final String encodedParam = URLEncoder.encode(param, GlobalConstant.UTF_8);
            return encodedParam;
        } catch (final UnsupportedEncodingException e) {
            log.error("unable to encode url params", e);
        }
        return param;
    }

    /**
     * 
     * @param clickUrl
     * @param querySnippet
     * @return
     */
    public String addUrlParams(final String clickUrl, final String querySnippet) {
        if (clickUrl.contains("?")) {
            return clickUrl + "&" + querySnippet;
        }
        return clickUrl + "?" + querySnippet;
    }


    /**
     * 
     * @param str
     * @return
     */
    public static String trim(final String str) {
        return WHITESPACE_2.matcher(WHITESPACE_1.matcher(str).replaceAll(" ")).replaceAll("");
    }

    /**
     * 
     * @param str
     * @return
     */
    public String jsInline(final String str) {
        return super.javascript(str).replaceAll("<\\/", "<' +'/");
    }

    /**
     * 
     * @param value
     * @return
     */
    public boolean isNonNull(final Object value) {
        return value != null;
    }

    /**
     * 
     * @param name
     * @param ns
     * @param value
     * @return
     */
    public String attrAction(final String name, final String ns, @Nullable final String value) {
        return value != null ? name + "=\"" + ns + html(value) + "\"" : "";
    }

    /**
     * 
     * @param name
     * @param value
     * @return
     */
    public String attr(final String name, @Nullable final String value) {
        return value != null ? name + "=\"" + html(value) + "\"" : "";
    }

    /**
     * 
     * @param name
     * @param value
     * @return
     */
    public String xattr(final String name, @Nullable final Object value) {
        return value != null ? name + "=\"" + xml(String.valueOf(value)) + "\"" : "";
    }

    /**
     * Returns true if sdkVersion is greater than the compareVersion, in a device agnostic sense.
     * <p/>
     * a350 >= 350 i350 >= 350
     * <p/>
     * SDK version must be a valid string satisfying the representation \alpha(\digit)+
     *
     * @param sdkVersion
     * @param compareVersion
     * @return true if sdkVersion >= compareVersion
     */
    public boolean sdkGreaterOrEquals(final String sdkVersion, final String compareVersion) {
        return isSdkVersionAboveOrEqualsGivenVersion(sdkVersion, compareVersion);
    }

    /**
     * @param imSdkVersionString - Detected imSdk version (e.g i350, a36)
     * @param givenVersionString - Version that needs to be compared (e.g 350, 362, 36)
     * @return true if extracted imSdkVersion >= given version, false otherwise (this includes cases like if any of the
     *         version string is empty, null, or invalid value)
     */
    public static boolean isSdkVersionAboveOrEqualsGivenVersion(final String imSdkVersionString,
            final String givenVersionString) {
        if (StringUtils.isNotEmpty(imSdkVersionString) && StringUtils.isNotEmpty(givenVersionString)) {
            try {
                final String imSdkString = imSdkVersionString.substring(1);
                final int minLen = Math.min(imSdkString.length(), givenVersionString.length());
                final int imSdkVersion = Integer.parseInt(imSdkString.substring(0, minLen));
                final int givenVersion = Integer.parseInt(givenVersionString.substring(0, minLen));
                return imSdkVersion >= givenVersion;
            } catch (final NumberFormatException e) {
                log.debug("Invalid imSdk value or invalid given version string.", e);
                return false;
            }
        }
        return false;
    }

    /**
     * 
     * @param urlString
     * @return
     */
    public boolean isMarketUrl(final String urlString) {
        return false;
    }

    /**
     * 
     * @param urlString
     * @return
     */
    public boolean isHttps(final String urlString) {
        final URI uri = URI.create(urlString);
        final String scheme = String.valueOf(uri.getScheme()).toLowerCase();
        return "https".equals(scheme);
    }

    /**
     * 
     * @param urlString
     * @param scheme
     * @return
     */
    public String setScheme(final String urlString, final String scheme) {
        try {
            return new URIBuilder(URI.create(urlString)).setScheme(scheme).build().toString();
        } catch (final URISyntaxException e) {
            log.error("Unable to parse URI, while setScheme. ", e);
            return urlString;
        }
    }

    /**
     * 
     * @param urlString
     * @param authority
     * @return
     */
    public String setAuthority(final String urlString, final String authority) {
        try {
            return new URIBuilder(URI.create(urlString)).setHost(authority).build().toString();
        } catch (final URISyntaxException e) {
            log.error("Unable to parse URI, while setAuthority. ", e);
            return urlString;
        }
    }


    /**
     * 
     * @param input
     * @return
     */
    public String base64(final String input) {
        // The escaping is not url safe, the input is decoded as base64 utf-8 string
        final Base64 base64 = new Base64();
        return base64.encodeAsString(input.getBytes(Charsets.UTF_8));
    }


    /**
     * 
     * @param jsonObject
     * @param jsonPathExpression
     * @return
     */
    public Object evalJsonPath(final Object jsonObject, final String jsonPathExpression) {
        final JsonPath jsonPath = JsonPath.compile(jsonPathExpression);
        try {
            return jsonPath.read(jsonObject);
        } catch (final InvalidPathException e) {
            return null;
        }
    }

    /**
     * 
     * @param val
     * @return
     */
    public String inspect(final Object val) {
        log.debug("Inspection: {}", jsonEncode(val));
        return StringUtils.EMPTY;
    }

    /**
     * 
     * @param json
     * @return
     */
    public String jsonEncode(final Object json) {
        return gson.toJson(json);
    }

    /**
     * 
     * @param abTestingMode
     * @param list
     * @param value
     * @return
     */
    public boolean newAsyncMode(final Boolean abTestingMode, final List<String> list, final String value) {
        if (abTestingMode == null || !abTestingMode) {
            return true;
        } else {
            return list.contains(value);
        }
    }

    /**
     * 
     * @param json
     * @return
     */
    public String getDecoratorJson(final Object json) {
        return gson.toJson(json);
    }

    /**
     * 
     * @param countryCode
     * @return
     */
    public boolean requestFromJapan(final String countryCode) {
        if ("JP".equalsIgnoreCase(countryCode)) {
            return true;
        }
        return false;
    }

    @Data
    public static class Scaled {
        private final int width;
        private final int height;
    }

    /**
     * 
     * @param supplyWidth
     * @param supplyHeight
     * @param creativeWidth
     * @param creativeHeight
     * @return
     */
    public Scaled getScaled(final int supplyWidth, final int supplyHeight, final int creativeWidth,
            final int creativeHeight) {
        // Scaled width == Supply Width (call this matchesWidth):
        // scaledHeight < supplyHeight
        // Implies:
        // creativeHeight * (supplyWidth / creativeWidth) < supplyHeight
        // Implies:
        // creativeHeight * supplyWidth < creativeWidth * supplyHeight
        // Hence,
        final boolean matchesWidth = creativeHeight * supplyWidth < creativeWidth * supplyHeight;
        if (matchesWidth) {
            final int scaledHeight = Math.round((float) (creativeHeight * supplyWidth) / creativeWidth);
            return new Scaled(supplyWidth, scaledHeight);
        } else {
            final int scaledWidth = Math.round((float) (creativeWidth * supplyHeight) / creativeHeight);
            return new Scaled(scaledWidth, supplyHeight);
        }

    }
}
