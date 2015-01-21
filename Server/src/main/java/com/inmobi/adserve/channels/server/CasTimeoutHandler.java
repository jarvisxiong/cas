package com.inmobi.adserve.channels.server;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.timeout.ReadTimeoutException;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.google.inject.Inject;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.servlet.ServletIXFill;
import com.inmobi.adserve.channels.server.servlet.ServletRtbd;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.casthrift.DemandSourceType;

/**
 * @author abhishek.parwal
 * @author rajashekhar.c
 */
public class CasTimeoutHandler extends ChannelDuplexHandler {

    private static ScheduledExecutorService executor;

    @Inject
    private static Map<String, Servlet> pathToServletMap;

    static {
        executor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    }

    private volatile long timeoutInMillis;
    private final long timeoutMillisForRTB;
    private final long timeoutMillisForDCP;
    private volatile long lastReadTime;
    private volatile ScheduledFuture<?> timeout;

    
    private volatile int dst;

    private DemandSourceType demandSourceType;



    public CasTimeoutHandler(final int timeoutMillisForRTB, final int timeoutMillisForDCP) {
        this.timeoutMillisForRTB = timeoutMillisForRTB;
        this.timeoutMillisForDCP = timeoutMillisForDCP;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {

        // if rtbd we are going with timeout of 175ms
        // else if dcp we are going with timeout of 600 ms

        final HttpRequest httpRequest = (HttpRequest) msg;

        final QueryStringDecoder queryStringDecoder = new QueryStringDecoder(httpRequest.getUri());
        final String path = queryStringDecoder.path();

        final Servlet servlet = pathToServletMap.get(path);

        if (servlet instanceof ServletRtbd) {
            timeoutInMillis = timeoutMillisForRTB;
            dst = DemandSourceType.RTBD.getValue();
        } else if (servlet instanceof ServletIXFill) {
            timeoutInMillis = timeoutMillisForRTB;
            dst = DemandSourceType.IX.getValue();
        } else {
            timeoutInMillis = timeoutMillisForDCP;
            dst = DemandSourceType.DCP.getValue();
        }

        demandSourceType = DemandSourceType.findByValue(dst);

        initialize(ctx);

        super.channelRead(ctx, msg);
    }

    private void initialize(final ChannelHandlerContext ctx) {
        lastReadTime = System.currentTimeMillis();
        timeout = executor.schedule(new ReadTimeoutTask(ctx), timeoutInMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) throws Exception {
        if (!ctx.channel().isOpen()) {
            return;
        }
        destroy();
        super.write(ctx, msg, promise);
    }

    private void destroy() {
        if (timeout != null) {
            timeout.cancel(true);
            timeout = null;
        }
    }

    private void readTimedOut(final ChannelHandlerContext ctx) {
        ctx.fireExceptionCaught(ReadTimeoutException.INSTANCE);
    }

    private final class ReadTimeoutTask implements Runnable {

        private final ChannelHandlerContext ctx;

        ReadTimeoutTask(final ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run() {
            final long currentTime = System.currentTimeMillis();

            // if rtbd & ix, then we are going with timeout of 170ms
            // else if dcp we are going with timeout of 600 ms
            final long latency = currentTime - lastReadTime;

            InspectorStats.updateYammerTimerStats(demandSourceType.name(),
                    InspectorStrings.CAS_TIMEOUT_HANDLER_LATENCY, latency);
            
            if (ctx.channel().isOpen() && latency >= timeoutInMillis) {
                readTimedOut(ctx);
            }
        }
    }
}
