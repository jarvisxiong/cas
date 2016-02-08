package com.inmobi.adserve.channels.api.trackers;

import static com.inmobi.adserve.channels.util.Utils.TestUtils.SampleStrings.impressionId;
import static org.apache.commons.codec.binary.Base64.decodeBase64;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;

import com.inmobi.types.eventserver.ImpressionInfo;

/**
 * Created by ishan.bhatnagar on 25/09/15.
 */
public final class DefaultLazyInmobiAdTrackerUtils {
    public static ImpressionInfo extractImpressionInfo(final String beacon) {
        final Pattern impInfoPattern = Pattern.compile("([^/]+)/[01]/[a-f0-9]+$");
        final Matcher m = impInfoPattern.matcher(beacon);
        String impressionInfoSegment = null;

        int count = 0;
        while (m.find()) {
            if (count > 0) {
                return null;
            }
            ++count;
            impressionInfoSegment = m.group(1);
        }

        if (0 == count) {
            return null;
        }

        final ImpressionInfo impressionInfo = new ImpressionInfo();

        final TDeserializer deserializer = new TDeserializer(new TCompactProtocol.Factory());

        try {
            deserializer.deserialize(impressionInfo, decodeBase64(impressionInfoSegment));
        } catch (TException e) {
            e.printStackTrace();
        }
        return impressionInfo;
    }

    public static String extractSegmentId(final String beacon) {
        final Pattern impInfoPattern = Pattern.compile(impressionId + "/-1/([a-z0-9]+)/");
        final Matcher m = impInfoPattern.matcher(beacon);
        String segmentSegment = null;

        int count = 0;
        while (m.find()) {
            if (count > 0) {
                return null;
            }
            ++count;
            segmentSegment = m.group(1);
        }

        if (0 == count) {
            return null;
        }

        return segmentSegment;
    }

}
