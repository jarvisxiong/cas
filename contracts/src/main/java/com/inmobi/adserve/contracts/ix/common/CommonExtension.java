package com.inmobi.adserve.contracts.ix.common;

import com.inmobi.adserve.contracts.ix.request.Blind;
import com.inmobi.adserve.contracts.ix.request.DtExtensions;
import com.inmobi.adserve.contracts.ix.request.RubiconExtension;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Ishan Bhatnagar
 */
@Getter
@Setter
@NoArgsConstructor
// @GsonContract not required here as there are no required fields/default values to enforce during deserialisation
public class CommonExtension {
    private RubiconExtension rp;
    private Blind blind;
    private DtExtensions dt;
}
