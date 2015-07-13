package com.inmobi.adserve.channels.adnetworks.taboola;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.inmobi.adserve.channels.api.AbstractDCPAdNetworkImpl;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.NativeResponseMaker;
import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity;
import com.inmobi.adserve.channels.entity.WapSiteUACEntity;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.contracts.misc.contentjson.CommonAssetAttributes;
import com.inmobi.adserve.contracts.misc.contentjson.Dimension;
import com.inmobi.adserve.contracts.misc.contentjson.ImageAsset;
import com.inmobi.adserve.contracts.misc.contentjson.NativeAdContentAsset;
import com.inmobi.adserve.contracts.misc.contentjson.NativeContentJsonObject;
import com.inmobi.template.context.App;
import com.inmobi.template.context.Icon;
import com.inmobi.template.context.Screenshot;
import com.inmobi.template.interfaces.TemplateConfiguration;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Created by thushara.v on 25/05/15.
 */
public class DCPTaboolaAdnetwork extends AbstractDCPAdNetworkImpl {
    private static final Logger LOG = LoggerFactory.getLogger(DCPTaboolaAdnetwork.class);

    private static final String APP_NAME = "app.name";
    private static final String THUMBNAIL_WIDTH = "rec.thumbnail.width";
    private static final String THUMBNAIL_HEIGHT = "rec.thumbnail.height";
    private static final String SOURCE_ID = "source.id";
    private static final String SOURCE_URL = "source.url";
    private static final String SOURCE_PLACEMENT = "source.placement";
    private static final String USER_ID = "user.id";
    private static final String USER_REFERRER = "user.referrer";
    private static final String USER_AGENT = "user.agent";
    private static final String USER_IP = "user.realip";
    private static final String READ_MORE = "Read More";

    @Inject
    protected static TemplateConfiguration templateConfiguration;

    private String iconUrl;
    private String notificationUrl;
    private int thumbnailWidth = 0;
    private int thumbnailHeight = 0;
    private WapSiteUACEntity wapSiteUACEntity;
    private boolean isScreenshotResponse = false;

    private NativeAdTemplateEntity templateEntity;
    protected final Gson gson;


    @Inject
    private static NativeResponseMaker nativeResponseMaker;

    public DCPTaboolaAdnetwork(final Configuration config, final Bootstrap clientBootstrap, final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
        gson = templateConfiguration.getGsonManager().getGsonInstance();

    }

    @Override
    protected boolean configureParameters() {
        if (StringUtils.isBlank(sasParams.getRemoteHostIp()) || StringUtils.isBlank(sasParams.getUserAgent())
                || StringUtils.isBlank(externalSiteId)) {
            LOG.debug("mandatory parameters missing for tabooladcp so exiting adapter");
            LOG.info("Configure parameters inside tabooladcp returned false");
            return false;
        }
        host = String.format(config.getString("taboola.host"), externalSiteId);
        notificationUrl = config.getString("taboola.notification");

        if (sasParams.getWapSiteUACEntity() != null
                && sasParams.getWapSiteUACEntity().isTransparencyEnabled() == true) {
            wapSiteUACEntity = sasParams.getWapSiteUACEntity();
        } else {
            LOG.info("Uac is not initialized for site {} in Taboola", sasParams.getSiteId());
            return false;
        }
        templateEntity = repositoryHelper.queryNativeAdTemplateRepository(sasParams.getPlacementId());
        if (templateEntity == null) {
            LOG.error("No template is available for PlacementId {}", sasParams.getPlacementId());
            return false;
        }
        return buildImageAssets();
    }

    @Override
    public URI getRequestUri() throws Exception {
        StringBuilder requestBuilder = new StringBuilder(host);

        if (StringUtils.isNotEmpty(wapSiteUACEntity.getAppTitle())) {
            appendQueryParam(requestBuilder, APP_NAME, getURLEncode(wapSiteUACEntity.getAppTitle(), format), false);
        } else if (StringUtils.isNotEmpty(wapSiteUACEntity.getSiteName())) {
            appendQueryParam(requestBuilder, APP_NAME, getURLEncode(wapSiteUACEntity.getSiteName(), format), false);
        }
        if (StringUtils.isNotEmpty(wapSiteUACEntity.getBundleId())) {
            appendQueryParam(requestBuilder, SOURCE_ID, wapSiteUACEntity.getBundleId(), false);
        }
        appendQueryParam(requestBuilder, SOURCE_PLACEMENT, blindedSiteId, false);
        if (StringUtils.isNotEmpty(wapSiteUACEntity.getSiteUrl())) {
            appendQueryParam(requestBuilder, SOURCE_URL, wapSiteUACEntity.getSiteUrl(), false);
        }
        appendQueryParam(requestBuilder, USER_IP, sasParams.getRemoteHostIp(), false);
        appendQueryParam(requestBuilder, USER_AGENT, getURLEncode(sasParams.getUserAgent(), format), false);
        String referralUrl = sasParams.getReferralUrl();
        if (StringUtils.isNotEmpty(referralUrl)) {
            appendQueryParam(requestBuilder, USER_REFERRER, referralUrl, false);
        }
        appendQueryParam(requestBuilder, THUMBNAIL_HEIGHT, thumbnailHeight, false);
        appendQueryParam(requestBuilder, THUMBNAIL_WIDTH, thumbnailWidth, false);
        String udid = getUid(true);
        if (udid != null) {
            appendQueryParam(requestBuilder, USER_ID, udid, false);
        }
        LOG.debug("Taboola AD request url {}", requestBuilder);
        return new URI(requestBuilder.toString());
    }

