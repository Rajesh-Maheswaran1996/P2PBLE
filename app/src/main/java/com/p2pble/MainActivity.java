package com.p2pble;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    private static final int REQUEST_ENABLE_BT = 0;
    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private StringBuilder sb;
    private WifiManager wMan;
    private WifiP2pManager wpMan;
    private TextView tx;
    List<WifiP2pDevice> devlist =new ArrayList<>();
    private RadioGroup g1;

    private LinearLayout ll;
    WifiP2pManager.PeerListListener plist=new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
            ll=(LinearLayout)findViewById(R.id.a1);
            tx=(TextView)findViewById(R.id.tx1);
            WifiP2pDevice dev;
            wMan.startScan();
            Iterator<WifiP2pDevice> i= wifiP2pDeviceList.getDeviceList().iterator();
            tx.setText("Devices Available :\n");
            devlist.clear();
            ll.removeAllViews();
            g1=new RadioGroup(MainActivity.this);
            ll.addView(g1);
            while(i.hasNext()){
                dev=i.next();
                devlist.add(dev);
                tx.setText(tx.getText().toString()+dev.deviceName+"\n");
                Log.d("Hello",dev.toString());
                    RadioButton c1 = new RadioButton(MainActivity.this);
                    c1.setText(dev.deviceName);
                    g1.addView(c1);

                //Toast.makeText(MainActivity.this,dev.toString(), Toast.LENGTH_SHORT).show();
                /*
                if(dev.deviceName.equals("AMUDA4")){
                    WifiP2pConfig config = new WifiP2pConfig();
                    config.deviceAddress = dev.deviceAddress;
                    config.wps.setup = WpsInfo.PBC;
                    mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                            Toast.makeText(MainActivity.this, "Connected to AMUDA1", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onFailure(int reason) {
                            Toast.makeText(MainActivity.this, "Connect failed. Retry.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                */
            }
        }
    };
    private WifiP2pDevice dev;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // BluetoothAdapter mBluetoothAdapter;
// Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

    checkpermissions();
        tx=(TextView)findViewById(R.id.tx1);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        wMan = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wpMan = (WifiP2pManager) getApplicationContext().getSystemService(Context.WIFI_P2P_SERVICE);
        p2pReceiver p2prec = new p2pReceiver();
        registerReceiver(p2prec,intentFilter);
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "Peers Discovery Initiated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int i) {

            }
        });
    scanLeDevice(true);
    }

    class p2pReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("P2P","ONRECEIVE");
            String action = intent.getAction();
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                // Determine if Wifi P2P mode is enabled or not, alert
                // the Activity.
                Log.d("P2P","STATE_CHANGED_ACTION");
                Toast.makeText(context, "STATE_CHANGED_ACTION", Toast.LENGTH_SHORT).show();

            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                // The peer list has changed!  We should probably do something about
                // that.
                mManager.requestPeers(mChannel,plist);
                Toast.makeText(context, "PEERS_CHANGED_ACTION", Toast.LENGTH_SHORT).show();
                Log.d("P2P","PEERS_CHANGED_ACTION");

            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                Toast.makeText(context, "CONNECTION_CHANGED_ACTION", Toast.LENGTH_SHORT).show();

            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                Toast.makeText(context, "THIS_DEVICE_CHANGED_ACTION", Toast.LENGTH_SHORT).show();
            }
        }
    }

void checkpermissions(){
    if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    if (ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {

            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.

        } else {

            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }
    }
}
void p2pcon(View view)
{
    int radioButtonID = g1.getCheckedRadioButtonId();
    RadioButton radioButton =(RadioButton) g1.findViewById(radioButtonID);
    final String info= radioButton.getText().toString();
    Iterator i =devlist.iterator();
    while (i.hasNext())
    {
        dev=(WifiP2pDevice) i.next();
        if(dev.deviceName.equals(info))
        {
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = dev.deviceAddress;
            config.wps.setup = WpsInfo.PBC;
            mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                    Toast.makeText(MainActivity.this, "Connected to "+info, Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onFailure(int reason) {
                    Toast.makeText(MainActivity.this, "Connect failed. Retry.",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
}
void p2pdiscon(View view)
{
    Toast.makeText(this, "P2P   Discon", Toast.LENGTH_SHORT).show();
    if (mManager != null && mChannel != null) {
        mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup group) {
                if (group != null && mManager != null && mChannel != null
                        && group.isGroupOwner()) {
                    mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {

                        @Override
                        public void onSuccess() {
                            Log.d("TAG", "removeGroup onSuccess -");
                        }

                        @Override
                        public void onFailure(int reason) {
                            Log.d("TAG", "removeGroup onFailure -" + reason);
                        }
                    });
                }
            }
        });
    }
    mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
        @Override
        public void onSuccess() {
            Toast.makeText(MainActivity.this, "Peers Discovery Initiated", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailure(int i) {

        }
    });
}
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 1000000;

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }

    }
// Device scan callback.
private BluetoothAdapter.LeScanCallback mLeScanCallback =
        new BluetoothAdapter.LeScanCallback() {
@Override
public void onLeScan(final BluetoothDevice device, int rssi,
                     byte[] scanRecord) {
        runOnUiThread(new Runnable() {
@Override
public void run() {
      //  mLeDeviceListAdapter.addDevice(device);
       // mLeDeviceListAdapter.notifyDataSetChanged();
    Toast.makeText(MainActivity.this, "Device "+device.getName()+"found", Toast.LENGTH_SHORT).show();
        }
        });
        }
        };
}
