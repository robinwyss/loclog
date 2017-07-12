package com.robinwyss.places;

import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class LocationService extends IntentService {

    private static final String TAG = "LocationService";
    private LocationRequest mLocationRequest;
    // TODO: store locations
    private List<String> locations = new ArrayList<>();

    public LocationService() {
        super("LocationService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            addLocationListener();
        }
    }

    private void addLocationListener() {
        mLocationRequest = createLocationRequest();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener( new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.d(TAG,"Added listener");
                startLocationUpdates();
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
               Log.w( TAG,"Failed to add listener ");
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        // since we only passively listen to location changes, we don't prompt the user to change the settings.
                        Log.w(TAG,"RESOLUTION_REQUIRED: Cannot get location updates as location services are disabled");
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        Log.w(TAG,"SETTINGS_CHANGE_UNAVAILABLE: Cannot get location updates as location services are disabled");
                        break;
                }
            }
        });

    }
    private void sendBroadcast(Location location){
        Intent localIntent =
                new Intent(Constants.LOCATION_UPDATE)
                        .putExtra(Constants.LOCATION_UPDATE_DATA, location);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private void startLocationUpdates() {
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    Date date = new Date(location.getTime());
                    DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
                    String newLocation = String.format(" %s - lat: %f, long: %f, %f", dateFormat.format(date), location.getLatitude(), location.getLongitude(), location.getAltitude());
                    locations.add(newLocation);
//                    printLocations();
                    sendBroadcast(location);
                }
            }
        };

        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_NO_POWER);
        return mLocationRequest;
    }
}
