package com.inmobi.template.context;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.inmobi.template.interfaces.Context;

@Getter
@ToString
public class Icon extends AbstractContext {

	private final int w;
	private final int width;
	private final int height;
	private final int h;
	private final String url;

	private Icon(final Builder builder) {
		w = builder.w;
		h = builder.h;
		height = builder.h;
		width = builder.w;
		url = builder.url;
		setValues(params);
	}

	@Override
	void setValues(final Map<String, Object> params) {
		params.put(KeyConstants.WIDTH, w);
		params.put(KeyConstants.HEIGHT, h);
		params.put(KeyConstants.URL, url);
	}

	public static Builder newBuilder() {
		return new Builder();
	}

	@Setter
	public static class Builder {

		private int w;
		private int h;
		private String url;

		public Context build() {
			return new Icon(this);
		}

	}

}
