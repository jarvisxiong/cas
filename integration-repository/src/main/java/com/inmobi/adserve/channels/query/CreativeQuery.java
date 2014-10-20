package com.inmobi.adserve.channels.query;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import com.inmobi.phoenix.data.RepositoryQuery;


@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class CreativeQuery implements RepositoryQuery {

    private String advertiserId;
    private String creativeId;

}