    @Override
    public void parseResponse(final String response, final HttpResponseStatus status) {
        adStatus = NO_AD;
        LOG.debug(traceMarker, "response is {}", response);
        if (status.code() != 200 || StringUtils.isBlank(response)) {
            statusCode = status.code();
            if (200 == statusCode) {
                statusCode = 500;
            }
            responseContent = DEFAULT_EMPTY_STRING;
            return;
        } else {
            statusCode = status.code();
            nativeAdBuilding(response);

        }
    }

    protected void nativeAdBuilding(String response) {
        InspectorStats.incrementStatCount(getName(), InspectorStrings.TOTAL_NATIVE_RESPONSES);
        try {
            final Map<String, String> params = new HashMap<String, String>();
            buildInmobiAdTracker();
            TaboolaResponse taboolaResponse = gson.fromJson(response, TaboolaResponse.class);
            if (taboolaResponse.getList().length > 0) {
                String nurl = String.format(notificationUrl,externalSiteId, taboolaResponse.getId());
                updateNativeParams(params, nurl);
                App.Builder appBuilder = App.newBuilder();
                NativeJson taboolaNative = taboolaResponse.getList()[0];
                String title = taboolaNative.getBranding();
                String description = taboolaNative.getName();
                if(null == title){
                    title = description;
                    description = taboolaNative.getDescription();
                }
                appBuilder.setTitle(title);
                appBuilder.setOpeningLandingUrl(taboolaNative.getUrl());
                appBuilder.setId(taboolaNative.getId());
                List<Icon> icons = new ArrayList<>();

                List<Screenshot> screenshotList = new ArrayList<>();

                if (isScreenshotResponse) {
                    setStaticIconForScreenshotResponse(icons);
                    updateScreenshotList(taboolaNative, screenshotList);
                } else {
                    updateIconList(taboolaNative, icons);
                }
                appBuilder.setIcons(icons);
                appBuilder.setScreenshots(screenshotList);
                appBuilder.setAdImpressionId(impressionId);
                appBuilder.setActionText(READ_MORE);
                if (null != description) {
                    appBuilder.setDesc(description);
                }
                App app = (App) appBuilder.build();
                responseContent = nativeResponseMaker.makeDCPNativeResponse(app, params,
                        repositoryHelper.queryNativeAdTemplateRepository(sasParams.getPlacementId()));
                adStatus = AD_STRING;
                LOG.debug(traceMarker, "response length is {}", responseContent.length());

            } else {
                adStatus = NO_AD;
                responseContent = DEFAULT_EMPTY_STRING;
            }
        } catch (final Exception e) {
            adStatus = NO_AD;
            responseContent = DEFAULT_EMPTY_STRING;
            LOG.error("Some exception is caught while filling the native template for placementId = {}, advertiser = {}, "
                    + "exception = {}", sasParams.getPlacementId(), getName(), e);
            InspectorStats.incrementStatCount(getName(), InspectorStrings.NATIVE_PARSE_RESPONSE_EXCEPTION);
        }
    }

    private void setStaticIconForScreenshotResponse(List<Icon> icons) {
        Icon.Builder iconBuilder = Icon.newBuilder();
        //static icon url is 300x300 size
        iconBuilder.setUrl(iconUrl);
        iconBuilder.setW(300);
        iconBuilder.setH(300);
        icons.add((Icon) iconBuilder.build());
    }

    private void updateIconList(NativeJson taboolaNative, List<Icon> icons) {
        Icon.Builder iconBuilder = Icon.newBuilder();
        for (Thumbnail image : taboolaNative.getThumbnail()) {
            iconBuilder.setH(image.getHeight());
            iconBuilder.setW(image.getWidth());
            iconBuilder.setUrl(image.getUrl());
            icons.add((Icon) iconBuilder.build());
        }
    }

    private void updateScreenshotList(NativeJson taboolaNative, List<Screenshot> screenshotList) {
        for (Thumbnail image : taboolaNative.getThumbnail()) {
            Screenshot.Builder builder = Screenshot.newBuilder();
            builder.setH(image.getHeight());
            builder.setW(image.getWidth());
            builder.setUrl(image.getUrl());
            screenshotList.add((Screenshot) builder.build());
        }
    }

    private void updateNativeParams(Map<String, String> params, String nurl) {
        params.put("beaconUrl", getBeaconUrl());
        params.put("impressionId", impressionId);
        params.put("placementId", String.valueOf(sasParams.getPlacementId()));
        params.put("nUrl", nurl);
    }

    @Override
    public String getId() {
        return config.getString("taboola.advertiserId");
    }

    @Override
    public String getName() {
        return "taboolaDCP";
    }

    private boolean buildImageAssets() {
        if (LOG.isDebugEnabled()) {
            LOG.debug(templateEntity.toString());
        }
        NativeContentJsonObject nativeContentObject = templateEntity.getContentJson();
        if(nativeContentObject == null) {
            LOG.info("Content json is empty for placement Id {}", sasParams.getPlacementId());
            return false;
        }
        for (ImageAsset imageAsset : nativeContentObject.getImageAssets()) {
            CommonAssetAttributes attributes = imageAsset.getCommonAttributes();
            Dimension dimensions = imageAsset.getDimension();
            thumbnailHeight = dimensions.getHeight();
            thumbnailWidth = dimensions.getWidth();

            if (attributes.getAdContentAsset() == NativeAdContentAsset.SCREENSHOT) {
                isScreenshotResponse = true;
                break;
            }
        }
        return true;
    }
}
