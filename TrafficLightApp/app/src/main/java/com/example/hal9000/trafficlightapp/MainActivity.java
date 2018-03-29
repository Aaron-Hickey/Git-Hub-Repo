package com.example.hal9000.trafficlightapp;

import android.content.res.Configuration;
import android.net.Uri;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;



public class MainActivity extends AppCompatActivity implements bluetooth_console.OnFragmentInteractionListener, global_view.OnFragmentInteractionListener, monitoring.OnFragmentInteractionListener, config.OnFragmentInteractionListener {
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;

    public Fragment globalF;
    Fragment configF;
    Fragment monitorF;
    Fragment consoleF;
    Fragment currentF;
    FragmentManager fragmentManager;

    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        setupDrawerContent(nvDrawer);
        drawerToggle = setupDrawerToggle();
        mDrawer.addDrawerListener(drawerToggle);
        setUpFragments();

    }

    public void setUpFragments()
    {
        fragmentManager = getSupportFragmentManager();
        globalF = global_view.newInstance();
        configF = config.newInstance();
        monitorF = monitoring.newInstance();
        consoleF = bluetooth_console.newInstance();
        fragmentManager.beginTransaction().add(R.id.flContent,globalF,"globalF").commit();
        fragmentManager.beginTransaction().add(R.id.flContent,configF,"configF").hide(configF).commit();
        fragmentManager.beginTransaction().add(R.id.flContent,monitorF,"monitorF").hide(monitorF).commit();
        fragmentManager.beginTransaction().add(R.id.flContent,consoleF,"consoleF").hide(consoleF).commit();
        currentF = globalF;
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
            case R.id.nav_first_fragment:
                swapFragment(globalF);
                break;
            case R.id.nav_second_fragment:
                swapFragment(configF);
                break;
            case R.id.nav_third_fragment:
                swapFragment(monitorF);
                break;
            case R.id.nav_bluetooth_console:
                swapFragment(consoleF);
                break;
            default:
                swapFragment(globalF);
        }


       // menuItem.setChecked(true);
        //setTitle(menuItem.getTitle());
        mDrawer.closeDrawers();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //you can leave it empty
    }

    @Override
    public void updateGlobal(String t) {
        global_view f =(global_view) fragmentManager.findFragmentByTag("globalF");
        f.applyTypology(t);
        swapFragment(globalF);
    }
    public void swapFragment(Fragment n)
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
