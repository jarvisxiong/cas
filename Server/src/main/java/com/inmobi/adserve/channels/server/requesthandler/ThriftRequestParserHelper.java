package com.inmobi.adserve.channels.server.requesthandler;

import static com.inmobi.adserve.channels.server.CasConfigUtil.repositoryHelper;
import static com.inmobi.adserve.channels.util.InspectorStrings.TOTAL_REQUEST_FOR_NAPP_SCORE;
import static com.inmobi.adserve.channels.util.InspectorStrings.TOTAL_REQUEST_WITHOUT_MAPP_RESPONSE;
import static com.inmobi.adserve.channels.util.InspectorStrings.TOTAL_REQUEST_WITHOUT_NAPP_SCORE;
import static com.inmobi.adserve.channels.util.InspectorStrings.TOTAL_REQUEST_WITH_SCORE_GREATER_THAN_100;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.inmobi.adserve.adpool.IntegrationType;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters.NappScore;
import com.inmobi.adserve.channels.entity.SdkMraidMapEntity;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.fds.thrift.mapp.MappResponse;
import com.inmobi.fds.thrift.mapp.Score;
import com.inmobi.segment.impl.AdTypeEnum;

import com.inmobi.user.photon.datatypes.attribute.core.CoreAttributes;
import com.inmobi.user.photon.datatypes.commons.attribute.IntAttribute;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

@Slf4j
class ThriftRequestParserHelper {
    private static final short CONFIDENT_GOOD_SCORE = 100;
    private static final short UNKNOWN_SCORE = 90;
    private static final short MAYBE_BAD_SCORE = 40;
    private static final short CONFIDENT_BAD_SCORE = 10;

    static final String DEFAULT_PUB_CONTROL_MEDIA_PREFERENCES =
            "{\"incentiveJSON\": \"{}\",\"video\" :{\"preBuffer\": \"WIFI\",\"skippable\": true,\"soundOn\": false}}";
    static final List<AdTypeEnum> DEFAULT_PUB_CONTROL_SUPPORTED_AD_TYPES =
            Arrays.asList(AdTypeEnum.BANNER, AdTypeEnum.VIDEO);

    static void populateNappScore(final SASRequestParameters params, final MappResponse mappResponse) {
        if (null != mappResponse) {
            final Score effectiveScore = mappResponse.getEffectiveScore();
            if (null != effectiveScore) {
                final short nappScore = (short) effectiveScore.getScore();
                if (nappScore <= CONFIDENT_BAD_SCORE) {
                    params.setNappScore(NappScore.CONFIDENT_BAD_SCORE);
                    InspectorStats.incrementStatCount(
                            TOTAL_REQUEST_FOR_NAPP_SCORE + NappScore.CONFIDENT_BAD_SCORE.getValue());
                } else if (nappScore <= MAYBE_BAD_SCORE) {
                    params.setNappScore(NappScore.MAYBE_BAD_SCORE);
                    InspectorStats
                            .incrementStatCount(TOTAL_REQUEST_FOR_NAPP_SCORE + NappScore.MAYBE_BAD_SCORE.getValue());
                } else if (nappScore <= UNKNOWN_SCORE) {
                    params.setNappScore(NappScore.UNKNOWN_SCORE);
                    InspectorStats
                            .incrementStatCount(TOTAL_REQUEST_FOR_NAPP_SCORE + NappScore.UNKNOWN_SCORE.getValue());
                } else if (nappScore <= CONFIDENT_GOOD_SCORE) {
                    params.setNappScore(NappScore.CONFIDENT_GOOD_SCORE);
                    InspectorStats.incrementStatCount(
                            TOTAL_REQUEST_FOR_NAPP_SCORE + NappScore.CONFIDENT_GOOD_SCORE.getValue());
                } else {
                    params.setNappScore(NappScore.CONFIDENT_GOOD_SCORE);
                    InspectorStats.incrementStatCount(TOTAL_REQUEST_WITH_SCORE_GREATER_THAN_100);
                }
            } else {
                params.setNappScore(NappScore.CONFIDENT_GOOD_SCORE);
                InspectorStats.incrementStatCount(TOTAL_REQUEST_WITHOUT_NAPP_SCORE);
            }
        } else {
            params.setNappScore(NappScore.CONFIDENT_GOOD_SCORE);
            InspectorStats.incrementStatCount(TOTAL_REQUEST_WITHOUT_MAPP_RESPONSE);
        }
    }

    static String getMraidPath(final String sdkVersion) {
        final String mraidPath;

        final SdkMraidMapEntity mraidEntity = repositoryHelper.querySdkMraidMapRepository(sdkVersion);
        if (null != mraidEntity) {
            mraidPath = mraidEntity.getMraidPath();
            log.debug("Mraid path is: {}", mraidPath);
        } else {
            mraidPath = null;
            log.info("Mraid Path not found for sdk version: {}", sdkVersion);
            InspectorStats.incrementStatCount(InspectorStrings.MRAID_PATH_NOT_FOUND + sdkVersion);
        }
        return mraidPath;
    }

    static String getSdkVersion(final IntegrationType integrationType, final Integer version) {
        if (null != version) {
            if (integrationType == IntegrationType.ANDROID_SDK) {
                return "a" + version;
            } else if (integrationType == IntegrationType.IOS_SDK) {
                return "i" + version;
            }
        }
        return null;
    }

    public static void updateCsiTags(final Set<Integer> csiTags, final CoreAttributes core) {
        final Set<Integer> coreCSIIds = new HashSet<>();
        if (null != core && core.isSetCsids()) {
            final IntAttribute intAttr = core.getCsids();
            if (intAttr.isSetValueMap()) {
                intAttr.getValueMap().forEach((s,t) -> coreCSIIds.addAll(t.keySet()));
            }
        }
        csiTags.addAll(coreCSIIds);
    }
}
