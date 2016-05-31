# visyblsdk-android
Android App with Visybl SDK to read and display Visybl beacons parameters.

Visybl.jar is used to read BLE packet coming from Visybl Beacons and provides an easier way to access all the parameters with few lines of code inside any Android App.

This example Android project provides a fully working App using Visybl.jar file. Find the below 3 steps to use Visybl.jar in any Android App.

Step 1: import com.visybl.api.Visybl;

Step 2: Visybl vis = Visybl.beaconsLinkedHashMap.get(name);

Step 3: Read all parameters from vis.

e.g.

      int rssi = vis.getRssi();  //get RSSI
      int batt = vis.getBatteryPercent();  //get battery percent
      int temp = vis.getCurrentTemperature(); //get current Temperature
      int advCount = vis.getReceivedAdvCount(); //get total Advertisement count
      boolean blinkOnAdv = vis.getBlinkOnAdv(); //get blink on Advertisement
      boolean state = vis.getState() //get button state on or off
