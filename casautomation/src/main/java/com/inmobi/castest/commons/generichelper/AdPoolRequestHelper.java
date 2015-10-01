package com.inmobi.castest.commons.generichelper;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.testng.Reporter;

import com.inmobi.adserve.adpool.AdPoolRequest;
import com.inmobi.adserve.adpool.AdPoolResponse;
import com.inmobi.castest.commons.iohelper.FenderTestIOHelper;
import com.inmobi.castest.utils.common.AdserveBackfillRequest;
import com.inmobi.castest.utils.common.AdserveBackfillRequest2;
import com.inmobi.castest.utils.common.CasServerDetails;
import com.inmobi.castest.utils.common.NewPostRequest;
import com.inmobi.castest.utils.common.ResponseBuilder;
import com.inmobi.castest.utils.common.config.ConfigChangeUtil;

public class AdPoolRequestHelper {
    private static Map<String, String> newRequestInput = new HashMap<String, String>();
    private static AdPoolRequest adpRequest = new AdPoolRequest();
    private static ResponseBuilder responseBuilder = new ResponseBuilder();
    private static ConfigChangeUtil chngConfigHelper = new ConfigChangeUtil();
    private static AdPoolResponse adPoolResponse = new AdPoolResponse();

    public static ResponseBuilder fireAdPoolRequestForRTBD(String testName) throws FileNotFoundException,
            InterruptedException {

        // Ad pool request part:
        testName = testName.toUpperCase();
        newRequestInput = FenderTestIOHelper.setTestParams(testName);

        chngConfigHelper.checkChangeConfFlag(newRequestInput);

        if (CasServerDetails.getFenderDebugger()) {
            Reporter.log("****** new Request input ***** \n" + newRequestInput, true);
        } else {
            Reporter.log("****** new Request input ***** \n" + newRequestInput, false);
        }

        adpRequest = AdserveBackfillRequest.formulateNewBackFillRequest(newRequestInput);

        System.out.println("Adpoolrequest :- " + adpRequest.toString());
        System.out.println(CasServerDetails.getCasServerEndPoint());

        try {

            responseBuilder = NewPostRequest.sendPost(adpRequest, "rtbdFill", testName);

        } catch (final Exception e) {

            e.printStackTrace();

        }
        return responseBuilder;
    }

    public static ResponseBuilder fireAdPoolRequestForIX(String testName) throws FileNotFoundException,
            InterruptedException {

        // Ad pool request part:
        testName = testName.toUpperCase();
        newRequestInput = FenderTestIOHelper.setTestParams(testName);

        chngConfigHelper.checkChangeConfFlag(newRequestInput);

        if (CasServerDetails.getFenderDebugger()) {
            Reporter.log("****** new Request input ***** \n" + newRequestInput, true);
        } else {
            Reporter.log("****** new Request input ***** \n" + newRequestInput, false);
        }

        adpRequest = AdserveBackfillRequest.formulateNewBackFillRequest(newRequestInput);

        System.out.println("Adpoolrequest :- " + adpRequest.toString());
        System.out.println(CasServerDetails.getCasServerEndPoint());

        try {

            responseBuilder = NewPostRequest.sendPost(adpRequest, "ixFill", testName);

        } catch (final Exception e) {

            e.printStackTrace();

        }
        return responseBuilder;
    }

    public static ResponseBuilder fireAdPoolRequestForDCP(String testName) throws FileNotFoundException,
            InterruptedException {

        testName = testName.toUpperCase();
        newRequestInput = FenderTestIOHelper.setTestParams(testName);

        chngConfigHelper.checkChangeConfFlag(newRequestInput);

        if (CasServerDetails.getFenderDebugger()) {
            Reporter.log("****** new Request input ***** \n" + newRequestInput, true);
        } else {
            Reporter.log("****** new Request input ***** \n" + newRequestInput, false);
        }

        adpRequest = AdserveBackfillRequest.formulateNewBackFillRequest(newRequestInput);

        System.out.println("Adpoolrequest :- " + adpRequest.toString());

        try {

            responseBuilder = NewPostRequest.sendPost(adpRequest, "backfill", testName);

        } catch (final Exception e) {

            e.printStackTrace();

        }
        return responseBuilder;
    }

    public static AdPoolResponse fireAdPoolRequestForBrand(String testName, final String className) throws Exception {

        testName = testName.toUpperCase();
        newRequestInput = FenderTestIOHelper.setDGTestParams(testName, className);

        if (CasServerDetails.getFenderDebugger()) {
            Reporter.log("****** new Request input ***** \n" + newRequestInput, true);
        } else {
            Reporter.log("****** new Request input ***** \n" + newRequestInput, false);
        }


        adpRequest = AdserveBackfillRequest2.formulateNewBackFillRequest(newRequestInput);

        System.out.println("Adpoolrequest :- " + adpRequest.toString());

        try {

            adPoolResponse = NewPostRequest.sendPost(adpRequest, testName);

        } catch (final Exception e) {

            e.printStackTrace();

        }
        return adPoolResponse;
    }
}
