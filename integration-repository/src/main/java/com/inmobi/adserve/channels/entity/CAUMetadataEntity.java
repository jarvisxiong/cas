/**
 * 
 */
package com.inmobi.adserve.channels.entity;


import com.google.gson.Gson;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

/**
 * @author ritwik.kumar
 *
 */
@Getter
@ToString
@Builder(builderClassName = "Builder", builderMethodName = "newBuilder")
public class CAUMetadataEntity implements IdentifiableEntity<Long> {
    private static final long serialVersionUID = 1L;
    private static final Gson GSON = new Gson();
    private Long id;
    private String elementSecureJson;
    private Constraint constraint;
    private int version;

    // private String name;
    // private int closeBtnId;
    // private int status;
    // private long frameId;
    // private String animationContent;


    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getJSON() {
        return GSON.toJson(this);
    }

    @Data
    @ToString
    public class Constraint {
        // {"supply_dim":{"w":320,"h":480},
        // "creative_dim":{"min":{"w":160.0,"h":240.0},"max":{"w":1600.0,"h":2400.0},"actual":{"w":199.0,"h":300.0},"aspectRatio":0.667,"tolerance":0.03}}
        private Dimension supply_dim;
        private CreativeDim creative_dim;
    }

    @Data
    @ToString
    public class CreativeDim {
        private Dimension min;
        private Dimension max;
        private Dimension actual;
        private double aspectRatio;
        private double tolerance;
    }

    @Data
    @ToString
    public class Dimension {
        private int w;
        private int h;
    }

}
