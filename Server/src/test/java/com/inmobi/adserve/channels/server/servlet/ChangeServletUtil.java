package com.inmobi.adserve.channels.server.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;

/**
 * @author ritwik.kumar
 */
public class ChangeServletUtil {
    private static final String URL_BASE_PATTERN = "http://cas%s.ads.%s.inmobi.com:8800/";
    private static final String URL_STATUS_PATTERN = "lbstatus";
    private static final String URL_GET_ADAPTER_PATTERN = "getAdapterConfig";
    private static final String URL_GET_SERVER_PATTERN = "getServerConfig";
    private static final String URL_GET_STATS_PATTERN = "stat";
    private static final String URL_CHANGE_PATTERN = "configChange?update=%s";
    private static final int BOX_LIMIT = 1018;

    private static final Set<String> COLO = new HashSet<>();
    private final BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
    private String colo;
    private String key;
    private String json;
    private Operation operation;

    private enum Operation {
        CHANGE_CONFIG, GET_ADAPTER_CONFIG, GET_SERVER_CONFIG, GET_STATS;
    }


    /**
     * 
     * @param args
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {
        final ChangeServletUtil util = new ChangeServletUtil();
        util.performOperation();
    }

    /**
     * 
     * @param method
     * @throws IOException
     */
    private void performOperation() throws IOException {
        System.out.println("Enter your choice : \n1 Get Adapter/Server Config \n2 ConfigChange \n3 Get stats");
        final String method = bufferRead.readLine();
        // Santization
        switch (method) {
            case "2":
                takeInput();
                operation = Operation.CHANGE_CONFIG;
                break;
            case "3":
                takeCommonInput();
                key = key.replaceAll("\\.", "#");
                operation = Operation.GET_STATS;
                break;
            case "1":
                takeCommonInput();
                if (key.startsWith("adapter.")) {
                    key = key.replaceFirst("adapter.", "");
                    operation = Operation.GET_ADAPTER_CONFIG;
                    break;
                } else if (key.startsWith("server.")) {
                    key = key.replaceFirst("server.", "");
                    operation = Operation.GET_SERVER_CONFIG;
                    break;
                } else {
                    System.err.println("Wrong key !!!");
                    break;
                }
            default:
                System.err.println("Wrong input !!!");
                break;
        }
        if (operation == null) {
            System.exit(-1);
        }


        final boolean isPilot = "PILOT".equalsIgnoreCase(colo);
        // perform operation
        for (final String curColo : COLO) {
            for (int i = 1000; i <= BOX_LIMIT; i++) {
                if (isPilot && i != 1001) {
                    continue;
                }
                try {
                    final String baseUrl = String.format(URL_BASE_PATTERN, i, curColo);
                    final String lbstatusUrl = baseUrl + URL_STATUS_PATTERN;
                    final boolean isUp = isHostUp(lbstatusUrl);
                    if (!isUp) {
                        System.out.println(String.format("%s -> is UP->%s", lbstatusUrl, isUp));
                    }
                    if (isUp) {
                        final String url = baseUrl + getActionUrl();
                        final String output = IOUtils.toString(new URL(url));
                        printResult(url, output);
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 
     * @throws IOException
     */
    private void takeCommonInput() throws IOException {
        System.out.println("Enter colo (uj1, uh1, lhr1, hkg1, all, pilot) :");
        colo = bufferRead.readLine();
        if ("ALL".equalsIgnoreCase(colo) || "PILOT".equalsIgnoreCase(colo)) {
            COLO.addAll(Arrays.asList("uj1", "uh1", "lhr1", "hkg1"));
        } else {
            COLO.add(colo);
        }

        System.out.println("Enter full key (e.g.adapter.ix.bidFloorPercent) :");
        key = bufferRead.readLine();
    }

    /**
     * 
     * @throws IOException
     */
    private void takeInput() throws IOException {
        takeCommonInput();
        System.out.println("Enter new value :");
        final String value = bufferRead.readLine();

        final Map<String, String> map = new HashMap<>();
        map.put(key, value);
        final Gson gson = new Gson();
        json = gson.toJson(map);

        System.out.println(String.format("Colo -> %s, Json -> %s, Continue (Y,N)", colo, json));
        final String yes = bufferRead.readLine();
        if (!"Y".equalsIgnoreCase(yes)) {
            System.exit(-1);
        }

    }

    /**
     * 
     * @param url
     * @return
     */
    private boolean isHostUp(final String url) {
        try {
            final String ok = IOUtils.toString(new URL(url));
            return "OK".equalsIgnoreCase(ok);
        } catch (final Exception e) {
            // System.err.println(e.getMessage());
        }
        return false;
    }



    /**
     * 
     * @param url
     * @param output
     */
    private void printResult(final String url, final String output) {
        switch (operation) {
            case CHANGE_CONFIG:
                System.out.println(output);
                break;
            case GET_ADAPTER_CONFIG:
            case GET_SERVER_CONFIG:
            case GET_STATS:
                try {
                    JSONObject object = new JSONObject(output);
                    final String keyArr[] = key.split("\\#");
                    for (int i = 0; i < keyArr.length; i++) {
                        if (i == keyArr.length - 1) {
                            System.out.println(url + " ->" + key + " ->" + object.get(keyArr[i].trim()));
                        } else {
                            object = object.getJSONObject(keyArr[i].trim());
                        }
                    }
                } catch (final JSONException e) {
                    System.err.println(url);
                    e.printStackTrace();
                }
                break;
        }

    }

    /**
     * 
     * @return
     */
    private String getActionUrl() {
        switch (operation) {
            case CHANGE_CONFIG:
                return String.format(URL_CHANGE_PATTERN, json);
            case GET_ADAPTER_CONFIG:
                return URL_GET_ADAPTER_PATTERN;
            case GET_SERVER_CONFIG:
                return URL_GET_SERVER_PATTERN;
            case GET_STATS:
                return URL_GET_STATS_PATTERN;
        }
        return null;
    }



}
