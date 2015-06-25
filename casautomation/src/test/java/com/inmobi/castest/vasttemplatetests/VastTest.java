package com.inmobi.castest.vasttemplatetests;

import java.io.IOException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import spark.Spark;

import com.inmobi.castest.commons.templatehelper.TemplateEncrypter;
import com.inmobi.castest.dataprovider.FenderDataProvider;
import com.inmobi.castest.utils.bidders.Main;
import com.inmobi.castest.utils.common.DummyBidderDetails;
import com.inmobi.castest.utils.common.ResponseBuilder;
import com.inmobi.commons.security.util.exception.InvalidMessageException;

public class VastTest {

    @Test(testName = "Vast Template test", dataProvider = "fender_ix_dp", dataProviderClass = FenderDataProvider.class)
    public void VAST_TEMPLATE_TEST(final String x, final ResponseBuilder responseBuilder) throws IOException,
            InvalidMessageException, InterruptedException {

        String responseToEncrypt = new String();
        final String grepFrom = "<!DOCTYPE html>";
        final String grepUpto = "</html>";

        final String response = new String(responseBuilder.getResponseData());
        System.out.println("\n\n***********************\n\n");
        System.out.println("RESPONSE IS : " + response);
        System.out.println("***********************\n\n");

        if (response.contains("<!DOCTYPE html>")) {
            responseToEncrypt =
                    response.substring(response.indexOf(grepFrom), response.indexOf(grepUpto) + grepUpto.length());
        } else {
            System.out
                    .println("The response received does not contain the expected String.Get a life (and another VAST xml)!");
        }
        System.out.println("-----\nENCRYPTED RESPONSE :\n" + responseToEncrypt);

        TemplateEncrypter.getEncryptedResponseHosted(responseToEncrypt);
    }

    @BeforeClass
    public void beforeClass() throws Exception {

        final String[] dummyBidderArguments =
                {DummyBidderDetails.getDumbidPort(), DummyBidderDetails.getDumbidTimeOut(),
                        DummyBidderDetails.getDumbidPercentAds(), DummyBidderDetails.getDumbidBudget(),
                        DummyBidderDetails.getDumbidSeatId(), DummyBidderDetails.getDumbidToggleUnderstress()};
        Main.hostDummyBidder(dummyBidderArguments);
        System.out.println("Hosting the Dummy Bidder !");
        Thread.sleep(2000);
    }

    public static void main(final String[] args) {
        Spark.get("/hello", (req, res) -> "Hello World");
    }

}
