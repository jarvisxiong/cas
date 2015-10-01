package com.inmobi.castest.dataprovider;

import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.DataProvider;

import com.inmobi.adserve.adpool.AdPoolResponse;
import com.inmobi.castest.commons.generichelper.AdPoolRequestHelper;
import com.inmobi.castest.commons.iohelper.JsonDataIOHelper;
import com.inmobi.castest.utils.common.ResponseBuilder;

public class FenderDataProvider {

    private static ResponseBuilder responseBuilder = new ResponseBuilder();
    private static Map<String, String> validationsMap = new HashMap<String, String>();
    private static AdPoolResponse adPoolResponse = new AdPoolResponse();

    @DataProvider(name = "fender_ix_dp")
    public static Object[][] createDataForIX(final Method method) throws FileNotFoundException, InterruptedException {

        System.out.println(method.getName());

        responseBuilder = AdPoolRequestHelper.fireAdPoolRequestForIX(method.getName().toUpperCase());

        return new Object[][] {{method.getName(), responseBuilder}};
    }

    @DataProvider(name = "fender_rtbd_dp")
    public static Object[][] createDataForRTBD(final Method method) throws FileNotFoundException, InterruptedException {

        System.out.println(method.getName());

        responseBuilder = AdPoolRequestHelper.fireAdPoolRequestForRTBD(method.getName().toUpperCase());

        return new Object[][] {{method.getName(), responseBuilder}};
    }

    @DataProvider(name = "fender_dcp_dp")
    public static Object[][] createDataForDCP(final Method method) throws FileNotFoundException, InterruptedException {

        System.out.println(method.getName());

        responseBuilder = AdPoolRequestHelper.fireAdPoolRequestForDCP(method.getName().toUpperCase());

        return new Object[][] {{method.getName(), responseBuilder}};
    }

    @DataProvider(name = "fender_brand_dp")
    public static Object[][] createDataForBrand(final Method method) throws Exception {

        System.out.println("Executing " + method.getName() + "from " + method.getDeclaringClass().getName() + "suite");

        adPoolResponse =
                AdPoolRequestHelper.fireAdPoolRequestForBrand(method.getName().toUpperCase(), method
                        .getDeclaringClass().getName());

        validationsMap =
                JsonDataIOHelper.readValidations(method.getName().toUpperCase(), method.getDeclaringClass().getName());

        return new Object[][] {{method.getName(), adPoolResponse, validationsMap}};
    }
}
