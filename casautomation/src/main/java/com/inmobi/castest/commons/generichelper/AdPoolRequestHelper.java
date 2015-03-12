package com.inmobi.castest.commons.generichelper;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.testng.Reporter;

import com.inmobi.adserve.adpool.AdPoolRequest;
import com.inmobi.castest.commons.iohelper.FenderTestIOHelper;
import com.inmobi.castest.utils.common.AdserveBackfillRequest;
import com.inmobi.castest.utils.common.CasServerDetails;
import com.inmobi.castest.utils.common.NewPostRequest;

public class AdPoolRequestHelper {
    private static Map<String, String> newRequestInput = new HashMap<String, String>();
    private static AdPoolRequest adpRequest = new AdPoolRequest();

    public static void fireAdPoolRequestForRTBD(String testName) throws FileNotFoundException {

        // Ad pool request part:
        testName = testName.toUpperCase();
        newRequestInput = FenderTestIOHelper.setTestParams(testName);

        Reporter.log("****** new Request input ***** \n" + newRequestInput, true);
        Reporter.log("****** new Request param - user interest ***** \n" + newRequestInput.get("user_interests"), true);

        adpRequest = AdserveBackfillRequest.formulateNewBackFillRequest(newRequestInput);

        System.out.println("Adpoolrequest :- " + adpRequest.toString());
        System.out.println(CasServerDetails.getCasServerEndPoint());

        try {

            NewPostRequest.sendPost(adpRequest, "rtbdFill", testName);

        } catch (final Exception e) {

            e.printStackTrace();

        }
    }

    public static void fireAdPoolRequestForIX(String testName) throws FileNotFoundException {

        // Ad pool request part:
        testName = testName.toUpperCase();
        newRequestInput = FenderTestIOHelper.setTestParams(testName);

        Reporter.log("****** new Request input ***** \n" + newRequestInput, true);

        adpRequest = AdserveBackfillRequest.formulateNewBackFillRequest(newRequestInput);

        System.out.println("Adpoolrequest :- " + adpRequest.toString());
        System.out.println(CasServerDetails.getCasServerEndPoint());

        try {

            NewPostRequest.sendPost(adpRequest, "ixFill", testName);

        } catch (final Exception e) {

            e.printStackTrace();

        }
    }

    public static void fireAdPoolRequestForDCP(String testName) throws FileNotFoundException {

        testName = testName.toUpperCase();
        newRequestInput = FenderTestIOHelper.setTestParams(testName);

        Reporter.log("****** new Request input ***** \n" + newRequestInput, true);

        adpRequest = AdserveBackfillRequest.formulateNewBackFillRequest(newRequestInput);

        System.out.println("Adpoolrequest :- " + adpRequest.toString());

        try {

            NewPostRequest.sendPost(adpRequest, "backfill", testName);

        } catch (final Exception e) {

            e.printStackTrace();

        }
    }
}
