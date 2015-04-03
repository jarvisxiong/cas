package com.inmobi.castest.commons.iohelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public class YamlDataIOHelper {

    @SuppressWarnings("unchecked")
    public static HashMap<String, String> readTestIndex() throws FileNotFoundException {

        File testParamsYaml = new File("src/test/resources/TestIndex.yml");
        if (!testParamsYaml.exists()) {
            testParamsYaml = new File("casautomation/src/test/resources/TestIndex.yml");
        }

        final InputStream input = new FileInputStream(testParamsYaml);
        final Yaml yml = new Yaml();
        final Object x = yml.load(input);
        System.out.println(x);
        return (HashMap<String, String>) x;

    }

    public static Map<String, String> readTestParams(String testCase) throws FileNotFoundException {

        testCase = testCase.toUpperCase();

        File testParamsYaml = new File("src/test/resources/TestParams.yml");
        if (!testParamsYaml.exists()) {
            testParamsYaml = new File("casautomation/src/test/resources/TestParams.yml");
        }

        final InputStream input = new FileInputStream(testParamsYaml);

        final Yaml yml = new Yaml();
        final Object x = yml.load(input);
        System.out.println(x);
        System.out.println(((HashMap<String, List>) x).keySet());
        System.out.println(((HashMap<String, List>) x).values());
        final List<Map> list = ((HashMap<String, List>) x).get(testCase);
        System.out.println("Sant TC NAME IS : " + testCase);
        final Map<String, String> map = (Map<String, String>) ((HashMap<String, List>) x).get(testCase).get(0);

        // System.out.println("LIST : " + list);

        // System.out.println(list.get(0));
        // = list.get(0);
        // System.out.println(map);
        //
        // System.out.println(map.get("r_adpool_requestid"));
        // System.out.println(map.get("r_os_id"));
        // System.out.println(map.get("r_u_id_params"));
        // System.out.println(map.get("r_site_type"));
        // System.out.println(map.get("expected_status"));
        // System.out.println(list.get(0).keySet());
        // System.out.println(list.get(0).values());
        return map;

        // /Users/santosh.vaidyanathan/cas_framework/cas/casautomation/src/test/resource/TestIndex.yml
    }

    public static void main(final String[] args) throws IOException {
        new YamlDataIOHelper();
        // new DummyToDelete().readTestIndex();
        YamlDataIOHelper.readTestParams("TEST3_2_4".toUpperCase());
        new YamlDataIOHelper();
        YamlDataIOHelper.readTestIndex();
    }
}
