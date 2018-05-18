package com.example.hal9000.trafficlightapp;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Handler;
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
    private String[] stateValues =  {"Off", "Active", "Passive", "Yellow Flashing"};
    private String[] substateValues = {"Full Red", "Green", "Orange", "Red", "Red Extended", "Green Flashing", "Yellow Flashing", "Full Red Barrage", "Green Barrage", "Orange Barrage", "Red Barrage"};
    //private String[] typologyValues = {"Error", }

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

        //   menuItem.setChecked(true);
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
    public void updateGlobal(String typology, String mode, int distance) throws IOException {
        global_view f = (global_view) fragmentManager.findFragmentByTag("globalF");
        f.createTrafficLights(typology, mode, distance);
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
        if (!bluetoothAdapter.isEnabled())
        {
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
                                if(data != null) {
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
    public void parseData(String data)
    {
        String response[] = data.split(":", 2);
        if(response.length == 2) {
            String command = response[1];
            if (response[0].equals("Config")) {
                System.out.println("Config is " + response[1]);
            } else if (response[0].equals("Monitoring")) {
                System.out.println("Monitoring is "+response[1]);
                global_view f = (global_view) fragmentManager.findFragmentByTag("globalF");

                int id = Character.getNumericValue(command.charAt(0));
                String state = stateValues[ Character.getNumericValue(command.charAt(1))];
                f.updateTrafficLights(id,state,"","2","mode1","5/h",200,1);

            }
        }
        else
        {
            System.out.println(response[0]);
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
}
