package com.inmobi.adserve.channels.adnetworks.rtb;

import java.util.regex.Pattern;

public class RTBCallbackMacros {
    public static final String AUCTION_ID = "${AUCTION_ID}";
    public static final String AUCTION_BID_ID = "${AUCTION_BID_ID}";
    public static final String AUCTION_IMP_ID = "${AUCTION_IMP_ID}";
    public static final String AUCTION_SEAT_ID = "${AUCTION_SEAT_ID}";
    public static final String AUCTION_AD_ID = "${AUCTION_AD_ID}";
    public static final String AUCTION_PRICE = "${AUCTION_PRICE}";
    public static final String AUCTION_CURRENCY = "${AUCTION_CURRENCY}";

    public static final String AUCTION_ID_INSENSITIVE = "(?i)" + Pattern.quote(AUCTION_ID);
    public static final String AUCTION_BID_ID_INSENSITIVE = "(?i)" + Pattern.quote(AUCTION_BID_ID);
    public static final String AUCTION_IMP_ID_INSENSITIVE = "(?i)" + Pattern.quote(AUCTION_IMP_ID);
    public static final String AUCTION_SEAT_ID_INSENSITIVE = "(?i)" + Pattern.quote(AUCTION_SEAT_ID);
    public static final String AUCTION_AD_ID_INSENSITIVE = "(?i)" + Pattern.quote(AUCTION_AD_ID);
    public static final String AUCTION_PRICE_INSENSITIVE = "(?i)" + Pattern.quote(AUCTION_PRICE);
    public static final String AUCTION_CURRENCY_INSENSITIVE = "(?i)" + Pattern.quote(AUCTION_CURRENCY);
    // InMobi specific macros
    public static final String WIN_BID_GET_PARAM = "?b=${WIN_BID}";
    public static final String AUCTION_PRICE_ENCRYPTED_INSENSITIVE = "(?i)" + Pattern.quote("${WIN_BID}");
    
    public static final String AUCTION_WIN_URL = "${BILLABLE_BEACON}";

    public static final String DEAL_GET_PARAM = "${DEAL_GET_PARAM}";
    public static final String DEAL_ID_INSENSITIVE = "(?i)" + Pattern.quote(DEAL_GET_PARAM);


}
