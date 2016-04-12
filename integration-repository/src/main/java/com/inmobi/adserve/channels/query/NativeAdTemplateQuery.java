/**
 * Copyright (c) 2015. InMobi, All Rights Reserved.
 */
package com.inmobi.adserve.channels.query;

import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity.TemplateClass;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author ritwik.kumar
 *
 */
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NativeAdTemplateQuery {
    private Long placementId;
    private TemplateClass templateClass;
}
