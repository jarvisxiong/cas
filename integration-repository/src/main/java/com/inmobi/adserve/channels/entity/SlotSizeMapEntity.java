package com.inmobi.adserve.channels.entity;

import java.awt.Dimension;
import java.sql.Timestamp;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import com.google.gson.Gson;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;

/**
 * Created by anshul.soni on 27/11/14.
 */
@Getter
@ToString
@Builder(builderClassName = "Builder", builderMethodName = "newBuilder")
public class SlotSizeMapEntity implements IdentifiableEntity<Short> {
    private final static long serialVersionUID = 1L;
    private final static Gson GSON = new Gson();
    private final Short slotId;
    private final Dimension dimension;
    private final Timestamp modifiedOn;

    @Override
    public Short getId() {
        return slotId;
    }

    @Override
    public String getJSON() {
        return GSON.toJson(this);
    }



}
