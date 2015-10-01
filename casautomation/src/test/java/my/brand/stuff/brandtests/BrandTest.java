package my.brand.stuff.brandtests;

import java.util.HashMap;

import org.testng.annotations.Test;

import com.inmobi.adserve.adpool.AdPoolResponse;
import com.inmobi.brandtest.utils.BrandValidator;
import com.inmobi.castest.dataprovider.FenderDataProvider;

public class BrandTest {

    @Test(testName = "TC_1", dataProvider = "fender_brand_dp", dataProviderClass = FenderDataProvider.class)
    public void TC_1(final String x, final AdPoolResponse adPoolResponse, final HashMap<String, String> validationsMap)
            throws Exception {

        BrandValidator.AssertIfParamsArePresent(adPoolResponse, validationsMap);

    }

}
