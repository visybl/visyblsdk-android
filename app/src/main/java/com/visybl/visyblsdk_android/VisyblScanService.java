package com.visybl.visyblsdk_android;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.visybl.api.Beacon;
import com.visybl.api.Visybl;


import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static com.visybl.visyblsdk_android.Logger.log;

public class VisyblScanService extends Service
{
	private static final int HEADER_DATA = 0x020102;
	private static final int  LOCAL_NAME = 0x09;
	private static final  int UUID = 0x03;
	private static final  int MAN_DATA = 0xFF;

    private static final String TAG = "Main Activity";

	public VisyblScanService()
	{
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public void onCreate()
	{
		super.onCreate();

		initBT();

	}

	private BluetoothManager bluetoothManager;
	static BluetoothAdapter bluetoothAdapter;
	private BluetoothLeScanner bluetoothLeScanner;
	void initBT()
	{
		log("In initBT()");

		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE))
		{
			Toast.makeText(this, "This device does not support Visybl beacons!",
					Toast.LENGTH_SHORT).show();
			stopSelf();
		}

		bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

		if (bluetoothManager == null)
		{
			Toast.makeText(this, "No Bluetooth on this device", Toast.LENGTH_SHORT)
					.show();
			stopSelf();
		}

		bluetoothAdapter = bluetoothManager.getAdapter();



		if(bluetoothAdapter==null)
		{
			Toast.makeText(this, "No Bluetooth adapter", Toast.LENGTH_SHORT)
					.show();
			stopSelf();
		}

		bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

		if(bluetoothLeScanner==null)
		{
			Toast.makeText(this, "No Bluetooth Scanner", Toast.LENGTH_SHORT)
					.show();
			stopSelf();
		}

	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		log("In VisyblScanService.onStartCommand");
		super.onStartCommand(intent, flags, startId);

		bluetoothLeScanner.startScan(startLeScanCallback);

		//bluetoothLeScanner.startScan(startLeScanCallback); //bluetoothAdapter. .startDiscovery(); // .startLeScan(startLeScanCallback);
		log("onStartCommand().startLeScan()=Start" );

		return(1);
	}

	@Override
	public void onDestroy()
	{
		log("In VisyblScanService.onDestroy");
		super.onDestroy();

		log("VisyblScanService.onDestroy: calling stopLeScan");
		//bluetoothAdapter.stopLeScan(startLeScanCallback);
		bluetoothLeScanner.stopScan(startLeScanCallback);
	}



	private ScanCallback startLeScanCallback = new ScanCallback()
	{

		@Override
		public void onScanResult(int callbackType, ScanResult result) {
			super.onScanResult(callbackType, result);

			//log("ScanRecord:  " + result);
			BluetoothDevice device = result.getDevice();
			final int rssi = result.getRssi();
			byte[] scanRecord = result.getScanRecord().getBytes();
			final String deviceAddress = device.getAddress().trim().toUpperCase();

            Log.i(TAG, "ScanRecord:  " + scanRecord);

            /*
            if (device.getName() != null)
			{

				if (device.getAddress().trim().toUpperCase().equals("SAFE_LE"))
				{
					log("scan: " + device.getName() + " " + BytesToHexString(scanRecord));



				}


			}
			*/



			Beacon bcn = Beacon.parseFromBytes(deviceAddress, rssi, scanRecord);

			if(bcn==null)
			{
				return;
			}

			Visybl vis = Visybl.parseManufacturerDataBytes(bcn);

			if (vis == null)
			{
				return;
			}

			if (vis != null)
			{

				// Beacon seen first time
				if (!Visybl.beaconsLinkedHashMap.containsKey(vis.getDeviceName()))
				{


					Visybl.beaconsLinkedHashMap.put(vis.getDeviceName(), vis);
					Intent updateIntent = new Intent(Constants.ACTION_UPDATE_NEW);
					updateIntent.putExtra(Constants.EXTRA_NAME, vis.getDeviceName());
					getApplicationContext().sendBroadcast(updateIntent);



				}
				// Beacon is already known, update values
				else
				{
					Visybl oldVis = Visybl.beaconsLinkedHashMap.get(vis.getDeviceName());

					//log("Updating " + oldVis);

					oldVis.newAdvert(vis);

					Visybl.beaconsLinkedHashMap.put(oldVis.getDeviceName(), oldVis);

					Intent updateIntent = new Intent(Constants.ACTION_UPDATE_OLD);
					updateIntent.putExtra(Constants.EXTRA_NAME, vis.getDeviceName());
					getApplicationContext().sendBroadcast(updateIntent);



				}
			}

		}
	};

	private void decodeData (byte[] scanRecord)
	{


		byte[] manufacturerDataBytes = new byte[0];

		int currentPos = 0;
		try {
			while (currentPos < scanRecord.length)
			{
				if (scanRecord == null)
				{
					return;
				}

				int length = scanRecord[currentPos++] & 0xFF;
				if (length == 0)
				{
					break;
				}
				int dataLength = length - 1;
				// fieldType is unsigned int.
				int fieldType = scanRecord[currentPos++] & 0xFF;
				log("Before switch");
				switch (fieldType)
				{
					case HEADER_DATA:
					{
						manufacturerDataBytes = extractBytes(scanRecord, currentPos + 1,
								currentPos + 2);

						String s = new String(manufacturerDataBytes, StandardCharsets.UTF_8);

						log("Header : " + s );

					}

				}


			}

		}

		catch (Exception e)
		{
			log("unable to parse scan record: " + Arrays.toString(scanRecord) + "(" + e.toString() + ")");

			// As the record is invalid, ignore all the parsed results for this packet
			// and return an empty record with raw scanRecord bytes in results
			// return new Beacon(null, (short)0, null, null, -1, Integer.MIN_VALUE, null, scanRecord);

		}

	}


	protected static byte[] extractBytes(byte[] scanRecord, int start, int length)
	{
		byte[] bytes = new byte[length];
		System.arraycopy(scanRecord, start, bytes, 0, length);
		return bytes;
	}

	static String BytesToHexString(byte[] bytes)
	{
		if (bytes == null)
		{
			log("BytesToHexString(): " + "null input");
		}

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



}
