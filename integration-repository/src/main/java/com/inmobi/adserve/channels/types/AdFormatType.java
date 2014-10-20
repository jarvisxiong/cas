package com.inmobi.adserve.channels.types;

/**
 * Created by yasir.imteyaz on 11/08/14.
 * 
 * These enum values are mapped with ad_format table in cmsdb.
 */
public enum AdFormatType {
	TEXT(0), BANNER(1), TDD(2), TBU(3), THREED(4), EXP(5), INT(6), CLICK_TO_RICH(7), RICH_BANNER(8), META_JSON(9), PLAYABLE(
			10), VIDEO(11);

	private int value;

	private AdFormatType(final int value) {
		this.value = value;
	}

	/**
	 * @return the value
	 */
	public int getValue() {
		return value;
	}
}
