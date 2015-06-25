package com.inmobi.castest.utils.bidders.stats;

import java.util.TimerTask;

import com.inmobi.castest.utils.bidders.HttpRequestHandler;

public class SleepTimeCalculatorTask extends TimerTask {

    public SleepTimeCalculatorTask() {}

    @Override
    public void run() {
        System.out.println("numberOfRequestsReceived " + HttpRequestHandler.numberOfRequestsReceived.longValue());
        System.out.println("numberOfRequestsResponded " + HttpRequestHandler.numberOfRequestsResponded.longValue());
        HttpRequestHandler.numberOfRequestsReceived.set(0);;
        HttpRequestHandler.numberOfRequestsResponded.set(0);
    }


}
