package com.inmobi.adserve.contracts.dcp.backward;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Created for backward compatibility for Native on Taboola
@Getter
@Setter
@NoArgsConstructor
public class Image {
    private double aspectratio;
    private int minwidth;
    private int maxwidth;

    public Image(Image other) {
        this.aspectratio = other.aspectratio;
        this.minwidth = other.minwidth;
        this.maxwidth = other.maxwidth;
    }

    public Image deepCopy() {
        return new Image(this);
    }
}
