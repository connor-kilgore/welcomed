package org.welcomedhere.welcomed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.slider.Slider;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    protected static final String apiKey = "AIzaSyDKAAE9roCSy-Kifb3a774-vabVEr3gl3s";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private int distance = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean locationPermissionGranted;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // set bottom nav bar properties
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        // set top toolbar properties
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView title = findViewById(R.id.title);
        title.setText("home");

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Find the AppCompatButton by its id
        AppCompatButton aboutUsButton = findViewById(R.id.about_us_section);

        // Set an onClickListener on the button
        aboutUsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create an Intent to navigate to the MissionStatementActivity
                Intent intent = new Intent(MainActivity.this, ourMission.class);
                startActivity(intent);
            }
        });

        // find the slider and distance text by id
        Slider distanceSlider = findViewById(R.id.distance_slider);
        TextView distanceText = findViewById(R.id.miles_text);

        // change running distance from
        distanceSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                //Use the value
                distance = (int) value;
                distanceText.setText(distance + " mi");
            }
        });

        // set listener for food button
        Button foodBtn = findViewById(R.id.food_button);
        foodBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocationPermission();
                getJsonData("restaurant", "nearby");
            }
        });

        // set listener for shop button
        Button shopBtn = findViewById(R.id.stores_button);
        shopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocationPermission();
                getJsonData("store|convenience_store|drugstore", "nearby");
            }
        });

        // set listener for bathroom button
        Button bathroomBtn = findViewById(R.id.bathrooms_button);
        bathroomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocationPermission();
                getJsonData("toilets near me", "text");
            }
        });

        // set listener for fun button
        Button funBtn = findViewById(R.id.fun_button);
        funBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocationPermission();
                getJsonData("fun things to do near me", "text");
            }
        });

        // set listener for Drinks button
        Button drinksBtn = findViewById(R.id.drinks_button);
        drinksBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocationPermission();
                getJsonData("bar|cafe|liquor_store", "text");
            }
        });

        Button mapNav = findViewById(R.id.map_navigate);
        mapNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void getJsonData(String type, String queryType) {
        // get current lat and long
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // create a separate thread for network request to get nearby google maps places
                                NearbyPlaceFinder getPlacesThread =  new NearbyPlaceFinder(location.getLatitude(), location.getLongitude(), (distance * 1609) + "", type, apiKey, queryType);
                                getPlacesThread.start();
                                try {
                                    getPlacesThread.join();
                                    // parse the json data
                                    showBottomDialog(getPlacesThread.places);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    });
        }
    }

    private void showBottomDialog(ArrayList<NearbyPlace> places) {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheet_layout);


        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

        LinearLayout layout = dialog.findViewById(R.id.dynamic_layout);

        // create a string array and add all placeIds to it
        ArrayList<String> mapIdList = new ArrayList<>();
        for(int index = 0; index < places.size(); index++)
        {
            // add googleID to mapIDList
            mapIdList.add(places.get(index).googlePlaceID);
        }

        // get the places top traits
        ArrayList<ArrayList<TraitValue>> nearbyTraits = getNearbyPlaceTraits(mapIdList);

        // iterate through all the nearby places
        for(int index = 0; index < places.size(); index++)
        {
            // add the view into the xml file
            View view = getLayoutInflater().inflate(R.layout.nearby_place, null);
            layout.addView(view);

            // set the name and address
            ((TextView)view.findViewById(R.id.name)).setText(places.get(index).name);
            ((TextView)view.findViewById(R.id.address)).setText(places.get(index).address);
            int distanceTemp = (int)(places.get(index).distance * 10);
            ((TextView)view.findViewById(R.id.distance)).setText(distanceTemp/10 + "." + distanceTemp % 10 + " mi");

            //((ImageView)view.findViewById(R.id.placeImg)).setImageBitmap(places.get(index).placeImgBM);  <-- this is way too expensive in terms of API calls

            int finalIndex = index;
            ((RelativeLayout)view.findViewById(R.id.review_box)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // get business data from server and send to details page
                    Business b = new Business(places.get(finalIndex).googlePlaceID, places.get(finalIndex).address, places.get(finalIndex).name);
                    b.request = DatabaseConstants.Request.ENTRY;
                    // set business request type to "entry"
                    b = new Business((Business) (new Client(b, "request")).getObjectFromServer());

                    Intent intent = new Intent(MainActivity.this, PlaceDetailsActivity.class);
                    intent.putExtra("businessInfo", b.toString());
                    startActivity(intent);
                }
            });

            // populate traits
            TextView traitReviewText = view.findViewById(R.id.good_trait_1);
            for(int inner = 0; inner < nearbyTraits.get(index).size(); inner++)
            {
                traitReviewText.setText(traitReviewText.getText() + nearbyTraits.get(index).get(inner).trait);
                if(inner != nearbyTraits.get(index).size() - 1)
                {
                    traitReviewText.setText(traitReviewText.getText() + ", ");
                }
            }
        }
    }

    public ArrayList<ArrayList<TraitValue>> getNearbyPlaceTraits(ArrayList<String> mapIds)
    {
        return (ArrayList<ArrayList<TraitValue>>) new Client(mapIds, "getNearbyPlaces").getObjectFromServer();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item){
        int itemID=item.getItemId();
        if(itemID==R.id.home_menu_item){
            return true;
        } else if (itemID==R.id.maps_menu_item) {
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        else if (itemID==R.id.profile_menu_item){
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClick(View v){
        int ID= v.getId();
        //todo add onclick handling for buttons
    }

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
}