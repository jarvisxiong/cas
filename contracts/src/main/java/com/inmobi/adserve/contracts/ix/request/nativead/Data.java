package com.inmobi.adserve.contracts.ix.request.nativead;

import com.inmobi.adserve.contracts.ix.common.CommonExtension;

import lombok.NonNull;

/**
 * @author ritwik.kumar Created by ishanbhatnagar on 22/1/15.
 */
@lombok.Data
public class Data {
    @NonNull
    private final Integer type;
    private Integer len;
    private CommonExtension ext;

    /**
     * @param type
     */
    public Data(final DataAssetType type) {
        this.type = type.getId();
    }


    public enum DataAssetType {
        // text : Sponsored By message where response should contain the brand name of the sponsor
        SPONSORED(1),
        // text : Descriptive text associated with the product or service being advertised.
        DESC(2),
        // number formatted as string : Rating of the product being offered to the user. For example an app’s rating in
        // an app store from 0-5
        RATING(3),
        // number formatted as string : Number of social ratings or "likes" of the product being offered to the user.
        LIKES(4),
        // number formatted as string: Number downloads/installs of this product
        DOWNLOADS(5),
        // number formatted as string: Price for product/app/in-app purchase. Value should include currency symbol in
        // localized format.
        PRICE(6),
        // number formatted as string : Sale price that can be used together with price to indicate a discounted price
        // compared to a regular price. Value should include currency symbol in localized format.
        SALE_PRICE(7),
        // formatted string : Phone number.
        PHONE(8),
        // text : Address.
        ADDRESS(9),
        // text : Additional descriptive text associated with the product or service being advertised.
        DESC_2(10),
        // text : Display URL for the text ad.
        DISPLAY_URL(11),
        // text : Descriptive text describing a 'call to action' button for the destination URL.
        CTA_TEXT(12);

        private final int id;

        private DataAssetType(final int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}
