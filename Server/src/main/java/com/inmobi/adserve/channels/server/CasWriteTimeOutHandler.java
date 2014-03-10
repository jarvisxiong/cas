package com.inmobi.adserve.channels.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.WriteTimeoutException;
import io.netty.handler.timeout.WriteTimeoutHandler;

import java.util.concurrent.TimeUnit;


/**
 * @author abhishek.parwal
 * 
 */
public class CasWriteTimeOutHandler extends WriteTimeoutHandler {

    /**
     * @param timeoutSeconds
     */
    public CasWriteTimeOutHandler(final int timeoutSeconds) {
        super(timeoutSeconds);
    }

    /**
     * @param timeout
     * @param unit
     */
    public CasWriteTimeOutHandler(final long timeout, final TimeUnit unit) {
        super(timeout, unit);
    }

    /**
     * overridden to not close the connection
     */
    @Override
    protected void writeTimedOut(final ChannelHandlerContext ctx) throws Exception {
        ctx.fireExceptionCaught(WriteTimeoutException.INSTANCE);
    }

}