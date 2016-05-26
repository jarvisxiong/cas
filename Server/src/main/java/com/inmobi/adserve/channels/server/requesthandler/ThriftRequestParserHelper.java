package com.inmobi.adserve.channels.server.requesthandler;

import java.util.Arrays;
import java.util.List;

import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.fds.thrift.mapp.MappResponse;
import com.inmobi.fds.thrift.mapp.Score;
import com.inmobi.segment.impl.AdTypeEnum;

/**
 * Created by avinash.kumar on 5/25/16.
 */
public class ThriftRequestParserHelper {
    public static final byte CONFIDENT_GOOD_SCORE = 100;
    public static final byte UNKNOWN_SCORE = 90;
    public static final byte MAYBE_BAD_SCORE = 40;
    public static final byte CONFIDENT_BAD_SCORE = 10;

    public static final String DEFAULT_PUB_CONTROL_MEDIA_PREFERENCES =
            "{\"incentiveJSON\": \"{}\",\"video\" :{\"preBuffer\": \"WIFI\",\"skippable\": true,\"soundOn\": false}}";
    public static final List<AdTypeEnum> DEFAULT_PUB_CONTROL_SUPPORTED_AD_TYPES =
            Arrays.asList(AdTypeEnum.BANNER, AdTypeEnum.VIDEO);

    public static void populateNappScore(final SASRequestParameters params,final MappResponse mappResponse) {
        if (null != mappResponse) {
            final Score nappScore = mappResponse.getEffectiveScore();
            if (null != nappScore) {
                switch (nappScore.getScore()) {
                    case CONFIDENT_GOOD_SCORE:
                        params.setNappScore(SASRequestParameters.NappScore.CONFIDENT_GOOD_SCORE);
                        InspectorStats.incrementStatCount(
                                InspectorStrings.TOTAL_REQUEST_FOR_NAPP_SCORE + SASRequestParameters.NappScore.CONFIDENT_GOOD_SCORE
                                        .getValue());
                        break;
                    case UNKNOWN_SCORE:
                        params.setNappScore(SASRequestParameters.NappScore.UNKNOWN_SCORE);
                        InspectorStats.incrementStatCount(
                                InspectorStrings.TOTAL_REQUEST_FOR_NAPP_SCORE + SASRequestParameters.NappScore.UNKNOWN_SCORE.getValue());
                        break;
                    case MAYBE_BAD_SCORE:
                        params.setNappScore(SASRequestParameters.NappScore.MAYBE_BAD_SCORE);
                        InspectorStats.incrementStatCount(
                                InspectorStrings.TOTAL_REQUEST_FOR_NAPP_SCORE + SASRequestParameters.NappScore.MAYBE_BAD_SCORE.getValue());
                        break;
                    case CONFIDENT_BAD_SCORE:
                        params.setNappScore(SASRequestParameters.NappScore.CONFIDENT_BAD_SCORE);
                        InspectorStats.incrementStatCount(
                                InspectorStrings.TOTAL_REQUEST_FOR_NAPP_SCORE + SASRequestParameters.NappScore.CONFIDENT_BAD_SCORE
                                        .getValue());
                        break;
                    default:
                        params.setNappScore(SASRequestParameters.NappScore.CONFIDENT_GOOD_SCORE);
                        InspectorStats.incrementStatCount(InspectorStrings.TOTAL_REQUEST_FOR_UNKNOWN_NAPP_SCORE);
                        break;
                }
            } else {
                params.setNappScore(SASRequestParameters.NappScore.CONFIDENT_GOOD_SCORE);
                InspectorStats.incrementStatCount(InspectorStrings.TOTAL_REQUEST_WITHOUT_NAPP_SCORE);
            }
        } else {
            params.setNappScore(SASRequestParameters.NappScore.CONFIDENT_GOOD_SCORE);
            InspectorStats.incrementStatCount(InspectorStrings.TOTAL_REQUEST_WITHOUT_MAPPRESPONSE);
        }
    }
}
