package com.example.navigation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.navigation.databinding.ActivityMapsBinding;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    /** GOOGLE MAPS PLATFORMS INSTANCE VARIABLES **/
    // Maps fields
    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    // Geofencing fields
    private GeofencingClient crosswalkDetectionGeofencingClient;
    private PendingIntent crosswalkDetectionGeofencePendingIntent;
    private List<Geofence> crosswalkDetectionGeofenceList = new ArrayList<>();

    private GeofencingClient walkingStraightGeofencingClient;
    private PendingIntent walkingStraightGeofencePendingIntent;
    private List<Geofence> walkingStraightGeofenceList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        /**
         * MAPS API SETUP
         */
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        System.out.println("Adding markers");

        /**
         * MARKER SETUP
         */
        addMarkersMain();

        System.out.println("Finished adding markers. My Location Setup");

        /**
         * MY LOCATION SETUP
         */
        myLocationSetupMain();

        System.out.println("My Location enabled. Geofencing");

        /**
         * GEOFENCING SETUP
         */
        geofenceSetupMain();
    }

    /**
     * MARKER SETUP HELPER METHODS
     */

    /**
     * Add Markers Main Helper Method
     */
    private void addMarkersMain() {

        // 1. Add all markers to map
        mMap.addMarker(new MarkerOptions().position(Constants.CSE_BUILDING.latLng).title(Constants.CSE_BUILDING.name));
        mMap.addMarker(new MarkerOptions().position(Constants.CSE_CROSSWALK.latLng).title(Constants.CSE_CROSSWALK.name));
        mMap.addMarker(new MarkerOptions().position(Constants.CSE_WALKING_STRAIGHT_0.latLng).title(Constants.CSE_WALKING_STRAIGHT_0.name));
        mMap.addMarker(new MarkerOptions().position(Constants.CSE_WALKING_STRAIGHT_1.latLng).title(Constants.CSE_WALKING_STRAIGHT_1.name));

        // 2. Center camera on CSE_BUILDING marker and set zoom
        mMap.moveCamera(CameraUpdateFactory.newLatLng(Constants.CSE_BUILDING.latLng));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(18));

    }


    /**
     * LOCATIONS PERMISSIONS HELPER METHODS
     */

    /**
     * My Location Setup Main Helper Method
     */
    private void myLocationSetupMain() {

        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();

    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private void enableMyLocation() {

        // 1. Check if permissions are granted, if so, enable the my location layer
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, permission.ACCESS_BACKGROUND_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            System.out.println("Location permissions granted. Enabling user location...");
            mMap.setMyLocationEnabled(true);
            return;
        }

        // 2. Otherwise, request location permissions from the user.
        System.out.println("Location permissions not granted. Requesting user for location permissions...");
        PermissionUtils.requestPermission(this, Constants.LOCATION_PERMISSION_REQUEST_CODE, permission.ACCESS_FINE_LOCATION, true);
        PermissionUtils.requestPermission(this, Constants.LOCATION_PERMISSION_REQUEST_CODE, permission.ACCESS_COARSE_LOCATION, true);
        PermissionUtils.requestPermission(this, Constants.LOCATION_PERMISSION_REQUEST_CODE, permission.ACCESS_BACKGROUND_LOCATION, true);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != Constants.LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION) || PermissionUtils
                .isPermissionGranted(permissions, grantResults,
                        Manifest.permission.ACCESS_COARSE_LOCATION) || PermissionUtils
                .isPermissionGranted(permissions, grantResults,
                        permission.ACCESS_BACKGROUND_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Permission was denied. Display an error message
            // Display the missing permission error dialog when the fragments resume.
            Constants.permissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (Constants.permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            Constants.permissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }


    /**
     * GEOFENCING HELPER METHODS
     */

    /**
     * Geofence Setup Main Helper Method
     */
    private void geofenceSetupMain() {

        geofenceSetupMainDifferentTypes(crosswalkDetectionGeofencingClient, crosswalkDetectionGeofenceList, crosswalkDetectionGeofencePendingIntent, Constants.CROSSWALK_DETECTION_GEOFENCE);
        geofenceSetupMainDifferentTypes(walkingStraightGeofencingClient, walkingStraightGeofenceList, walkingStraightGeofencePendingIntent, Constants.WALKING_STRAIGHT_GEOFENCE);

    }

    /**
     * Helper method for two different Geofence Main Setups
     */
    private void geofenceSetupMainDifferentTypes(GeofencingClient geofencingClient, List<Geofence> geofenceList, PendingIntent geofencePendingIntent, String geofenceType) {

        geofencingClient = LocationServices.getGeofencingClient(this);

        if (geofenceType.equals(Constants.CROSSWALK_DETECTION_GEOFENCE)) {
            addGeofenceToList(geofenceList, Constants.CSE_CROSSWALK, Constants.GEOFENCE_RADIUS_FOR_CROSSWALK_DETECTION);
        }

        else if (geofenceType.equals(Constants.WALKING_STRAIGHT_GEOFENCE)) {
            addGeofenceToList(geofenceList, Constants.CSE_WALKING_STRAIGHT_0, Constants.GEOFENCE_RADIUS_FOR_WALKING_STRAIGHT);
            addGeofenceToList(geofenceList, Constants.CSE_WALKING_STRAIGHT_1, Constants.GEOFENCE_RADIUS_FOR_WALKING_STRAIGHT);
        }

        System.out.println("geofenceList: " + geofenceList);

        // Add all Geofences to geofencingClient
        addGeofencesToClient(geofencingClient, geofenceList, geofencePendingIntent, geofenceType);

    }

    /**
     * Helper method to add Geofence to geofenceList
     */
    private void addGeofenceToList(List<Geofence> geofenceList, LocEntry locEntry, float geofenceRadius) {

        // Add geofence to geofenceList
        geofenceList.add(new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(locEntry.name)

                .setCircularRegion(
                        locEntry.latLng.latitude,
                        locEntry.latLng.longitude,
                        geofenceRadius
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());

        // Add visual geofence circle to Geofence
        addCircleToGeofence(locEntry, geofenceRadius);

    }

    /**
     * Helper method to add visual geofence circle to Geofences
     */
    private void addCircleToGeofence(LocEntry locEntry, float geofenceRadius) {

        CircleOptions circle = new CircleOptions().center(locEntry.latLng).radius(geofenceRadius);
        if (geofenceRadius == Constants.GEOFENCE_RADIUS_FOR_CROSSWALK_DETECTION) {
            circle.strokeColor(Color.RED).strokeWidth(4f).fillColor(Color.argb(64, 255, 0, 0));
        } else if (geofenceRadius == Constants.GEOFENCE_RADIUS_FOR_WALKING_STRAIGHT) {
            circle.strokeColor(Color.GREEN).strokeWidth(4f).fillColor(Color.argb(64, 0, 255, 0));
        }
        mMap.addCircle(circle);

    }

    /**
     * Helper method to add all Geofences to Geofencing Client
     */
    @SuppressLint("MissingPermission")
    private void addGeofencesToClient(GeofencingClient geofencingClient, List<Geofence> geofenceList, PendingIntent geofencePendingIntent, String geofenceType) {

        GeofencingRequest geofencingRequest = getGeofencingRequest(geofenceList);
        PendingIntent gPI = getGeofencePendingIntent(geofencePendingIntent, geofenceType);

        // Add all Geofences in geofenceList to Geofencing Client to publish to application geofencing API
        geofencingClient.addGeofences(geofencingRequest, gPI)

            .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    System.out.println("onSuccess: Geofences added successfully to Geofencing Client to publish to application geofencing API");
                    System.out.println("onSuccess: Geofencing client = " + geofencingClient);
                    System.out.println("onSuccess: Geofence list = " + geofenceList);
                    System.out.println("onSuccess: Geofence request = " + geofencingRequest.getGeofences());
                    System.out.println("onSuccess: Geofence pending intent = " + gPI);
                    System.out.println("onSuccess: Geofence type = " + geofenceType);

                }
            })

            .addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    System.out.println("onFailure: ");
                    System.out.println("Beginning of geofencingClient.addGeofences.onFailure() Exception --- ");
                    e.printStackTrace();
                    System.out.println(" --- End of geofencingClient.addGeofences.onFailure() Exception");
                }

            });

    }

    /**
     * Specifies geofences and initial triggers
     */
    private GeofencingRequest getGeofencingRequest(List<Geofence> geofenceList) {

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        return builder.build();

    }

    /**
     * Defines a broadcast receiver for geofence transitions
     */
    private PendingIntent getGeofencePendingIntent(PendingIntent geofencePendingIntent, String geofenceType) {

        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }

        Intent intent;
        if (geofenceType.equals(Constants.CROSSWALK_DETECTION_GEOFENCE)) {
            intent = new Intent(this, GeofenceBroadcastReceiver.class);
            intent.putExtra("geofenceType", geofenceType);
            crosswalkDetectionGeofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            return crosswalkDetectionGeofencePendingIntent;
        } else {
            intent = new Intent(this, GeofenceBroadcastReceiver.class);
            intent.putExtra("geofenceType", geofenceType);
            walkingStraightGeofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            return walkingStraightGeofencePendingIntent;
        }

//        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
//        intent.putExtra("geofenceType", geofenceType);
//
//        geofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        return geofencePendingIntent;
    }

}