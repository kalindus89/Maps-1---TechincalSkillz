package com.techincalskillz.your_truly_taxi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.PendingIntent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.techincalskillz.R;
import com.techincalskillz.mitch_2017_easy_learn_android.MapFragmentActivity;
import com.techincalskillz.retrofit_singleton_pattern.adapters.CustomMakerAdapter;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class YourTrulyActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerDragListener {

    GoogleMap googleMap;
    Marker mainMarker;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    HandlerThread handlerThread;
    LinearLayout currentLocation, getLocationName, getLocationDetails, geo_Fence;
    Marker carLocationMarker;
    Circle carLocationAccuracyCircle;
    GeofencingClient geofencingClient;
    GeoFenceHelper geoFenceHelper;

    private float GEOFENCE_RADIUS = 300; // this in meters

    // get Current location update continuously and app in background also
    LocationCallback locationCallBack = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult == null) {
                return;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Log.d("aaaaaaa1",locationResult.getLastLocation().getLatitude() + " Longitude: " + locationResult.getLastLocation().getLongitude());
                    //  System.out.println("aaaaaaaa2 " + "Latitude: " + locationResult.getLastLocation().getLatitude() + " Longitude: " + locationResult.getLastLocation().getLongitude());

                    if (googleMap != null) {
                        movingCarMarker(locationResult.getLastLocation());
                    }


                }
            });

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_truly);

        currentLocation = findViewById(R.id.currentLocation);
        currentLocation.setOnClickListener(this);

        getLocationName = findViewById(R.id.getLocationName);
        getLocationName.setOnClickListener(this);

        getLocationDetails = findViewById(R.id.getLocationDetails);
        getLocationDetails.setOnClickListener(this);

        geo_Fence = findViewById(R.id.geo_Fence);
        geo_Fence.setOnClickListener(this);


        //  getSupportActionBar().hide();

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_Fragment);
        mapFragment.getMapAsync(this); //onMapReady method automatically call. your Default map

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(YourTrulyActivity.this);

        geofencingClient = LocationServices.getGeofencingClient(this);
        geoFenceHelper = new GeoFenceHelper(this);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(3000); // location update time
        locationRequest.setFastestInterval(3000); // location update from the other apps in phone

        // checkSettingsAndStartLocationUpdates();
        getLastLocation();
    }

    private void checkSettingsAndStartLocationUpdates() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true);

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);

        Task<LocationSettingsResponse> locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build());

        locationSettingsResponseTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {

                startLocationUpdates(); // setting of device is satisfied. GPS is on

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                if (e instanceof ResolvableApiException) {
                    ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                    try {
                        resolvableApiException.startResolutionForResult(YourTrulyActivity.this, 101);
                    } catch (IntentSender.SendIntentException sendIntentException) {
                        sendIntentException.printStackTrace();
                    }
                }


            }
        });

    }

    private void startLocationUpdates() {


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        handlerThread = new HandlerThread("LocationCallBackThread");
        handlerThread.start();
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, handlerThread.getLooper());
    }

    public void stopLocationUpdates() {
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
        }
        if (handlerThread != null) {
            handlerThread.quit();
        }
    }

    private void getLastLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    Toast.makeText(YourTrulyActivity.this, "Latitude1: " + location.getLatitude() + " Longitude1: " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                    markOnMap(location.getLatitude(), location.getLongitude(), 15, "My Location", "Address: Nugegoda\nPhone Number: +94777");
                } else {
                    Toast.makeText(YourTrulyActivity.this, "No location found", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

        // markOnMap(6.8649, 79.8997, 15,"My Location","Address: Nugegoda\nPhone Number: +94777");

        googleMap.setOnMapLongClickListener(this);
        googleMap.setOnMarkerDragListener(this);


        //to get current location maker on the map
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true); // to enable current location with blue color marker
        googleMap.getUiSettings().setZoomControlsEnabled(true);

    }

    private void movingCarMarker(Location lastLocation) {

        LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());

        if (carLocationMarker == null) {
            //create new car marker
            googleMap.clear();
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.car_icon));
            markerOptions.rotation(lastLocation.getBearing()); // make car icon direction towards the road.
            markerOptions.anchor((float) 0.5, (float) 0.5);// default location fence to center of  car icon.
            carLocationMarker = googleMap.addMarker(markerOptions);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20)); // to animate camara View
        } else {
            //update car marker
            carLocationMarker.setPosition(latLng);
            carLocationMarker.setRotation(lastLocation.getBearing()); // make car icon direction towards the road.
            //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20)); // to animate camara View

        }

        if (carLocationAccuracyCircle == null) {

            //help to create small fence
            CircleOptions circleOptions = new CircleOptions();
            circleOptions.center(latLng);
            circleOptions.strokeWidth(4);
            circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
            circleOptions.fillColor(Color.argb(32, 255, 0, 0));
            circleOptions.radius(lastLocation.getAccuracy());
            carLocationAccuracyCircle = googleMap.addCircle(circleOptions);

        } else {
            carLocationAccuracyCircle.setCenter(latLng);
            carLocationAccuracyCircle.setRadius(lastLocation.getAccuracy());
        }

    }

    public void markOnMap(double latitude, double longitude, float zoomLevel, String locationName, String snippet) {

        googleMap.clear(); // clear map and remove markers

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setInfoWindowAdapter(new CustomMakerAdapter(YourTrulyActivity.this));
        LatLng latLng = new LatLng(latitude, longitude);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title(locationName);
        markerOptions.position(latLng);
        markerOptions.snippet(snippet); //place info's
        markerOptions.draggable(true);// we can drag marker any where we want. https://www.youtube.com/watch?v=9qGLYKsD-5I&list=PLdHg5T0SNpN3GBUmpGqjiKGMcBaRT2A-m&index=5
        mainMarker = googleMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel);  //max zoom 21. 1world, 5Continents, 10Cities, 15Streets, 20Buildings

        googleMap.animateCamera(CameraUpdateFactory.zoomTo(2.0f));
        googleMap.animateCamera(cameraUpdate, 3000, new GoogleMap.CancelableCallback() { // moving to position with time
            @Override
            public void onFinish() {

            }

            @Override
            public void onCancel() {

            }
        });

    }

    public void getLocationDetailsFromName(String location) {
        Geocoder geocoder = new Geocoder(YourTrulyActivity.this, Locale.getDefault());
        try {

            // List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // get only one results. you can add more
            List<Address> addressList = geocoder.getFromLocationName(location, 1); // get only one results. you can add more

            if (addressList.size() > 0) {


                Toast.makeText(YourTrulyActivity.this, "Country " + addressList.get(0).getCountryName() +
                        " city:" + addressList.get(0).getLocality()
                        + " : country code:" + addressList.get(0).getCountryCode()
                        + " Address Line " + addressList.get(0).getAddressLine(0), Toast.LENGTH_SHORT).show();

                markOnMap(addressList.get(0).getLatitude(), addressList.get(0).getLongitude(), 15,
                        addressList.get(0).getLocality(), addressList.get(0).getAddressLine(0));

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getLocationDetailsFromLatLong(LatLng latLng) {
        Geocoder geocoder = new Geocoder(YourTrulyActivity.this, Locale.getDefault());
        try {

            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // get only one results. you can add more

            if (addressList.size() > 0) {


                Toast.makeText(YourTrulyActivity.this, "Country " + addressList.get(0).getCountryName() +
                        " city:" + addressList.get(0).getLocality()
                        + " : country code:" + addressList.get(0).getCountryCode()
                        + " Address Line " + addressList.get(0).getAddressLine(0), Toast.LENGTH_SHORT).show();

                markOnMap(addressList.get(0).getLatitude(), addressList.get(0).getLongitude(), 15, addressList.get(0).getLocality(), addressList.get(0).getAddressLine(0));

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.currentLocation) {
            //getLastLocation();
            checkSettingsAndStartLocationUpdates();
        } else if (view.getId() == R.id.getLocationName) {
            getLocationDetailsFromName("Nugegoda");
        } else if (view.getId() == R.id.getLocationDetails) {
            getLocationDetailsFromLatLong(new LatLng(6.8649, 79.8997));
        } else if (view.getId() == R.id.geo_Fence) {


            if (Build.VERSION.SDK_INT >= 29) {

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    LatLng latLng = new LatLng(6.8649, 79.8997);
                    markOnMap(6.8649, 79.8997, 17, "My Location", "Address: Nugegoda\nPhone Number: +94777");
                    addCircle(latLng, GEOFENCE_RADIUS);
                    addGeoFence(latLng, GEOFENCE_RADIUS);

                } else {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 10002);
                    }else {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 10002);

                    }

                }
            }else{
                LatLng latLng = new LatLng(6.8649, 79.8997);
                markOnMap(6.8649, 79.8997, 17, "My Location", "Address: Nugegoda\nPhone Number: +94777");
                addCircle(latLng, GEOFENCE_RADIUS);
                addGeoFence(latLng, GEOFENCE_RADIUS);
            }


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==10002) {

            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){

                Toast.makeText(this, "bg service enable", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "bg service not enable", Toast.LENGTH_SHORT).show();

            }

        }
    }

    private void addGeoFence(LatLng latLng, float radius) {


        Geofence geofence = geoFenceHelper.getGeofence("GEOFENCE_ID", latLng, radius, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = geoFenceHelper.getGeofencingRequest(geofence);
        PendingIntent pendingIntent = geoFenceHelper.getPendingIntent();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Log.d("TAG", "onSuccess: geo fences added");


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                String errorMsg = geoFenceHelper.getErrorString(e);
                Log.d("TAG", "OnFailure: " + errorMsg);
            }
        });


    }

    private void addCircle(LatLng latLng, float radius) {
        //  help to create small fence
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius); // in meters
        circleOptions.strokeWidth(4);
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
        circleOptions.fillColor(Color.argb(64, 255, 0, 0));
        googleMap.addCircle(circleOptions);
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        getLocationDetailsFromLatLong(latLng);
    }

    @Override
    public void onMarkerDragStart(@NonNull Marker marker) {
    }

    @Override
    public void onMarkerDrag(@NonNull Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(@NonNull Marker marker) {
        getLocationDetailsFromLatLong(marker.getPosition());

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }
}