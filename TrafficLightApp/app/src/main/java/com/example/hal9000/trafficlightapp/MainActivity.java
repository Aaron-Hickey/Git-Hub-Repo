package com.example.hal9000.trafficlightapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.service.notification.StatusBarNotification;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements global_view.globalInterface, monitoring.OnFragmentInteractionListener, config.configInterface, deviceScreen.deviceScreenInterface {
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private Fragment globalF;
    private Fragment configF;
    private Fragment monitorF;
    private Fragment currentF;
    private Fragment devicesF;
    private FragmentManager fragmentManager;
    private ActionBarDrawerToggle drawerToggle;

    private Thread workerThread;
    volatile boolean stopWorker;
    private boolean hasAdapter = false;
    private BluetoothAdapter bluetoothAdapter;
    private bluetoothFunctions bf;

    private String[] stateValues = {"Off", "Active", "Passive"};
    private String[] substateValues = {"Full Red", "Green", "Yellow", "Red", "Red Extended", "Green Flashing", "Yellow Flashing", "Full Red Barrage", "Green Barrage", "Yellow Barrage", "Red Barrage"};
    private String[] typologyValues = {"Error", "2F P Turning", "3F P Turning", "3F P PR SE", "4F P Turning", "4F PR SE A A", "4F PR SE S S", "4F PR SE A S", "4F PR SE S A"};
    private String[] modeValues = {"Pendular", "Red Barrage", "Green Force"};
    private String[] densityValues = {"Low", "Average", "Strong", "Very Strong", "Max"};
    private String[] batteryValues = {"Out", "Deep Discharge", "Discharged", "Normal", "Full", "Charging"};

    private NotificationManager notificationManager;

    private boolean opticalFailure = false;
    private boolean fallen = false;
    private boolean cycleDesync = false;
    private boolean signalLost = false;

    private ArrayList<trafficLight> trafficLightList = new ArrayList();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawer = findViewById(R.id.drawer_layout);
        nvDrawer = findViewById(R.id.nvView);
        setupDrawerContent(nvDrawer);
        drawerToggle = setupDrawerToggle();
        mDrawer.addDrawerListener(drawerToggle);
        setUpFragments();
        setTitle("Bluetooth Devices");
        connectAdapter();
        bf = bluetoothFunctions.getInstance();
        bf.connectAdapter();
        notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        listenForResponse();
    }

    public void setUpFragments() {
        fragmentManager = getSupportFragmentManager();
        globalF = global_view.newInstance();
        configF = config.newInstance();
        monitorF = monitoring.newInstance();
        devicesF = deviceScreen.newInstance();
        fragmentManager.beginTransaction().add(R.id.flContent, devicesF, "devicesF").commit();
        fragmentManager.beginTransaction().add(R.id.flContent, globalF, "globalF").hide(globalF).commit();
        fragmentManager.beginTransaction().add(R.id.flContent, configF, "configF").hide(configF).commit();
        fragmentManager.beginTransaction().add(R.id.monitorPopUp, monitorF, "monitorF").hide(monitorF).commit();
        currentF = devicesF;
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open, R.string.drawer_close);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_Global:
                swapFragment(globalF);
                break;
            case R.id.nav_Config:
                swapFragment(configF);
                break;
            case R.id.nav_Monitor:
                swapFragment(monitorF);
                break;
            case R.id.nav_bluetooth_console:
                swapFragment(devicesF);
                break;
            default:
                swapFragment(devicesF);
        }
        setTitle(menuItem.getTitle());
        mDrawer.closeDrawers();
    }

    @Override
    public void returnToGlobal() {
        setTitle("Global View");

        swapFragment(globalF);
    }

    @Override
    public void updateMonitoring(trafficLight tl) {
        monitoring fr = (monitoring) fragmentManager.findFragmentByTag("monitorF");
        fr.updateInfo(tl);
        setTitle("Monitoring");
        swapFragment(monitorF);
    }

    @Override
    public void updateGlobal(String typology) throws IOException {
        global_view f = (global_view) fragmentManager.findFragmentByTag("globalF");
        trafficLightList = f.createTrafficLights(typology);
        //   System.out.println("The ID "+trafficLightList.get(0).getId());
        setTitle("Global View");
        swapFragment(globalF);
    }

    public void connectAdapter() {
        if (hasAdapter == false) {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                System.out.println("No Adapter Found");
            }
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(bluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
        }
    }

    private void listenForResponse() {
        stopWorker = false;
        final Handler handler = new Handler();
        workerThread = new Thread(new Runnable() {
            public void run() {

                while (!Thread.currentThread().isInterrupted() && !stopWorker) {

                    try {
                        final String data = bf.listenForResponse();
                        handler.post(new Runnable() {
                            public void run() {
                                if (data != null) {
                                    parseData(data);
                                }
                            }
                        });
                        Thread.sleep(100);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        workerThread.start();
    }

    public void parseData(String data) {
        String response[] = data.split(":", 2);
        if (response.length == 2) {
            String command = response[1];
            if (response[0].equals("Config")) {
               configResponse(command);
            } else if (response[0].equals("Monitoring")) {
                monitoringResponse(command);
            } else if (response[0].equals("Global")) {
                globalResponse(command);
            }
        } else {
            System.out.println(response[0]);
        }
    }

    public void globalResponse(String command) {
        if (Character.isDigit(command.charAt(0))) {
            int tempInt = Character.getNumericValue(command.charAt(0));
            if (tempInt > 0 && tempInt < typologyValues.length) {
                try {
                    updateGlobal(typologyValues[tempInt]);
                } catch (IOException e) {
                    e.printStackTrace();
                    notifyWarning("Failed to Update Global View", 6);
                }
            }
        }
    }

    public void monitoringResponse(String command) {
        global_view f = (global_view) fragmentManager.findFragmentByTag("globalF");
        boolean valid = true;
        for(int x = 0; x < command.length(); x++)
        {
            if(!Character.isDigit(command.charAt(x)))
            {
                valid = false;
            }
        }
        if (command.length() >= 13 && valid) {
            //ID
            int temp = Character.getNumericValue(command.charAt(0));
            if(temp > 4 || temp < 1)
            {
                valid = false;
            }
            int id = Character.getNumericValue(command.charAt(0));
            //State
            temp = Character.getNumericValue(command.charAt(1));
            if(temp > stateValues.length || temp < 0)
            {
                valid = false;
            }
            String state = stateValues[Character.getNumericValue(command.charAt(1))];
            //Substate
            temp = Character.getNumericValue(command.charAt(2));
            if(temp > substateValues.length || temp < 0)
            {
                valid = false;
            }
            String substate = substateValues[Character.getNumericValue(command.charAt(2))];
            //Typology
            temp = Character.getNumericValue(command.charAt(3));
            if(temp > typologyValues.length || temp < 0)
            {
                valid = false;
            }
            String typology = typologyValues[Character.getNumericValue(command.charAt(3))];
            //Mode
            temp = Character.getNumericValue(command.charAt(4));
            if(temp > modeValues.length || temp < 0)
            {
                valid = false;
            }
            String mode = modeValues[Character.getNumericValue(command.charAt(4))];
            //Density
            temp = Character.getNumericValue(command.charAt(5));
            if(temp > densityValues.length || temp < 0)
            {
                valid = false;
            }
            String density = densityValues[Character.getNumericValue(command.charAt(5))];

            //Distance
            StringBuilder sb = new StringBuilder();
            int distance = Integer.parseInt(sb.append(command.charAt(6)).append(command.charAt(7)).toString()) * 100;
            if(distance<100 || distance > 3000)
            {
                valid = false;
            }
            //Battery
            temp = Character.getNumericValue(command.charAt(8));
            if(temp > batteryValues.length || temp < 0)
            {
                valid = false;
            }
            String battery = batteryValues[Character.getNumericValue(command.charAt(8))];

            if(valid)
            {
            if (Character.getNumericValue(command.charAt(9)) == 1) {
                opticalFailure = true;
                notifyWarning("Traffic Light " + id + " has an optical failure", id);
            }
            else
            {
                opticalFailure = false;
            }
            if (Character.getNumericValue(command.charAt(10)) == 1) {
                fallen = true;
                notifyWarning("Traffic Light " + id + " has fallen over", id);
            }
            else
            {
                fallen = false;
            }
            if (Character.getNumericValue(command.charAt(11)) == 1) {
                cycleDesync = true;
                notifyWarning("Traffic Light " + id + " has a cycle desync", id);
            }
            else
            {
                cycleDesync = false;
            }
            if (Character.getNumericValue(command.charAt(12)) == 1) {
                signalLost = true;
                notifyWarning("Traffic Light " + id + " has lost signal", id);
            }
            else
            {
                signalLost = false;
            }

                f.updateTrafficLights(id, state, substate, typology, mode, density, distance, battery, opticalFailure, fallen, cycleDesync, signalLost);
            }
            else
            {
                notifyWarning("Invalid Response from Traffic Light", 6);
            }
        }
        else
        {
            notifyWarning("Invalid Response from Traffic Light", 6);

        }
    }

    public void configResponse(String command)
    {
        if (command.equals("no")) {
            notifyWarning("Configuration Failed", 5);
        }
    }

    public void swapFragment(Fragment n) {
        if (currentF != n) {

            fragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .hide(currentF)
                    .commit();
            fragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .show(n)
                    .commit();

            currentF = n;
        }
    }

    public void notifyWarning(String message, int id) {
        // Intent intent = new Intent(this, NotificationReceiver.class);
// use System.currentTimeMillis() to have a unique ID for the pending intent
        //  PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

// build notification
// the addAction re-use the same intent to keep the example short
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

            long[] pattern = {0, 500, 250, 500};

            Notification n = new Notification.Builder(this)
                    .setContentTitle("Alert")
                    .setContentText(message)
                    .setSmallIcon(R.drawable.ic_warning_sign)
                    .setLights(Color.BLUE, 100, 100)
                    .setSound(Uri.parse("android.resource://"
                            + this.getPackageName() + "/" + R.raw.alert))
                    // .setContentIntent(pIntent)
                    .setAutoCancel(true)
                    // .addAction(R.drawable.icon, "Call", pIntent)
                    // .addAction(R.drawable.icon, "More", pIntent)
                    // .addAction(R.drawable.icon, "And more", pIntent)
                    .setVibrate(pattern)
                    .build();

            StatusBarNotification[] notifications;
            notifications = notificationManager.getActiveNotifications();
            boolean notificationEnabled = false;
            for (StatusBarNotification notification : notifications) {
                if (notification.getId() == id) {
                    notificationEnabled = true;
                }
            }
            if (!notificationEnabled) {
                notificationManager.notify(id, n);
            }
        } else {
            Notification n = new Notification.Builder(this)
                    .setContentTitle("Alert")
                    .setContentText(message)
                    .setSmallIcon(R.drawable.ic_warning_sign)
                    .setLights(Color.BLUE, 100, 100)

                    // .setContentIntent(pIntent)
                    .setAutoCancel(true)
                    // .addAction(R.drawable.icon, "Call", pIntent)
                    // .addAction(R.drawable.icon, "More", pIntent)
                    // .addAction(R.drawable.icon, "And more", pIntent)
                    .build();
            notificationManager.notify(id, n);
        }
    }
}
