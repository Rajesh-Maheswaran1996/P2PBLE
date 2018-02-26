package com.p2pble;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
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
import android.os.ParcelUuid;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    private static String TAG="ACT";
    WifiManager wMan;
    List<ScanResult> wifiList;
    TextView tv;
    StringBuilder sb;
    Button b1;
    Button b2;
    Button b3;
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
    private EditText dname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        setTitle("Available Hosts");
        checkpermissions();
        advertise();

        b1 = (Button) findViewById(R.id.Set);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                set(view);
            }
        });

        b2 = (Button) findViewById(R.id.host);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                host(view);
            }
        });

        b3 = (Button) findViewById(R.id.button);
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addgroup(view);
            }
        });

        boolean permission;
        Context context=getApplicationContext();
        wMan = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        macrssi=new HashMap<String, Integer>();
        expListView=(ExpandableListView)findViewById(R.id.el1);
        p2pReceiver p2prec = new p2pReceiver();
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
        in.putExtra("dname",dname.getText().toString());
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

    private void advertise() {
        BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode( AdvertiseSettings.ADVERTISE_MODE_BALANCED )
                .setTxPowerLevel( AdvertiseSettings.ADVERTISE_TX_POWER_HIGH )
                .setConnectable(false)
                .build();

        ParcelUuid pUuid = new ParcelUuid( UUID.fromString( getString( R.string.ble_uuid ) ) );
        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName( true )
                .addServiceData( pUuid, "hi".getBytes(Charset.forName("UTF-8") ) )
                .build();

        AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
                Toast.makeText(getApplicationContext(),"CallBack called",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartFailure(int errorCode) {
                Log.e( "BLE", "Advertising onStartFailure: " + errorCode );
                super.onStartFailure(errorCode);
            }
        };

        advertiser.startAdvertising( settings, data, advertisingCallback );
        Toast.makeText(this,"Advertisement started",Toast.LENGTH_SHORT).show();
    }


    void host(View view){
        dname=(EditText)findViewById(R.id.dname);
        Intent in = new Intent(this,host.class);
        in.putExtra("dname",dname.getText().toString());
        startActivity(in);

    }
    void set(View view)
    {
        dname=(EditText)findViewById(R.id.dname);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        setDeviceName(dname.getText().toString());
        dname.setEnabled(false);
        Button b =(Button)findViewById(R.id.Set);
        b.setEnabled(false);

        wpMan = (WifiP2pManager) getApplicationContext().getSystemService(Context.WIFI_P2P_SERVICE);
        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        p2pReceiver p2prec = new p2pReceiver();
        mChannel = mManager.initialize(this, getMainLooper(), null);

        registerReceiver(p2prec,intentFilter);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
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
            }
        });
        t.start();




        disconnect();
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
    public void setDeviceName(String devName) {

       mBluetoothAdapter.setName(devName);

        try {
            Class[] paramTypes = new Class[3];
            paramTypes[0] = WifiP2pManager.Channel.class;
            paramTypes[1] = String.class;
            paramTypes[2] = WifiP2pManager.ActionListener.class;
            Method setDeviceName = mManager.getClass().getMethod(
                    "setDeviceName", paramTypes);
            setDeviceName.setAccessible(true);

            Object arglist[] = new Object[3];
            arglist[0] = mChannel;
            arglist[1] = devName;
            arglist[2] = new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                    Log.e
                            ("tag","setDeviceName succeeded");
                }

                @Override
                public void onFailure(int reason) {
                    Log.e("tag","setDeviceName failed");
                }
            };

            setDeviceName.invoke(mManager, arglist);

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }
}
