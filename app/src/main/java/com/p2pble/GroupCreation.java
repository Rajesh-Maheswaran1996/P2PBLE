package com.p2pble;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.util.MutableChar;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class GroupCreation extends AppCompatActivity implements Serializable {
    private static String TAG="v";
    FloatingActionButton fab;
    WifiManager wMan;
    HashMap<String, Integer> macrssi;
    List<ScanResult> wifiList;
    LinearLayout sv;
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    String devip;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    private RadioGroup g1;
    private TextView dname;
    List<String> namelist;
    private static WifiP2pManager mManager;
    private WifiP2pManager wpMan;
    private static WifiP2pManager.Channel mChannel;
    TextView stat;
    private WifiP2pDeviceList devlist;
    WifiP2pManager.PeerListListener plist=new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
            WifiP2pDevice dev;
            devlist=wifiP2pDeviceList;
            init();
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
            listAdapter = new com.p2pble.ExpandableListAdapter(GroupCreation.this, listDataHeader, listDataChild);
//            expListView.setAdapter(listAdapter);

        }
    };
    private WifiP2pDevice dev;
    private String peerip;
    private WifiP2pDevice devx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_creation);
        dname=(EditText)findViewById(R.id.dname);
        dname.setText(getIntent().getStringExtra("dname"));
        setTitle("Join Group");
        wMan = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        wMan = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wpMan = (WifiP2pManager) getApplicationContext().getSystemService(Context.WIFI_P2P_SERVICE);
        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        GroupCreation.p2pReceiver p2prec = new GroupCreation.p2pReceiver();
        registerReceiver(p2prec,intentFilter);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                wpMan.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d("WIFIP2P","Peer Discovery Sucessful");
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.d("WIFIP2P","Peer Discovery Failed !" );
                    }
                });
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
        init();
    }

    private void init() {
        sv=(LinearLayout) findViewById(R.id.sv);
        sv.removeAllViews();
        Snackbar.make(findViewById(R.id.R1),"View Refreshed", Snackbar.LENGTH_SHORT).show();

         g1=new RadioGroup(this);
        sv.addView(g1);
if(devlist!=null){
        Iterator<WifiP2pDevice> devx=devlist.getDeviceList().iterator();
    while(devx.hasNext()) {
        dev = devx.next();
        RadioButton c1 = new RadioButton(this);
        c1.setText(dev.deviceName);
        g1.addView(c1);
    }
    }}
    void invite(View view) throws InterruptedException {
        dname = (TextView) findViewById(R.id.dname);

    setDeviceName(dname.getText().toString());
        stat=(TextView)findViewById(R.id.stat);
        int radioButtonID = g1.getCheckedRadioButtonId();
        RadioButton radioButton =(RadioButton) g1.findViewById(radioButtonID);
       final String info= radioButton.getText().toString();
      stat.setText("trying to connect to "+info);
       Iterator i=devlist.getDeviceList().iterator();
      while(i.hasNext())
      {
          dev=(WifiP2pDevice) i.next();
          if(dev.deviceName.equals(info))
          {
              WifiP2pConfig config = new WifiP2pConfig();
              config.deviceAddress = dev.deviceAddress;
              mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                  @Override
                  public void onSuccess() {

                      mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
                          @Override
                          public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
                              while(!wifiP2pInfo.groupFormed){
                                  stat.setText("Group not joined !\nFailure");
                              }
                              Toast.makeText(GroupCreation.this, "Group Formed !", Toast.LENGTH_SHORT).show();
                          }
                      });

                      // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                      Toast.makeText(GroupCreation.this, "Connected cha  to "+info, Toast.LENGTH_SHORT).show();
                        stat.setText("Connected to "+info);
                      dname = (TextView) findViewById(R.id.dname);
                      final String invite = "JOIN-GROUP_" + dname.getText().toString() + "_";


//                      stat.setText("Connected to "+info+"\nIP:"+devip);
//                      // Log.d("IP",getIPFromMac(""));
//                      if(devip.equals("192.168.49.1"))
//                          peerip="192.168.49.10";
//                      else
//                          peerip="192.168.49.1";
//
//                      Thread t = new Thread() {
//                          @Override
//                          public void run() {
//                              try {
//                                  UdpClientThread send = new UdpClientThread(invite.getBytes(), peerip, 4445);
//                                  send.start();
//                                 // Toast.makeText(GroupCreation.this, "Invite sent to "+peerip, Toast.LENGTH_SHORT).show();
//
//                              } catch (Exception e) {
//                              e.printStackTrace();
//                              }
//
//                          }
//                      };
//                      t.start();
//
//                      stat.setText(stat.getText().toString()+"\nInvite sent to"+peerip);
                      mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
                          @Override
                          public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
                            if(wifiP2pGroup!=null){
                              Iterator<WifiP2pDevice> i=wifiP2pGroup.getClientList().iterator();
                              stat.setText("Connected to :\n");
                              while(i.hasNext())
                              {
                                  devx=i.next();
                                  stat.setText(stat.getText().toString()+devx.deviceName+"\n");
                              }
                          }}
                      });
                  }

                  @Override
                  public void onFailure(int reason) {
                      Toast.makeText(GroupCreation.this, "Connect failed. Retry.",
                              Toast.LENGTH_SHORT).show();
                      stat.setText("Connection Failed "+info);

                  }
              });

          }
      }

        myhandler handler = new myhandler(this);
        Inviteresponse x = new Inviteresponse(4445,handler,1);
        x.start();
    }
    public static boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }

        return networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED;
    }
    void ref(View view){
        dname = (TextView) findViewById(R.id.dname);
       setDeviceName(dname.getText().toString());

    }
    class wifiReceiver extends BroadcastReceiver implements Serializable {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO: This method is called when the BroadcastReceiver is receiving

            listDataHeader = new ArrayList<String>();
            listDataChild = new HashMap<String, List<String>>();
            wifiList = wMan.getScanResults();
            //List<List<String>> tot = new ArrayList<List<String>>();

            for(int i = 0; i < wifiList.size(); i++){
                List<String> info=new ArrayList<String>();
                listDataHeader.add((i+1)+"."+wifiList.get(i).SSID);
                info.add("MAC:"+wifiList.get(i).BSSID);
                info.add("RSSI:"+wifiList.get(i).level);

                macrssi.put(wifiList.get(i).BSSID,wifiList.get(i).level);
                listDataChild.put(listDataHeader.get(i), info);
            }
            init();
        }
    }
    private static HashMap<String, String> nameip;
    private static HashMap<String, String> ipname;

    public static class myhandler extends Handler implements Serializable {
        private GroupCreation parent;
        private int ct=0;
        private int dsa=0;


        public myhandler(GroupCreation parent) {
            super();
            this.parent=parent;
        }

        @Override
        public void handleMessage(Message msg) {
            DatagramPacket packet= (DatagramPacket) msg.obj;
        int io=0;
                Object m = null;
                try {
                    m = parent.deserialize(packet.getData());
                    if(m instanceof ArrayList)
                        parent.namelist=(ArrayList)m;
                    else if (parent.nameip == null)
                        parent.nameip = (HashMap<String, String>) m;
                    else
                        parent.ipname = (HashMap<String, String>) m;
                    io = 1;
                    if (ipname != null)
                        Log.d("RECEIVED HashMap :", ipname.toString());
                    if(nameip!=null &&ipname!=null)
                    {   Intent in = new Intent(parent, GroupSelect.class);
                    Bundle b = new Bundle();
                    in.putExtra("SEN", false);
                    in.putExtra("Name", parent.dname.getText().toString());
                    b.putSerializable("nameip", parent.nameip);
                    b.putSerializable("ipname", parent.ipname);
                        b.putSerializable("namelist", (Serializable) parent.namelist);
                    in.putExtras(b);
                    parent.startActivity(in);
                }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
        if(io==0){
                String invite=new String(packet.getData());
            String address=packet.getAddress().toString();
            String x[]=invite.split("_");
            parent.disconnect();
            parent.wpMan.createGroup(parent.mChannel,null);
            switch (msg.what) {
                case 1:
                    if (x[1].equals("ACCEPT")) {
                        Toast.makeText(parent, "Invitation Accepted !", Toast.LENGTH_SHORT).show();
                        parent.stat.setText(parent.stat.getText().toString()+"\nInvitation Accepted !");
                    dsa++;
                    }
                        else if (x[1].equals("REJECT")) {
                        parent.stat.setText(parent.stat.getText().toString()+"\nInvitation Rejected !");
                        Toast.makeText(parent, "Invitation Rejected !", Toast.LENGTH_SHORT).show();
                    }
            }}
    }}
    public static byte[] serialize(Object obj) throws IOException {

        try(ByteArrayOutputStream b = new ByteArrayOutputStream()){
            try(ObjectOutputStream o = new ObjectOutputStream(b)){
                o.writeObject(obj);

            }
            return b.toByteArray();
        }
    }

    public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try(ByteArrayInputStream b = new ByteArrayInputStream(bytes)){
            try(ObjectInputStream o = new ObjectInputStream(b)){
                return o.readObject();
            }
        }
    }

    class p2pReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("P2P","ONRECEIVE");
            String action = intent.getAction();
            mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
                    if(wifiP2pGroup!=null){
                        Iterator<WifiP2pDevice> i=wifiP2pGroup.getClientList().iterator();
                        stat.setText("Connected to :\n");
                        while(i.hasNext())
                        {
                            devx=i.next();
                            stat.setText(stat.getText().toString()+devx.deviceName+"\n");
                        }
                    }}
            });
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
                NetworkInfo networkInfo = (NetworkInfo) intent
                        .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                if (networkInfo.isConnected()) {
                    stat.setText("Connected ");
                    // We are connected with the other device, request connection
                    // info to find group owner IP
                    mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
                        @Override
                        public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
                            stat=(TextView)findViewById(R.id.stat);
                            if(wifiP2pGroup!=null){
                                Iterator<WifiP2pDevice> i=wifiP2pGroup.getClientList().iterator();
                                stat.setText("Conencted to :\n");
                                while(i.hasNext())
                                {
                                    devx=i.next();
                                    //nodes.add(devx);
                                    stat.setText(stat.getText().toString()+"\n"+devx.deviceName);
                                }
                            }
                        }
                    });
                    mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
                        @Override
                        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
                            String oip=wifiP2pInfo.groupOwnerAddress.getHostAddress();
                        }
                    });
                }


            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                Toast.makeText(context, "THIS_DEVICE_CHANGED_ACTION", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setDeviceName(String devName) {
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
                    Log.d("tag","setDeviceName succeeded");
                }

                @Override
                public void onFailure(int reason) {
                    Log.d("tag","setDeviceName failed");
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

    private byte[] getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress instanceof Inet4Address) { // fix for Galaxy Nexus. IPv4 is easy to use :-)
                            return inetAddress.getAddress();
                        }
                        //return inetAddress.getHostAddress().toString(); // Galaxy Nexus returns IPv6
                    }
                }
            }
        } catch (SocketException ex) {
            //Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
        } catch (NullPointerException ex) {
            //Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
        }
        return null;
    }

    private String getDottedDecimalIP(byte[] ipAddr) {
        //convert to dotted decimal notation:
        String ipAddrStr = "";
        for (int i=0; i<ipAddr.length; i++) {
            if (i > 0) {
                ipAddrStr += ".";
            }
            ipAddrStr += ipAddr[i]&0xFF;
        }
        return ipAddrStr;
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