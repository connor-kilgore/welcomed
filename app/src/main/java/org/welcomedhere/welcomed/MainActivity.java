package org.welcomedhere.welcomed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MaterialToolbar toolbar = findViewById(R.id.materialToolbar_main);
        setSupportActionBar(toolbar);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navbar_profile2);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

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





}