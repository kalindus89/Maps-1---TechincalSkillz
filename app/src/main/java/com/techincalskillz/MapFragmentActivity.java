package com.techincalskillz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_fragment);

        searchText  = findViewById(R.id.searchText);
        searchIcon  = findViewById(R.id.searchIcon);

        checkPermissions(savedInstanceState);


        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String locationEnter=searchText.getText().toString();
                if(locationEnter==null){
                    Toast.makeText(MapFragmentActivity.this, "Type location name", Toast.LENGTH_SHORT).show();
                }else{

                    Geocoder geocoder = new Geocoder(MapFragmentActivity.this, Locale.getDefault());
                    try {
                        List<Address> addressList =geocoder.getFromLocationName(locationEnter,1); // get only one results. you can add more

                        if(addressList.size()>0){

                            // lati and longi value of first results in addressList
                            LatLng latLng = new LatLng(addressList.get(0).getLatitude(),addressList.get(0).getLongitude());

                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.title("My Position");
                            markerOptions.position(latLng);
                            googleMap.addMarker(markerOptions);
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12);
                            googleMap.animateCamera(cameraUpdate); // moving to position


                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

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
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        LatLng latLng = new LatLng(6.8649, 79.8997); // latitude and Longitude
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title("My Position");
        markerOptions.position(latLng);
        googleMap.addMarker(markerOptions);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 9);
        //googleMap.moveCamera(cameraUpdate); //directly show
        googleMap.animateCamera(cameraUpdate); // moving to position

        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true); // Compass not showing until you rotate the map

        // googleMap.getUiSettings().setZoomGesturesEnabled(false); // map cant zoom
        googleMap.getUiSettings().setZoomGesturesEnabled(true);

        //googleMap.getUiSettings().setScrollGesturesEnabled(false);// map cant Scroll
        googleMap.getUiSettings().setScrollGesturesEnabled(true);// map cant Scroll

        //googleMap.getUiSettings().setRotateGesturesEnabled(false);// map cant Rotate
        googleMap.getUiSettings().setRotateGesturesEnabled(true);



        //to get current location maker on the map
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);


    }

    public void checkPermissions(Bundle savedInstanceState) {
        Dexter.withContext(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

                //  Toast.makeText(MainActivity.this, "Permission Granted!", Toast.LENGTH_SHORT).show();

                //google map required Google play services in the phone
                if (checkGooglePlayServices()) {
                    Toast.makeText(MapFragmentActivity.this, "Google play services Available", Toast.LENGTH_SHORT).show();
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
        }

        else if (googleApiAvailability.isUserResolvableError(result)) {
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
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId()==R.id.none){
            googleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        }else if(item.getItemId()==R.id.normalMap){
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }else if(item.getItemId()==R.id.satelliteMap){
            googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }else if(item.getItemId()==R.id.mapHybrid){
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }else if(item.getItemId()==R.id.mapTerrain){
            googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        }
        return super.onOptionsItemSelected(item);
    }
}