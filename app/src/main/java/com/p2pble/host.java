package com.p2pble;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
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

public class host extends AppCompatActivity implements Serializable {

    private static final String TAG ="ACT" ;
    private WifiConfiguration netConfig;
    private WifiManager wifiManager;
    private ProgressDialog p1;
    private ToggleButton t1;
    private TextView SSID;
    private WifiManager wMan;
    private LinearLayout LL;
    HashMap<String,String> dev ;
    private ArrayList<Object> listNote;
    private TextView textResult;
    private ListView list;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    List<WifiP2pDevice> nodes;
    HashMap<String, String> macip;
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    HashMap<String,String> nameip=new HashMap<>();
    private UdpClientThread send1;
    private UdpClientThread send2;
    List<String> namelist=new ArrayList<>();

    private HashMap<String, String> ipname=new HashMap<>();
    private static WifiP2pManager mManager;
    private WifiP2pManager wpMan;
    private static WifiP2pManager.Channel mChannel;
    private TextView stat;
    private WifiP2pDeviceList devlist;
    WifiP2pManager.PeerListListener plist=new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {

        }
    };
    private WifiP2pDevice devx;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);
        SSID=(EditText)findViewById(R.id.SSID);
        SSID.setText(getIntent().getStringExtra("dname"));
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

        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();
        macip=new HashMap<String, String>();
        expListView=(ExpandableListView)findViewById(R.id.el1);
        listNote = new ArrayList<>();
        dev=new HashMap<String, String>();
        textResult=(TextView)findViewById(R.id.res);
        //list=(ListView)findViewById(R.id.list1);
        LL=(LinearLayout)findViewById(R.id.LL1);
        netConfig = new WifiConfiguration();
        wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wMan = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        t1=(ToggleButton)findViewById(R.id.toggleButton);
        SSID=(TextView)findViewById(R.id.SSID);
        nodes=new ArrayList<>();
        host.p2pReceiver p2prec = new host.p2pReceiver();
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
    }

    void turnon(View view) {

        setDeviceName("P2PHOST_"+SSID.getText().toString());
        /*
        nameip.put(SSID.getText().toString(),"192.168.43.1");
        ipname.put("192.168.43.1",SSID.getText().toString());
        namelist.add(SSID.getText().toString());
        p1=new ProgressDialog(this);
        p1.setMessage("Please Wait ...\nTurning on Personal Hotspot");
        p1.show();
        if(t1.isChecked()) {
            final ProgressDialog p = new ProgressDialog(this);
            p.setMessage("Turning on hotspot ...");
            p.show();
            Thread t = new Thread() {
                @Override
                public void run() {
                    wMan.setWifiEnabled(false);
                    netConfig.SSID = SSID.getText().toString();
                    netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                    netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                    netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                    netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

                    try {
                        Method setWifiApMethod = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                        boolean apstatus = (Boolean) setWifiApMethod.invoke(wifiManager, netConfig, true);

                        Method isWifiApEnabledmethod = wifiManager.getClass().getMethod("isWifiApEnabled");
                        while (!(Boolean) isWifiApEnabledmethod.invoke(wifiManager)) {
                        }
                        ;
                        Method getWifiApStateMethod = wifiManager.getClass().getMethod("getWifiApState");
                        int apstate = (Integer) getWifiApStateMethod.invoke(wifiManager);
                        Method getWifiApConfigurationMethod = wifiManager.getClass().getMethod("getWifiApConfiguration");
                        netConfig = (WifiConfiguration) getWifiApConfigurationMethod.invoke(wifiManager);
                        Log.e("CLIENT", "\nSSID:" + netConfig.SSID + "\nPassword:" + netConfig.preSharedKey + "\n");
                        p.dismiss();
                    } catch (Exception e) {
                        Log.e(this.getClass().toString(), "", e);
                    }
                }
            };
            t.start();
        }
        else
        {
            wMan.setWifiEnabled(true);
        }
        p1.dismiss();

        myhandler hand=new myhandler(this);
        UdpServerThread receive=new UdpServerThread(4445,hand);
        receive.start();
*/

    }

    public void dis(View view) {
        readAddresses();
        if(listNote.size()==0)
            textResult.setText("No Devices Found");
        textResult.setText("");
        for(int i=0; i<listNote.size(); i++){
            textResult.append(i+1. + "   ");
            textResult.append(listNote.get(i).toString());
            textResult.append("\n");
        }
    }
    private void readAddresses() {
        listNote.clear();
        BufferedReader bufferedReader = null;

        try {
            bufferedReader = new BufferedReader(new FileReader("/proc/net/arp"));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    String ip = splitted[0];
                    String mac = splitted[3];
                    if (mac.matches("..:..:..:..:..:..")) {
                        Node thisNode = new Node(ip, mac);
                        listNote.add(thisNode);
                        macip.put(ip,mac);
                    }
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class Node {
        String ip;
        String mac;

        Node(String ip, String mac){
            this.ip = ip;
            this.mac = mac;
        }

        @Override
        public String toString() {
            return "IP:"+ip + "  MAC:" + mac;
        }
    }
private void deliver(final String Message, String addr)
{
    final String address=addr.substring(1);

    final String finalAddress = address;
    new AlertDialog.Builder(this)
            .setTitle("Title")
            .setMessage("Invite from "+Message)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Accept", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton) {
                    textResult.setText(textResult.getText().toString()+"\n"+Message);
                    List<String> info=new ArrayList<String>();
                    listDataHeader.add(Message);
                    info.add(macip.get(finalAddress));
                    namelist.add(Message);
                    listDataChild.put(listDataHeader.get(listDataHeader.size()-1), info);
                    listAdapter = new ExpandableListAdapter(host.this, listDataHeader, listDataChild);
                    expListView.setAdapter(listAdapter);

                    /*
                    disconnect();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    wpMan.createGroup(mChannel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onFailure(int i) {

                        }
                    });
                */
                }}

            )
            .setNegativeButton("Reject",new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton) {
                    Toast.makeText(host.this, "Rejected", Toast.LENGTH_SHORT).show();
                    Snackbar.make(findViewById(R.id.LL1),"Invite from "+Message+"Rejected !", Snackbar.LENGTH_SHORT).show();
                    UdpClientThread send=new UdpClientThread("_REJECT_".getBytes(),address,4445);
                    send.start();
                }}).show();
    //textResult.setText(Message);

}
    public static class myhandler extends Handler implements Serializable {
        private host parent;
        private String peerip;

        public myhandler(host parent) {
            super();
            this.parent = parent;
        }

        @Override
        public void handleMessage(Message msg) {
            parent.readAddresses();
           DatagramPacket packet= (DatagramPacket) msg.obj;
           peerip=packet.getAddress().toString();
           String invite=new String(packet.getData());
            String dname;
            String address=packet.getAddress().toString();

                  String[] x=invite.split("_");
                if(x[0].equals("JOIN-GROUP")) {
                    dname = x[1];
                    //Toast.makeText(parent, "Invite from "+dname+"\nIP:"+address, Toast.LENGTH_SHORT).show();
                    parent.deliver(dname, address);
                   parent.disconnect();

                }


    }}
