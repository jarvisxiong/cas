package com.inmobi.adserve.channels.server.module;

import java.util.Map;
import java.util.Set;

import javax.ws.rs.Path;

import org.apache.commons.configuration.Configuration;
import org.apache.hadoop.thirdparty.guava.common.collect.Maps;
import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.ChannelServer;
import com.inmobi.adserve.channels.server.annotations.LoggerConfiguration;
import com.inmobi.adserve.channels.server.annotations.ServerConfiguration;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.requesthandler.AsyncRequestMaker;
import com.inmobi.adserve.channels.server.requesthandler.AuctionEngine;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.Logging;
import com.inmobi.adserve.channels.server.requesthandler.MatchSegments;


/**
 * @author abhishek.parwal
 * 
 */
public class ServerModule extends AbstractModule {

    private final Configuration    loggerConfiguration;
    private final RepositoryHelper repositoryHelper;
    private final Reflections      reflections;
    private final Configuration    adapterConfiguration;
    private final Configuration    serverConfiguration;

    public ServerModule(final Configuration loggerConfiguration, final Configuration adapterConfiguration,
            final Configuration serverConfiguration, final RepositoryHelper repositoryHelper) {
        this.loggerConfiguration = loggerConfiguration;
        this.adapterConfiguration = adapterConfiguration;
        this.serverConfiguration = serverConfiguration;
        this.repositoryHelper = repositoryHelper;
        this.reflections = new Reflections("com.inmobi.adserve.channels", new TypeAnnotationsScanner());
    }

    @Override
    protected void configure() {

        configureApplicationLogger();

        bind(RepositoryHelper.class).toInstance(repositoryHelper);
        bind(MatchSegments.class).asEagerSingleton();
        bind(Configuration.class).annotatedWith(ServerConfiguration.class).toInstance(serverConfiguration);
        bind(Configuration.class).annotatedWith(LoggerConfiguration.class).toInstance(loggerConfiguration);

        requestStaticInjection(AsyncRequestMaker.class);
        requestStaticInjection(ChannelSegment.class);
        requestStaticInjection(Logging.class);
        requestStaticInjection(AuctionEngine.class);

        install(new AdapterConfigModule(adapterConfiguration, ChannelServer.dataCentreName));
        install(new ChannelSegmentFilterModule());

    }

    @Singleton
    @Provides
    Map<String, Servlet> provideServletMap(final Injector injector) {

        Map<String, Servlet> pathToServletMap = Maps.newHashMap();

        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Path.class);

        for (Class<?> class1 : classes) {
            pathToServletMap.put(class1.getAnnotation(Path.class).value(), (Servlet) injector.getInstance(class1));
        }
        return pathToServletMap;
    }

    private void configureApplicationLogger() {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(lc);
        lc.reset();

        try {
            configurator.doConfigure(loggerConfiguration.getString("slf4jLoggerConf"));
        }
        catch (JoranException e) {
            throw new RuntimeException(e);
        }
    }

}
