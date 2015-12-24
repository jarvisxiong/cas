package com.inmobi.adserve.contracts.common.request.nativead;

import java.util.ArrayList;
import java.util.List;

import com.inmobi.adserve.contracts.ix.common.CommonExtension;

/**
 * @author ritwik.kumar Created by ishanbhatnagar on 23/1/15.
 */
@lombok.Data
public final class Image {
    private Integer type;
    private Integer w;
    private Integer wmin;
    private Integer h;
    private Integer hmin;
    private List<String> mimes;
    private CommonExtension ext;

    public Image() {
    }

    /**
     * @param other
     */
    public Image(final Image other) {
        type = other.type;
        w = other.w;
        wmin = other.wmin;
        h = other.h;
        hmin = other.hmin;
        if (null != other.mimes) {
            mimes = new ArrayList<>(other.mimes);
        } else {
            mimes = null;
        }
        ext = other.ext; // deep copy will not happen
    }

    /**
     * @param type
     */
    public void setType(ImageAssetType type) {
        this.type = type.getId();
    }

    public enum ImageAssetType {
        // Icon image
        ICON(1),
        // Logo image for the brand/app
        LOGO(2),
        // Large image preview for the ad
        MAIN(3);

        private final int id;

        private ImageAssetType(final int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}
