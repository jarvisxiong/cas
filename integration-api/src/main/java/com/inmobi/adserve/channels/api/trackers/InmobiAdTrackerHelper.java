package com.inmobi.adserve.channels.api.trackers;

import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Created by ishanbhatnagar on 12/5/15.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class InmobiAdTrackerHelper {
    private static final String URLPATHSEP = "/";
    private static final Gson gson = new Gson();

    protected static String getIdBase36(final Long id) {
        return Long.toString(id, 36);
    }

    protected static String getIdBase36(final int id) {
        return Integer.toString(id, 36);
    }

    protected static String getCarrierIdBase36(final int carrierId, final int countryId) {
        return Long.toString(carrierId != 0 ? carrierId : -countryId, 36);
    }

    protected static String getEncodedJson(final Map<String, String> clickUidMap) {
        final String unEncodedString = gson.toJson(clickUidMap);
        final byte[] unEncoded = unEncodedString.getBytes();
        final String encodedString = new String(Base64.encodeBase64(unEncoded))
                .replaceAll("\\+", "-").replaceAll("\\/", "_").replaceAll("=", "~");
        return encodedString;
    }

    protected static String appendSeparator(final String parameter) {
        return URLPATHSEP + parameter;
    }

    protected static String getIntegrationVersionStr(int integrationVersion) {
        String ver = String.valueOf(integrationVersion);
        String integrationVersionStr = StringUtils.EMPTY;
        if (StringUtils.isNotEmpty(ver)) {
            integrationVersionStr += ver.charAt(0);
            for (int i = 1; i < ver.length(); i++) {
                integrationVersionStr += "." + ver.charAt(i);
            }
        }
        return integrationVersionStr;
    }
}
