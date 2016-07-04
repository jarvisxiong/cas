package com.inmobi.castest.commons.iohelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.inmobi.castest.utils.common.CasServerDetails;

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
        if (CasServerDetails.getFenderDebugger()) {
            System.out.println(x);
        }
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


        try {
            final Object x = yml.load(input);
            if (CasServerDetails.getFenderDebugger()) {
                System.out.println(x);
                System.out.println(((HashMap<String, List>) x).keySet());
                System.out.println(((HashMap<String, List>) x).values());
                final List<Map> list = ((HashMap<String, List>) x).get(testCase);
                System.out.println("Sant TC NAME IS : " + testCase);
            }
            System.out.println("* * * * * Running Test Case : " + testCase + " * * * * *");
            final Map<String, String> map = (Map<String, String>) ((HashMap<String, List>) x).get(testCase).get(0);

            return map;
        } catch (Exception e) {
            System.out.println("Unable to load TestParams.yml file " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static HashMap<String, String> readE2ETestIndex() throws FileNotFoundException {

        File testParamsYaml = new File("src/test/resources/E2ETestIndex.yml");
        if (!testParamsYaml.exists()) {
            testParamsYaml = new File("casautomation/src/test/resources/E2ETestIndex.yml");
        }

        final InputStream input = new FileInputStream(testParamsYaml);
        final Yaml yml = new Yaml();
        final Object x = yml.load(input);
        if (CasServerDetails.getFenderDebugger()) {
            System.out.println(x);
        }
        return (HashMap<String, String>) x;

    }
    public static Map<String, String> readE2ETestParams(String e2etestcase) throws FileNotFoundException{
        e2etestcase = e2etestcase.toUpperCase();
        File e2etestParamsYaml=new File("src/test/resources/E2ETestParams.yml");
        if (!e2etestParamsYaml.exists()) {
            e2etestParamsYaml = new File("casautomation/src/test/resources/E2ETestParams.yml");
        }
        final InputStream input = new FileInputStream(e2etestParamsYaml);
        final Yaml yml = new Yaml();
        try {
            final Object x = yml.load(input);
            if (CasServerDetails.getFenderDebugger()) {
                System.out.println(x);
                System.out.println(((HashMap<String, List>) x).keySet());
                System.out.println(((HashMap<String, List>) x).values());
                final List<Map> list = ((HashMap<String, List>) x).get(e2etestcase);
                System.out.println("Sant TC NAME IS : " + e2etestcase);
            }
            System.out.println("* * * * * Running Test Case : " + e2etestcase + " * * * * *");
            final Map<String, String> map = (Map<String, String>) ((HashMap<String, List>) x).get(e2etestcase).get(0);

            return map;
        } catch (Exception e){
            System.out.println("Unable to load E2ETestParams.yml file " + e.getMessage());
            return null;
        }
    }
}
