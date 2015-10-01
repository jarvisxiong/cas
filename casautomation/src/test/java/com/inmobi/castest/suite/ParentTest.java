package com.inmobi.castest.suite;

import org.testng.annotations.BeforeSuite;

import com.inmobi.castest.utils.common.DummyBidderDetails;

public class ParentTest {

    @BeforeSuite
    public void beforeSuite() throws Exception {

        final String[] dummyBidderArguments =
                {DummyBidderDetails.getDumbidPort(), DummyBidderDetails.getDumbidTimeOut(),
                        DummyBidderDetails.getDumbidPercentAds(), DummyBidderDetails.getDumbidBudget(),
                        DummyBidderDetails.getDumbidSeatId(), DummyBidderDetails.getDumbidToggleUnderstress()};
        // Main.hostDummyBidder(dummyBidderArguments);
        System.out.println("Hosting the Dummy Bidder");
        Thread.sleep(1000);

    }
}
