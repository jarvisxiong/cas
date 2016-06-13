package com.inmobi.adserve.channels.adnetworks.taboola;

import static com.inmobi.adserve.channels.entity.NativeAdTemplateEntity.TemplateClass.STATIC;

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
import com.inmobi.adserve.channels.api.trackers.DefaultLazyInmobiAdTrackerBuilder;
import com.inmobi.adserve.channels.api.trackers.InmobiAdTrackerBuilder;
import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity;
import com.inmobi.adserve.channels.entity.WapSiteUACEntity;
import com.inmobi.adserve.channels.repository.NativeConstraints;
import com.inmobi.adserve.channels.repository.NativeConstraints.Mandatory;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.VelocityTemplateFieldConstants;
import com.inmobi.adserve.contracts.common.request.nativead.Image;
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
    private static final String AD_COUNT = "rec.count";
    private static final String READ_MORE = "Read More";
    private static final int ICON_DEFAULT_DIMENSION = 150;

    private String iconUrl;
    private String notificationUrl;
    private int thumbnailWidth = 0;
    private int thumbnailHeight = 0;
    private WapSiteUACEntity wapSiteUACEntity;
    private boolean isScreenshotResponse = false;
    private NativeAdTemplateEntity templateEntity;
    private final List<String> oemSiteIds;
    private boolean isOemSite;

    protected final Gson gson;
    @Inject
    protected static TemplateConfiguration templateConfiguration;

    @Inject
    private static NativeResponseMaker nativeResponseMaker;

    @SuppressWarnings("unchecked")
    public DCPTaboolaAdnetwork(final Configuration config, final Bootstrap clientBootstrap,
            final HttpRequestHandlerBase baseRequestHandler, final Channel serverChannel) {
        super(config, clientBootstrap, baseRequestHandler, serverChannel);
        gson = templateConfiguration.getGsonManager().getGsonInstance();
        oemSiteIds = config.getList("taboola.oemSites");
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
        iconUrl = config.getString("taboola.icon");
        notificationUrl = config.getString("taboola.notification");
        isOemSite = oemSiteIds.contains(sasParams.getSiteId());
        if (sasParams.getWapSiteUACEntity() != null
                && (sasParams.getWapSiteUACEntity().isTransparencyEnabled() || isOemSite)) {
            wapSiteUACEntity = sasParams.getWapSiteUACEntity();
        } else {
            LOG.info("Uac is not initialized for site {} in Taboola", sasParams.getSiteId());
            return false;
        }
        templateEntity = repositoryHelper.queryNativeAdTemplateRepository(sasParams.getPlacementId(), STATIC);
        if (templateEntity == null) {
            LOG.error("No template is available for PlacementId {}", sasParams.getPlacementId());
            return false;
        }
        return buildImageAssets();
    }

    @Override
    public URI getRequestUri() throws Exception {
        final StringBuilder requestBuilder = new StringBuilder(host);
        appendQueryParam(requestBuilder, AD_COUNT,
                isOemSite ? sasParams.getRqMkAdcount() : 1, false);
        if (StringUtils.isNotEmpty(wapSiteUACEntity.getAppTitle())) {
            appendQueryParam(requestBuilder, APP_NAME, getURLEncode(wapSiteUACEntity.getAppTitle(), format), false);
        } else if (StringUtils.isNotEmpty(wapSiteUACEntity.getSiteName())) {
            appendQueryParam(requestBuilder, APP_NAME, getURLEncode(wapSiteUACEntity.getSiteName(), format), false);
        }

        // TODO: IX/RTBD pass the market id in the bundle id field, investigate why
        if (StringUtils.isNotEmpty(wapSiteUACEntity.getBundleId())) {
            appendQueryParam(requestBuilder, SOURCE_ID, wapSiteUACEntity.getBundleId(), false);
        }
        appendQueryParam(requestBuilder, SOURCE_PLACEMENT, blindedSiteId, false);
        if (StringUtils.isNotEmpty(wapSiteUACEntity.getSiteUrl())) {
            appendQueryParam(requestBuilder, SOURCE_URL, wapSiteUACEntity.getSiteUrl(), false);
        }
        appendQueryParam(requestBuilder, USER_IP, sasParams.getRemoteHostIp(), false);
        appendQueryParam(requestBuilder, USER_AGENT, getURLEncode(sasParams.getUserAgent(), format), false);
        final String referralUrl = sasParams.getReferralUrl();
        if (StringUtils.isNotEmpty(referralUrl)) {
            appendQueryParam(requestBuilder, USER_REFERRER, referralUrl, false);
        }
        appendQueryParam(requestBuilder, THUMBNAIL_HEIGHT, thumbnailHeight, false);
        appendQueryParam(requestBuilder, THUMBNAIL_WIDTH, thumbnailWidth, false);
        final String udid = getUid(true);
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

    protected void nativeAdBuilding(final String response) {
        InspectorStats.incrementStatCount(getName(), InspectorStrings.TOTAL_NATIVE_RESPONSES);
        try {
            final Map<String, String> params = new HashMap<>();
            buildInmobiAdTracker();
            final String beacon = getBeaconUrl();
            final TaboolaResponse taboolaResponse = gson.fromJson(response, TaboolaResponse.class);
            if (taboolaResponse.getList().length > 0) {
                StringBuilder responseBuilder = new StringBuilder();
                final String nurl = String.format(notificationUrl, externalSiteId, taboolaResponse.getId(),taboolaResponse.getSession());
                for (NativeJson taboolaNative : taboolaResponse.getList()) {
                    final App.Builder appBuilder = App.newBuilder();
                    updateNativeParams(params, nurl, beacon);
                    String title = taboolaNative.getBranding();
                    String description = taboolaNative.getName();
                    if (null == title) {
                        title = description;
                        description = taboolaNative.getDescription();
                    }
                    appBuilder.setTitle(title);
                    appBuilder.setOpeningLandingUrl(taboolaNative.getUrl());
                    appBuilder.setId(taboolaNative.getId());
                    final List<Icon> icons = new ArrayList<>();

                    final List<Screenshot> screenshotList = new ArrayList<>();
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
                    final List<String> pixelUrls = new ArrayList<>();
                    pixelUrls.add(beacon);
                    appBuilder.setPixelUrls(pixelUrls);
                    final App app = (App) appBuilder.build();
                    responseBuilder
                            .append(nativeResponseMaker.makeDCPNativeResponse(app, params, sasParams.isNoJsTracking()))
                            .append(",");
                }
                responseContent = responseBuilder.substring(0, responseBuilder.length() - 1);
                adStatus = AD_STRING;
                LOG.debug(traceMarker, "response length is {}", responseContent.length());

            } else {
                adStatus = NO_AD;
                responseContent = DEFAULT_EMPTY_STRING;
            }
        } catch (final Exception exp) {
            adStatus = NO_AD;
            responseContent = DEFAULT_EMPTY_STRING;
            final String msg = String.format("Exception with native template for placementId = %s , advertiser = %s",
                    sasParams.getPlacementId(), getName());
            LOG.error(msg, exp);
            InspectorStats.incrementStatCount(getName(), InspectorStrings.NATIVE_VM_TEMPLATE_ERROR);
        }
    }

    @Override
    protected void overrideInmobiAdTracker(final InmobiAdTrackerBuilder builder) {
        if (builder instanceof DefaultLazyInmobiAdTrackerBuilder) {
            final DefaultLazyInmobiAdTrackerBuilder trackerBuilder = (DefaultLazyInmobiAdTrackerBuilder) builder;
            if (isNativeRequest() && null != templateEntity) {
                trackerBuilder.setNativeTemplateId(templateEntity.getTemplateId());
            }
        }
    }

    private void setStaticIconForScreenshotResponse(final List<Icon> icons) {
        final Icon.Builder iconBuilder = Icon.newBuilder();
        // static icon url is 300x300 size
        iconBuilder.setUrl(iconUrl);
        iconBuilder.setW(180);
        iconBuilder.setH(180);
        icons.add((Icon) iconBuilder.build());
    }

    private void updateIconList(final NativeJson taboolaNative, final List<Icon> icons) {
        final Icon.Builder iconBuilder = Icon.newBuilder();
        for (final Thumbnail image : taboolaNative.getThumbnail()) {
            iconBuilder.setH(image.getHeight());
            iconBuilder.setW(image.getWidth());
            iconBuilder.setUrl(image.getUrl());
            icons.add((Icon) iconBuilder.build());
        }
    }

    private void updateScreenshotList(final NativeJson taboolaNative, final List<Screenshot> screenshotList) {
        for (final Thumbnail image : taboolaNative.getThumbnail()) {
            final Screenshot.Builder builder = Screenshot.newBuilder();
            builder.setH(image.getHeight());
            builder.setW(image.getWidth());
            builder.setUrl(image.getUrl());
            screenshotList.add((Screenshot) builder.build());
        }
    }

    private void updateNativeParams(final Map<String, String> params, final String nurl, final String beacon) {
        params.put(NativeResponseMaker.BEACON_URL_PARAM, beacon);
        params.put(NativeResponseMaker.TEMPLATE_ID_PARAM, String.valueOf(templateEntity.getTemplateId()));
        params.put(NativeResponseMaker.NURL_URL_PARAM, nurl);
        params.put(VelocityTemplateFieldConstants.IMAI_BASE_URL, sasParams.getImaiBaseUrl());
        params.put(NativeResponseMaker.CLICK_URL_PARAM, getClickUrl());
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
        /*final NativeContentJsonObject nativeContentObject = templateEntity.getContentJson();
        if (nativeContentObject == null) { */
        setDimensionForCustomTemplates();
        /*} else {
            for (final ImageAsset imageAsset : nativeContentObject.getImageAssets()) {
                final CommonAssetAttributes attributes = imageAsset.getCommonAttributes();
                final NativeAdContentAsset adContentAsset = attributes.getAdContentAsset();
                final Dimension dimensions = imageAsset.getDimension();
                thumbnailHeight = dimensions.getHeight();
                thumbnailWidth = dimensions.getWidth();
                if (adContentAsset == NativeAdContentAsset.SCREENSHOT) {
                    isScreenshotResponse = true;
                    break;
                }
            }
        }*/
        return true;
    }

    private void setDimensionForCustomTemplates() {
        final List<Mandatory> mandatoryKeys = NativeConstraints.getMandatoryList(templateEntity.getMandatoryKey());
        for (final Mandatory mandatory : mandatoryKeys) {
            switch (mandatory) {
                case ICON:
                    thumbnailHeight = ICON_DEFAULT_DIMENSION;
                    thumbnailWidth = ICON_DEFAULT_DIMENSION;
                    break;
                case SCREEN_SHOT:
                    final Image screen = NativeConstraints.getImage(templateEntity.getImageKey());
                    thumbnailHeight = screen.getHmin();
                    thumbnailWidth = screen.getWmin();
                    isScreenshotResponse = true;
                    break;
                default:
                    break;
            }
        }
    }
}
