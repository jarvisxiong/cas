package com.inmobi.adserve.channels.api;

public class ServerException extends Exception {
    private static final long serialVersionUID = 1L;

    public String             errorString;

    public ServerException(String errorMessage) {
        errorString = errorMessage;
    }
}
