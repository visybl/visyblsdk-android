package com.visybl.visyblsdk_android;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.os.IBinder;
import android.widget.Toast;

import com.visybl.api.Beacon;
import com.visybl.api.Visybl;


import static com.visybl.visyblsdk_android.Logger.log;

public class VisyblScanService extends Service
{


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

	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		log("In VisyblScanService.onStartCommand");
		super.onStartCommand(intent, flags, startId);

		boolean retval = bluetoothAdapter.startLeScan(startLeScanCallback);
		log("onStartCommand().startLeScan()=" + retval);

		return(0);
	}

	@Override
	public void onDestroy()
	{
		log("In VisyblScanService.onDestroy");
		super.onDestroy();

		log("VisyblScanService.onDestroy: calling stopLeScan");
		bluetoothAdapter.stopLeScan(startLeScanCallback);
	}


	private BluetoothAdapter.LeScanCallback startLeScanCallback = new BluetoothAdapter.LeScanCallback()
	{
		@Override
		public void onLeScan(BluetoothDevice device, final int rssi, byte[] scanRecord)
		{
			final String deviceAddress = device.getAddress().trim().toUpperCase();

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
}
