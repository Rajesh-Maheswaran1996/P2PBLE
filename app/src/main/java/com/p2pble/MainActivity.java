package com.p2pble;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    private static String TAG="ACT";
    WifiManager wMan;
    List<ScanResult> wifiList;
    TextView tv;
    StringBuilder sb;
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    private static final int REQUEST_ENABLE_BT = 0;
    HashMap<String,Integer> macrssi ;
    private static int CODE_WRITE_SETTINGS_PERMISSION;
    private static WifiP2pManager mManager;
    private static WifiP2pManager.Channel mChannel;
    private WifiP2pManager wpMan;
    private android.content.IntentFilter intentFilter;
    private BluetoothAdapter mBluetoothAdapter;
    private WifiP2pDeviceList devlist;
    WifiP2pManager.PeerListListener plist=new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
            WifiP2pDevice dev;
           devlist=wifiP2pDeviceList;
            listDataHeader = new ArrayList<String>();
            listDataChild = new HashMap<String, List<String>>();
            wMan.startScan();
            Iterator<WifiP2pDevice> i= wifiP2pDeviceList.getDeviceList().iterator();
            List<String>info = new ArrayList<>();
            while(i.hasNext()){
                info = new ArrayList<>();
            dev=i.next();
            info.add(dev.deviceAddress);
            listDataHeader.add(dev.deviceName);
            listDataChild.put(dev.deviceName, info);
            }
            listAdapter = new com.p2pble.ExpandableListAdapter(MainActivity.this, listDataHeader, listDataChild);
            expListView.setAdapter(listAdapter);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Available Hosts");
        checkpermissions();

        boolean permission;
        Context context=getApplicationContext();
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        wMan = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wpMan = (WifiP2pManager) getApplicationContext().getSystemService(Context.WIFI_P2P_SERVICE);
         final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        macrssi=new HashMap<String, Integer>();
        expListView=(ExpandableListView)findViewById(R.id.el1);
        p2pReceiver p2prec = new p2pReceiver();
        registerReceiver(p2prec,intentFilter);
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "Peers Discovery Initiated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int i) {
                Toast.makeText(MainActivity.this, "Peer Discovery Failed", Toast.LENGTH_SHORT).show();

            }
        });
disconnect();

    }
    void ref(View view) {
        //Snackbar.make(findViewById(R.id.R2), "Wifi Search Initiated", Snackbar.LENGTH_SHORT).show();
      //  wMan.startScan();
    }

    void addgroup(View view)
    {
        ref(view);
        Bundle b = new Bundle();
        //b.putSerializable("wifiList", (Serializable) wifiList);
        Intent in = new Intent(this,GroupCreation.class);
        //b.putSerializable("macrssi",macrssi);
        in.putExtras(b);
        startActivity(in);
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

    class wifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO: This method is called when the BroadcastReceiver is receiving
            sb = new StringBuilder();
            listDataHeader = new ArrayList<String>();
            listDataChild = new HashMap<String, List<String>>();
            //wifiList = wMan.getScanResults();
            sb.append("\n        Number Of Wifi connections :"+wifiList.size()+"\n\n");
            //List<List<String>> tot = new ArrayList<List<String>>();
            //Toast.makeText(context, "Scanned"+wifiList.size(), Toast.LENGTH_SHORT).show();
            Log.d("TAG","Scanned");

            for(int i = 0; i < wifiList.size(); i++){
                String ssi=wifiList.get(i).SSID;
                Boolean b =ssi.equals("HelloMoto");
                Log.d("WiFiList",ssi+b.toString());
                List<String> info=new ArrayList<String>();
                listDataHeader.add((i+1)+"."+wifiList.get(i).SSID);
                info.add("MAC:"+wifiList.get(i).BSSID);
                info.add("RSSI:"+wifiList.get(i).level);
                //Log.d("TXP:",wifiList.get(i).toString());
                // Toast.makeText(context, wifiList.get(i).toString(), Toast.LENGTH_SHORT).show();
                macrssi.put(wifiList.get(i).BSSID,wifiList.get(i).level);
                listDataChild.put(listDataHeader.get(i), info);

            }
            listAdapter = new com.p2pble.ExpandableListAdapter(context, listDataHeader, listDataChild);
            expListView.setAdapter(listAdapter);
        }
    }
    void host(View view){

        Intent in = new Intent(this,host.class);
        startActivity(in);

    }
    void checkpermissions(){

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        if (wMan == null || !wMan.isWifiEnabled()) {
            wMan=(WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wMan.setWifiEnabled(true);

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
    public static void disconnect() {
        if (mManager != null && mChannel != null) {
            mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group) {
                    if (group != null && mManager != null && mChannel != null
                            && group.isGroupOwner()) {
                        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "removeGroup onSuccess -");
                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.d(TAG, "removeGroup onFailure -" + reason);
                            }
                        });
                    }
                }
            });
        }
    }
}
