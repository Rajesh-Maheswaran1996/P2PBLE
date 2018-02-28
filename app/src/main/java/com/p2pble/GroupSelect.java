package com.p2pble;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class GroupSelect extends AppCompatActivity implements Serializable,SensorEventListener,StepListener {
    SensorManager mSensorManager;
    static int color=0;
    SimpleStepDetector simpleStepDetector;
    int numSteps;
    TextView tx, txt1, st;
    //boolean type;
    private TextView mypath;
    private TextView path;
    static HashMap<String, String> macip;
    HashMap<String, String> hostip;
    HashMap<String, String> iphost;
    String distances[][];
    Bitmap mutableBitmap;
    Bitmap workingBitmap;
    Paint[] paint = new Paint[4];
    private BluetoothLeScanner mBluetoothLeScanner;
    private Handler mHandler = new Handler();
    public ScanCallback mScanCallback;
    HashMap<String, Integer> name_rssi;
    HashMap<String,String[]> name_pos;
    TextView mText;
    HashMap<String,Integer> ipcolor;
    File dir;
    File file;
    FileOutputStream fileOutputStream = null;
    OutputStreamWriter outputStreamWriter;

    long ts;
    double time;
    Double deg = 0.0, gy, gy1;

    ArrayList<Float> final_x;
    ArrayList<Float> final_y;


    float x, y;
    float a, b;
    double theta;
    TextView degree;
    TextView textView;
    static int cnt = 4;
    static HashMap<String,constraint> ipcons = new HashMap<>();
    float ix, iy, fx, fy;
    private WifiP2pDevice dev;
    private String peerip;
    private WifiP2pDevice devx;
    private Inviteresponse x1;
    private WifiP2pDevice mydev;
    private WifiP2pDeviceList devlist;
    private static WifiP2pManager mManager;
    private WifiP2pManager wpMan;
    private static WifiP2pManager.Channel mChannel;
    TextView stat;
    String devicename;
    Canvas canvas;
    byte[] devip1;
    TextView textView2;
    ImageView imageView;
    Button bx;//Start
    Button b_stop;
    Button b_reset;
    Button b_step;
    Button b_track;
    Button b_discover;
    Button b_trilaterate;
    int master=0;
    String master_address;
    HashMap<String[],Integer> pos_rssi;
    HashMap<String, String> devip;
    HashMap<String, String> devip_for_nodes;
    HashMap<String, String[]> ipdist;
    private Double temp = 0.0;
    private double chng;

    WifiP2pManager.PeerListListener plist = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
            WifiP2pDevice dev;
            //Toast.makeText(GroupCreation.this, "plist !", Toast.LENGTH_SHORT).show();
            devlist = wifiP2pDeviceList;
            //init();
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
    GroupSelect.p2pReceiver p2prec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_select);
        stat = (TextView) findViewById(R.id.stat);
        ipdist = new HashMap<String, String[]>();
        name_pos = new HashMap<String, String[]>();
        x = 0;
        y = 0;
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        for (int i = 0; i < 4; i++) {
            paint[i] = new Paint();
        }
        ipcolor = new HashMap<String,Integer>();
        final_x = new ArrayList<>();
        final_y = new ArrayList<>();
        bx = (Button) findViewById(R.id.start);
        b_reset = (Button) findViewById(R.id.reset);
        b_stop = (Button) findViewById(R.id.stop);
        b_step = (Button) findViewById(R.id.step);
        b_track = (Button) findViewById(R.id.button2);
        b_discover = (Button) findViewById(R.id.button3);
        b_trilaterate = (Button) findViewById(R.id.button4);
        tx = (TextView) findViewById(R.id.tx1);
        txt1 = (TextView) findViewById(R.id.txt1);
        st = (TextView) findViewById(R.id.status);
        degree = (TextView) findViewById(R.id.deg);
        // type=getIntent().getBooleanExtra("SEN",false);
        numSteps = 0;
        mypath = (TextView) findViewById(R.id.mypath);
        path = (TextView) findViewById(R.id.path);
        mText = (TextView) findViewById(R.id.rssi);
        ts = System.currentTimeMillis();
        gy = 0.0;
        gy1 = 0.0;
        mBluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
        name_rssi = new HashMap<String,Integer>();

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        wpMan = (WifiP2pManager) getApplicationContext().getSystemService(Context.WIFI_P2P_SERVICE);
        pos_rssi = new HashMap<String[], Integer>();

        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        p2prec = new p2pReceiver();
        registerReceiver(p2prec, intentFilter);


        bx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start(view);
            }
        });

        b_step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    add(view);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        b_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stop(view);
            }
        });

        b_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reset(view);
            }
        });

        b_track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    track(view);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        b_discover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                discover(view);
            }
        });

        b_trilaterate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                trilaterate(view);
            }
        });

        //devlist = (WifiP2pDeviceList) getIntent().getExtras().get("devlist");
        devip = (HashMap<String, String>) getIntent().getExtras().get("devip");

        try {
            devip1 = serialize(devip);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            devip = (HashMap<String,String>)deserialize(devip1);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


        imageView = (ImageView) findViewById(R.id.imageView);
        BitmapFactory.Options myOptions = new BitmapFactory.Options();
        myOptions.inDither = true;
        myOptions.inScaled = false;
        myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        myOptions.inPurgeable = true;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.csels, myOptions);
        paint[0].setAntiAlias(true);
        paint[0].setColor(Color.BLUE);
        paint[1].setAntiAlias(true);
        paint[1].setColor(Color.RED);
        paint[2].setAntiAlias(true);
        paint[2].setColor(Color.YELLOW);
        paint[3].setAntiAlias(true);
        paint[3].setColor(Color.BLACK);
        workingBitmap = Bitmap.createBitmap(bitmap);
        mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                canvas = new Canvas(mutableBitmap);


                if (cnt == 4) {
                    // Toast.makeText(getApplicationContext(),"if",Toast.LENGTH_SHORT).show();
                    ix = event.getX();
                    iy = event.getY();
                    Toast.makeText(GroupSelect.this, "x value" + ix + "y value" + iy, Toast.LENGTH_SHORT).show();
                    x = ix / 10;
                    y = iy / 10;
                    canvas.drawCircle(ix, iy, 25, paint[0]);
                    cnt--;
                } else if (cnt == 3) {
                    AlertDialog.Builder builder;
                    builder = new AlertDialog.Builder(GroupSelect.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
                    builder.setTitle("Coordinates").setMessage("x: " + ix + "y: " + iy).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            cnt--;
                        }
                    }).setIcon(android.R.drawable.ic_dialog_info)
                            .show();

                } else if (cnt == 2) {
                    //  Toast.makeText(getApplicationContext(),"else",Toast.LENGTH_SHORT).show();
                    fx = event.getX();
                    fy = event.getY();
                    Toast.makeText(GroupSelect.this, "x value" + fx + "y value" + fy, Toast.LENGTH_SHORT).show();
                    canvas.drawCircle(fx, fy, 25, paint[0]);
                    cnt--;

                    theta = Math.round(Math.toDegrees(Math.atan((fy - iy) / (fx - ix))));
                    if (fx > ix && fy > iy)
                        theta += 360;
                    else if (fy > iy && fx < ix)
                        theta += 180;
                    else if (fx < ix && fy < iy)
                        theta = 180 + theta;
                    //if(fx>ix&&fy>iy)
                    //  theta+=360;
                    //else if(fy>iy &&fx<ix)
                    //theta+=180;
                    //else if(fx<ix && fy<iy)
                    //theta=180+theta;
                    bx.setEnabled(true);
                    //deg+=theta;
                    imageView.setOnTouchListener(null);
                }
                imageView.setAdjustViewBounds(true);
                imageView.setImageBitmap(mutableBitmap);

                // mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
                Toast.makeText(getApplicationContext(), String.valueOf(cnt), Toast.LENGTH_SHORT).show();

                return true;
            }
        });

    }




    void track(View view) throws IOException {
        Iterator<Float> ite_x = final_x.iterator();
        Iterator<Float> ite_y = final_y.iterator();
        dir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/IMU");
        dir.mkdirs();
        file = new File(dir, "IMUDATA1.txt");
        fileOutputStream = new FileOutputStream(file, true);
        outputStreamWriter = new OutputStreamWriter(fileOutputStream);

        while (ite_x.hasNext()) {
            float temp_x = ite_x.next();
            float temp_y = ite_y.next();
            outputStreamWriter.write("" + temp_x + " " + temp_y + "\n");
        }
        outputStreamWriter.close();
        fileOutputStream.close();
        Toast.makeText(this, "File written", Toast.LENGTH_SHORT).show();
    }

    getrssi receive;
    ipsender ipsender;
    Thread t;
    Thread t1;
    Thread s1;

    void start(View view) {
        st.setText("**Recording**");

        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_GAME);

        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);

        simpleStepDetector = new SimpleStepDetector();
        simpleStepDetector.registerListener(this);


        myhandler hand = new myhandler(this);
        iphandler iphandler = new iphandler(this);
        ipsender = new ipsender(4554,iphandler,0);
        receive = new getrssi(4555, hand, 0);
        receive.start(); //Todo remove comments after sensor is working
        ipsender.start();

        Toast.makeText(getApplicationContext(),devip.toString(),Toast.LENGTH_SHORT).show();

        s1 = new Thread(){
            @Override
            public void run() {
                mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
                    @Override
                    public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
                        if(wifiP2pGroup.isGroupOwner()){
                            Iterator<String> it = devip.values().iterator();
                            while (it.hasNext()) {
                                UdpClientThread send = new UdpClientThread(devip1, it.next(), 4554);
                                send.start();
                            }
                        }
                    }
                });
            }
        };
        s1.run();
    }


    double avg_x,avg_y;
    Thread t5;

    public void trilaterate(View view){
        pos_rssi.clear();
        Set<String> name1 = name_rssi.keySet();
        Set<String> name2 = name_pos.keySet();

        for(String s1:name1){
            for(String s2 : name2){
                String val[] = s2.split(" ");
                if(s1.equals(val[1])){
                    if(pos_rssi.containsKey(name_pos.get(val[1]))) {
                        pos_rssi.remove(name_pos.get(val[1]));
                        pos_rssi.put(name_pos.get(s2), name_rssi.get(s1));
                    }
                    else{
                        pos_rssi.put(name_pos.get(s2), name_rssi.get(s1));
                    }
                    break;
                }
            }
        }
                constraint c[]= new constraint[4];
                for(int u=0;u<4;u++){
                    c[u] = new constraint();
                }
                Set<String[]> pos1 = pos_rssi.keySet();
                if(pos_rssi.size()>=3) {

                    int k = 0;
                    for (String[] val : pos1) {
                        c[k].set(Double.parseDouble(val[0]), Double.parseDouble(val[1]), new Double(pos_rssi.get(val)));
                        k++;
                    }


                    int len = pos_rssi.size();
                    int p = 0;

                    double n = nCr(len, 2);
                    double[][] arr = new double[(int) n][4];


                    for (int i = 0; i < len; i++) {
                        for (int j = 0; j < i; j++) {
                            if (i != j) {
                                //System.out.println(i + " " + j);
                                getcommon(c[i], c[j], p, arr);
                                p++;
                            }
                        }
                    }

                    for (int i = 0; i < n; i++) {
                        for (int j = 0; j < 4; j++) {
                            System.out.print(arr[i][j] + " ");
                        }
                        System.out.println();
                    }


                    double d1 = distance(arr[0][0], arr[0][1], arr[1][0], arr[1][1]);
                    double d2 = distance(arr[0][0], arr[0][1], arr[1][2], arr[1][3]);
                    double d3 = distance(arr[0][2], arr[0][3], arr[1][0], arr[1][1]);
                    double d4 = distance(arr[0][2], arr[0][3], arr[1][2], arr[1][3]);

                    double fin_dist = (Math.min(Math.min(Math.min(d1, d2), d3), d4));

                    System.out.println(" fin_dist = " + fin_dist);

                    double a = 0, b = 0;

                    if (fin_dist == d1 || fin_dist == d2) {
                        a = arr[0][0];
                        b = arr[0][1];
                    } else if (fin_dist == d3 || fin_dist == d4) {
                        a = arr[0][2];
                        b = arr[0][3];
                    }

                    System.out.println("----- final ----");

                    double sum_x = 0, sum_y = 0;
                    double radius = 0;
                    for (int i = 0; i < n; i++) {
                        double d11 = distance(a, b, arr[i][0], arr[i][1]);
                        double d12 = distance(a, b, arr[i][2], arr[i][3]);
                        double d13 = Math.min(d11, d12);
                        radius = Math.max(d11, d12);
                        if (d13 == d11) {
                            System.out.println(arr[i][0] + " " + arr[i][1]);
                            sum_x += arr[i][0];
                            sum_y += arr[i][1];
                        } else {
                            System.out.println(arr[i][2] + " " + arr[i][3]);
                            sum_x += arr[i][2];
                            sum_y += arr[i][3];
                        }
                    }

                    avg_x = sum_x / n;
                    avg_y = sum_y / n;

                    System.out.println(" Final coordinate - ");
                    //System.out.println(avg_x + " " + avg_y);
                    Toast.makeText(getApplicationContext(),""+avg_x + " " + avg_y,Toast.LENGTH_SHORT).show();

                }//pos_rssi size limit "if"

    }

    void stop(View view) {
        st.setText("**Paused**");
        mSensorManager.unregisterListener(this);
        t.stop();
        receive.socket.close();
        unregisterReceiver(p2prec);

    }

    void reset(View view) {
        deg = 0.0;
        gy = 0.0;
        numSteps = 0;
        tx.setText("Rotation :0 Degrees");
        txt1.setText("Steps0");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // for the system's orientation sensor registered listeners
        //    mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
        //          SensorManager.SENSOR_DELAY_GAME);
    }

    void add(View view) throws IOException {
        numSteps++;
        txt1.setText("Steps " + numSteps);
        File dir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/IMU");
        dir.mkdirs();
        File file = new File(dir, "IMUDATA.txt");
        FileOutputStream fileOutputStream = new FileOutputStream(file, true);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
        outputStreamWriter.write(0.65 + " " + deg + "\n");
        outputStreamWriter.close();
        fileOutputStream.close();

    }

    void discover(View view) {
        ScanFilter filter = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(UUID.fromString(getString(R.string.ble_uuid))))
                .build();
        List<ScanFilter> filters = new ArrayList<ScanFilter>();
        filters.add(filter);

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .build();

        mScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                //Toast.makeText(getApplicationContext(), "onScanResult is called", Toast.LENGTH_SHORT).show();//TODO remove later
                if (result == null
                        || result.getDevice() == null
                        || TextUtils.isEmpty(result.getDevice().getName())) {
                    //Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show(); TODO remove later(comment)
                } else {
                    name_rssi.put(result.getDevice().getName(), result.getRssi());
                    //Toast.makeText(getApplicationContext(),name_rssi.toString(),Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Toast.makeText(getApplicationContext(), "onBatchScanResult is called", Toast.LENGTH_SHORT).show();
                Iterator<ScanResult> ite = results.iterator();
                while (ite.hasNext()) {
                    Toast.makeText(getApplicationContext(), ite.next().getDevice().getName() + " " + ite.next().getRssi() + "", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onScanFailed(int errorCode) {
                Log.e("BLE", "Discovery onScanFailed: " + errorCode);
                super.onScanFailed(errorCode);
            }
        };

        mBluetoothLeScanner.startScan(mScanCallback);
        Toast.makeText(this, "Scan has started", Toast.LENGTH_SHORT).show();

//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mBluetoothLeScanner.stopScan(mScanCallback);
//            }
//        }, 5000);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                simpleStepDetector.updateAccel(
                        event.timestamp, event.values[0], event.values[1], event.values[2]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //double vel = (double) (event.values[2]) * 180 / Math.PI;
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            double vel = (double) (event.values[1]) * 180 / Math.PI;
            //Log.d("Gyro", Float.toString(event.values[0]));
            time = System.currentTimeMillis() - ts;
            ts = System.currentTimeMillis();
            time /= 1000;
            deg += time * (vel + gy) / 2;
            if (deg > 360)
                deg -= 360;
            if (deg < -360)
                deg += 360;
            gy = vel;
            //Toast.makeText(this, "Sensing"+Double.toString(vel), Toast.LENGTH_SHORT).show();
            //deg = deg+theta;
            tx.setText("Rotation: " + Math.round(deg) + " degrees");
            chng = deg - temp;
            temp = deg;
            if (fy > iy + 10) {
                theta = (theta - chng);
            } else if (fy + 10 < iy) {
                theta = (theta - chng);
            } else if (fx > ix) {
                theta = theta - chng;
            } else if (fx < ix) {
                theta = theta + chng;
            }
            if (theta > 360)
                theta = theta % 360;
            else if (theta < -360)
                theta += 360;

            degree.setText("" + theta);

        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void step(long timeNs) throws IOException {
        numSteps++;
        txt1.setText("Steps" + numSteps);
        x += 0.65 * Math.cos(Math.toRadians(theta));
        y += 0.65 * Math.sin(Math.toRadians(theta));

        mypath.setText(Math.round(x) + "  " + Math.round(y));
        degree.setText("" + theta);


        final_x.add(x);
        final_y.add(y);

        Log.d("Devip-1", devip.toString());
        mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(final WifiP2pGroup wifiP2pGroup) {
                if (wifiP2pGroup.isGroupOwner()) {
                    master=1;
                    t = new Thread() {
                        @Override
                        public void run() {
                            try {
                                while (true) {
                                    Iterator<String> name_it = devip.keySet().iterator();
                                    Iterator<String> it = devip.values().iterator();
                                    while (it.hasNext()) {
                                        String dev = name_it.next();
                                        int rssi = name_rssi.get(dev);
                                        if(rssi<70) {
                                            UdpClientThread send = new UdpClientThread((String.valueOf(x) + "_" + String.valueOf(y) + "_" + mydev.deviceName).getBytes(), it.next(), 4555);
                                            send.start();
                                        }
                                        else{
                                            continue;
                                        }
                                    }
                                    Thread.sleep(2000);
                                }
                            } catch (Exception e) {
                            }
                        }
                    };
                    t.start(); //Todo remove comments after sensor is working
                } else {
                    mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
                        @Override
                        public void onConnectionInfoAvailable(final WifiP2pInfo wifiP2pInfo) {
                            master_address = wifiP2pInfo.groupOwnerAddress.getHostAddress();
                            t1 = new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        while (true) {

                                            Iterator<String> ite = devip_for_nodes.values().iterator();
                                            Iterator<String> name_ite = devip_for_nodes.keySet().iterator();

                                            while(ite.hasNext()) {
                                                String name = name_ite.next();
                                                int rssi = name_rssi.get(name);
                                                if(rssi<70) {
                                                    UdpClientThread send = new UdpClientThread((String.valueOf(x) + "_" + String.valueOf(y) + "_" + mydev.deviceName).getBytes(), ite.next(), 4555);
                                                    send.start();
                                                }
                                                else{
                                                    continue;
                                                }
                                            }
                                            Thread.sleep(2000);
                                        }
                                    } catch (Exception e) {
                                    }
                                }
                            };
                            t1.start(); //Todo remove comments after sensor is working
                        }
                    });


                }
            }
        });

    }

    Thread t4;

    public static class myhandler extends Handler {
        private GroupSelect parent;

        public myhandler(GroupSelect parent) {
            super();
            this.parent = parent;
        }


        @Override
        public void handleMessage(Message msg) {
            final DatagramPacket packet = (DatagramPacket) msg.obj;
            //    switch(msg.what){
            //       case 0 :
            String invite = new String(packet.getData()).trim();


            final String address = packet.getAddress().toString();
            final String x[] = invite.split("_");
            Log.d("Coordinates", "X: " + x[0] + "Y " + x[1]);
            Log.d("Address", address);
            parent.path.setText(Long.toString(Math.round(Double.parseDouble(x[0]))) + "    " + Long.toString(Math.round(Double.parseDouble(x[1]))));
            parent.ipdist.put(packet.getAddress().toString(), x);
            parent.name_pos.put(x[2],x);
            //Log.d("Name_pos", parent.name_pos.toString());
            Collection<String[]> arr = parent.ipdist.values();
            Iterator<String[]> ite = arr.iterator();
            int n = arr.size();
            int i=0;
            while (ite.hasNext()) {
                parent.canvas = new Canvas(parent.mutableBitmap);
                String[] y = ite.next();
                parent.a = Float.parseFloat(y[0]);
                parent.b = Float.parseFloat(y[1]);
                parent.canvas.drawCircle(parent.a * 10, parent.b * 10, 15, parent.paint[i]);
                i++;
                //ite.next();
            }
            //parent.canvas.drawCircle(parent.x*10,parent.y*10,15,parent.paint[3]);
            parent.imageView.setAdjustViewBounds(true);
            parent.imageView.setImageBitmap(parent.mutableBitmap);
            parent.mutableBitmap = parent.workingBitmap.copy(Bitmap.Config.ARGB_8888, true);

        }


    }






