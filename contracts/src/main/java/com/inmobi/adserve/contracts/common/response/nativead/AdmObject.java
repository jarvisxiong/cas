package com.inmobi.adserve.contracts.common.response.nativead;

import com.google.gson.annotations.SerializedName;
import com.inmobi.template.gson.GsonContract;
import com.inmobi.template.gson.Required;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Created by ishanbhatnagar on 13/4/15.
 */
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@GsonContract
public final class AdmObject {
    @NonNull @Required @SerializedName("native") private Native nativeObj;
}
