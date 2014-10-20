package com.inmobi.adserve.channels.server.module;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.inmobi.adserve.channels.api.NativeResponseMaker;
import com.inmobi.adserve.channels.api.natives.NativeBuilder;
import com.inmobi.adserve.channels.api.natives.NativeBuilderFactory;
import com.inmobi.adserve.channels.api.natives.NativeBuilderImpl;

public class NativeModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(NativeResponseMaker.class).asEagerSingleton();
        install(new FactoryModuleBuilder().implement(NativeBuilder.class, NativeBuilderImpl.class).build(
                NativeBuilderFactory.class));

    }

}
