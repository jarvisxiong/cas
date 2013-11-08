package com.inmobi.adserve.channels.entity;

import com.inmobi.phoenix.batteries.data.IdentifiableEntity;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class SiteTaxonomyEntity implements IdentifiableEntity<String>
{

    private static final long serialVersionUID = 1L;

    private String            id;
    private String            name;
    private String            parentId;

    public SiteTaxonomyEntity(String id, String name, String parentId)
    {
        super();
        this.id = id;
        this.name = name;
        this.parentId = parentId;
    }

    @Override
    public String getJSON()
    {
        return null;
    }

    @Override
    public String getId()
    {
        return id;
    }

}
