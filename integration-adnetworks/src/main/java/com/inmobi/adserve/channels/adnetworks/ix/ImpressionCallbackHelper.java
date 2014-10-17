package com.inmobi.adserve.channels.adnetworks.ix;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Request;


public class ImpressionCallbackHelper {
  private final static Logger LOG = LoggerFactory.getLogger(ImpressionCallbackHelper.class);

  public boolean writeResponse(final URI uriCallBack, final Request callBackRequest,
      final AsyncHttpClient asyncHttpClient) {

    LOG.debug("In Adapter {}", this.getClass().getSimpleName());

    try {
      asyncHttpClient.executeRequest(callBackRequest);
    } catch (final Exception e) {
      LOG.debug("Exception in makeAsyncRequest : {}", e);
    }

    return true;

  }
}
