package com.inmobi.adserve.channels.server.circuitbreaker;

import org.apache.commons.configuration.Configuration;

import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.instrumentation.MovingWindowCounter;

/**
 * We get requests from UMP/Nginx, and we forward the requests to RTB/DCP partners, some partners don’t return in time.
 * If we forward it to 5 partners and one partner doesn’t return in time. Then at times our timer also fails to tick on
 * time. This inturn has cascading effect on the upstream in terms of success rate of the ad-pool. As part of this
 * feature we would be identifying the chronic offenders and turn them off/on dynamically.
 * 
 * Design doc is present at See <a href=
 * "https://docs.google.com/a/inmobi.com/document/d/16eaG1I3aIeOkhedzgtHdLo9iSA3GS55Y2jHx3FpJs10/edit#heading=h.kelyp9vdeq1p"
 * >Design Doc</a>
 * 
 * @author rajashekhar.c
 * 
 */
public class CircuitBreakerImpl implements CircuitBreakerInterface {
    enum State {
        CIRCUIT_CLOSED, 
        CIRCUIT_OPEN, 
        UNDER_OBSERVATION
    }
    
    private static final long REINITIALIZE_WINDOW = 10 * 60 * 1000; // 10 Minutes
    private static final long TEN_SECONDS = 10 * 1000; // 10 Seconds
    private State currentState;
    private long startTimeOfObservation;
    private long startTimeOfCircuitOpen;
    private MovingWindowCounter failureMovingWindowCounter;
    private MovingWindowCounter requestMovingWindowCounter;
    private final Configuration serverConfig;
    private double failureThreshold;
    private long minimumNumberOfRequests;
    private long lengthOfMovingWindowCounterInSeconds;
    private long numberOfMillisecondsInOpenCircuit;
    private long numberOfMillisecondsUnderObservation;
    private long lastTimeReinitialized;
    private final Object lockObject;
    private final String advertiserName;

    public CircuitBreakerImpl(String advertiserName) {
        this.advertiserName = advertiserName;
        serverConfig = CasConfigUtil.getServerConfig();
        lockObject = new Object();
        init();
    }

    private void init() {
        lengthOfMovingWindowCounterInSeconds =
                serverConfig.getLong("circuitbreaker.lengthOfMovingWindowCounterInSeconds");
        failureMovingWindowCounter = new MovingWindowCounter(lengthOfMovingWindowCounterInSeconds);
        requestMovingWindowCounter = new MovingWindowCounter(lengthOfMovingWindowCounterInSeconds);

        failureThreshold = serverConfig.getDouble("circuitbreaker.failureThreshold");
        minimumNumberOfRequests = serverConfig.getLong("circuitbreaker.minimumNumberOfRequests");
        numberOfMillisecondsInOpenCircuit = serverConfig.getLong("circuitbreaker.lengthOfMovingWindowCounterInSeconds") * 1000;
        numberOfMillisecondsUnderObservation =
                serverConfig.getLong("circuitbreaker.numberOfSecondsUnderObservation") * 1000;

        currentState = State.CIRCUIT_CLOSED;
        lastTimeReinitialized = System.currentTimeMillis();
    }

    private void reInitializeIfRequired() {
        if (System.currentTimeMillis() - lastTimeReinitialized > REINITIALIZE_WINDOW) {
            synchronized (lockObject) {
                if (System.currentTimeMillis() - lastTimeReinitialized > REINITIALIZE_WINDOW) {
                    if (lengthOfMovingWindowCounterInSeconds != serverConfig
                            .getLong("circuitbreaker.lengthOfMovingWindowCounterInSeconds")
                            || minimumNumberOfRequests != serverConfig
                                    .getLong("circuitbreaker.minimumNumberOfRequests")
                            || numberOfMillisecondsUnderObservation != serverConfig
                                    .getLong("circuitbreaker.numberOfSecondsUnderObservation") * 1000
                            || Math.abs(failureThreshold - serverConfig.getDouble("circuitbreaker.failureThreshold")) > 0.001) {
                        init();
                    } else {
                        lastTimeReinitialized = System.currentTimeMillis();
                    }
                }
            }
        }
    }

