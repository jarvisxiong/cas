package com.inmobi.template.interfaces;

import java.util.List;


public abstract class Tools {


	public abstract Object jpath(final Context context, final String key);

	public abstract String jpathStr(final Context context, final String key);


	public boolean isNonNull(final Object value) {
		return value != null;
	}

	// public boolean isNotEmpty(Object value) {
	// return isNonNull(value) && value.
	// }

	public abstract String jsonEncode(final Object json);


	public abstract String nativeAd(final Context context, final String pubContent);

	public boolean newAsyncMode(final Boolean abTestingMode, final List<String> list, final String value) {
		return true;
	}

	public Object boltObject() {
		return null;
	}

}
