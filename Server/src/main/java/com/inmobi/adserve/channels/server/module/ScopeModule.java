package com.inmobi.adserve.channels.server.module;

import org.slf4j.Marker;

import com.google.inject.AbstractModule;
import com.inmobi.adserve.channels.server.NettyRequestScope;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;
import com.inmobi.adserve.channels.util.annotations.BatchScoped;


/**
 * @author abhishek.parwal
 * 
 */
public class ScopeModule extends AbstractModule {

    @Override
    protected void configure() {
        NettyRequestScope nettyRequestScope = new NettyRequestScope();
        bindScope(BatchScoped.class, nettyRequestScope);
        bind(NettyRequestScope.class).toInstance(nettyRequestScope);
        bind(Marker.class).toProvider(NettyRequestScope.<Marker> seededKeyProvider()).in(nettyRequestScope);
        bind(Servlet.class).toProvider(NettyRequestScope.<Servlet> seededKeyProvider()).in(nettyRequestScope);
        bind(ResponseSender.class).toProvider(NettyRequestScope.<ResponseSender> seededKeyProvider()).in(
                nettyRequestScope);

    }

}
