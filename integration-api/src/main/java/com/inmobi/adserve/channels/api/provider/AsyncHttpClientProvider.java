package com.inmobi.adserve.channels.api.provider;

import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.http.client.HttpClient;
import org.jboss.netty.bootstrap.ClientBootstrap;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.inmobi.adserve.channels.util.annotations.WorkerExecutorService;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.AsyncHttpProvider;
import com.ning.http.client.extra.ThrottleRequestFilter;
import com.ning.http.client.providers.netty.NettyAsyncHttpProvider;
import com.ning.http.client.providers.netty.NettyAsyncHttpProviderConfig;


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
		AsyncHttpClientConfig.Builder cfRtbd = new AsyncHttpClientConfig.Builder()
		.setRequestTimeoutInMs(
				serverConfig.getRtbRequestTimeoutInMillis())
		.setConnectionTimeoutInMs(
				serverConfig.getRtbRequestTimeoutInMillis())
		.setMaximumConnectionsTotal(
				serverConfig.getMaxRtbOutGoingConnections() * Runtime.getRuntime().availableProcessors())
		.setFollowRedirects(false)
		.setMaxRequestRetry(0)
		.setAllowPoolingConnection(true)
		.setExecutorService(Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("outbound-rtbd-client-%d").build()));
		rtbAsyncHttpClient = new AsyncHttpClient(cfRtbd.build());

		AsyncHttpClientConfig.Builder cfDcp = new AsyncHttpClientConfig.Builder()
				.setRequestTimeoutInMs(
						serverConfig.getDcpRequestTimeoutInMillis())
				.setConnectionTimeoutInMs(
						serverConfig.getDcpRequestTimeoutInMillis())
				.setMaximumConnectionsTotal(
						serverConfig.getMaxDcpOutGoingConnections() * Runtime.getRuntime().availableProcessors())
				.setFollowRedirects(false)
				.setMaxRequestRetry(0)
				.setAllowPoolingConnection(true)
				.setExecutorService(Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("outbound-dcpbackfill-client-%d").build()));
		dcpAsyncHttpClient = new AsyncHttpClient(cfDcp.build());

		
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
