package com.visybl.visyblsdk_android;

/**
 * Created by pvs on 5/17/15.
 */
public abstract class Constants
{
	static String FILTER_STRINGS = "filterStrings";
	static String ZONE_DETECT = "zoneDetect", MIN_ZONE_RSSI = "minZoneRssi";
	static String SMS_ACTION = "sendSmsAction", SMS_NUMBER = "smsNumber";

	static final String ACTION_UPDATE_NEW = "ACTION_UPDATE_NEW", ACTION_UPDATE_OLD = "ACTION_UPDATE_OLD";
	static final String EXTRA_NAME = "EXTRA_NAME";
	static final String SERVICE_NAME = ".VisyblScanService";

	static final int MAX_SKIPPED_ADVS = 5, MIN_MSECS_BETWEEN_SIGNIFICANT_CHANGE = 60000, MAX_MSECS_BETWEEN_SIGNIFICANT_CHANGE = 300000;

	static long TEN_MB = 10485760;
}
