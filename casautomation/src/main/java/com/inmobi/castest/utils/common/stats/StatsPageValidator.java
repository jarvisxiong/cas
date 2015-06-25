package com.inmobi.castest.utils.common.stats;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.inmobi.castest.utils.common.CasServerDetails;

/**
 * Created by ankit.kumar on 13/05/15.
 */

public class StatsPageValidator {

    public CasServerDetails server = new CasServerDetails();
    String CasServer = server.getCasServerEndPoint();
    private final BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));

    private String getJsonKey() throws IOException {
        System.out.println("Enter JsonKey as <<< adapter.ix.bidFloorPercent  >>> :  ");
        final String temp = bufferRead.readLine();
        return temp;
    }

    public String getStatPageDetails(final String CasServer) {
        try {
            final String rawStats = IOUtils.toString(new URL(CasServer));
            return rawStats;

        } catch (final Exception e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    public void printJsonKeyValues(final String stats, final String jsonKey) {
        try {
            JSONObject statsJsonObject = new JSONObject(stats);
            final String jsonKeys[] = jsonKey.split("\\.");

            for (int i = 0; i < jsonKeys.length; i++) {
                if (i == jsonKeys.length - 1) {
                    System.out.println(jsonKey + " : " + statsJsonObject.get(jsonKeys[i].trim()));
                } else {
                    statsJsonObject = statsJsonObject.getJSONObject(jsonKeys[i].trim());
                }
            }
        } catch (final JSONException e) {
            System.err.println();
            e.printStackTrace();
        }
    }

    public void nullPointerChecker(final String stats) {
        final Pattern re =
                Pattern.compile("(?:,|\\{)?([^:]*):(\"[^\"]*\"|\\{[^}]*\\}|[^},]*)", Pattern.CASE_INSENSITIVE
                        | Pattern.MULTILINE | Pattern.DOTALL);
        final Pattern im = Pattern.compile("[a-zA-Z]..stats\\:");
        final Matcher m = im.matcher(stats);
        System.out.println(m.groupCount());
        int i = 0;
        while (m.find()) {

            System.out.println("Found value: " + m.group(i));
            i++;
            // System.out.println("Found value: " + m.group(2) );
        }

    }

    public static void main(final String[] args) throws IOException {
        final StatsPageValidator obj = new StatsPageValidator();

        final String jsonKey = obj.getJsonKey();
        final String stats = obj.getStatPageDetails("http://cas1001.ads.uj1.inmobi.com:8800/stat");
        // System.out.println(stats);

        obj.printJsonKeyValues(stats, jsonKey);
        // obj.nullPointerChecker(stats);

    }

}
