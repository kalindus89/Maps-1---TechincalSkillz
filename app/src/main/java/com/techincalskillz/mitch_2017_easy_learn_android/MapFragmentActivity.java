package com.techincalskillz.mitch_2017_easy_learn_android;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.HandlerThread;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.techincalskillz.R;
import com.techincalskillz.retrofit_singleton_pattern.AutoCompleteApiInterface;
import com.techincalskillz.retrofit_singleton_pattern.Credentials;
import com.techincalskillz.retrofit_singleton_pattern.Model.PredictionArrayModel;
import com.techincalskillz.retrofit_singleton_pattern.adapters.CustomMakerAdapter;
import com.techincalskillz.retrofit_singleton_pattern.adapters.RecyclerAdapterAutoComplete;
import com.techincalskillz.retrofit_singleton_pattern.response.AutoCompleteResponse;
import com.techincalskillz.retrofit_singleton_pattern.ServiceClass;
import com.techincalskillz.your_truly_taxi.YourTrulyActivity;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapFragmentActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, GoogleMap.OnMapClickListener {


    GoogleMap googleMap;
    EditText searchText;
    ImageView searchIcon;
    LinearLayout getLocationName, locationInfo, moveAnimationCam, stickMap, currentLocation, notiLocation, routeInGoogleMap, taxiMoving;
    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;
    boolean currentLocationUpdate = false;
    HandlerThread handlerThread;
    LocationCallback locationCallBack;
    RecyclerView recyclerView;
    RelativeLayout autoCompletePlaces;
    TextView empty_msg;
    RecyclerAdapterAutoComplete mainRecyclerAdapter;
    boolean keywordSearch = true;
    Marker mainMarker;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_fragment);

        searchText = findViewById(R.id.searchText);
        recyclerView = findViewById(R.id.recyclerView);
        autoCompletePlaces = findViewById(R.id.autoCompletePlaces);
        empty_msg = findViewById(R.id.empty_msg);

        searchIcon = findViewById(R.id.searchIcon);
        searchIcon.setOnClickListener(this);

        getLocationName = findViewById(R.id.getLocationName);
        getLocationName.setOnClickListener(this);

        moveAnimationCam = findViewById(R.id.moveAnimationCam);
        moveAnimationCam.setOnClickListener(this);

        stickMap = findViewById(R.id.stickMap);
        stickMap.setOnClickListener(this);

        currentLocation = findViewById(R.id.currentLocation);
        currentLocation.setOnClickListener(this);

        notiLocation = findViewById(R.id.notiLocation);
        notiLocation.setOnClickListener(this);

        locationInfo = findViewById(R.id.locationInfo);
        locationInfo.setOnClickListener(this);

        routeInGoogleMap = findViewById(R.id.routeInGoogleMap);
        routeInGoogleMap.setOnClickListener(this);

        taxiMoving = findViewById(R.id.taxiMoving);
        taxiMoving.setOnClickListener(this);


        checkPermissions(savedInstanceState);


    /*    searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {
                    hideSoftKeyboard();
                    setGetLocation();
                }
                return false;
            }
        });*/

        searchText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keywordSearch = true;
            }
        });

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                getAutocompleteResults(editable.toString());
            }
        });


    }

    private void getAutocompleteResults(String text) {

        if (keywordSearch == true) {

            AutoCompleteApiInterface movieApiInterface = ServiceClass.getAutoCompleteApiInterface();

            movieApiInterface.getAutoCompleteResponse(text, Credentials.API_KEY).enqueue(new Callback<AutoCompleteResponse>() {
                @Override
                public void onResponse(Call<AutoCompleteResponse> call, Response<AutoCompleteResponse> response) {

                    if (response.isSuccessful()) {
                        recyclerView.setVisibility(View.VISIBLE);
                        empty_msg.setVisibility(View.GONE);

                        List<PredictionArrayModel> getPredictionsList = response.body().getPredictions();

                        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MapFragmentActivity.this, RecyclerView.VERTICAL, false);
                        recyclerView.setLayoutManager(layoutManager);
                        mainRecyclerAdapter = new RecyclerAdapterAutoComplete(MapFragmentActivity.this, getPredictionsList);
                        recyclerView.setAdapter(mainRecyclerAdapter);
                        mainRecyclerAdapter.notifyDataSetChanged();


                    } else {
                        try {
                            recyclerView.setVisibility(View.GONE);
                            empty_msg.setVisibility(View.VISIBLE);

                            //  Log.v("aaaaaaaaaaaaaaa2", response.errorBody().toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                }

                @Override
                public void onFailure(Call<AutoCompleteResponse> call, Throwable t) {

                }
            });
        } else {

            recyclerView.setVisibility(View.GONE);
            empty_msg.setVisibility(View.GONE);
            hideSoftKeyboard();
        }
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {

        getLocationDetails(latLng);

    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.searchText) {
            hideSoftKeyboard();
            setGetLocation();
        } else if (view.getId() == R.id.notiLocation) {

            Intent intent = new Intent(MapFragmentActivity.this, BatchLocationActivity.class);
            startActivity(intent);

        } else if (view.getId() == R.id.moveAnimationCam) {
            //animation with time
            markOnMap(6.7230, 80.0647, 15, "Nugegoda", "Address: Nugegoda\nPhone Number: +94777");

        } else if (view.getId() == R.id.taxiMoving) {

            Intent intent = new Intent(MapFragmentActivity.this, YourTrulyActivity.class);
            startActivity(intent);
            finish();

        } else if (view.getId() == R.id.locationInfo) {

            try {
                if (mainMarker.isInfoWindowShown()) {
                    mainMarker.hideInfoWindow();
                } else {
                    mainMarker.showInfoWindow();
                }
            } catch (Exception e) {

            }

        } else if (view.getId() == R.id.getLocationName) {
            //reverse geocoder
            getLocationDetails(new LatLng(6.8649, 79.8997));
        } else if (view.getId() == R.id.routeInGoogleMap) {

            //open route in mp

            String latitude = String.valueOf(6.7230);  //destination latitude
            String longitude = String.valueOf(80.0647); //destination longitude

            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            try {
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                }
            } catch (NullPointerException e) {
                Log.e(TAG, "onClick: NullPointerException: Couldn't open map." + e.getMessage());
                Toast.makeText(MapFragmentActivity.this, "Couldn't open map", Toast.LENGTH_SHORT).show();
            }
        } else if (view.getId() == R.id.stickMap) {

            double bottomBoundary = 6.7230 - 0.3;
            double leftBoundary = 80.0647 - 0.3;
            double topBoundary = 6.7230 + 0.3;
            double rightBoundary = 80.0647 + 0.3;

            LatLngBounds latLngBounds = new LatLngBounds(new LatLng(bottomBoundary, leftBoundary), new LatLng(topBoundary, rightBoundary));

            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 0));
            // googleMap.setLatLngBoundsForCameraTarget(latLngBounds);
            //googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,400,400,1));

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLngBounds.getCenter());//getcenter retruns latlan values
            googleMap.addMarker(markerOptions);
        } else if (view.getId() == R.id.currentLocation) {
            // go to  CheckGps() to change location update time

            if (currentLocationUpdate == true) {
                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapFragmentActivity.this);

                if (ActivityCompat.checkSelfPermission(MapFragmentActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapFragmentActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                // get Current location update continuously and app in background also
                locationCallBack = new LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        if (locationResult == null) {
                            return;
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                markOnMap(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude(), 15, "Current Location", "Address: Nugegoda\nPhone Number: +94777");
                                //Log.d("aaaaaaa1",locationResult.getLastLocation().getLatitude() + " Longitude: " + locationResult.getLastLocation().getLongitude());
                                //  System.out.println("aaaaaaaa2 " + "Latitude: " + locationResult.getLastLocation().getLatitude() + " Longitude: " + locationResult.getLastLocation().getLongitude());
                            }
                        });

                    }
                };

                handlerThread = new HandlerThread("LocationCallBackThread");
                handlerThread.start();
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, handlerThread.getLooper());


                // get Current location update only one time
                    /*fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            Toast.makeText(MapFragmentActivity.this, "Latitude1: " + location.getLatitude() + " Longitude1: " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                            markOnMap(location.getLatitude(),location.getLongitude(),15);
                        }
                    });

                    or

                    fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {

                            Location location = (Location) task.getResult();
                            Toast.makeText(MapFragmentActivity.this, "Latitude1: " + location.getLatitude() + " Longitude1: " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                            markOnMap(location.getLatitude(),location.getLongitude(),15);

                        }
                    });

                    */


            }
        }
    }

    public void getLocationDetails(LatLng latLng) {
        Geocoder geocoder = new Geocoder(MapFragmentActivity.this, Locale.getDefault());
        try {

            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // get only one results. you can add more

            if (addressList.size() > 0) {


                Toast.makeText(MapFragmentActivity.this, "Country " + addressList.get(0).getCountryName() +
                        " city:" + addressList.get(0).getLocality()
                        + " : country code:" + addressList.get(0).getCountryCode()
                        + " Address Line " + addressList.get(0).getAddressLine(0), Toast.LENGTH_SHORT).show();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void setGetLocation() {
        String locationEnter = searchText.getText().toString();
        if (locationEnter == null) {
            Toast.makeText(MapFragmentActivity.this, "Type location name", Toast.LENGTH_SHORT).show();
        } else {

            Geocoder geocoder = new Geocoder(MapFragmentActivity.this, Locale.getDefault());
            try {
                List<Address> addressList = geocoder.getFromLocationName(locationEnter, 1); // get only one results. you can add more

                if (addressList.size() > 0) {

                    // lati and longi value of first results in addressList
                    //  addressList.get(0).getAddressLine(0) , addressList.get(0).getCountryName() ,  addressList.get(0).getLocality()
                    markOnMap(addressList.get(0).getLatitude(), addressList.get(0).getLongitude(), 12, addressList.get(0).getCountryName(), "Address: " + addressList.get(0).getAddressLine(0) + "\nPhone Number: " + addressList.get(0).getPhone());

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setGetLocationFromPlaceID(String locationEnter, String place_id) {

        keywordSearch = false;
        searchText.setText(locationEnter);
        hideSoftKeyboard();

        Places.initialize(getApplicationContext(), "AIzaSyB6AVYYBx_GkxYQuEEJHZqWqVuNMEbM7ow");
        final List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.RATING);

        final FetchPlaceRequest request = FetchPlaceRequest.newInstance(place_id, placeFields);


        PlacesClient placesClient = Places.createClient(this);

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();

            //    System.out.println("aaaaaa company name"+place.getName()+" / company address"+place.getAddress()+" / "+place.getPhoneNumber()+" / "+place.getWebsiteUri()+" / ");


            markOnMap(place.getLatLng().latitude, place.getLatLng().longitude, 14, place.getName(), "Address: " + place.getAddress() + "\nPhone Number: " + place.getPhoneNumber());
            hideSoftKeyboard();

        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                final ApiException apiException = (ApiException) exception;
                Log.e("aaaaa11", "aaaaa11 " + exception.getMessage());
                final int statusCode = apiException.getStatusCode();
                // TODO: Handle error with given status code.
            }
        });
    }

    private void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    public void closeBackgroundMethods() {
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
        }
        if (handlerThread != null) {
            handlerThread.quit();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeBackgroundMethods();
    }

    @Override
    protected void onStop() {
        super.onStop();
        closeBackgroundMethods();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeBackgroundMethods();
    }

    public void markOnMap(double latitude, double longitude, float zoomLevel, String locationName, String snippet) {

        googleMap.clear(); // clear map and remove markers

        googleMap.setInfoWindowAdapter(new CustomMakerAdapter(MapFragmentActivity.this));
        LatLng latLng = new LatLng(latitude, longitude);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title(locationName);
        markerOptions.position(latLng);
        markerOptions.snippet(snippet); //place info's
        // googleMap.addMarker(markerOptions).remove();
        mainMarker = googleMap.addMarker(markerOptions);
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

        markOnMap(6.8649, 79.8997, 15, "My Location", "Address: Nugegoda\nPhone Number: +94777");

        googleMap.setOnMapClickListener(this);

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