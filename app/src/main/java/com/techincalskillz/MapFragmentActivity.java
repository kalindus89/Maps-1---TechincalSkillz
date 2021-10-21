package com.techincalskillz;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapFragmentActivity extends AppCompatActivity implements OnMapReadyCallback {


    GoogleMap googleMap;
    EditText searchText;
    ImageView searchIcon;
    LinearLayout getLocationName, moveAnimationCam, stickMap, currentLocation;
    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;
    boolean currentLocationUpdate = false;

    LocationCallback locationCallBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_fragment);

        searchText = findViewById(R.id.searchText);
        searchIcon = findViewById(R.id.searchIcon);
        getLocationName = findViewById(R.id.getLocationName);
        moveAnimationCam = findViewById(R.id.moveAnimationCam);
        stickMap = findViewById(R.id.stickMap);
        currentLocation = findViewById(R.id.currentLocation);

        checkPermissions(savedInstanceState);



        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String locationEnter = searchText.getText().toString();
                if (locationEnter == null) {
                    Toast.makeText(MapFragmentActivity.this, "Type location name", Toast.LENGTH_SHORT).show();
                } else {

                    Geocoder geocoder = new Geocoder(MapFragmentActivity.this, Locale.getDefault());
                    try {
                        List<Address> addressList = geocoder.getFromLocationName(locationEnter, 1); // get only one results. you can add more

                        if (addressList.size() > 0) {

                            // lati and longi value of first results in addressList
                            markOnMap(addressList.get(0).getLatitude(), addressList.get(0).getLongitude(), 12);

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });


        getLocationName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                //reverse geocoder
                Geocoder geocoder = new Geocoder(MapFragmentActivity.this, Locale.getDefault());
                try {
                    List<Address> addressList = geocoder.getFromLocation(6.8649, 79.8997, 1); // get only one results. you can add more

                    if (addressList.size() > 0) {


                        Toast.makeText(MapFragmentActivity.this, "Country " + addressList.get(0).getCountryName() + " city:" + addressList.get(0).getLocality() + " : country code:" + addressList.get(0).getCountryCode(), Toast.LENGTH_SHORT).show();

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        moveAnimationCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*LatLng latLng = new LatLng(6.7230, 80.0647); // latitude and Longitude
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                googleMap.addMarker(markerOptions);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12); // max 21. 1world, 5Continents, 10Cities, 15Streets, 20Buildings
                googleMap.animateCamera(cameraUpdate); // moving to position*/

                //animation with time

                markOnMap(6.7230, 80.0647, 15);


            }
        });
        stickMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                double bottomBoundry = 6.7230 - 0.3;
                double leftoundry = 80.0647 - 0.3;
                double topBoundry = 6.7230 + 0.3;
                double rightBoundry = 80.0647 + 0.3;

                LatLngBounds latLngBounds = new LatLngBounds(new LatLng(bottomBoundry, leftoundry), new LatLng(topBoundry, rightBoundry));

                //googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,1));
                googleMap.setLatLngBoundsForCameraTarget(latLngBounds);
                //googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,400,400,1));

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLngBounds.getCenter());//getcenter retruns latlan values
                googleMap.addMarker(markerOptions);
            }
        });

        currentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // go to  CheckGps() to change location update time

                if (currentLocationUpdate == true) {
                    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapFragmentActivity.this);

                    if (ActivityCompat.checkSelfPermission(MapFragmentActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapFragmentActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {

                        return;
                    }
                    // get Current location update continuously and app in background also
                    locationCallBack=new LocationCallback() {
                        @Override
                        public void onLocationResult(@NonNull LocationResult locationResult) {
                            super.onLocationResult(locationResult);
                            if(locationResult==null){
                                return;
                            }
                            System.out.println("aaaaaaaa " + "Latitude: " + locationResult.getLastLocation().getLatitude() + " Longitude: " + locationResult.getLastLocation().getLongitude());
                        }
                    };

                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack,Looper.getMainLooper());


                    // get Current location update only one time
                    /*fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            Toast.makeText(MapFragmentActivity.this, "Latitude1: " + location.getLatitude() + " Longitude1: " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                            markOnMap(location.getLatitude(),location.getLongitude(),15);
                        }
                    });*/


                }
            }

        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fusedLocationProviderClient != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (fusedLocationProviderClient != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
        }
    }

    public void markOnMap(double latitude, double longitude, float zoomLevel) {

        googleMap.clear(); // clear map and remove markers

        LatLng latLng = new LatLng(latitude, longitude);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title("My Position");
        markerOptions.position(latLng);
        // googleMap.addMarker(markerOptions).remove();
        googleMap.addMarker(markerOptions);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel);  //max zoom 21. 1world, 5Continents, 10Cities, 15Streets, 20Buildings
        //googleMap.moveCamera(cameraUpdate); //directly show
        // googleMap.animateCamera(cameraUpdate); // moving to position without time

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

    private void showMap(Bundle savedInstanceState) {

        // if you use mapfragment then lifecycle methods are not needed
        //otherwise map will not display
        // https://developers.google.com/maps/documentation/android-sdk/reference/com/google/android/libraries/maps/MapView?hl=en
        //https://developers.google.com/maps/documentation/android-sdk/reference/com/google/android/libraries/maps/MapFragment?hl=en

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_Fragment);
        mapFragment.getMapAsync(this); //onMapReady method automatically call. your Default map

        CheckGps();

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

        markOnMap(6.8649, 79.8997, 15);

        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true); // Compass not showing until you rotate the map

        // googleMap.getUiSettings().setZoomGesturesEnabled(false); // map cant zoom
        googleMap.getUiSettings().setZoomGesturesEnabled(true);

        //googleMap.getUiSettings().setScrollGesturesEnabled(false);// map cant Scroll
        googleMap.getUiSettings().setScrollGesturesEnabled(true);// map cant Scroll

        //googleMap.getUiSettings().setRotateGesturesEnabled(false);// map cant Rotate
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        googleMap.getUiSettings().setTiltGesturesEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true); // when you tap on marker, shows open from google map and direction

        //to get current location maker on the map
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);

       /* googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                return true;
            }
        });*/

    }

    private void CheckGps() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000); // location update time
        locationRequest.setFastestInterval(3000); // location update time

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true);

        Task<LocationSettingsResponse> locationSettingsResponseTask = LocationServices.getSettingsClient(getApplicationContext()).checkLocationSettings(builder.build());

        locationSettingsResponseTask.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);

                    currentLocationUpdate = true;

                    //  Toast.makeText(MapFragmentActivity.this, "Gps is already enable", Toast.LENGTH_SHORT).show();
                } catch (ApiException e) {

                    if (e.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                        ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                        try {
                            resolvableApiException.startResolutionForResult(MapFragmentActivity.this, 101);
                        } catch (IntentSender.SendIntentException sendIntentException) {
                            sendIntentException.printStackTrace();
                        }
                    }
                    if (e.getStatusCode() == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {
                        Toast.makeText(MapFragmentActivity.this, "Setting not available", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(MapFragmentActivity.this, "Now Gps is Ok", Toast.LENGTH_SHORT).show();
            }
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(MapFragmentActivity.this, "Denied GPS enable", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void checkPermissions(Bundle savedInstanceState) {
        Dexter.withContext(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

                //  Toast.makeText(MainActivity.this, "Permission Granted!", Toast.LENGTH_SHORT).show();

                //google map required Google play services in the phone
                if (checkGooglePlayServices()) {
                    //Toast.makeText(MapFragmentActivity.this, "Google play services Available", Toast.LENGTH_SHORT).show();
                    showMap(savedInstanceState);
                } else {
                    Toast.makeText(MapFragmentActivity.this, "Google play services NOT  Available", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), "");
                intent.setData(uri);
                startActivity(intent);
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                permissionToken.continuePermissionRequest();
            }
        }).check();
    }


    private boolean checkGooglePlayServices() {

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int result = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (result == ConnectionResult.SUCCESS) {
            return true;
        } else if (googleApiAvailability.isUserResolvableError(result)) {
            Dialog dialog = googleApiAvailability.getErrorDialog(this, result, 201, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    Toast.makeText(MapFragmentActivity.this, "User Cancelled Dialog", Toast.LENGTH_SHORT).show();

                }
            });
            dialog.show();
        }

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.none) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        } else if (item.getItemId() == R.id.normalMap) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } else if (item.getItemId() == R.id.satelliteMap) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else if (item.getItemId() == R.id.mapHybrid) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        } else if (item.getItemId() == R.id.mapTerrain) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        }
        return super.onOptionsItemSelected(item);
    }
}