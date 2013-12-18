package com.inmobi.adserve.channels.adnetworks.rtb;

import java.net.InetSocketAddress;
import java.net.URI;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.HttpRequest;

import com.inmobi.adserve.channels.util.DebugLogger;


public class ImpressionCallbackHelper {
    public boolean writeResponse(ClientBootstrap clientBootstrap, final DebugLogger logger, URI uriCallBack,
            HttpRequest callBackRequest) {
        ChannelFuture channelFuture = clientBootstrap.connect(new InetSocketAddress(uriCallBack.getHost(), uriCallBack
                .getPort() == -1 ? 80 : uriCallBack.getPort()));
        ChannelFuture futureCallBack = null;
        try {
            if (channelFuture.getChannel().isWritable()) {
                futureCallBack = channelFuture.getChannel().write(callBackRequest);
            }
        }
        catch (Exception e) {
            logger.info("Error in making callback request" + e.getMessage());
        }
        if (null == futureCallBack) {
            logger.debug("Could not make callback connection ");
            return false;
        }
        futureCallBack.addListener(new ChannelFutureListener() {

            public void operationComplete(ChannelFuture future) throws Exception {

                if (!future.isSuccess()) {
                    logger.info("error sending callback");
                    return;
                }
                logger.debug("CallBack is sent");
                return;
            }
        });
        futureCallBack.addListener(ChannelFutureListener.CLOSE);
        logger.debug("Callback channel is closed");
        return true;
    }
}
