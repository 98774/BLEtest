package com.example.bletest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Button scanButton;
    TextView scanText;
    TextView debugText;
    IntentFilter scanIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
    BroadcastReceiver scanModeReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                int modeValue = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                if (modeValue == BluetoothAdapter.SCAN_MODE_CONNECTABLE) {
                    scanText.setText("The device is not in discoverable mode but can still recieve conections");
                } else if (modeValue == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    scanText.setText("The device is in discoverable mode");
                } else if (modeValue == BluetoothAdapter.SCAN_MODE_NONE) {
                    scanText.setText("The device is in discoverable mode and cannot recieve connections");
                } else {
                    scanText.setText("Error");
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scanButton = (Button) findViewById(R.id.scanButton);
        scanText = (TextView) findViewById(R.id.scanText);
        debugText = (TextView) findViewById(R.id.debugText);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //broadcast intent to other devices
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 8);
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
                    debugText.setText("Requesting permissions");
                    scanText.setText("DSFf");
                }

                startActivity(discoverableIntent);

                Thread2 t=new Thread2();
                t.start();
            }
        });

        registerReceiver(scanModeReciever, scanIntentFilter);
    }

    Handler handler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            debugText.setText(String.valueOf((message.arg1)));
            return false;
        }
    });
    private class Thread2 extends Thread
    {
        public void run()
        {
            for(int i = 0 ; i < 50 ; i++){
                //This doesn't work:
                // debugText.setText(String.valueOf(i));

                //Do this instead
                Message message=Message.obtain();
                message.arg1=i;
                handler.sendMessage(message);


                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}