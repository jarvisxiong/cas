package com.inmobi.adserve.channels.server;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.concurrent.TimeUnit;


/**
 * @author abhishek.parwal
 * 
 */
public class CasTimeoutHandler extends ChannelDuplexHandler {

	private volatile long 				timeoutMillis;
    private final long                  timeoutMillisForRTB;
    private final long                  timeoutMillisForDCP;
    private volatile boolean            flag = false;
    private volatile long               lastReadTime;

    private volatile ScheduledFuture<?> timeout;

    /**
     * @param timeoutMillis
     */
    public CasTimeoutHandler(final int timeoutMillisForRTB, final int timeoutMillisForDCP) {
        this.timeoutMillisForRTB = timeoutMillisForRTB;
        this.timeoutMillisForDCP = timeoutMillisForDCP;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        flag = true;
        lastReadTime = System.currentTimeMillis();
        
        // if rtbd we are going with timeout of 190ms 
        // else if dcp we are going with timeout of 600 ms
        if(((DefaultFullHttpRequest)msg).getUri() == "/rtbdFill"){
        	timeoutMillis = timeoutMillisForRTB;
        }else{
        	timeoutMillis = timeoutMillisForDCP;
        }
        
        super.channelRead(ctx, msg);
    }

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel().isActive() && ctx.channel().isRegistered()) {
            // channelActvie() event has been fired already, which means this.channelActive() will
            // not be invoked. We have to initialize here instead.
            initialize(ctx);
        }
        else {
            // channelActive() event has not been fired yet. this.channelActive() will be invoked
            // and initialization will occur there.
        }
    }

    @Override
    public void channelRegistered(final ChannelHandlerContext ctx) throws Exception {
        // Initialize early if channel is active already.
        if (ctx.channel().isActive()) {
            initialize(ctx);
        }
        super.channelRegistered(ctx);
    }

    private void initialize(final ChannelHandlerContext ctx) {

        lastReadTime = System.currentTimeMillis();
        timeout = ctx.executor().schedule(new ReadTimeoutTask(ctx), timeoutMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) throws Exception {
        flag = false;
        super.write(ctx, msg, promise);
    }

    @Override
    public void handlerRemoved(final ChannelHandlerContext ctx) throws Exception {
        destroy();
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        destroy();
        super.channelInactive(ctx);
    }

    private void destroy() {
        if (timeout != null) {
            timeout.cancel(false);
            timeout = null;
        }
    }

    private void readTimedOut(final ChannelHandlerContext ctx) {
        flag = false;
        ctx.fireExceptionCaught(ReadTimeoutException.INSTANCE);
    }

    private final class ReadTimeoutTask implements Runnable {

        private final ChannelHandlerContext ctx;

        ReadTimeoutTask(final ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run() {
            if (!ctx.channel().isOpen()) {
                return;
            }
            
            long currentTime = System.currentTimeMillis();
            
            // if rtbd we are going with timeout of 190ms 
            // else if dcp we are going with timeout of 600 ms
            
            long nextDelay = timeoutMillis - (currentTime - lastReadTime);
            
            if (nextDelay <= 0) {
                // Read timed out - set a new timeout and notify the callback.
                timeout = ctx.executor().schedule(this, timeoutMillis, TimeUnit.MILLISECONDS);
                try {
                    // Don't send unnecessary timeout
                    if (flag) {
                        readTimedOut(ctx);
                    }
                }
                catch (Throwable t) {
                    ctx.fireExceptionCaught(t);
                }
            }
            else {
                // Read occurred before the timeout - set a new timeout with shorter delay.
                timeout = ctx.executor().schedule(this, nextDelay, TimeUnit.MILLISECONDS);
            }
        }

    }
}