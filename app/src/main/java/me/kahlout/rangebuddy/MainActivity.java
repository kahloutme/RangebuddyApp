package me.kahlout.rangebuddy;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;

import com.google.firebase.analytics.FirebaseAnalytics;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Toolbar mToolbar;
    DrawerLayout mDrawer;
    NavigationView mNavigationView;
    public static Activity mActivity;

    // Firebase Analytics
    private FirebaseAnalytics mFirebaseAnalytics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mActivity = this;

        /// Set first screen
        displaySelectedScreen(R.id.nav_map);
    }
    //in the mActivity we store the activity we have in OnCreate method and use this variable in the project instead of using getContext(),getActivity() etc.
    public static Activity getActivity(){

        return mActivity;
    }
    //Here we add onResume which will be called each time you minimize and maximize the app. It will check if the mActivity is not null and assign the current Activity.
    //Sometimes after minimizing the app system can kill some variables for RAM economy purposes, So recently defined mActivity can become null.
    @Override
    protected void onResume() {
        super.onResume();
        if(mActivity==null)
        {
            mActivity = MainActivity.this;
        }
    }

    @Override
    public void onBackPressed() {
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public boolean displaySelectedScreen(int itemId) {
        // Handle navigation view item clicks here.
//        int id = item.getItemId();

        Fragment myFragment = null;

        switch (itemId) {
            case R.id.nav_map:
                myFragment = new MapFragment();
                break;

            case R.id.nav_premium:
                myFragment = new PremiumFragment();
                break;

            case R.id.nav_settings:
                myFragment = new SettingsFragment();
                break;
        }

        //replacing the fragment
        if (myFragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, myFragment);
            ft.commit();
        }

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /// Correct location settings results
        if (requestCode == 999) {

            /// When results are back they come to Activity instead of Fragment. Here we send them back.
            Fragment frg = getSupportFragmentManager().findFragmentById(R.id.content_frame);
            if (frg != null) {
                frg.onActivityResult(requestCode, resultCode, data);
            }
        }

    }

    //// Relevant but not used methods
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        //calling the method displayselectedscreen and passing the id of selected menu
        displaySelectedScreen(item.getItemId());
        return true;
    }


}
