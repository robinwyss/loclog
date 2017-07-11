package com.robinwyss.places;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

import io.paperdb.Paper;


public class MainActivity extends AppCompatActivity {

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
//    private TextView textView;
//    private List<String> locations = new ArrayList<>();
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        textView = (TextView) findViewById(R.id.textView);
//        textView.setText("Hello");
        setSupportActionBar(toolbar);
        Intent mServiceIntent = new Intent(this, LocationService.class);
        startService(mServiceIntent);

        LocationUpdateReceiver locationUpdateReceiver = new LocationUpdateReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(locationUpdateReceiver,  new IntentFilter(
                Constants.LOCATION_UPDATE));

//        mServiceIntent.setData(Uri.parse(dataUrl));



        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLocation();
    }

//    private void addLocationListener() {
//        mLocationRequest = createLocationRequest();
//        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
//                .addLocationRequest(mLocationRequest);
//
//        mLocationCallback = new LocationCallback() {
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                for (Location location : locationResult.getLocations()) {
//                    Date date = new Date(location.getTime());
//                    DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
//                    String newLocation = String.format(" %s - lat: %f, long: %f, %f",dateFormat.format(date), location.getLatitude(), location.getLongitude(), location.getAltitude());
//                    locations.add(newLocation);
//                    printLocations();
//                }
//            }
//        };
//
//
//        SettingsClient client = LocationServices.getSettingsClient(this);
//        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
//
//        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
//            @Override
//            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
//                Log.d(TAG,"Added listener");
//            }
//        });
//
//        task.addOnFailureListener(this, new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(getApplicationContext(), "Failed to add listener ", Toast.LENGTH_SHORT).show();
//                int statusCode = ((ApiException) e).getStatusCode();
//                switch (statusCode) {
//                    case CommonStatusCodes.RESOLUTION_REQUIRED:
//                        // Location settings are not satisfied, but this can be fixed
//                        // by showing the user a dialog.
//                        try {
//                            // Show the dialog by calling startResolutionForResult(),
//                            // and check the result in onActivityResult().
//                            ResolvableApiException resolvable = (ResolvableApiException) e;
//                            resolvable.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
//                        } catch (IntentSender.SendIntentException sendEx) {
//                            // Ignore the error.
//                        }
//                        break;
//                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
//                        // Location settings are not satisfied. However, we have no way
//                        // to fix the settings so we won't show the dialog.
//                        break;
//                }
//            }
//        });
//        startLocationUpdates();
//    }

    private void printLocations(ArrayList<String> locations) {
        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(TextUtils.join("\n", locations));
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
                            textView.setText(String.format("lat: %f, long: %f, altitude, %f", location.getLatitude(), location.getLongitude(), location.getAltitude()));
//                            Toast.makeText(getApplicationContext(), String.format("lat: %d, long: %d, altitude, %d",location.getLatitude(), location.getLongitude(), location.getAltitude()),  Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getApplicationContext(), "Need location permission",  Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
            GetLastKnowLocation();
//            addLocationListener();
        }
    }

//    private void startLocationUpdates() {
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
//                mLocationCallback,
//                null /* Looper */);
//    }
//
//    protected LocationRequest createLocationRequest() {
//        LocationRequest mLocationRequest = new LocationRequest();
//        mLocationRequest.setInterval(10000);
//        mLocationRequest.setFastestInterval(5000);
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_NO_POWER);
//        return mLocationRequest;
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
}
