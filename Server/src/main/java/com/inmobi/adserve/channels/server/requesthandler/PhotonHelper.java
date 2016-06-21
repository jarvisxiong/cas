package com.inmobi.adserve.channels.server.requesthandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.inmobi.user.photon.datatypes.profile.UserProfileView;
import com.inmobi.user.photon.service.PhotonException;
import org.apache.http.HttpStatus;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TIOStreamTransport;

import com.google.inject.Inject;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.user.photon.datatypes.attribute.Attributes;
import com.inmobi.user.photon.datatypes.attribute.brand.BrandAttributes;
import com.inmobi.user.photon.datatypes.commons.Tenant;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import static com.inmobi.adserve.channels.util.InspectorStrings.LATENCY_FOR_NING_PHOTON_RESPONSE;
import static com.inmobi.adserve.channels.util.InspectorStrings.PHOTON;
import static com.inmobi.adserve.channels.util.InspectorStrings.TOTAL_PHOTON_ERROR_CALLBACK_FOR;
import static com.inmobi.adserve.channels.util.InspectorStrings.TOTAL_PHOTON_EXCEPTION;
import static com.inmobi.adserve.channels.util.InspectorStrings.TOTAL_PHOTON_IO_EXCEPTION;
import static com.inmobi.adserve.channels.util.InspectorStrings.TOTAL_PHOTON_REQUEST;
import static com.inmobi.adserve.channels.util.InspectorStrings.TOTAL_PHOTON_REQUEST_THRIFT_PARSE_EXCEPTION;
import static com.inmobi.adserve.channels.util.InspectorStrings.TOTAL_PHOTON_REQUEST_TIMEOUT;
import static com.inmobi.adserve.channels.util.InspectorStrings.TOTAL_PHOTON_RESPONSE_STATUS_CODE;
import static com.inmobi.adserve.channels.util.InspectorStrings.TOTAL_PHOTON_RESPONSE_THRIFT_PARSE_EXCEPTION;

/**
 * Created by avinash.kumar on 6/6/16.
 */
@Data
@Slf4j
public class PhotonHelper {
    private final static Tenant tenant = Tenant.BRAND;
    private static final String POST = "POST";
    private static final String CONTENT_TYPE_KEY = "Content-Type";
    private static final String CONTENT_TYPE_VALUE = "application/x-thrift";
    private static AsyncHttpClient asyncHttpClient;
    private static String endPoint;
    private static String headerKey;
    private static String headerValue;

    @Inject
    public PhotonHelper(final AsyncHttpClient asyncHttpClient, final String endpoint, final String headerKey,
                        final String headerValue) {
        this.asyncHttpClient = asyncHttpClient;
        this.endPoint = endpoint;
        this.headerKey = headerKey;
        this.headerValue = headerValue;
        log.debug("PhotonHelper is initializing with endpoint : {}, headerKey : {}, headerValue : {}",
                endpoint, headerKey, headerValue);
    }

    public static ListenableFuture<BrandAttributes> getBrandAttribute(final String uId) {
        log.debug("photon call for user id : {}", uId);
        ListenableFuture<BrandAttributes> brandAttributesFuture = null;
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final TIOStreamTransport transport = new TIOStreamTransport(byteArrayOutputStream);
        final TProtocol protocol = new TBinaryProtocol(transport);
        PhotonThriftParser photonThriftParser = new PhotonThriftParser(protocol);
        try {
            photonThriftParser.send(uId, tenant);
            final Request request = getNingRequestBuilder(byteArrayOutputStream);
            final long startTime = System.currentTimeMillis();
            brandAttributesFuture =
                    asyncHttpClient.executeRequest(request, getAsyncCompletionHandler(photonThriftParser, startTime));
            InspectorStats.incrementStatCount(PHOTON, TOTAL_PHOTON_REQUEST);
        } catch (IOException e) {
            InspectorStats.incrementStatCount(PHOTON, TOTAL_PHOTON_IO_EXCEPTION);
        } catch (Exception e) {
            InspectorStats.incrementStatCount(PHOTON, TOTAL_PHOTON_REQUEST_THRIFT_PARSE_EXCEPTION);
            log.debug("Exception occur while building request : {}", e);
        }
        return brandAttributesFuture;
    }

    private static Request getNingRequestBuilder(final ByteArrayOutputStream byteOutStream) {
        return new RequestBuilder(POST).setUrl(endPoint).setHeader(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE)
                .setHeader(headerKey, headerValue).setBody(byteOutStream.toByteArray()).build();
    }

    private static AsyncCompletionHandler getAsyncCompletionHandler(final PhotonThriftParser photonThriftParser,
             final long startTime) {
        return new AsyncCompletionHandler() {
            @Override
            public BrandAttributes onCompleted(Response response) throws Exception {
                final int statusCode = response.getStatusCode();
                log.debug("Photon Response status : {}, response body : {}", statusCode, response.getResponseBody());
                InspectorStats.incrementStatCount(PHOTON, TOTAL_PHOTON_RESPONSE_STATUS_CODE + statusCode);
                InspectorStats.updateYammerTimerStats(PHOTON, LATENCY_FOR_NING_PHOTON_RESPONSE,
                        (System.currentTimeMillis()-startTime));
                BrandAttributes brandAttr = null;
                switch (statusCode) {
                    case HttpStatus.SC_OK:
                        final TProtocol protocol = new TBinaryProtocol(new TIOStreamTransport(response.getResponseBodyAsStream()));
                        try {
                            final UserProfileView userProfileView = photonThriftParser.receive(protocol);
                            final Attributes attributes = (null != userProfileView) ? userProfileView.getAttributes() : null;
                            brandAttr = (null != attributes) ? attributes.getBrand() : null;
                        } catch (final PhotonException e) {
                            log.debug("Excepton sent from photon server : {}", e);
                            InspectorStats.incrementStatCount(PHOTON, TOTAL_PHOTON_EXCEPTION);
                        } catch (final Exception e) {
                            log.debug("Excepton occur while parsing response to Thrift", e);
                            InspectorStats.incrementStatCount(PHOTON,
                                TOTAL_PHOTON_RESPONSE_THRIFT_PARSE_EXCEPTION + e.getClass().getName());
                        }
                        break;
                    case HttpStatus.SC_GATEWAY_TIMEOUT:
                    case HttpStatus.SC_REQUEST_TIMEOUT:
                        InspectorStats.incrementStatCount(PHOTON, TOTAL_PHOTON_REQUEST_TIMEOUT);
                        break;
                }
                return brandAttr;
            }

            @Override
            public void onThrowable(final Throwable t) {
                InspectorStats.incrementStatCount(PHOTON, TOTAL_PHOTON_ERROR_CALLBACK_FOR + t.getClass().getName());
            }
        };
    }
}
