package com.robinwyss.places;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.DateFormat;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private static final String TAG = "MainActivity";
    private TextView textView;
    private DateFormat dateFormat;
    private DateFormat timeFormat;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final Location location = intent.getParcelableExtra(Constants.LOCATION_UPDATE_DATA);

            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    UpdateLastKnownLocation(location);
                    textView = (TextView) findViewById(R.id.textView);
                    textView.setText(textView.getText() + String.format("%s %s - lat: %f, long: %f, %f %n", timeFormat.format(location.getTime()), dateFormat.format(location.getTime()), location.getLatitude(), location.getLongitude(), location.getAccuracy()));
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        textView = (TextView) findViewById(R.id.textView);
        dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
        timeFormat = android.text.format.DateFormat.getTimeFormat(getApplicationContext());

        setSupportActionBar(toolbar);
        Intent mServiceIntent = new Intent(this, LocationService.class);
        startService(mServiceIntent);

//        LocationReceiver receiver = new LocationReceiver(new Handler());

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(
                Constants.LOCATION_UPDATE));

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLocation();
    }

    private void UpdateLastKnownLocation(Location location) {
        final TextView textView = (TextView) findViewById(R.id.currentLocation);
        textView.setText(String.format("%s: lat: %f, long: %f, ", timeFormat.format(location.getTime()), location.getLatitude(), location.getLongitude()));
    }

    private void GetLastKnowLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        final TextView textView = (TextView) findViewById(R.id.textView);
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            UpdateLastKnownLocation(location);
                        } else {
                            Toast.makeText(getApplicationContext(), "Could not get last location", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        getLocation();
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            Toast.makeText(getApplicationContext(), "Need location permission", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
            GetLastKnowLocation();
        }
    }

}
