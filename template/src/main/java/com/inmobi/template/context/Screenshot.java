package com.inmobi.template.context;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.inmobi.template.interfaces.Context;


@ToString
@Getter
public final class Screenshot extends AbstractContext {


	private final int w;
	private final int h;
	private final int width;
	private final int height;
	private final String ar;
	private final String url;



	private Screenshot(final Builder builder) {
		w = builder.w;
		h = builder.h;
		width = w;
		height = h;
		url = builder.url;
		ar = builder.ar;
		setValues(params);
	}

	@Override
	public void setValues(final Map<String, Object> params) {
		params.put("w", w);
		params.put("h", h);
		params.put("width", w);
		params.put("height", h);
		params.put("ar", ar);
		params.put("url", url);
	}

	public static Builder newBuilder() {
		return new Builder();
	}


	public static class Builder {

		@Setter
		private int w;
		@Setter
		private int h;
		@Setter
		private String ar;
		@Setter
		private String url;

		public Context build() {
			return new Screenshot(this);

		}

	}


}
