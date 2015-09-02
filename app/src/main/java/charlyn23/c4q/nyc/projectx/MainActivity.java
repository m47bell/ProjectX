package charlyn23.c4q.nyc.projectx;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.parse.ParseFacebookUtils;

import charlyn23.c4q.nyc.projectx.shames.ShameDetailActivity;


public class MainActivity extends AppCompatActivity implements ProjectXMapFragment.OnDataPass, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static final String TAG = "c4q.nyc.projectx";
    public static final String LAT_LONG = "latLong";
    public static final String LOGGED_IN = "isLoggedIn";
    public static final String LOGGED_IN_GOOGLE = "logIn_Google";
    public static final String SHOULD_RESOLVE = "should_resolve";
    public static final String IS_RESOLVING = "is_resolving";
    public static final int MAP_VIEW = 0;
    public static final int RC_SIGN_IN = 0;
    public static final String SHARED_PREFERENCE = "sharedPreference";
    private NoSwipeViewPager viewPager;
    private PagerAdapter viewPagerAdapter;
    public GoogleApiClient googleLogInClient;
    private boolean isLoggedIn, isLoggedIn_google;
    private SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE);
        isLoggedIn = preferences.getBoolean(LOGGED_IN, false);
        isLoggedIn_google = preferences.getBoolean(LOGGED_IN_GOOGLE, false);

        // Connect to Geolocation API to make current location request & load map
        buildGoogleApiClient(this);
        setUpActionBar();
    }

    protected synchronized void buildGoogleApiClient(Context context) {
        googleLogInClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(new Scope(Scopes.EMAIL))
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("LOG OUT PELASE", "onConnected: Google+");

        if (Plus.PeopleApi.getCurrentPerson(googleLogInClient) != null && !isLoggedIn_google) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(LOGGED_IN, true).apply();
            editor.putBoolean(IS_RESOLVING, false).apply();
            editor.putBoolean(MainActivity.SHOULD_RESOLVE, true).apply();
            isLoggedIn_google = true;
            editor.putBoolean(LOGGED_IN_GOOGLE, true).apply();
            Toast.makeText(this, "Signing in", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onConnectionSuspended ( int i){
        Log.d("LOG OUT PELASE", "Connection suspended in mainactivity");
        googleLogInClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("LOG OUT PELASE", "onConnectionFailed: " + connectionResult);

            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(this, RC_SIGN_IN);
                    preferences.edit().putBoolean(IS_RESOLVING, true).apply();
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "Could not resolve ConnectionResult.", e);
                    preferences.edit().putBoolean(IS_RESOLVING, false).apply();
                }
            } else {
                Toast.makeText(this, getString(R.string.network_connection_problem), Toast.LENGTH_LONG).show();
            }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            // If the error resolution was not successful we should not resolve further.
            if (resultCode != RESULT_OK) {
                preferences.edit().putBoolean(SHOULD_RESOLVE, false).apply();
            }

            preferences.edit().putBoolean(IS_RESOLVING, false).apply();
            googleLogInClient.connect();

            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(LOGGED_IN, true).apply();
            editor.putBoolean(MainActivity.SHOULD_RESOLVE, true).apply();
            editor.putBoolean(LOGGED_IN_GOOGLE, true).apply();
            Toast.makeText(this, "Signing in", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    public void setUpActionBar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        viewPager = (NoSwipeViewPager) findViewById(R.id.view_pager);
        setSupportActionBar(mToolbar);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.map));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.stats));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.profile));

        viewPagerAdapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount(), isLoggedIn, googleLogInClient);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 1) {
                    StatsFragment statsFragment = (StatsFragment) viewPagerAdapter.getItem(position);
                    statsFragment.pageSelected();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    //displays the first page on the Back Button pressed
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            viewPager.setCurrentItem(0, true);
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onDataPass(double latitude, double longitude, String when, String who, String type) {
        Log.d("onDataPass" , String.valueOf(latitude) + " " +  String.valueOf(longitude) +" " +  when +" " +   who +" " +  type);
        double shameLat = latitude;
        double shameLong = longitude;
        String shameDateTime = when;
        String shameGroup = who;
        String shameType =  type;

        Intent intent = new Intent(MainActivity.this, ShameDetailActivity.class);
        intent.putExtra("when", when);
        Log.i("date intent has ", String.valueOf(when));
        intent.putExtra("who", who);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        intent.putExtra("type", type);
        startActivity(intent);

//        TextView group = (TextView) shameDetailActivity.findViewById(R.id.group);
//        group.setText(who);

    }

    @Override
    protected void onStop() {
        super.onStop();
        googleLogInClient.disconnect();
        Log.d("LOG IN PELASE", "DISCONNECTED IN MAIN ACTIVITY");
    }
}
