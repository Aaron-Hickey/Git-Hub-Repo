package com.example.hal9000.trafficlightapp;

import android.bluetooth.BluetoothDevice;
import android.content.res.Configuration;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements bluetooth_console.OnFragmentInteractionListener, global_view.globalInterface, monitoring.OnFragmentInteractionListener, config.configInterface, deviceScreen.deviceScreenInterface {
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private Fragment globalF;
    private Fragment configF;
    private Fragment monitorF;
    private Fragment consoleF;
    private Fragment currentF;
    private Fragment devicesF;
    private FragmentManager fragmentManager;
    private ActionBarDrawerToggle drawerToggle;

    private bluetoothFunctions bf;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawer =  findViewById(R.id.drawer_layout);
        nvDrawer =  findViewById(R.id.nvView);
        setupDrawerContent(nvDrawer);
        drawerToggle = setupDrawerToggle();
        mDrawer.addDrawerListener(drawerToggle);
        setUpFragments();
        setTitle("Global View");
        bf= bluetoothFunctions.getInstance();
        bf.connectAdapter();

    }

    public void setUpFragments()
    {
        fragmentManager = getSupportFragmentManager();
        globalF = global_view.newInstance();
        configF = config.newInstance();
        monitorF = monitoring.newInstance();
        consoleF = bluetooth_console.newInstance();
        devicesF = deviceScreen.newInstance();
        fragmentManager.beginTransaction().add(R.id.flContent,devicesF,"devicesF").commit();
        fragmentManager.beginTransaction().add(R.id.flContent,globalF,"globalF").hide(globalF).commit();
        fragmentManager.beginTransaction().add(R.id.flContent,configF,"configF").hide(configF).commit();
        fragmentManager.beginTransaction().add(R.id.monitorPopUp,monitorF,"monitorF").hide(monitorF).commit();
        fragmentManager.beginTransaction().add(R.id.flContent,consoleF,"consoleF").hide(consoleF).commit();
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
                swapFragment(consoleF);
                break;
            default:
                swapFragment(globalF);
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
    public void updateMonitoring(trafficLight t) {
        monitoring f = (monitoring) fragmentManager.findFragmentByTag("monitorF");
        f.updateInfo(t);
        setTitle("Monitoring");
        swapFragment(monitorF);
    }

    @Override
    public void updateGlobal(String typology, String mode, int distance) throws IOException {
        global_view f =(global_view) fragmentManager.findFragmentByTag("globalF");
        f.createTrafficLights(typology, mode ,distance);
        setTitle("Global View");
        swapFragment(globalF);
    }

    public void swapFragment(Fragment n)
    {
        if(currentF != n)
        {

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
