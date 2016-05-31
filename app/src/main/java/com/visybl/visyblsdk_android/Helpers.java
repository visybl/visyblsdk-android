package com.visybl.visyblsdk_android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.SparseArray;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by pvs on 5/17/15.
 */
public abstract class Helpers
{


	static String bytesToHexString(byte[] bytes)
	{
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < bytes.length; i++)
		{
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1)
			{
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return (hexString.toString());
	}

	static byte[] hexStringToBytes(String hexString)
	{

		int len = hexString.length();

		if ((len & 0x01) != 0)
		{
			return (null); // bad string length
		}

		byte[] bytes = new byte[len / 2];
		for (int i = 0; i < len; i += 2)
		{
			int char1 = Character.digit(hexString.charAt(i), 16);
			int char2 = Character.digit(hexString.charAt(i + 1), 16);

			if (char1 < 0 || char2 < 0)
			{
				return (null); // invalid hex digit
			}

			bytes[i / 2] = (byte) ((char1 << 4) + char2);
		}
		return bytes;
	}



}
