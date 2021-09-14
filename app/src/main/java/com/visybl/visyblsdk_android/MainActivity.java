package com.visybl.visyblsdk_android;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


import com.visybl.api.Visybl;

import java.util.ArrayList;
import java.util.Iterator;


import static com.visybl.visyblsdk_android.Logger.log;


public class MainActivity extends AppCompatActivity
{

    private static final String TAG = "Main Activity";
    static BluetoothManager bluetoothManager;
    static BluetoothAdapter bluetoothAdapter;

    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;


    private static final int REQUEST_ENABLE_BT = 1;
    private static final String ID = "ID", BATTERY = "BATTERY", RSSI = "RSSI", TEMPERATURE = "TEMPERATURE", ADV_COUNT = "ADV_COUNT",
            BATT_ICON = "BATT_ICON", RSSI_ICON="RSSI_ICON";

    private static final String START = "Start", PAUSE = "Pause";

    private static String status = START;

    private IntentFilter mIntentFilter = null;

    SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_main);



        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Visybl App needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
                    }
                });
                builder.show();
            }
        }


        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Constants.ACTION_UPDATE_NEW);
        mIntentFilter.addAction(Constants.ACTION_UPDATE_OLD);

        initBT();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_FINE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Fine location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {

            String name = intent.getStringExtra(Constants.EXTRA_NAME);
            Visybl vis = Visybl.beaconsLinkedHashMap.get(name);

            //vis.getDeviceName();

            if (vis == null)
            {
                log("NULL Visybl");
                return;
            }

            if (intent.getAction().equals(Constants.ACTION_UPDATE_NEW))
            {
                log(vis.toString()+":"+vis.getHwVersion());
                addVisyblToTable(vis);

                updateTotal();



            }
            else if (intent.getAction().equals(Constants.ACTION_UPDATE_OLD))
            {
                updateTable(vis);
            }
        }
    };

    private void addVisyblToTable(Visybl vis)
    {
        TableLayout tLayout = (TableLayout) findViewById(R.id.beaconTableLayout);
        TableRow tRow = createTableRow(vis);
        tLayout.addView(tRow, new TableLayout.LayoutParams(
                TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
    }

    private void updateTable(Visybl vis)
    {
        int rssi = vis.getRssi();
        int batt = vis.getBatteryPercent();
        int temp = vis.getCurrentTemperature();
        int advCount = vis.getReceivedAdvCount();
        boolean blinkOnAdv = vis.getBlinkOnAdv();
        boolean inZone = vis.inZone();

        TableLayout tLayout = (TableLayout) findViewById(R.id.beaconTableLayout);
        TableRow tRow = (TableRow) tLayout.findViewWithTag(vis.getDeviceName());

        if (tRow == null)
        {
            log("tableRow = NULL!");
        }
        else
        {
            TextView tv = (TextView)tRow.findViewWithTag(ID);

            tv.setTypeface(null, vis.getSignificantChange()?Typeface.BOLD:Typeface.NORMAL);

            int background;
            if(vis.getState())
            {
                background = R.drawable.roundcorner_inzone;
            }
            else
            {
                background = R.drawable.roundcorner;
            }
            tv.setBackground(getResources().getDrawable(background));


            ImageView rssiIcon = (ImageView) tRow.findViewWithTag(RSSI_ICON);
            rssiIcon.setImageResource(getRssiIcon(rssi));

            if(blinkOnAdv)
            {
                Animation anim = new AlphaAnimation(0.0f, 1.0f);
                anim.setDuration(50);
                anim.setStartOffset(0);
                //anim.setRepeatMode(Animation.REVERSE);
                anim.setRepeatCount(0);
                rssiIcon.startAnimation(anim);
            }


            TextView rssiTv = (TextView) tRow.findViewWithTag(RSSI);
            rssiTv.setText(rssi + "");

            ImageView battIcon = (ImageView) tRow.findViewWithTag(BATT_ICON);
            battIcon.setImageResource(getBatteryIcon(batt));

            TextView battTv = (TextView) tRow.findViewWithTag(BATTERY);
            battTv.setText(batt + "");

            TextView tempTv = (TextView) tRow.findViewWithTag(TEMPERATURE);
            //tempTv.setText(String.format("%+.2f", temp));
            tempTv.setText(temp + " C");
            TextView advCountTv = (TextView) tRow.findViewWithTag(ADV_COUNT);
            advCountTv.setText(advCount + "");

        }
    }

    private void updateTotal()
    {
        TextView tv = ((TextView) findViewById(R.id.totalCountTextView));
        tv.setTextColor(Color.BLACK);
        tv.setText(String.format("%03d", Visybl.beaconsLinkedHashMap.size()));
    }

    // Updates entire UI
    // To handle screen rotations
    public void updateUI()
    {
        if (status.compareTo(PAUSE) == 0) // scanning in progress already
        {
            TextView tv = ((TextView) findViewById(R.id.startPauseButton));
            tv.setText(PAUSE);
            tv.setTag(PAUSE);
        }

        TableLayout tLayout = (TableLayout) findViewById(R.id.beaconTableLayout);
        tLayout.removeAllViews();
        Iterator keys = Visybl.beaconsLinkedHashMap.keySet().iterator();
        while (keys.hasNext())
        {
            Visybl v = Visybl.beaconsLinkedHashMap.get(keys.next());
            addVisyblToTable(v);
        }

        updateTotal();
    }

    //This creates the Table row in UI to show Visybl beacon Info

    TableRow createTableRow(final Visybl vis)
    {
        TableRow tRow = new TableRow(getApplicationContext());
        tRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView beaconNameTextView = new TextView(this);
        int background;
        if(vis.getState())
        {
            background = R.drawable.roundcorner_inzone;
        }
        else
        {
            background = R.drawable.roundcorner;
        }
        beaconNameTextView.setBackground(getResources().getDrawable(background));
        beaconNameTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        beaconNameTextView.setTextColor(Color.BLACK);

        TableRow.LayoutParams lp5 = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 5.5f);
        beaconNameTextView.setLayoutParams(lp5);
        String label;
        if(vis.getFriendlyName() == null)
        {
            label = vis.getDeviceName();
        }
        else
        {
            label = vis.getFriendlyName();
        }
        beaconNameTextView.setText(label);
        beaconNameTextView.setTag(ID);


        // show VisyblActivity for beacon
        beaconNameTextView.setOnClickListener(new View.OnClickListener()
        {
            // Toggles between showing name or MAC address
            @Override
            public void onClick(View v)
            {

                //Add code here for showing detail activity

            }
        });


        tRow.addView(beaconNameTextView);

        TableRow.LayoutParams lp2 = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f);
        TableRow.LayoutParams lp1 = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);

        TextView temperatureTextView = new TextView(this);
        temperatureTextView.setTag(TEMPERATURE);
        temperatureTextView.setTextColor(Color.BLACK);
        temperatureTextView.setText(vis.getCurrentTemperature()+" C");
        temperatureTextView.setLayoutParams(lp2);
        temperatureTextView.setGravity(Gravity.RIGHT);
        tRow.addView(temperatureTextView);

        ImageView tempIcon = new ImageView(this);
        tempIcon.setImageResource(R.drawable.temp);
        tempIcon.setLayoutParams(lp1);
        tRow.addView(tempIcon);

        int rssi = vis.getRssi();
        TextView rssiTextView = new TextView(this);
        rssiTextView.setTag(RSSI);
        rssiTextView.setText(rssi + "");
        rssiTextView.setTextColor(Color.BLACK);
        rssiTextView.setLayoutParams(lp1);
        rssiTextView.setGravity(Gravity.RIGHT);
        tRow.addView(rssiTextView);

        ImageView rssiIcon = new ImageView(this);
        rssiIcon.setTag(RSSI_ICON);
        rssiIcon.setImageResource(getRssiIcon(rssi));
        rssiIcon.setLayoutParams(lp1);
        tRow.addView(rssiIcon);

        int batteryPercent = vis.getBatteryPercent();
        TextView battTextView = new TextView(this);
        battTextView.setTag(BATTERY);
        battTextView.setText(batteryPercent + "");
        battTextView.setTextColor(Color.BLACK);
        battTextView.setLayoutParams(lp1);
        battTextView.setGravity(Gravity.RIGHT);
        tRow.addView(battTextView);

        ImageView battIcon = new ImageView(this);
        battIcon.setTag(BATT_ICON);
        battIcon.setImageResource(getBatteryIcon(batteryPercent));
        battIcon.setLayoutParams(lp1);
        tRow.addView(battIcon);

        TextView advCountTextView = new TextView(getApplicationContext());
        advCountTextView.setTag(ADV_COUNT);
        advCountTextView.setTextColor(Color.BLACK);
        advCountTextView.setText(vis.getReceivedAdvCount() + "");
        advCountTextView.setLayoutParams(lp2);
        advCountTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        tRow.addView(advCountTextView);


        tRow.setTag(vis.getDeviceName());
        // tRow.setBackgroundColor(Color.RED);

        return (tRow);
    }

    private int getBatteryIcon(int level)
    {
        if (level > 75)
        {
            return (R.drawable.stat_sys_battery_100);
        }
        else if (level > 50)
        {
            return (R.drawable.stat_sys_battery_71);
        }
        else if (level > 25)
        {
            return (R.drawable.stat_sys_battery_43);
        }
        else
        {
            return (R.drawable.stat_sys_battery_10);
        }
    }

    private int getRssiIcon(int level)
    {
        if (level > -40 )
        {
            return (R.drawable.stat_sys_signal_4_cdma);
        }
        else if (level > -45)
        {
            return (R.drawable.stat_sys_signal_3_cdma);
        }
        else if (level > -55)
        {
            return (R.drawable.stat_sys_signal_2_cdma);
        }
        else if (level > -75)
        {
            return (R.drawable.stat_sys_signal_1_cdma);
        }
        else
        {
            return (R.drawable.stat_sys_signal_0_cdma);
        }
    }

    Button startPauseButton;

    public void startPauseOnClickHandler(View b)
    {
        log("In startPauseOnClickHandler");

        String tag = b.getTag().toString();
        if (tag.compareToIgnoreCase(START) == 0)
        {
            ComponentName cn = startService (new Intent(getApplicationContext(), VisyblScanService.class));


            if(cn!=null)
            {
                b.setTag(PAUSE);
                ((Button) b).setText(PAUSE);

                startTimerThread();


            }
            else
            {
                log("Error starting Service");
                //bluetoothAdapter.stopLeScan(startLeScanCallback);
            }
        }
        else
        {
            log("stopLeScan()");
            //bluetoothAdapter.stopLeScan(startLeScanCallback);
            stopService(new Intent(getApplicationContext(), VisyblScanService.class));
            b.setTag(START);
            ((Button) b).setText(START);


        }

        status = b.getTag().toString(); // to handle screen rotations
    }


    private long timerCount = 0;

    private void startTimerThread()
    {
        final Handler handler = new Handler();

        final long timerInterval = 100;

        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                long millis = (timerCount % 1000) / 100;
                long x = timerCount / 1000;
                long seconds = x % 60;
                x /= 60;
                long minutes = x % 60;
                x /= 60;
                long hours = x % 24;

                TextView tv = (TextView) findViewById(R.id.timerTextView);
                // PVS
                if(tv!=null)
                    tv.setText(String.format("%02d:%02d:%02d:%01d", hours, minutes, seconds, millis));
                timerCount += timerInterval;

                Button b = (Button) findViewById(R.id.startPauseButton);
                String tag = b.getTag().toString();
                if (tag.compareToIgnoreCase(START) != 0)
                {
                    handler.postDelayed(this, timerInterval);
                }
            }
        };

        handler.postDelayed(runnable, 100);

    }

    public void clearOnClickHandler(View b)
    {
        log("In clearOnClickHandler");

        timerCount = 0;




        Visybl.beaconsLinkedHashMap.clear();

        TableLayout tLayout = (TableLayout) findViewById(R.id.beaconTableLayout);
        tLayout.removeAllViews();

        updateTotal();
    }

    public void settingsOnClickHandler(View b)
    {
        log("In settingsOnClickHandler");

    }



    // initializes the app state
    void initApp()
    {
        startPauseButton = (Button) findViewById(R.id.startPauseButton);
        startPauseButton.setTag(START);
        startPauseButton.setEnabled(true);

    }


    // Checks whether BT is OK, enabled etc
    void initBT()
    {
        log("In initBT()");

        if (!getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE))
        {
            Toast.makeText(this, "This device does not support Visybl beacons!",
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        if (bluetoothManager == null)
        {
            Toast.makeText(this, "No Bluetooth on this device", Toast.LENGTH_SHORT)
                    .show();
            finish();
        }

        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else
        {
            initApp();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        log("onActivityResult(): resultCode=" + resultCode);
        if (requestCode == REQUEST_ENABLE_BT)
        {
            if (resultCode == RESULT_OK)
            {
                initBT();
            }
        } else
        {
            finish();
        }
    }

    protected void onResume()
    {
        super.onResume();
        log("In onResume()");

        if (mIntentFilter != null)
            registerReceiver(mIntentReceiver, mIntentFilter);

        updateUI();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        log("Main.onPause(): unregistering mIntentReceiver");
        unregisterReceiver(mIntentReceiver);
    }

}