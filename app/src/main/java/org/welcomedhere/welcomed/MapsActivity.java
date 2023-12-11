package org.welcomedhere.welcomed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.LocationManager;
import android.os.Looper;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;


import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.maps.GeoApiContext;


import org.welcomedhere.welcomed.data.ProfileManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class MapsActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback {

    // maximum number of places to search
    private GoogleMap map;
    private PlacesClient placesClient;
    private GeoApiContext apiContext;

    private final LatLng defaultLocation = new LatLng(35.17730277850528, -111.65703426059879);

    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private static final String TAG = MapsActivity.class.getSimpleName();

    private FusedLocationProviderClient fusedLocationProviderClient;

    private Location lastKnownLocation;

    private boolean locationPermissionGranted;
    protected static final String apiKey = "AIzaSyDKAAE9roCSy-Kifb3a774-vabVEr3gl3s";

    private LatLng lastKnownLatLng;

    private static com.google.maps.model.LatLng convert_android_latlng_to_java_latlng(
            com.google.android.gms.maps.model.LatLng in){
        com.google.maps.model.LatLng out = new com.google.maps.model.LatLng(in.latitude, in.longitude);
        return out;
    }

    private static com.google.android.gms.maps.model.LatLng convert_java_latlng_to_android_latlng
            (com.google.maps.model.LatLng in) {
        com.google.android.gms.maps.model.LatLng out = new LatLng(in.lat, in.lng);
        return out;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        // set bottom nav bar properties
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        // set top toolbar properties
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView title = findViewById(R.id.title);
        title.setText("map");

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        GeoApiContext.Builder apiContextBuilder = new GeoApiContext.Builder();
        apiContextBuilder.apiKey(apiKey);
        apiContextBuilder.queryRateLimit(20);

        apiContext = apiContextBuilder.build();

        Places.initialize(getApplicationContext(), apiKey);
        placesClient = Places.createClient(this);
        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        // Build the map.
        // [START maps_current_place_map_fragment]
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);

        mapFragment.getMapAsync(this);

        AutocompleteSupportFragment autoCompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autoComplete);

        // Specify the types of place data to return.
        autoCompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG, Place.Field.ADDRESS));

        // Set up a PlaceSelectionListener to handle the response.
        autoCompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                openPlacesDialog(place);
            }


            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });


        // [END maps_current_place_map_fragment]
        // [END_EXCLUDE]
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        final int itemID = item.getItemId();

        // bottom navbar items
        if (itemID == R.id.home_menu_item) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        } else if (itemID == R.id.maps_menu_item) {
            // we're already inside the map view, we don't need to do anything
            return true;
        } else if (itemID == R.id.profile_menu_item) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        // end bottom navbar items

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }


        // [START_EXCLUDE]
        // [START map_current_place_set_info_window_adapter]
        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        this.map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                        (FrameLayout) findViewById(R.id.mapFragment), false);

                TextView title = infoWindow.findViewById(R.id.title);
                title.setText(marker.getTitle());

                TextView snippet = infoWindow.findViewById(R.id.snippet);
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });
        // [END map_current_place_set_info_window_adapter]

        //make the app display the place details for the selected place
        this.map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(@NonNull Marker marker) {
                String tag = (String) marker.getTag();
                if (tag instanceof String) {
                    Intent intent = new Intent(MapsActivity.this, PlaceDetailsActivity.class);
                    intent.putExtra("businessInfo", tag);
                    startActivity(intent);
                } else {
                    return;
                }
            }
        });

        // Prompt the user for permission.
        getLocationPermission();

        // [END_EXCLUDE]

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();


    }
    // [END maps_current_place_on_map_ready]


    //Found on stackoverflow at https://stackoverflow.com/questions/52004666/requesting-location-updates-using-fusedlocationproviderclient-is-not-working-ca
    private class SingleShotLocationProvider {

        private final Context context;

        final LocationManager locManager;
        private final FusedLocationProviderClient mFusedLocationClient;
        private LocationCallback fusedTrackerCallback;

        public SingleShotLocationProvider(Context context) {
            this.context = context;
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

            locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (locManager != null && !locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Log.e("SiSoLocProvider", "GPS IS NOT enabled.");
            } else {
                Log.d("SiSoLocProvider", "GPS is enabled.");
            }
        }

        public void requestSingleUpdate() {

            final long startTime = System.currentTimeMillis();
            fusedTrackerCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {

                    if ((locationResult.getLastLocation() != null) && (System.currentTimeMillis() <= startTime + 30 * 1000)) {
                        System.out.println("LOCATION: " + locationResult.getLastLocation().getLatitude() + "|" + locationResult.getLastLocation().getLongitude());
                        System.out.println("ACCURACY: " + locationResult.getLastLocation().getAccuracy());
                        mFusedLocationClient.removeLocationUpdates(fusedTrackerCallback);
                    } else {
                        System.out.println("LastKnownNull? :: " + (locationResult.getLastLocation() == null));
                        System.out.println("Time over? :: " + (System.currentTimeMillis() > startTime + 30 * 1000));
                    }


                    mFusedLocationClient.removeLocationUpdates(this);
                }
            };

            LocationRequest req = new LocationRequest();
            req.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            req.setFastestInterval(2000);
            req.setInterval(2000);
            // Receive location result on UI thread.
            if (ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "No location permission available");
            }
            mFusedLocationClient.requestLocationUpdates(req, fusedTrackerCallback, Looper.getMainLooper());
        }
    }



    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    // [START maps_current_place_get_device_location]
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        SingleShotLocationProvider singleShotLocationProvider= new SingleShotLocationProvider(this);
        singleShotLocationProvider.requestSingleUpdate();

        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.getResult()!=null) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();

                            if (lastKnownLocation != null) {
                                System.out.println("Location="+lastKnownLocation);
                                lastKnownLatLng= new LatLng(lastKnownLocation.getLatitude(),
                                        lastKnownLocation.getLongitude());
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLatLng
                                        , DEFAULT_ZOOM));
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            map.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }
    // [END maps_current_place_get_device_location]

    /**
     * Prompts the user for permission to use the device location.
     */
    // [START maps_current_place_location_permission]
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    // [END maps_current_place_location_permission]

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    // [START maps_current_place_update_location_ui]
    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    // [END maps_current_place_update_location_ui]

    public void toPlaceDetails(View v){
        Intent intent = new Intent(this, PlaceDetailsActivity.class);
        startActivity(intent);
    }

    /**
     * Displays a form allowing the user to select a place from a list of likely places.
     */
    // [START maps_current_place_open_places_dialog]
    private void openPlacesDialog(Place place) {
        // Position the map's camera at the location of the marker.
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(),
                DEFAULT_ZOOM));

        Business b=new Business(place.getId(), place.getAddress(), place.getName());
        b.request = DatabaseConstants.Request.ENTRY;
        // set business request type to "entry"
        b = new Business((Business) (new Client(b, "request")).getObjectFromServer());

        User usr = ProfileManager.getCurrentUser();

        // create an empty review with user and businessID
        Review sendReview = new Review(b.businessID, null, usr.userID, false, null, false, null);
        sendReview.request = DatabaseConstants.Request.GET_MATCHING_TRAITS;

        // get all the traits that match with the user
        ArrayList<TraitValue> traitMatches = (ArrayList<TraitValue>) new Client(sendReview, "").getObjectFromServer();

        // get all the preferences the user has
        usr.op = User.operation.GET_PREFERENCES;
        ArrayList<String> myTraits = (ArrayList<String>) new Client(usr, "").getObjectFromServer();

        // count all the positive traits
        double netPositive = 0;
        for(int index = 0; index < traitMatches.size(); index++)
        {
            // check if positive
            if(traitMatches.get(index).value > 0)
            {
                netPositive++;
            }
            // otherwise, check if negative
            else if(traitMatches.get(index).value < 0)
            {
                netPositive--;
            }
        }

        // divide netPositive by myTraits.size
        double ratio = netPositive/myTraits.size();

        BitmapDrawable bitmapdraw;
        // check if ratio is 50% or higher
        if(ratio >= 0.5)
        {
            bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.green_marker);
        }
        else if(ratio < 0)
        {
            bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.red_marker);
        }
        else
        {
            bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.grey_marker);
        }

        int side = 400;
        Bitmap bitmap = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(bitmap, side, side, false);

        // Add a marker for the selected place, with an info window
        // showing information about that place.
        Marker m= map.addMarker(new MarkerOptions()
                .title(place.getName())
                .position(place.getLatLng())
                .snippet("tap for details")
                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));

        m.setTag(b.toString());

    }
    // [END maps_current_place_open_places_dialog]

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}