void start(View view) throws InterruptedException {
    stat=(TextView)findViewById(R.id.stat);
    stat.setText("IP:"+getDottedDecimalIP(getLocalIPAddress()));
/*
    UdpClientThread send;
    String[] ips=macip.keySet().toArray(new String[macip.size()]);
    for(int i=0;i<macip.size();i++)
    {
        try {
            send1=new UdpClientThread(serialize(namelist),ips[i],4445);
            send1.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            send1=new UdpClientThread(serialize(nameip),ips[i],4445);
            send1.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            send2=new UdpClientThread(serialize(ipname),ips[i],4445);
            send2.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        send=new UdpClientThread("_START_".getBytes(),ips[i],4445);
        send.start();
    }
    Intent in = new Intent(this,GroupSelect.class);
    in.putExtra("SEN",true);
    Bundle b = new Bundle();
    b.putSerializable("macip",macip);
    b.putSerializable("nameip",nameip);
    b.putSerializable("ipname",ipname);
    b.putSerializable("namelist", (Serializable) namelist);
    in.putExtra("Name",SSID.getText().toString());
    in.putExtras(b);
    startActivity(in);
   // Toast.makeText(this, "Localise", Toast.LENGTH_SHORT).show();
*/

}

    public  byte[] serialize(Object obj) throws IOException {
        try(ByteArrayOutputStream b = new ByteArrayOutputStream()){
            try(ObjectOutputStream o = new ObjectOutputStream(b)){
                o.writeObject(obj);
            }
            return b.toByteArray();
        }
    }

    public Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
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

             if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                // The peer list has changed!  We should probably do something about
                // that.
                mManager.requestPeers(mChannel,plist);
                Toast.makeText(context, "PEERS_CHANGED_ACTION", Toast.LENGTH_SHORT).show();
                 Log.d("P2P","PEERS_CHANGED_ACTION");

            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                 stat=(TextView)findViewById(R.id.stat);
               // Toast.makeText(context, "CONNECTION_CHANGED_ACTION", Toast.LENGTH_SHORT).show();
                 NetworkInfo networkInfo = (NetworkInfo) intent
                         .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                 if (networkInfo.isConnected()) {

                     // We are connected with the other device, request connection
                     // info to find group owner IP

                     mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
                         @Override
                         public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
                             if(wifiP2pInfo.groupFormed)
                             {
                                 wifiP2pInfo.groupOwnerAddress.getHostAddress();
                             }
                         }
                     });
                 }
                mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
                    @Override
                    public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
                      stat=(TextView)findViewById(R.id.stat);
                        if(wifiP2pGroup!=null){
                            Iterator<WifiP2pDevice> i=wifiP2pGroup.getClientList().iterator();
                            stat.setText("");
                            while(i.hasNext())
                            {
                                devx=i.next();
                                nodes.add(devx);
                                stat.setText(stat.getText().toString()+"\n"+devx.deviceName);
                            }
                    }

                    }
                });
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

