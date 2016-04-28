package com.inmobi.adserve.channels.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.google.gson.Gson;
import com.inmobi.phoenix.batteries.data.IdentifiableEntity;


@Data
@NoArgsConstructor
public class SiteTaxonomyEntity implements IdentifiableEntity<String> {
    private final static long serialVersionUID = 1L;
    private final static Gson GSON = new Gson();
    private String id;
    private String name;
    private String parentId;

    public SiteTaxonomyEntity(final String id, final String name, final String parentId) {
        super();
        this.id = id;
        this.name = name;
        this.parentId = parentId;
    }

    @Override
    public String getJSON() {
        return GSON.toJson(this);
    }

    @Override
    public String getId() {
        return id;
    }

}
