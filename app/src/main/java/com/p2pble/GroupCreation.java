package com.p2pble;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import java.util.Arrays;
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
    //String devip;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    private RadioGroup g1;
    private TextView dname;
    List<String> namelist;
    private static WifiP2pManager mManager;
    private WifiP2pManager wpMan;
    private static WifiP2pManager.Channel mChannel;
    TextView stat;
    HashMap<String,String>devip = new HashMap<>();
    private WifiP2pDeviceList devlist;
    WifiP2pManager.PeerListListener plist=new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
            WifiP2pDevice dev;
            //Toast.makeText(GroupCreation.this, "plist !", Toast.LENGTH_SHORT).show();
            devlist=wifiP2pDeviceList;
            init();
                /*
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
*/
        }
    };
    private WifiP2pDevice dev;
    private String peerip;
    private WifiP2pDevice devx;
    private Inviteresponse x;
    GroupCreation.p2pReceiver p2prec;
    private WifiP2pDevice mydev;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_creation);

        Snackbar.make(findViewById(R.id.R1),"Receiver Initialised !",Snackbar.LENGTH_SHORT).show();
        myhandler handler = new myhandler(GroupCreation.this);
        x = new Inviteresponse(4445, handler, 1);
        x.start();
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
       p2prec = new GroupCreation.p2pReceiver();
        registerReceiver(p2prec,intentFilter);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                wpMan.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d("WIFIP2P","Peer Discovery Sucessful");
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.d("WIFIP2P","Peer Discovery Failed !" );
                    }
                }

                );
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }}
        });
        t.start();

        /*
        wpMan.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(GroupCreation.this, "Peer Discovery Sucessful", Toast.LENGTH_SHORT).show();
                        Log.d("WIFIP2P","Peer Discovery Sucessful");
                        mManager.requestPeers(mChannel,plist);
                    }

                    @Override
                    public void onFailure(int i) {
                        Toast.makeText(GroupCreation.this, "Peer Discovery Failed!", Toast.LENGTH_SHORT).show();
                        Log.d("WIFIP2P","Peer Discovery Failed !" );
                    }
                }

        );
*/

    }

    private void init() {
        sv=(LinearLayout) findViewById(R.id.sv);
        sv.removeAllViews();
       // Snackbar.make(findViewById(R.id.R1),"View Refreshed", Snackbar.LENGTH_SHORT).show();

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
        if(radioButtonID==-1)
        {
            AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);

            builder.setTitle("Sorry")
                    .setMessage("Your Menu Just got Refreshed ... \nTry again Soon !")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                        }
                    })

                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        else
        {
        RadioButton radioButton =(RadioButton) g1.findViewById(radioButtonID);
       final String info= radioButton.getText().toString();
      stat.setText("trying to connect to "+info);
       Iterator i=devlist.getDeviceList().iterator();
      while(i.hasNext())
      {
          dev=(WifiP2pDevice) i.next();
          if(dev.deviceName.equals(info))
          {
              final WifiP2pConfig config = new WifiP2pConfig();
              config.deviceAddress = dev.deviceAddress;
              config.groupOwnerIntent=0;
              mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                  @Override
                  public void onSuccess() {
                      //stat.setText("Connected to "+dev.deviceName);

//                      mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
//                          @Override
//                          public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
//                              while(!wifiP2pInfo.groupFormed){
//                                  stat.setText("Group not joined !\nFailure");
//                              }
//                              Toast.makeText(GroupCreation.this, "Group Formed !", Toast.LENGTH_SHORT).show();
//                          }
//                      });
//
//                      // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
//                      Toast.makeText(GroupCreation.this, "Connected cha  to "+info, Toast.LENGTH_SHORT).show();
//                        stat.setText("Connected to "+info);
//                      dname = (TextView) findViewById(R.id.dname);
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
//                      mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
//                          @Override
//                          public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
//                            if(wifiP2pGroup!=null){
//                              Iterator<WifiP2pDevice> i=wifiP2pGroup.getClientList().iterator();
//                              stat.setText("Connected to :\n");
//                              while(i.hasNext())
//                              {
//                                  devx=i.next();
//                                  stat.setText(stat.getText().toString()+devx.deviceName+"\n");
//                              }
//                          }}
//                      });
                  }

                  @Override
                  public void onFailure(int reason) {
                    stat.setText("Connection failed retry");
                      Toast.makeText(GroupCreation.this, "Connect failed. Retry.",
                              Toast.LENGTH_SHORT).show();
                      stat.setText("Connection Failed "+info+reason );
                      if(reason==2)
                      {
                          Thread t = new Thread(new Runnable() {
                              @Override
                              public void run() {
                                  try {
                                      Thread.sleep(2000);
                                  } catch (InterruptedException e) {
                                      e.printStackTrace();
                                  }
                                  mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

                                      @Override
                                      public void onSuccess() {
                                      }

                                      @Override
                                      public void onFailure(int i) {
                                          stat.setText("Connectivity failed !\nPlease try again");
                                      }
                                  });
                              }
                          });
                      }

                  }
              });

          }
      }}
 }
    void ref(View view){
        mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
                List<String> p = new ArrayList<>();
                if (wifiP2pGroup.isGroupOwner())
                {
                   Iterator<String> i=devip.keySet().iterator();
                   while(i.hasNext())
                    p.add(i.next());

                   Log.d("IP-Map",devip.values().toString());
                Iterator<String> ix=devip.values().iterator();
                   String message="START";
                    byte[] bytes = new byte[0];

                        Log.d("SER","Serialized !\n\n\n\n\n\n\n\n\n");
                    while(ix.hasNext()){
                        bytes=shift(p).getBytes();
                    UdpClientThread asd=new UdpClientThread(bytes,ix.next(),4445);
                    asd.start();

                    }}
            }
        });
    }
    private static HashMap<String, String> nameip;
    private static HashMap<String, String> ipname;

    @Override
    public void onBackPressed() {
        unregisterReceiver(p2prec);
        super.onBackPressed();
    }

    public class myhandler extends Handler implements Serializable {
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
            List<String>  group = null;
            //------------------------------------------------
            String St = new String(packet.getData());
            Toast.makeText(parent,St, Toast.LENGTH_SHORT).show();
            String []a=St.split("_");

            if(a.length>1) {
                group = deshift(St);
                Toast.makeText(parent, "deshift", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(GroupCreation.this, android.R.style.Theme_Material_Dialog_Alert);
                final List<String> finalGroup = group;
                builder.setTitle("Confirm Group")
                        .setMessage("Group Members:" + group.toString())
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                                Intent in = new Intent(GroupCreation.this, GroupSelect.class);
                                try {
                                    in.putExtra("group", serialize(finalGroup));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                startActivity(in);
                            }
                        })

                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();
            }
         //---------------------------------------------------
               else {
                    String Name = new String(packet.getData());
                    String addr = packet.getAddress().toString();
                    addr=addr.substring(1);
                    Snackbar.make(findViewById(R.id.R1), Name + addr, Snackbar.LENGTH_SHORT).show();
                    devip.put(Name, addr);

                }

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
        public void onReceive(Context context, final Intent intent) {
            Log.d("P2P","ONRECEIVE");
            String action = intent.getAction();
            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            stat = (TextView) findViewById(R.id.stat);
            if (networkInfo!=null && networkInfo.isConnected()) {

                stat.setText(stat.getText().toString()+"\nYou are Connected ");
            }
            else {
                stat.setText("\nNot Connected !");
            }
                if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                // Determine if Wifi P2P mode is enabled or not, alert
                // the Activity.
                Log.d("P2P","STATE_CHANGED_ACTION");
               // Toast.makeText(context, "STATE_CHANGED_ACTION", Toast.LENGTH_SHORT).show();

            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                // The peer list has changed!  We should probably do something about
                // that.
                mManager.requestPeers(mChannel,plist);
             //   Toast.makeText(context, "PEERS_CHANGED_ACTION", Toast.LENGTH_SHORT).show();
                Log.d("P2P","PEERS_CHANGED_ACTION");

            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
             //   Toast.makeText(context, "CONNECTION_CHANGED_ACTION", Toast.LENGTH_SHORT).show();
                networkInfo = (NetworkInfo) intent
                        .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                if (networkInfo!=null && networkInfo.isConnected()) {
                    stat=(TextView)findViewById(R.id.stat);

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
                                    stat.setText(stat.getText().toString()+"\n"+devx.deviceName);
                                    Log.d("Connected Devices",devx.deviceName);
                                }
                                Log.d("Connected Devices :",wifiP2pGroup.getClientList().toString());
                            }
                        }
                    });
                        mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
                            @Override
                            public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
                                if (wifiP2pInfo.groupFormed)
                                {

                                    String name=mydev.deviceName;
                                    UdpClientThread send=new UdpClientThread(name.getBytes(),wifiP2pInfo.groupOwnerAddress.getHostAddress(),4445);
                                    send.start();
                                    Snackbar.make(findViewById(R.id.R1),"Pinged with the group owner !",Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        });
                }
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                    mydev = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
               // Toast.makeText(context, "THIS_DEVICE_CHANGED_ACTION", Toast.LENGTH_SHORT).show();
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
    public static String getIPFromMac(String MAC) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {

                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    // Basic sanity check
                    String device = splitted[5];
                    if (device.matches(".*p2p-p2p0.*")){
                        String mac = splitted[3];
                        if (mac.matches(MAC)) {
                            return splitted[0];
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
public String shift(List<String> devstore)
{
    String z="";
    //Toast.makeText(this,"DevStore:"+ devstore.toString(), Toast.LENGTH_SHORT).show();
    Iterator<String> i=devstore.iterator();
    z=i.next();
    while(i.hasNext())
    {
        z=z.concat("#").concat(i.next());
    }
    z=devstore.toString();
    //List<String> dex_list = new ArrayList<>(z);
    Toast.makeText(this, "z:"+z, Toast.LENGTH_SHORT).show();
    Log.d("Z:",z);

    return z;
}
    public List<String> deshift(String z)
    {
        String[] a=z.split("#");
        List<String> dex=new ArrayList<String>(Arrays.asList(a));
        return dex;
        //upx
    }
}