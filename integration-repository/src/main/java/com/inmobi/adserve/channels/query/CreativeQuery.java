package com.inmobi.adserve.channels.query;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import com.inmobi.phoenix.data.RepositoryQuery;


@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CreativeQuery implements RepositoryQuery {
    private String advertiserId;
    private String creativeId;

}
