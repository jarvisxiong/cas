package com.inmobi.castest.dataprovider;

import java.io.FileNotFoundException;
import java.lang.reflect.Method;

import org.testng.annotations.DataProvider;

import com.inmobi.castest.commons.generichelper.AdPoolRequestHelper;
import com.inmobi.castest.utils.common.ResponseBuilder;

public class FenderDataProvider {

    private static ResponseBuilder responseBuilder = new ResponseBuilder();

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
}
