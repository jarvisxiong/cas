package com.inmobi.adserve.channels.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;


@Getter
@ToString
@Builder(builderClassName = "Builder", builderMethodName = "newBuilder")
public class IMEIEntity {
    private final String androidId;
    private String imei;
}
