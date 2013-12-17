package com.inmobi.adserve.channels.api;

public class ServerException extends Exception {
    private static final long serialVersionUID = 1L;

    public String             errorString;

    public ServerException(final String errorMessage) {
        errorString = errorMessage;
    }

    public ServerException(final Exception e) {
        super(e);
    }
}
