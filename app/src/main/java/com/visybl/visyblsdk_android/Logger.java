package com.visybl.visyblsdk_android;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger
{
	static boolean LOG = true;

	static String logTag = "VISYBLSCAN";

	static void log(String str)
	{
		if (LOG)
		{
			Log.d(logTag, str);
		}

		//PVS

		try
		{
			File root = Environment.getExternalStorageDirectory();
			if (root.canWrite())
			{
				File beaconCountLogFile = new File(root, "visybl-log.txt");

				if (beaconCountLogFile.length() > Constants.TEN_MB)
				{
					Log.d(logTag, "Log file > 10 MB, deleting");
					beaconCountLogFile.delete();
				}

				FileWriter fileWriter = new FileWriter(beaconCountLogFile, true);
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);



				bufferedWriter.write(getLogTimeStamp(System.currentTimeMillis()) + ": " + str + "\n");

				bufferedWriter.close();
				fileWriter.close();
			}
			else
			{

			}
		}
		catch (Exception e)
		{

		}


	}

	// Used to log data in CSV formats
	static void logToFile(String str, String filename)
	{
		try
		{
			File root = Environment.getExternalStorageDirectory();
			if (root.canWrite())
			{
				File advLogFile = new File(root, filename);

				FileWriter fileWriter = new FileWriter(advLogFile, true);
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

				bufferedWriter.write(getLogTimeStamp(System.currentTimeMillis()) + "," + str + "\n");

				bufferedWriter.close();
				fileWriter.close();
			}
			else
			{

			}
		}
		catch (Exception e)
		{

		}

	}

	private static String getLogTimeStamp(long msecs)
	{
		Date date = new Date(msecs);
		SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd HH:mm:ss.SSSZ");
		//("hh:mm:ss a',' MMMM d ");
		return (formatter.format(date));
	}
}
