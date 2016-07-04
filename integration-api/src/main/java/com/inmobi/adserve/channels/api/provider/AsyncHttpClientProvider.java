package com.inmobi.adserve.channels.api.provider;

import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;


/**
 * @author abhishek.parwal
 *
 */
public class AsyncHttpClientProvider {
    private final ServerConfig serverConfig;
    private AsyncHttpClient dcpAsyncHttpClient;
    private AsyncHttpClient rtbAsyncHttpClient;
    private AsyncHttpClient photonAsyncHttpClient;

    @Inject
    public AsyncHttpClientProvider(final ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    @PostConstruct
    public void setup() {
        final AsyncHttpClientConfig.Builder cfRtbd =
                new AsyncHttpClientConfig.Builder()
                        .setRequestTimeoutInMs(serverConfig.getNingTimeoutInMillisForRTB())
                        .setConnectionTimeoutInMs(serverConfig.getNingTimeoutInMillisForRTB())
                        .setMaximumConnectionsTotal(
                                serverConfig.getMaxRtbOutGoingConnections()
                                        * Runtime.getRuntime().availableProcessors())
                        .setFollowRedirects(false)
                        .setMaxRequestRetry(0)
                        .setAllowPoolingConnection(true)
                        .setExecutorService(
                                Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat(
                                        "outbound-rtbd-client-%d").build()));
        rtbAsyncHttpClient = new AsyncHttpClient(cfRtbd.build());

        final AsyncHttpClientConfig.Builder cfDcp =
                new AsyncHttpClientConfig.Builder()
                        .setRequestTimeoutInMs(serverConfig.getNingTimeoutInMillisForDCP())
                        .setConnectionTimeoutInMs(serverConfig.getNingTimeoutInMillisForDCP())
                        .setMaximumConnectionsTotal(
                                serverConfig.getMaxDcpOutGoingConnections()
                                        * Runtime.getRuntime().availableProcessors())
                        .setFollowRedirects(false)
                        .setMaxRequestRetry(0)
                        .setAllowPoolingConnection(true)
                        .setExecutorService(
                                Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat(
                                        "outbound-dcpbackfill-client-%d").build()));
        dcpAsyncHttpClient = new AsyncHttpClient(cfDcp.build());

        final AsyncHttpClientConfig.Builder cfPhoton =
                new AsyncHttpClientConfig.Builder()
                        .setRequestTimeoutInMs(serverConfig.getNingTimeoutInMillisForPhoton())
                        .setConnectionTimeoutInMs(serverConfig.getNingTimeoutInMillisForPhoton())
                        .setMaximumConnectionsTotal(
                                serverConfig.getMaxPhotonOutGoingConnections()
                                        * Runtime.getRuntime().availableProcessors())
                        .setFollowRedirects(false)
                        .setMaxRequestRetry(0)
                        .setAllowPoolingConnection(true)
                        .setExecutorService(
                                Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat(
                                        "outbound-photon-client-%d").build()));
        photonAsyncHttpClient = new AsyncHttpClient(cfPhoton.build());
    }

    /**
     * @return the dcpAsyncHttpClient
     */
    public AsyncHttpClient getDcpAsyncHttpClient() {
        return dcpAsyncHttpClient;
    }

    /**
     * @return the rtbAsyncHttpClient
     */
    public AsyncHttpClient getRtbAsyncHttpClient() {
        return rtbAsyncHttpClient;
    }

    /**
     * @return the photonAsyncHttpClient
     */
    public AsyncHttpClient getPhotonAsyncHttpClient() {
        return photonAsyncHttpClient;
    }

    @PreDestroy
    public void tearDown() {
        dcpAsyncHttpClient.close();
        rtbAsyncHttpClient.close();
    }

}
