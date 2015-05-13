package com.inmobi.adserve.channels.server.module;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.inmobi.adserve.channels.api.NativeResponseMaker;
import com.inmobi.adserve.channels.api.natives.IxNativeBuilderFactory;
import com.inmobi.adserve.channels.api.natives.IxNativeBuilderImpl;
import com.inmobi.adserve.channels.api.natives.NativeBuilder;
import com.inmobi.adserve.channels.api.natives.NativeBuilderFactory;
import com.inmobi.adserve.channels.api.natives.RtbdNativeBuilderFactory;
import com.inmobi.adserve.channels.api.natives.RtbdNativeBuilderImpl;

public class NativeModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(NativeResponseMaker.class).asEagerSingleton();
        install(new FactoryModuleBuilder()
                .implement(NativeBuilder.class, RtbdNativeBuilderImpl.class)
                .build(Key.get(NativeBuilderFactory.class, RtbdNativeBuilderFactory.class)));
        install(new FactoryModuleBuilder()
                .implement(NativeBuilder.class, IxNativeBuilderImpl.class)
                .build(Key.get(NativeBuilderFactory.class, IxNativeBuilderFactory.class)));
    }

}
