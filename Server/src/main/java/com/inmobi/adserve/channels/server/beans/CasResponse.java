package com.inmobi.adserve.channels.server.beans;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Builder;


@Getter
@ToString
@Builder
public class CasResponse {

    private final boolean noFill;

    private final String  htmlSnippet;

    private final boolean keepAlive;
}
