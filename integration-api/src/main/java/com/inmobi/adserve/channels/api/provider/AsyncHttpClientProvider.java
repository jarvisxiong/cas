package com.inmobi.adserve.channels.api.provider;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.google.inject.Inject;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.inmobi.adserve.channels.util.annotations.WorkerExecutorService;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;


/**
 * @author abhishek.parwal
 * 
 */
public class AsyncHttpClientProvider {

    private final ServerConfig    serverConfig;
    private final ExecutorService executorService;
    private AsyncHttpClient       dcpAsyncHttpClient;
    private AsyncHttpClient       rtbAsyncHttpClient;

    @Inject
    public AsyncHttpClientProvider(final ServerConfig serverConfig,
            @WorkerExecutorService final ExecutorService executorService) {
        this.serverConfig = serverConfig;
        this.executorService = executorService;
    }

    @PostConstruct
    public void setup() {
        AsyncHttpClientConfig asyncHttpClientConfig = new AsyncHttpClientConfig.Builder()
                .setRequestTimeoutInMs(serverConfig.getDcpRequestTimeoutInMillis())
                .setConnectionTimeoutInMs(serverConfig.getDcpRequestTimeoutInMillis())
                .setMaximumConnectionsTotal(serverConfig.getMaxDcpOutGoingConnections())
                .setAllowPoolingConnection(true).setExecutorService(Executors.newCachedThreadPool()).build();

        dcpAsyncHttpClient = new AsyncHttpClient(asyncHttpClientConfig);

        asyncHttpClientConfig = new AsyncHttpClientConfig.Builder()
                .setRequestTimeoutInMs(serverConfig.getRtbRequestTimeoutInMillis())
                .setConnectionTimeoutInMs(serverConfig.getRtbRequestTimeoutInMillis())
                .setMaximumConnectionsTotal(serverConfig.getMaxRtbOutGoingConnections())
                .setAllowPoolingConnection(true).setExecutorService(Executors.newCachedThreadPool()).build();

        rtbAsyncHttpClient = new AsyncHttpClient(asyncHttpClientConfig);

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

    @PreDestroy
    public void tearDown() {
        dcpAsyncHttpClient.close();
        rtbAsyncHttpClient.close();
    }

}
