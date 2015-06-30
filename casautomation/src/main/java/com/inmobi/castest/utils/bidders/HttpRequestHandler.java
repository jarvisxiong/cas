package com.inmobi.castest.utils.bidders;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TSimpleJSONProtocol;

import com.inmobi.castest.utils.bidders.stats.InspectorStats;
import com.inmobi.castest.utils.bidders.stats.InspectorStrings;

public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static ScheduledExecutorService executor;
    private static Random rand = new Random();
    public static AtomicLong numberOfRequestsReceived = new AtomicLong();
    public static AtomicLong numberOfRequestsResponded = new AtomicLong();

    static {
        executor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    }
    private final int waitMultiplier;
    private final int adRatio;
    private final double budget;
    private double balance;
    private boolean printOnConsole = true;

    // stats
    private static int ads = 0;
    private static int noAds = 0;
    private static int callbacks = 0;
    private static int totalRequests = 0;
    private static int stats = 0;
    private static int inValidRequest = 0;
    private static int exceptions = 0;

    private long lastReadTime;
    private ScheduledFuture<?> timeout;

    private HttpResponse response;
    private final HttpResponse noAdResponse = new DefaultHttpResponse(HTTP_1_1, NO_CONTENT);
    private static final byte empty_gif[] = {'G', 'I', 'F', '8', '9', 'a', /* header */

    /* logical screen descriptor */
    0x01, 0x00, /* logical screen width */
    0x01, 0x00, /* logical screen height */
    (byte) 0x80, /* global 1-bit color table */
    0x01, /* background color #1 */
    0x00, /* no aspect ratio */

    /* global color table */
    0x00, 0x00, 0x00, /* #0: black */
    (byte) 0xff, (byte) 0xff, (byte) 0xff, /* #1: white */

    /* graphic control extension */
    0x21, /* extension introducer */
    (byte) 0xf9, /* graphic control label */
    0x04, /* block size */
    0x01, /* transparent color is given, */
    /* no disposal specified, */
    /* user input is not expected */
    0x00, 0x00, /* delay time */
    0x01, /* transparent color #1 */
    0x00, /* block terminator */

    /* image descriptor */
    0x2c, /* image separator */
    0x00, 0x00, /* image left position */
    0x00, 0x00, /* image top position */
    0x01, 0x00, /* image width */
    0x01, 0x00, /* image height */
    0x00, /* no local color table, no interlaced */

    /* table based image data */
    0x02, /* LZW minimum code size, */
    /* must be at least 2-bit */
    0x02, /* block size */
    0x4c, 0x01, /* compressed bytes 01_001_100, 0000000_1 */
    /* 100: clear code */
    /* 001: 1 */
    /* 101: end of information code */
    0x00, /* block terminator */

    0x3B /* trailer */
    };

    private final RtbAdRequestHandler rtbAdRequestHandler;
    private final IXAdRequestHandler ixAdRequestHandler;

    public HttpRequestHandler(final int waitMultiplier, final int adRatio, final double budget,
            final boolean printOnConsole, final RtbAdRequestHandler rtbAdRequestHandler,
            final IXAdRequestHandler ixAdRequestHandler) {
        this.waitMultiplier = waitMultiplier;
        this.adRatio = adRatio;
        this.budget = budget;
        balance = budget;
        this.rtbAdRequestHandler = rtbAdRequestHandler;
        this.ixAdRequestHandler = ixAdRequestHandler;
        this.printOnConsole = printOnConsole;
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable e) throws Exception {
        printIfAllowed("exception in exception caught is " + e.toString());
        InspectorStats.incrementStatCount(e.getCause().toString());
        exceptions++;
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, final FullHttpRequest request) throws Exception {
        totalRequests++;
        numberOfRequestsReceived.getAndIncrement();
        InspectorStats.incrementStatCount(InspectorStrings.totalRequests);
        printIfAllowed("path is " + request.getUri());
        final String msgContent = request.content().toString(CharsetUtil.UTF_8);
        printIfAllowed("content is " + msgContent);
        final String urlString = request.getUri();
        final boolean isCallback = urlString.contains("callback");
        final boolean isStatRequest = urlString.contains("stat");
        final boolean isAdRequest = urlString.contains("getIXBid") || urlString.contains("getRTBDBid");
        final StringBuilder str = new StringBuilder();
        if (isStatRequest) {
            printIfAllowed("This is a stat request");
            stats++;
            str.append("TotalRequests: ").append(totalRequests).append("\n ads: ").append(ads).append("\n noads: ")
                    .append(noAds).append("\n stats: ").append(stats).append("\n callbacks: ").append(callbacks)
                    .append("\n inValidRequest: ").append(inValidRequest).append("\n budget: ").append(budget)
                    .append("\n balance: ").append(balance).append("\n exceptions: ").append(exceptions);
            final ByteBuf channelBuffer = Unpooled.wrappedBuffer(InspectorStats.getStats().getBytes(CharsetUtil.UTF_8));
            response = new DefaultFullHttpResponse(HTTP_1_1, OK, channelBuffer);
            response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, channelBuffer.readableBytes());
            printIfAllowed("Response is " + str.toString());
        } else if (isCallback) {
            printIfAllowed("This is a callBack request");
            callbacks++;
            final ByteBuf channelBuffer = Unpooled.wrappedBuffer(empty_gif);
            response = new DefaultFullHttpResponse(HTTP_1_1, OK, channelBuffer);
            final QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
            final String callbackContent = queryStringDecoder.path();
            final String json = callbackContent.split("callback/")[1];
            printIfAllowed("callback json is " + json);
            if (printOnConsole) {
                balance = balance - Double.parseDouble(json) / 1000;
            }
            printIfAllowed("Response is " + Arrays.toString(empty_gif));
        } else if (!isAdRequest) {
            printIfAllowed("This is an invalid request");
            inValidRequest++;
            str.append("InValid Request");
            final ByteBuf channelBuffer = Unpooled.wrappedBuffer(str.toString().getBytes(CharsetUtil.UTF_8));
            response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, channelBuffer.readableBytes());
            response = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST, channelBuffer);
            printIfAllowed("Response is " + str.toString());
        } else {
            printIfAllowed("This is an ad request");
            final AdRequestHandler adRequestHandler =
                    urlString.contains("IX") ? ixAdRequestHandler : rtbAdRequestHandler;
            handleAdRequest(adRequestHandler, msgContent);
        }
        //
        // if(new Random().nextInt(3) == 0){
        // waitMultiplier = 750;
        // }
        final Channel channel = ctx.channel();
        if (channel != null) {
            lastReadTime = System.currentTimeMillis();
            final long timeoutTime = waitMultiplier;
            timeout = executor.schedule(new ReadTimeoutTask(ctx), timeoutTime, TimeUnit.MILLISECONDS);
        }
    }

    private <K extends TBase, V extends TBase> void handleAdRequest(final AdRequestHandler<K, V> adRequestHandler,
            final String msgContent) {

        final K bidRequest = adRequestHandler.parseRequest(msgContent);
        final V bidResponse = getBidResponse(bidRequest, adRequestHandler, msgContent);

        final StringBuilder str = new StringBuilder();
        if (null != bidResponse) {
            ads++;
            final TSerializer serializer = new TSerializer(new TSimpleJSONProtocol.Factory());
            try {
                str.append(serializer.toString(bidResponse));
            } catch (final TException e1) {
                e1.printStackTrace();
            }
            final ByteBuf buffer = Unpooled.wrappedBuffer(str.toString().getBytes(CharsetUtil.UTF_8));
            response = new DefaultFullHttpResponse(HTTP_1_1, OK, buffer);
            response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, buffer.readableBytes());
            printIfAllowed("Response is " + bidResponse.toString());
        } else {
            noAds++;
            response = noAdResponse;
        }
    }

    private <K extends TBase, V extends TBase> V getBidResponse(final K bidRequest,
            final AdRequestHandler<K, V> adRequestHandler, final String msgContent) {
        V bidResponse = null;
        if (bidRequest == null) {
            printIfAllowed("bid request is null");
        } else if (balance < 0) {
            printIfAllowed("balance is 0");
        } else if (rand.nextInt(100) > adRatio) {
            printIfAllowed("Randomly generated number is more than adRatio, so giving noADResponse");
        } else {
            bidResponse = adRequestHandler.makeBidResponse(bidRequest);
            if (bidResponse == null) {
                printIfAllowed("bid response if null");
            }
        }
        return bidResponse;
    }

    private final class ReadTimeoutTask implements Runnable {

        private final ChannelHandlerContext ctx;

        ReadTimeoutTask(final ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void run() {
            final long latency = System.currentTimeMillis() - lastReadTime;
            InspectorStats.updateYammerTimerStats("Bidder", "timeoutValue", latency);

            if (!ctx.channel().isOpen()) {
                destroy();
                printIfAllowed("channel closed");
                return;
            }

            numberOfRequestsResponded.getAndIncrement();
            final ChannelFuture future = ctx.channel().writeAndFlush(response);
            InspectorStats.incrementStatCount(InspectorStrings.totalFills);
            printIfAllowed("Sent response");
            // future.addListener(ChannelFutureListener.CLOSE);
            destroy();
        }

        private void destroy() {
            if (timeout != null) {
                timeout.cancel(true);
                timeout = null;
            }
        }
    }

    private void printIfAllowed(final String statement) {
        // printOnConsole = true;
        if (printOnConsole) {
            System.out.println(statement);
        }
    }

}