//    public static class rssi_handler extends Handler {
//        private GroupSelect parent;
//
//
//        rssi_handler(GroupSelect parent) {
//            this.parent = parent;
//        }
//
//
//        @Override
//        public void handleMessage(Message msg) {
//            byte[] rcvx = ((DatagramPacket) msg.obj).getData();
//            //Constructor
//            String msg1 = new String(rcvx);
//            Log.d("Data Received :", msg1);
//                DatagramPacket packet = (DatagramPacket) msg.obj;
//                //    switch(msg.what){
//                //       case 0 :
//                Toast.makeText(parent, "Data Received from " + packet.getAddress().toString(), Toast.LENGTH_SHORT).show();
//                byte[] rcvmsg = packet.getData();
//                //Constructor
//                //c1 = new constraint(rcvmsg);
//                //ipcons.put(packet.getAddress().toString(), c1);
//                //Log.d("TABLE:", ipcons.toString());
//                if (ipcons.size() >= 3)
//                //String address=packet.getAddress().toString();
//                {
//                    constraint[] c = ipcons.values().toArray(new constraint[ipcons.values().size()]);
//                    int len = ipcons.size();
//                    parent.canvas = new Canvas(parent.mutableBitmap);
//                    int p = 0;
//
//                    double n = nCr(len, 2);
//                    double[][] arr = new double[(int) n][4];
//
//
//                    for (int i = 0; i < len; i++) {
//                        for (int j = 0; j < i; j++) {
//                            if (i != j) {
//                                //System.out.println(i + " " + j);
//                                getcommon(c[i], c[j], p, arr);
//                                p++;
//                            }
//                        }
//                    }
//
//                    for (int i = 0; i < n; i++) {
//                        for (int j = 0; j < 4; j++) {
//                            System.out.print(arr[i][j] + " ");
//                        }
//                        System.out.println();
//                    }
//
//
//                    double d1 = distance(arr[0][0], arr[0][1], arr[1][0], arr[1][1]);
//                    double d2 = distance(arr[0][0], arr[0][1], arr[1][2], arr[1][3]);
//                    double d3 = distance(arr[0][2], arr[0][3], arr[1][0], arr[1][1]);
//                    double d4 = distance(arr[0][2], arr[0][3], arr[1][2], arr[1][3]);
//
//                    double fin_dist = (Math.min(Math.min(Math.min(d1, d2), d3), d4));
//
//                    System.out.println(" fin_dist = " + fin_dist);
//
//                    double a = 0, b = 0;
//
//                    if (fin_dist == d1 || fin_dist == d2) {
//                        a = arr[0][0];
//                        b = arr[0][1];
//                    } else if (fin_dist == d3 || fin_dist == d4) {
//                        a = arr[0][2];
//                        b = arr[0][3];
//                    }
//
//                    System.out.println("----- final ----");
//
//                    double sum_x = 0, sum_y = 0;
//                    double radius = 0;
//                    for (int i = 0; i < n; i++) {
//                        double d11 = distance(a, b, arr[i][0], arr[i][1]);
//                        double d12 = distance(a, b, arr[i][2], arr[i][3]);
//                        double d13 = Math.min(d11, d12);
//                        radius = Math.max(d11, d12);
//                        if (d13 == d11) {
//                            System.out.println(arr[i][0] + " " + arr[i][1]);
//                            sum_x += arr[i][0];
//                            sum_y += arr[i][1];
//                        } else {
//                            System.out.println(arr[i][2] + " " + arr[i][3]);
//                            sum_x += arr[i][2];
//                            sum_y += arr[i][3];
//                        }
//                    }
//
//                    avg_x = sum_x / n;
//                    avg_y = sum_y / n;
//
//                    System.out.println(" Final coordinate - ");
//                    System.out.println(avg_x + " " + avg_y);
//                }
//
//        }
//
//    }


    public static class iphandler extends Handler{
        GroupSelect parent;

        iphandler(GroupSelect parent){
            this.parent = parent;
        }

        @Override
        public void handleMessage(Message msg) {
            byte[] rcvx = ((DatagramPacket) msg.obj).getData();
            //Constructor
            DatagramPacket packet = (DatagramPacket) msg.obj;
            //    switch(msg.what){
            //       case 0 :
            Toast.makeText(parent, "Data Received from " + packet.getAddress().toString(), Toast.LENGTH_SHORT).show();
            byte[] rcvmsg = packet.getData();
            try {
                parent.devip_for_nodes =(HashMap<String,String>)deserialize(rcvx);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            Toast.makeText(parent,parent.devip_for_nodes.toString(),Toast.LENGTH_LONG).show();
        }
    }


        class p2pReceiver extends BroadcastReceiver {

            @Override
            public void onReceive(Context context, final Intent intent) {
                Log.d("P2P", "ONRECEIVE");
                String action = intent.getAction();
                NetworkInfo networkInfo = (NetworkInfo) intent
                        .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                //stat = (TextView) findViewById(R.id.stat);
                if (networkInfo != null && networkInfo.isConnected()) {

                   // stat.setText(stat.getText().toString() + "\nYou are Connected ");
                } else {
                    //stat.setText("\nNot Connected !");
                }
                if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                    // Determine if Wifi P2P mode is enabled or not, alert
                    // the Activity.
                    Log.d("P2P", "STATE_CHANGED_ACTION");
                    // Toast.makeText(context, "STATE_CHANGED_ACTION", Toast.LENGTH_SHORT).show();

                } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                    // The peer list has changed!  We should probably do something about
                    // that.
                    mManager.requestPeers(mChannel, plist);
                    //   Toast.makeText(context, "PEERS_CHANGED_ACTION", Toast.LENGTH_SHORT).show();
                    Log.d("P2P", "PEERS_CHANGED_ACTION");

                } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                    //   Toast.makeText(context, "CONNECTION_CHANGED_ACTION", Toast.LENGTH_SHORT).show();
//                networkInfo = (NetworkInfo) intent
//                        .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
//
//                if (networkInfo!=null && networkInfo.isConnected()) {
//                    stat=(TextView)findViewById(R.id.stat);
//
//                    stat.setText("Connected ");
//
//                    // We are connected with the other device, request connection
//                    // info to find group owner IP
//                    mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
//                        @Override
//                        public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
//                            stat=(TextView)findViewById(R.id.stat);
//                            if(wifiP2pGroup!=null){
//                                Iterator<WifiP2pDevice> i=wifiP2pGroup.getClientList().iterator();
//                                stat.setText("Conencted to :\n");
//                                while(i.hasNext())
//                                {
//                                    devx=i.next();
//                                    stat.setText(stat.getText().toString()+"\n"+devx.deviceName);
//                                    Log.d("Connected Devices",devx.deviceName);
//                                }
//                                Log.d("Connected Devices :",wifiP2pGroup.getClientList().toString());
//                            }
//                        }
//                    });
//                    mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
//                        @Override
//                        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
//                            if (wifiP2pInfo.groupFormed)
//                            {
//
//                                String name=mydev.deviceName;
//                                Log.d("Who knew the name",name);
//                                UdpClientThread send=new UdpClientThread(name.getBytes(),wifiP2pInfo.groupOwnerAddress.getHostAddress(),4445);
//                                send.start();
//                                Snackbar.make(findViewById(R.id.R1),"Pinged with the group owner !",Snackbar.LENGTH_SHORT).show();
//                            }
//                        }
//                    });
//                }
                } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                    mydev = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
                    // Toast.makeText(context, "THIS_DEVICE_CHANGED_ACTION", Toast.LENGTH_SHORT).show();
                }
            }
        }


        public static byte[] serialize(Object obj) throws IOException {

            try (ByteArrayOutputStream b = new ByteArrayOutputStream()) {
                try (ObjectOutputStream o = new ObjectOutputStream(b)) {
                    o.writeObject(obj);

                }
                return b.toByteArray();
            }
        }

        public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
            try (ByteArrayInputStream b = new ByteArrayInputStream(bytes)) {
                try (ObjectInputStream o = new ObjectInputStream(b)) {
                    return o.readObject();
                }
            }
        }




    static class constraint implements Serializable {
        byte[] convert_str() {
            String str = Double.toString(xi) + "_" + Double.toString(yi) + "_" + Double.toString(rssi);
            return str.getBytes();
        }

        constraint(byte[] b) {
            //String s = b.toString();

            String s = new String(b);
            String[] d = s.split("_");
            Log.d("fdsa", s);
            xi = Double.parseDouble(d[0]);
            yi = Double.parseDouble(d[1]);
            rssi = Double.parseDouble(d[2]);
            System.out.println(xi + " " + yi + " " + rssi);


        }

        double xi, yi, r, rssi, p = -45, n = 2.07344;

        constraint() {
            xi = 0;
            yi = 0;
        }

        void s(double x, double y, double distance) {
            xi = x;
            yi = y;
            r = distance;
            rssi = 0;
        }

        void set(double x, double y, double rssi) {
            xi = x;
            yi = y;
            this.rssi = rssi;
            r = Math.pow(10, ((p + rssi) / 20.73));
            // System.out.println("r = " +r);
        }
    }

    static double distance(double x1, double y1, double x2, double y2) {

        double q = Math.abs(x1 - x2);
        double w = Math.abs(y1 - y2);
        double dist = Math.sqrt((q * q) + (w * w));
        return dist;
    }

    static double nCr(int n, int r) {
        int rfact = 1, nfact = 1, nrfact = 1, temp1 = n - r, temp2 = r;
        if (r > n - r) {
            temp1 = r;
            temp2 = n - r;
        }
        for (int i = 1; i <= n; i++) {
            if (i <= temp2) {
                rfact *= i;
                nrfact *= i;
            } else if (i <= temp1) {
                nrfact *= i;
            }
            nfact *= i;
        }
        return nfact / (double) (rfact * nrfact);
    }


    static void getcommon(constraint c1, constraint c2, int p, double arr[][]) {
        double x1, y1, x2, y2, xt, yt;
        double d = distance(c1.xi, c1.yi, c2.xi, c2.yi);


        if (d > c1.r + c2.r) {
            //System.out.println("inside if");
            double k1 = c1.r;
            double k2 = d - c1.r;

            x1 = ((k1 * c2.xi) + (k2 * c1.xi)) / (k1 + k2);
            y1 = ((k1 * c2.yi) + (k2 * c1.yi)) / (k1 + k2);

            double k11 = c2.r;
            double k22 = d - c2.r;

            x2 = ((k11 * c1.xi) + (k22 * c2.xi)) / (k11 + k22);
            y2 = ((k11 * c1.yi) + (k22 * c2.yi)) / (k11 + k22);

            //System.out.println(" Inner point = " + x1 + " " + y1);
            //	System.out.println(" outer point = " + x2 + " " + y2);

            xt = (x1 + x2) / 2;
            yt = (y1 + y2) / 2;

            //System.out.println(" avg point = " + xt + " " + yt);

        } else if (d + c2.r < c1.r) {
            System.out.println("inside first elseif");
            double k1 = c2.r;
            double k2 = c2.r + d;
            x1 = ((k1 * c1.xi) - (k2 * c2.xi)) / (k1 - k2);
            y1 = ((k1 * c1.yi) - (k2 * c2.yi)) / (k1 - k2);

            double k11 = c2.r + (c1.r - k2);
            double k22 = c1.r;

            x2 = ((k11 * c1.xi) - (k22 * c2.xi)) / (k11 - k22);
            y2 = ((k11 * c1.yi) - (k22 * c2.yi)) / (k11 - k22);

            //System.out.println(" Inner point = " + x1 + " " + y1);
            //System.out.println(" outer point = " + x2 + " " + y2);

            xt = (x1 + x2) / 2;
            yt = (y1 + y2) / 2;

            //System.out.println(" point = " + xt + " " + yt);
        } else if (d + c1.r < c2.r) {
            System.out.println("inside 2nd elsif");
            double k1 = c1.r;
            double k2 = c1.r + d;
            x1 = ((k1 * c2.xi) - (k2 * c1.xi)) / (k1 - k2);
            y1 = ((k1 * c2.yi) - (k2 * c1.yi)) / (k1 - k2);

            double k11 = c1.r + (c2.r - k2);
            double k22 = c2.r;

            x2 = ((k11 * c2.xi) - (k22 * c1.xi)) / (k11 - k22);
            y2 = ((k11 * c2.yi) - (k22 * c1.yi)) / (k11 - k22);

            //System.out.println(" Inner point = " + x1 + " " + y1);
            //System.out.println(" outer point = " + x2 + " " + y2);

            xt = (x1 + x2) / 2;
            yt = (y1 + y2) / 2;

            //System.out.println(" point = " + xt + " " + yt);
        } else {
            double a = ((c1.r * c1.r) - (c2.r * c2.r) + (d * d)) / (2 * d);

            double h = Math.sqrt((c1.r * c1.r) - (a * a));

            xt = c1.xi + (a * (c2.xi - c1.xi)) / d;

            yt = c1.yi + (a * (c2.yi - c1.yi)) / d;

            x1 = xt + (h * (c2.yi - c1.yi)) / d;
            y1 = yt - (h * (c2.xi - c1.xi)) / d;
            x2 = xt - (h * (c2.yi - c1.yi)) / d;
            y2 = yt + (h * (c2.xi - c1.xi)) / d;
            //System.out.println(" first point " + x1 + " " + y1);
            // System.out.println(" second point " + x2 + " " + y2);
        }
        arr[p][0] = x1;
        arr[p][1] = y1;
        arr[p][2] = x2;
        arr[p][3] = y2;
    }

}

