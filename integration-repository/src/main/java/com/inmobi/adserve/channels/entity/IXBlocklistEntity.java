package com.inmobi.adserve.channels.entity;

import java.sql.Timestamp;

import com.google.gson.Gson;
import com.inmobi.adserve.channels.query.IXBlocklistsQuery;
import com.inmobi.adserve.channels.types.IXBlocklistKeyType;
import com.inmobi.adserve.channels.types.IXBlocklistType;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Created by ishanbhatnagar on 1/6/15.
 */
@Data
@RequiredArgsConstructor
@Builder(builderClassName = "Builder", builderMethodName = "newBuilder")
public class IXBlocklistEntity implements IdentifiableEntity<IXBlocklistsQuery> {
    private static final long serialVersionUID = 1L;
    private final static Gson GSON = new Gson();
    private final String blocklistName;
    private final String keyId;
    private final IXBlocklistKeyType keytype;
    private final IXBlocklistType blocklistType;
    private final int blocklistSize;
    private final Timestamp modifiedOn;

    @Override
    public IXBlocklistsQuery getId() {
        return new IXBlocklistsQuery(keyId, keytype, blocklistType);
    }

    @Override
    public String getJSON() {
        return GSON.toJson(this);
    }
}
