package com.inmobi.adserve.channels.adnetworks.ifd;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.codehaus.plexus.util.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;


public class IFDAdNetwork extends AbstractDCPAdNetworkImpl {
    // Updates the request parameters according to the Ad Network. Returns true on
    // success.i

    private static final Logger LOG = LoggerFactory.getLogger(IFDAdNetwork.class);

    public IFDAdNetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
    }

    // Assign the value to the parameters
    public boolean configureParameters(final SASRequestParameters param, final String externalSiteKey,
            final String clurl) {
        return false;
    }

    // Assign the value to the parameters
    @Override
    public boolean configureParameters() {
        return true;
    }

    @Override
    public String getName() {
        return "ifd";
    }

    @Override
    public boolean isInternal() {
        return true;
    }

    // forming the url with the available parameter
    @Override
    public URI getRequestUri() throws Exception {
        try {
            String url = config.getString("ifd.host") + createIFDParams();
            return (new URI(url));
        } catch (URISyntaxException exception) {
            errorStatus = ThirdPartyAdResponse.ResponseStatus.MALFORMED_URL;
        }
        return null;
    }

    // Returns the Channel Id for the TPAN as in our database. This will be
    // hardcoded.
    @Override
    public String getId() {
        return config.getString("ifd.advertiserId");
    }

    // writing channel logs
    @Override
    public JSONObject getLogline() {
        return null;
    }

    public String createIFDParams() throws Exception {
        String normalizedParms = null;

        Map<String, String> paramMap = new HashMap<String, String>();

        try {
            paramMap.put("tid", sasParams.getTid());
        } catch (Exception exception) {
            throw new Exception("mandatory parameter tid value is missing");
        }

        try {
            paramMap.put("mk-ad-slot", sasParams.getSlot().toString());
        } catch (Exception exception) {
            throw new Exception("mandatory parameter mk-ad-slot value is missing");
        }

        if (StringUtils.isEmpty(sasParams.getSiteId())) {
            throw new Exception("mandatory parameter siteId value is missing");
        }
        paramMap.put("site-id", sasParams.getSiteId());

        try {
            paramMap.put("rq-h-user-agent", sasParams.getUserAgent());
        } catch (Exception exception) {
            throw new Exception("mandatory parameter rq-h-user-agent value is missing");
        }

        try {
            paramMap.put("h-id", sasParams.getHandsetInternalId() + "");
        } catch (Exception exception) {
            throw new Exception("mandatory parameter handset value is either null or not formatted properly.");
        }

        try {
            paramMap.put("cid", sasParams.getCarrierId() + "");
            paramMap.put("cnid", sasParams.getCountryCode());
            paramMap.put("cc", sasParams.getCountryId().toString());
        } catch (Exception exception) {
            throw new Exception("mandatory parameter carrier value is either null or not formatted properly.");
        }

        paramMap.put("u-id", casInternalRequestParameters.uid);

        String format = null;
        try {
            format = sasParams.getRFormat();
        } catch (Exception exception) {
            LOG.info("error in parsing format passed by network");
        }
        if (StringUtils.isEmpty(format)) {
            format = config.getString("ifd.responseFormat");
        }
        paramMap.put("format", format);

        try {
            paramMap.put("sdk-version", sasParams.getSdkVersion());
        } catch (Exception exception) {
            LOG.info("error in parsing sdk-version passed by network");
        }

        try {
            paramMap.put("u-latlong", sasParams.getLatLong());
        } catch (Exception exception) {
            LOG.info("error in parsing latlong passed by network");
        }
        normalizedParms = normalizeParameters(paramMap);

        return normalizedParms;
    }

    public static String normalizeParameters(final Map<String, String> parameterMap) {
        List<String> parameters = new ArrayList<String>();

        // parameters in the HTTP POST request body and HTTP GET parameters in the
        // query part
        for (Map.Entry<String, String> parameterMapEntry : parameterMap.entrySet()) {
            String key = parameterMapEntry.getKey();

            // the same parameter name can have multiple values
            String value = parameterMapEntry.getValue();

            // "... parameter names and values are escaped using RFC3986 percent-encoding..."
            if (value != null) {
                try {
                    parameters.add(URLEncoder.encode(key, "UTF-8") + '=' + URLEncoder.encode(value, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new IllegalStateException(e);
                }
            }
        }

        StringBuilder parameterBaseBuilder = new StringBuilder();

        // append each name-value pair, delimited with ampersand
        for (Iterator<String> parameterItr = parameters.iterator(); parameterItr.hasNext();) {
            parameterBaseBuilder.append(parameterItr.next());
            if (parameterItr.hasNext()) {
                parameterBaseBuilder.append('&');
            }
        }

        return parameterBaseBuilder.toString();
    }

    // parsing the response message to get HTTP response code and httpresponse
    public void parseResponse(final String response) {
        if (response == null || response.contains("<!-- mKhoj: No advt for this position -->")) {
            statusCode = 500;
            return;
        }
        LOG.debug("response is {}", response);
        String responseArray[] = response.split("\n");
        StringBuilder responseBuilder = new StringBuilder();
        int index = 0;
        for (index = 0; index < responseArray.length; index++) {
            if (index == 0) {
                try {
                    String responseHeader[] = responseArray[index].split(" ");
                    statusCode = Integer.parseInt(responseHeader[1].trim());
                    LOG.debug("status code is {}", statusCode);
                    if (statusCode != 200) {
                        return;
                    }
                } catch (Exception ex) {
                    errorStatus = ThirdPartyAdResponse.ResponseStatus.INVALID_RESPONSE;
                    statusCode = 500;
                    LOG.debug("status code is 500 inside parseResponse");
                    return;
                }
            } else if (responseArray[index].trim().isEmpty()) {
                // blank line indicates end of header and start of body
                LOG.debug("got a blank line to separate header and body");
                break;
            }
        }
        for (int nextIndex = index + 1; nextIndex < responseArray.length; nextIndex++) {
            responseBuilder.append(responseArray[nextIndex]);
        }
        responseContent = new String(responseBuilder.toString());
        adStatus = "AD";
        LOG.debug("response length is {}", responseContent.length());
    }
}