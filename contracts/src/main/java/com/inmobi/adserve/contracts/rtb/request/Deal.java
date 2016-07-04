package com.inmobi.adserve.contracts.rtb.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public final class Deal {
    @NonNull
    private final String id;
    private double bidfloor = 0.0;
    private String bidfloorcur = "USD";
    private Integer at;
}
