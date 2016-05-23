package com.inmobi.template.interfaces;

import java.util.List;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;

/**
 * 
 * @author ritwik.kumar
 *
 */
public abstract class Tools {

    /**
     * 
     * @param context
     * @return
     */
    public abstract String getVastXMl(final Context context);

    public abstract Object getAdUnitTrackersJson(final  Context context);

    /**
     * 
     * @param context
     * @param key
     * @return
     */
    public abstract Object jpath(final Context context, final String key);

    /**
     * 
     * @param context
     * @param key
     * @return
     */
    public abstract String jpathStr(final Context context, final String key);

    /**
     * 
     * @param json
     * @return
     */
    public abstract String jsonEncode(final Object json);

    /**
     * 
     * @param context
     * @param pubContent
     * @return
     */
    public abstract String nativeAd(final Context context, final String pubContent);

    public abstract Object evalJsonPath(final String json, final String jsonPathExpression);

    public abstract Object evalJsonPath(Object jsonObject, String jsonPathExpression);

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
     * @param abTestingMode
     * @param list
     * @param value
     * @return
     */
    public boolean newAsyncMode(final Boolean abTestingMode, final List<String> list, final String value) {
        return true;
    }

    /**
     * 
     * @return
     */
    public Object boltObject() {
        return null;
    }

    public String base64(String content) {
        final Base64 base64 = new Base64();
        return base64.encodeAsString(content.getBytes(Charsets.UTF_8));
    }

}