    private void changeStateIfNeeded() {
        reInitializeIfRequired();
        switch (currentState) {
            case CIRCUIT_CLOSED:
                if (isTenSecondsOver() && isThresholdCrossed()) {
                    moveToCircuitOpenState();
                    InspectorStats.incrementStatCount(advertiserName, InspectorStrings.NUMBER_OF_TIMES_CIRCUIT_OPENED);
                }
                break;

            case CIRCUIT_OPEN:
                if (hasCompletedTenureInCircuitOpen()) {
                    moveToObservationState();
                }
                break;

            case UNDER_OBSERVATION:
                if (hasCompletedTenureInUnderObservation()) {
                    if (isThresholdCrossed()) {
                        moveToCircuitOpenState();
                    } else {
                        moveToCircuitClosedState();
                    }
                }
                break;
        }
    }

    /**
     * If the circuit is in closed or under-observation then we can forward the request. If the circuit is in open
     * state, then it cannot be forwarded.
     */
    @Override
    public boolean canForwardTheRequest() {
        changeStateIfNeeded();
        if (isCircuitClose() || isUnderObservation()) {
            return true;
        }
        return false;
    }

    private void moveToCircuitOpenState() {
        currentState = State.CIRCUIT_OPEN;
        startTimeOfCircuitOpen = System.currentTimeMillis();
        resetCounters();
    }

    private void moveToObservationState() {
        currentState = State.UNDER_OBSERVATION;
        startTimeOfObservation = System.currentTimeMillis();
        resetCounters();
    }

    private void moveToCircuitClosedState() {
        currentState = State.CIRCUIT_CLOSED;
        resetCounters();
    }

    private void resetCounters() {
        failureMovingWindowCounter.resetTotalCounters();
        requestMovingWindowCounter.resetTotalCounters();
    }

    /**
     * For every advertiser, failure counter is increased on every failure. Here failure means timeouts + terminates
     */
    @Override
    public void incrementFailureCounter(final long startTime) {
        failureMovingWindowCounter.incrementRequest(startTime);
    }


    /**
     * For every advertiser, counter is increased on every request sent to the advertiser
     */
    @Override
    public void incrementTotalCounter(final long startTime) {
        requestMovingWindowCounter.incrementRequest(startTime);
    }

    private boolean isCircuitClose() {
        if (currentState == State.CIRCUIT_CLOSED) {
            return true;
        }
        return false;
    }

    private boolean isUnderObservation() {
        if (currentState == State.UNDER_OBSERVATION) {
            return true;
        }
        return false;
    }

    private long startTimeOfOneMinute = System.currentTimeMillis();

    private boolean isTenSecondsOver() {
        if (System.currentTimeMillis() - startTimeOfOneMinute > TEN_SECONDS) {
            startTimeOfOneMinute = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    private boolean hasCompletedTenureInCircuitOpen() {
        if (System.currentTimeMillis() - startTimeOfCircuitOpen > numberOfMillisecondsInOpenCircuit) {
            return true;
        }
        return false;
    }

    private boolean hasCompletedTenureInUnderObservation() {
        if (requestMovingWindowCounter.getTotalCounter() < minimumNumberOfRequests) {
            return false;
        }

        if (System.currentTimeMillis() - startTimeOfObservation > numberOfMillisecondsUnderObservation) {
            return true;
        }
        return false;
    }

    private boolean isThresholdCrossed() {
        final long failedRequests = failureMovingWindowCounter.getMovingWindowCounter();
        final long totalRequests = requestMovingWindowCounter.getMovingWindowCounter();
        if (totalRequests == 0) {
            return false;
        }

        //System.out.println("The failure ratio is " + (double) failedRequests / (double) totalRequests * 100);
        if ((double) failedRequests / (double) totalRequests > failureThreshold) {
            return true;
        }
        return false;
    }

    void setCurrentState(final State state) {
        switch (state) {
            case CIRCUIT_CLOSED:
                moveToCircuitClosedState();
                break;
            case CIRCUIT_OPEN:
                moveToCircuitOpenState();
                break;
            case UNDER_OBSERVATION:
                moveToObservationState();
                break;
        }
    }
}
