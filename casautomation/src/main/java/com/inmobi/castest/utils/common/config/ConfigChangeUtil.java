package com.inmobi.castest.utils.common.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.inmobi.castest.utils.common.CasServerDetails;

/**
 * Created by ankit.kumar on 18/05/15.
 */
public class ConfigChangeUtil {

    public CasServerDetails server = new CasServerDetails();
    public String CasServer = server.getCasServerEndPoint();
    private final BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));

    private String getJsonKey() throws IOException {

        System.out.println("Enter JsonKey as <<< adapter.ix.bidFloorPercent  >>> :  ");
        final String temp = bufferRead.readLine();
        return temp;

    }

    public void checkChangeConfFlag(final Map<String, String> testParams) throws InterruptedException {
        System.out.println();
        System.out.println("#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#");
        System.out.println("Inside Change Config Util ");

        if (testParams.containsKey("enable_change_conf")) {
            final String confFlag = testParams.get("enable_change_conf");
            String changeConfVal;
            String changeConfKey;
            System.out.println();
            System.out.println("*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*");
            System.out.println("Conf for Change config is : " + confFlag);

            if (confFlag.equalsIgnoreCase("true")) {
                if (testParams.containsKey("change_config_key")) {
                    changeConfKey = testParams.get("change_config_key");
                    System.out.println("Key value for adapter is  : " + changeConfKey);
                } else {
                    System.out.println("Adapter key for config change not found!!");
                    System.out.println("No changes to the config have been made. Exiting ChangeConf Util ");
                    return;
                }

                if (testParams.containsKey("change_config_val")) {
                    changeConfVal = testParams.get("change_config_val");
                    System.out.println("Key value for adapter is  : " + changeConfVal);

                } else {
                    System.out.println("New Adapter value for config change not found!!");
                    System.out.println("No changes to the config have been made. Exiting ChangeConf Util");
                    return;
                }

                changeConf(CasServer, changeConfKey, changeConfVal);
                Thread.sleep(2000);
            }

            System.out.println("*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*_*");
            System.out.println();
            System.out.println("#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#");
        } else {
            System.out.println("Change config utility not required for this test case");
            System.out.println("#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#_#");
            System.out.println();
        }
    }

    public String changeConf(final String url, final String Key, final String newVal) {
        // http://10.14.118.66:8800/configChange?update={"adapter.ifcUMPTest20.status"="on"}
        final String urlParams = "configChange?update={%22" + Key + "%22=%22" + newVal + "%22}";
        final String req = url + urlParams;

        System.out.println("Request is : " + req);

        try {
            String result = IOUtils.toString(new URL(req));

            System.out.println(result.length());
            if (result.length() <= 61) {
                result = "Could not make the required changes! Re-Check the requested JSON.";
            }
            System.out.println(result);
            return result;
        } catch (final Exception e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    public static void main(final String[] args) throws IOException {

        final ConfigChangeUtil obj = new ConfigChangeUtil();
        obj.CasServer = "http://10.14.118.66:8800/";

        final String changeJSONKey = obj.getJsonKey();
        System.out.println("Enter value for Key '" + changeJSONKey + "' :  ");
        final String changeJSONVal = obj.bufferRead.readLine();

        final String res = obj.changeConf(obj.CasServer, changeJSONKey, changeJSONVal);
        System.out.println(res);
    }
}
