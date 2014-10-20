package com.inmobi.adserve.channels.server.requesthandler.filters;

import org.slf4j.Marker;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.inmobi.adserve.channels.server.api.Servlet;


/**
 * @author abhishek.parwal
 * 
 */
public class TestScopeModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(Marker.class).toProvider(new Provider<Marker>() {
			@Override
			public Marker get() {
				return null;
			}
		});
		bind(Servlet.class).toProvider(new Provider<Servlet>() {

			@Override
			public Servlet get() {
				return null;
			}
		});

	}

}
