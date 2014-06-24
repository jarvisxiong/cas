package com.inmobi.adserve.channels.server;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.inject.Inject;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.servlet.ServletRtbd;

/**
 * @author abhishek.parwal
 * 
 */
public class CasTimeoutHandler extends ChannelDuplexHandler {

	private volatile long timeoutInNanos;
	private final long timeoutInNanosForRTB;
	private final long timeoutInNanosForCAS;
	private volatile long lastReadTime;

	private volatile ScheduledFuture<?> timeout;

	@Inject
	private static Map<String, Servlet> pathToServletMap;

	/**
	 * convert milliseconds into nanoseconds
	 */
	public CasTimeoutHandler(final int timeoutMillisForRTB, final int timeoutMillisForCAS) {
		this.timeoutInNanosForRTB = TimeUnit.MILLISECONDS.toNanos(timeoutMillisForRTB);
		this.timeoutInNanosForCAS = TimeUnit.MILLISECONDS.toNanos(timeoutMillisForCAS);
	}

	@Override
	public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {

		// if rtbd we are going with timeout of 190ms
		// else if dcp we are going with timeout of 600 ms

		HttpRequest httpRequest = (HttpRequest) msg;

		QueryStringDecoder queryStringDecoder = new QueryStringDecoder(httpRequest.getUri());
		String path = queryStringDecoder.path();

		Servlet servlet = pathToServletMap.get(path);

		if (servlet instanceof ServletRtbd) {
			timeoutInNanos = timeoutInNanosForRTB;
		} else {
			timeoutInNanos = timeoutInNanosForCAS;
		}

		initialize(ctx);

		super.channelRead(ctx, msg);
	}

	private void initialize(final ChannelHandlerContext ctx) {
		lastReadTime = System.nanoTime();
		timeout = ctx.executor().schedule(new ReadTimeoutTask(ctx), timeoutInNanos, TimeUnit.NANOSECONDS);
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
			if (!ctx.channel().isOpen()) {
				return;
			}

			long currentTime = System.nanoTime();

			// if rtbd we are going with timeout of 190ms
			// else if dcp we are going with timeout of 600 ms
			long nextDelay = timeoutInNanos - (currentTime - lastReadTime);

			if (nextDelay <= 0) {

				try {
					readTimedOut(ctx);
				} catch (Throwable t) {
					ctx.fireExceptionCaught(t);
				}
			} else {
				// Read occurred before the timeout - set a new timeout with
				// shorter delay.
				timeout = ctx.executor().schedule(this, nextDelay, TimeUnit.NANOSECONDS);
			}
		}
	}
}