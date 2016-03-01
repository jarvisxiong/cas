/**
 * 
 */
package com.inmobi.adserve.channels.server.circuitbreaker;

/**
 * @author rajashekhar.c
 * 
 */
public interface CircuitBreakerInterface {

    /**
     * If the circuit is in open state, then it cannot be forwarded.
     * 
     * @return true or false based on the state of the circuit
     */
    boolean canForwardTheRequest();

    /**
     * For every advertiser, failure counter is increased on every failure
     * 
     * @param startTime: It it the time at which the request was received to us by the UMP
     */
    void incrementFailureCounter(final long startTime);

    /**
     * For every advertiser, request counter is increased on every request sent to the advertiser
     * 
     * @param startTime: It it the time at which the request was received to us by the UMP
     */
    void incrementTotalCounter(final long startTime);

}
