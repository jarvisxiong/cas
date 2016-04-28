package com.inmobi.template.interfaces;

import java.util.List;

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

}
