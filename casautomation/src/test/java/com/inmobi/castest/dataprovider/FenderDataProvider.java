package com.inmobi.castest.dataprovider;

import java.io.FileNotFoundException;
import java.lang.reflect.Method;

import org.testng.annotations.DataProvider;

import com.inmobi.castest.commons.generichelper.AdPoolRequestHelper;

public class FenderDataProvider {

    @DataProvider(name = "fender_ix_dp")
    public static Object[][] createDataForIX(final Method method) throws FileNotFoundException {

        System.out.println(method.getName());

        AdPoolRequestHelper.fireAdPoolRequestForIX(method.getName().toUpperCase());

        return new Object[][] {{method.getName()}};
    }

    @DataProvider(name = "fender_rtbd_dp")
    public static Object[][] createDataForRTBD(final Method method) throws FileNotFoundException {

        System.out.println(method.getName());

        AdPoolRequestHelper.fireAdPoolRequestForRTBD(method.getName().toUpperCase());

        return new Object[][] {{method.getName()}};
    }

    @DataProvider(name = "fender_dcp_dp")
    public static Object[][] createDataForDCP(final Method method) throws FileNotFoundException {

        System.out.println(method.getName());

        AdPoolRequestHelper.fireAdPoolRequestForDCP(method.getName().toUpperCase());

        return new Object[][] {{method.getName()}};
    }
}
