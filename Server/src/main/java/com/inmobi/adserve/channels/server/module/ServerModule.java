package com.inmobi.adserve.channels.server.module;

import java.util.Map;
import java.util.Set;

import javax.ws.rs.Path;

import org.apache.commons.configuration.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.adnetworks.module.AdapterConfigModule;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.auction.AuctionEngine;
import com.inmobi.adserve.channels.server.auction.AuctionFilterApplier;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.Logging;
import com.inmobi.adserve.channels.server.requesthandler.MatchSegments;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;
import com.inmobi.adserve.channels.util.ConfigurationLoader;
import com.inmobi.adserve.channels.util.DocumentBuilderHelper;
import com.inmobi.adserve.channels.util.JaxbHelper;
import com.inmobi.adserve.channels.util.annotations.RtbConfiguration;
import com.inmobi.adserve.channels.util.annotations.ServerConfiguration;
import com.inmobi.template.module.TemplateModule;


/**
 * @author abhishek.parwal
 * 
 */
public class ServerModule extends AbstractModule {
    private final RepositoryHelper repositoryHelper;
    private final Reflections reflections;
    private final Configuration adapterConfiguration;
    private final Configuration serverConfiguration;
    private final Configuration rtbConfiguration;
    private final String dataCentreName;

    public ServerModule(final ConfigurationLoader configurationLoader, final RepositoryHelper repositoryHelper,
        final String dataCentreName) {
        adapterConfiguration = configurationLoader.getAdapterConfiguration();
        serverConfiguration = configurationLoader.getServerConfiguration();
        rtbConfiguration = configurationLoader.getRtbConfiguration();
        this.repositoryHelper = repositoryHelper;
        this.dataCentreName = dataCentreName;
        reflections = new Reflections("com.inmobi.adserve.channels", new TypeAnnotationsScanner(),
                new SubTypesScanner());
    }

    @Override
    protected void configure() {
        bind(RepositoryHelper.class).toInstance(repositoryHelper);
        bind(MatchSegments.class).asEagerSingleton();
        bind(Configuration.class).annotatedWith(ServerConfiguration.class).toInstance(serverConfiguration);
        bind(Configuration.class).annotatedWith(RtbConfiguration.class).toInstance(rtbConfiguration);
        bind(JaxbHelper.class).asEagerSingleton();
        bind(DocumentBuilderHelper.class).asEagerSingleton();

        requestStaticInjection(BaseAdNetworkImpl.class);
        requestStaticInjection(ChannelSegment.class);
        requestStaticInjection(Logging.class);
        requestStaticInjection(AuctionFilterApplier.class);
        requestStaticInjection(AuctionEngine.class);
        requestStaticInjection(ResponseSender.class);

        install(new NativeModule());
        install(new InmobiAdTrackerModule());
        install(new TemplateModule());
        install(new AdapterConfigModule(adapterConfiguration, dataCentreName));
        install(new ChannelSegmentFilterModule());
        install(new ScopeModule());
        install(new AuctionFilterModule());
    }

    @Singleton
    @Provides
    Map<String, Servlet> provideServletMap(final Injector injector) {
        final Map<String, Servlet> pathToServletMap = Maps.newHashMap();
        final Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Path.class);
        for (final Class<?> class1 : classes) {
            pathToServletMap.put(class1.getAnnotation(Path.class).value(), (Servlet) injector.getInstance(class1));
        }
        return pathToServletMap;
    }
}
