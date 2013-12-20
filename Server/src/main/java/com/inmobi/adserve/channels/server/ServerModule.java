package com.inmobi.adserve.channels.server;

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
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.requesthandler.AsyncRequestMaker;
import com.inmobi.adserve.channels.server.requesthandler.Filters;
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

    public ServerModule(final Configuration loggerConfiguration, final Configuration adapterConfiguration,
            final RepositoryHelper repositoryHelper) {
        this.loggerConfiguration = loggerConfiguration;
        this.adapterConfiguration = adapterConfiguration;
        this.repositoryHelper = repositoryHelper;
        this.reflections = new Reflections("com.inmobi", new TypeAnnotationsScanner());
    }

    @Override
    protected void configure() {

        configureApplicationLogger();

        bind(RepositoryHelper.class).toInstance(repositoryHelper);
        bind(MatchSegments.class).asEagerSingleton();

        requestStaticInjection(AsyncRequestMaker.class);
        requestStaticInjection(Filters.class);

        install(new AdapterConfigModule(adapterConfiguration, ChannelServer.dataCentreName));

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
