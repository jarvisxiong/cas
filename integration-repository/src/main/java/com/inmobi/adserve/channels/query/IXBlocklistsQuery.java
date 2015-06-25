package com.inmobi.adserve.channels.query;

import com.inmobi.adserve.channels.types.IXBlocklistKeyType;
import com.inmobi.adserve.channels.types.IXBlocklistType;
import com.inmobi.phoenix.data.RepositoryQuery;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Created by ishanbhatnagar on 1/6/15.
 */
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class IXBlocklistsQuery implements RepositoryQuery {
    private String keyId;
    private IXBlocklistKeyType keyType;
    private IXBlocklistType blocklistType;
}
