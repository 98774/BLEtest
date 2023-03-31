package com.example.bletest;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
        Button scanButton;
    TextView scanText;
    TextView debugText;
    UUID NUS_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    UUID RX_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    UUID TX_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");

    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public static final String TAG = "BluetoothLeService";
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTED = 2;
    public int connectionState;
    private final ScanCallback scanCallback = new ScanCallback() {
        private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    // successfully connected to the GATT Server
                    connectionState = STATE_CONNECTED;
                    sendBroadcast(new Intent(ACTION_GATT_CONNECTED));
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    // disconnected from the GATT Server
                    connectionState = STATE_DISCONNECTED;
                    sendBroadcast(new Intent(ACTION_GATT_DISCONNECTED));
                }
            }
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    sendBroadcast(new Intent(ACTION_GATT_SERVICES_DISCOVERED));
                } else {
                    Log.w(TAG, "onServicesDiscovered received: " + status);
                }
            }
        };
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            //BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner().stopScan(scanCallback);

            BluetoothDevice device = result.getDevice();
            //DeviceControlsActivity dca = new DeviceControlsActivity();

            // ...do whatever you want with this found device
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 3);
            }
            scanText.setText(device.getName());
            scanText.setText(device.getAddress());

//          //Connect to the device "LED Controller" in this case

            BluetoothGatt gatt = device.connectGatt(MainActivity.this, false, bluetoothGattCallback);
            gatt.connect();
            boolean disc = gatt.discoverServices();
            if(disc){
                Log.w(TAG, "dixover servicse" );
                scanText.setText("DISSDFSDfSDF");
                Log.w(TAG, gatt.getServices().toString());
            } else {
                Log.w(TAG, "cant discover");


            }
            //scanText.setText(device.getUuids().toString());

            /*BluetoothGattService service = gatt.getService(NUS_UUID);

            int properties = BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE;
            int permissions = BluetoothGattCharacteristic.PERMISSION_WRITE;
            BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(RX_UUID, properties, permissions);
            debugText.setText(characteristic.getService().toString());
            //byte[] test = new byte[] {(byte)0xaa, (byte)0x12, (byte)0x23};
            characteristic.setValue("this is a string".getBytes());
            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            boolean res;
            res = gatt.writeCharacteristic(characteristic);
            scanText.setText(Boolean.toString(res));
            */
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            // Ignore for now
        }

        @Override
        public void onScanFailed(int errorCode) {
            debugText.setText("Nothing Found");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scanButton = (Button) findViewById(R.id.scanButton);
        scanText = (TextView) findViewById(R.id.scanText);
        debugText = (TextView) findViewById(R.id.debugText);

        int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 4);
        } else {
            // if(areLocationServicesEnabled(this))
            debugText.setText("Permission not there");
        }
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();

        String[] names = new String[]{"LED Controller"};
        List<ScanFilter> filters = null;
        if (names != null) {
            filters = new ArrayList<>();
            for (String name : names) {
                ScanFilter filter = new ScanFilter.Builder()
                        .setDeviceName(name)
                        .build();
                filters.add(filter);
            }
        }

        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
                .setReportDelay(0L)
                .build();



        List<ScanFilter> finalFilters = filters;
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 2);
                }
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scanner.stopScan(scanCallback);
                    }
                }, 10000);
                scanner.startScan(finalFilters, scanSettings, scanCallback);
                scanText.setText("Scan Satret");


                /*
                if (scanner != null) {
                    scanner.startScan(finalFilters, scanSettings, scanCallback);
                    scanText.setText("Scan Satret");
                    //Log.d(TAG, "scan started");
                }  else {
                    scanText.setText("could not get scanner object");
                    //Log.e(TAG, "could not get scanner object");
                }
                */
                //broadcast intent to other devices

            }
        });
    }
}