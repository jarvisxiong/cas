package com.inmobi.adserve.channels.api;

import org.jboss.netty.channel.*;
import org.jboss.netty.util.ExternalResourceReleasable;
import org.jboss.netty.util.internal.ExecutorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.concurrent.Executor;

public class CustomExecutorHandler implements ChannelUpstreamHandler, ChannelDownstreamHandler, ExternalResourceReleasable {
    private static final Logger LOG = LoggerFactory.getLogger(CustomExecutorHandler.class);

    private final Executor executor;

    /**
     * Creates a new instance with the specified {@link Executor}.
     * Specify an {@link org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor} if unsure.
     */
    public CustomExecutorHandler(Executor executor) {
        if (executor == null) {
            throw new NullPointerException("executor");
        }
        this.executor = executor;
    }

    /**
     * Returns the {@link Executor} which was specified with the constructor.
     */
    public Executor getExecutor() {
        return executor;
    }

    /**
     * Shuts down the {@link Executor} which was specified with the constructor
     * and wait for its termination.
     */
    public void releaseExternalResources() {
        ExecutorUtil.terminate(getExecutor());
    }

    public void handleUpstream(
            ChannelHandlerContext context, ChannelEvent e) throws Exception {
        LOG.debug("inside handleUpstream ....");
        executor.execute(new CustomChannelEventRunnable(context, e));
    }

    public void handleDownstream(
            ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        LOG.debug("inside handledownstream ....");
        LOG.debug("Generating new requestId 3 {}", ctx.getChannel().getId());
        MDC.put("requestId", ctx.getChannel().getId().toString());
        if (e instanceof ChannelStateEvent) {
            ChannelStateEvent cse = (ChannelStateEvent) e;
            if (cse.getState() == ChannelState.INTEREST_OPS &&
                    (((Integer) cse.getValue()).intValue() & Channel.OP_READ) != 0) {

                // setReadable(true) requested
                boolean readSuspended = ctx.getAttachment() != null;
                if (readSuspended) {
                    // Drop the request silently if MemoryAwareThreadPool has
                    // set the flag.
                    e.getFuture().setSuccess();
                    return;
                }
            }
        }

        ctx.sendDownstream(e);
    }
}
