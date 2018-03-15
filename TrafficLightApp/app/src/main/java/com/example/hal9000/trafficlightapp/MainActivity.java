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

    Fragment globalF;
    Fragment configF;
    Fragment monitorF;
    Fragment consoleF;
    Fragment currentF;
    FragmentManager fragmentManager;
    // Make sure to be using android.support.v7.app.ActionBarDrawerToggle version.
    // The android.support.v4.app.ActionBarDrawerToggle has been deprecated.
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        // Setup drawer view
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
        fragmentManager.beginTransaction().add(R.id.flContent,globalF).commit();
        fragmentManager.beginTransaction().add(R.id.flContent,configF).hide(configF).commit();
        fragmentManager.beginTransaction().add(R.id.flContent,monitorF).hide(monitorF).commit();
        fragmentManager.beginTransaction().add(R.id.flContent,consoleF).hide(consoleF).commit();
        currentF = globalF;
    }
    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open, R.string.drawer_close);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
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
        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;
        //Class fragmentClass;
        switch (menuItem.getItemId()) {
            case R.id.nav_first_fragment:
                fragment = globalF;
                break;
            case R.id.nav_second_fragment:
                fragment = configF;
                break;
            case R.id.nav_third_fragment:
                fragment = monitorF;
                break;
            case R.id.nav_bluetooth_console:
                fragment = consoleF;
                break;
            default:
                fragment = globalF;
        }


        // Insert the fragment by replacing any existing fragment
       // fragmentManager = getSupportFragmentManager();
        //fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
        //ft.add( R.id.flContent,fragment);
        fragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .hide(currentF)
                .commit();
        fragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .show(fragment)
                .commit();

        currentF = fragment;

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        // Set action bar title
        setTitle(menuItem.getTitle());
        // Close the navigation drawer
        mDrawer.closeDrawers();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //you can leave it empty
    }
}
