package org.welcomedhere.welcomed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;


import org.welcomedhere.welcomed.data.ProfileManager;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{

    // constants for preferences variables
    public static final String USER_DATA = "user_data";
    public static final String NAME_KEY = "name_key";
    public static final String GENDER_KEY = "gender_key";
    public static final String RACE_KEY = "race_key";
    public static final String SO_KEY = "so_key";
    public static final String INCLUSION_KEY = "inclusion_key";
    public static final String ANON_KEY = "anon_key";
    SharedPreferences userdata;

    private Button saveBtn;
    private Button logoutBtn;

    final int PICK_FROM_GALLERY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // set bottom nav bar properties
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        // set top toolbar properties
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView title = findViewById(R.id.title);
        title.setText("profile");


        // force portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // gender
        Spinner genderSpinner = findViewById(R.id.gender_spinner2);
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this,
                R.array.gender_options, R.layout.spinner_item);
        genderAdapter.setDropDownViewResource(R.layout.dropdown_options);
        genderSpinner.setAdapter(genderAdapter);

        // post anonymously
        Spinner anonSpinner = findViewById(R.id.anon_spinner);
        ArrayAdapter<CharSequence> yesNoAdapter = ArrayAdapter.createFromResource(this,
                R.array.yes_or_no, R.layout.spinner_item);
        yesNoAdapter.setDropDownViewResource(R.layout.dropdown_options);
        anonSpinner.setAdapter(yesNoAdapter);

        // sexual orientation
        Spinner sexualOrientationSpinner = findViewById(R.id.SO_spinner);
        ArrayAdapter<CharSequence> sexualOrientationAdapter = ArrayAdapter.createFromResource(this,
                R.array.sexual_orientation_options, R.layout.spinner_item);
        sexualOrientationAdapter.setDropDownViewResource(R.layout.dropdown_options);
        sexualOrientationSpinner.setAdapter(sexualOrientationAdapter);

        // race
        Spinner ethnicitySpinner = findViewById(R.id.ethnicity_spinner);
        ArrayAdapter<CharSequence> ethnicityAdapter = ArrayAdapter.createFromResource(this,
                R.array.ethnicity_options, R.layout.spinner_item);
        ethnicityAdapter.setDropDownViewResource(R.layout.dropdown_options);
        ethnicitySpinner.setAdapter(ethnicityAdapter);

        // inclusion supports
        Spinner abilitiesSpinner = findViewById(R.id.disability_spinner);
        ArrayAdapter<CharSequence> abilitiesAdapter = ArrayAdapter.createFromResource(this,
                R.array.abilities_options, R.layout.spinner_item);
        abilitiesAdapter.setDropDownViewResource(R.layout.dropdown_options);
        abilitiesSpinner.setAdapter(abilitiesAdapter);

        // get stored data
        userdata = getSharedPreferences(USER_DATA, Context.MODE_PRIVATE);

        // get all internally stored values
        String name = userdata.getString(NAME_KEY, null);
        String gender = userdata.getString(GENDER_KEY, null);
        String race = userdata.getString(RACE_KEY, null);
        String so = userdata.getString(SO_KEY, null);
        String inclusion = userdata.getString(INCLUSION_KEY, null);
        String anon = userdata.getString(ANON_KEY, null);

        //System.out.println(name + ", " + gender + ", " + race + ", " + so + ", " + inclusion + ", " + anon);

        // check if these values exist
        if(name != null && gender != null && race != null && so != null && inclusion != null && anon != null)
        {
            // set them
            ((EditText)findViewById(R.id.name_field)).setText(name);
            genderSpinner.setSelection(getIndex(genderSpinner, gender));
            ethnicitySpinner.setSelection(getIndex(ethnicitySpinner, race));
            sexualOrientationSpinner.setSelection(getIndex(sexualOrientationSpinner, so));
            abilitiesSpinner.setSelection(getIndex(abilitiesSpinner, inclusion));
            anonSpinner.setSelection(getIndex(anonSpinner, anon));
        }
        // if values don't exist, attempt to retrieve from database
        else
        {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();

            // Check if this user has already created a profile
            // if so, we'll fill out the fields for them
            User existing = ProfileManager.retrieveUser(mAuth.getUid());

            // check that user exists in database
            if (existing != null) {

                // set the user name
                ((EditText) findViewById(R.id.name_field)).setText(existing.name);

                // get the users descriptors
                existing.op = User.operation.GET_DESCRIPTORS;
                existing.descriptors = (ArrayList<Descriptor>) new Client(existing, "").getObjectFromServer();


                // check that descriptors is not null
                if(existing.descriptors != null)
                {
                    // if so, populate the spinners
                    for(int index = 0; index < existing.descriptors.size(); index++)
                    {

                        // check for descriptor type
                        if(existing.descriptors.get(index).descriptorType.equals("gender"))
                        {
                            genderSpinner.setSelection(getIndex(genderSpinner, existing.descriptors.get(index).descriptor));
                        }
                        else if(existing.descriptors.get(index).descriptorType.equals("race"))
                        {
                            ethnicitySpinner.setSelection(getIndex(ethnicitySpinner, existing.descriptors.get(index).descriptor));
                        }
                        else if(existing.descriptors.get(index).descriptorType.equals("sexual orientation"))
                        {
                            sexualOrientationSpinner.setSelection(getIndex(sexualOrientationSpinner, existing.descriptors.get(index).descriptor));
                        }
                        else if(existing.descriptors.get(index).descriptorType.equals("inclusion supports"))
                        {
                            abilitiesSpinner.setSelection(getIndex(abilitiesSpinner, existing.descriptors.get(index).descriptor));
                        }
                        else if(existing.descriptors.get(index).descriptorType.equals("anonymous"))
                        {
                            anonSpinner.setSelection(getIndex(anonSpinner, existing.descriptors.get(index).descriptor));
                        }
                    }
                }
            }
        }

        // set the saveBtn
        saveBtn = (Button) findViewById(R.id.save_button);

        // set the logoutBtn
        logoutBtn = (Button) findViewById(R.id.logoutButton);

        // set a click listener to send data to database
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get the user info from filled in fields
                User newUser = buildUserFromEntries();
                User result = ProfileManager.updateUser(newUser);

                if (result == null || result.op != User.operation.ADD_SUCCESS) {
                    Toast.makeText(ProfileActivity.this,
                            "Failed to update info.", Toast.LENGTH_LONG);
                } else {
                    Toast.makeText(ProfileActivity.this,
                            "Profile updated successfully!", Toast.LENGTH_LONG);
                }

                // save into shared preferences userdata
                SharedPreferences.Editor editor = userdata.edit();

                editor.putString(NAME_KEY, ((EditText)findViewById(R.id.name_field)).getText().toString());
                editor.putString(GENDER_KEY, genderSpinner.getSelectedItem().toString());
                editor.putString(RACE_KEY, ethnicitySpinner.getSelectedItem().toString());
                editor.putString(SO_KEY, sexualOrientationSpinner.getSelectedItem().toString());
                editor.putString(INCLUSION_KEY, abilitiesSpinner.getSelectedItem().toString());
                editor.putString(ANON_KEY, anonSpinner.getSelectedItem().toString());

                // apply saved values
                editor.apply();
            }
        });

        // set change photo btn
        TextView changePhoto = findViewById(R.id.change_photo);

        // when changePhoto is selected, prompt to open photo gallery
        changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    // check if permission is not granted
                    if (ActivityCompat.checkSelfPermission(ProfileActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        // if so, ask for permission
                        ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_FROM_GALLERY);
                    } else {
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(galleryIntent, PICK_FROM_GALLERY);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // set a click listener to log out the current user
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);

                // Get Firebase instance and clear credentials
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();

                // return to login screen
                startActivity(intent);
                finish();
            }
        });


    }

    // when gallery permissions is granted, this method is called to send user to gallery UI
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PICK_FROM_GALLERY:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(galleryIntent, PICK_FROM_GALLERY);
                } else {
                    // request permission again
                    ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_FROM_GALLERY);
                }
                break;
        }
    }

    // when the photo from gallery is selected, this is called
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, final Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            // Handle error
            return;
        }

        switch (requestCode) {
            case PICK_FROM_GALLERY:
                // Get photo picker response for single select.
                Uri currentUri = data.getData();

                // get the profile picture element
                ImageView profilePic = findViewById(R.id.profile_picture);

                // set it's image to currentUri
                profilePic.setImageURI(currentUri);

                Client picSend = new Client(currentUri, "sendProfilePicture");
                picSend.context = ProfileActivity.this;
                picSend.start();

                //you got image path, now you may use this
                return;

        }
    }


    private User buildUserFromEntries() {
        // get current user data
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        User newUser = ProfileManager.retrieveUser(mAuth.getUid());

        String userName = ((EditText)findViewById(R.id.name_field))
                .getText()
                .toString();

        // apply descriptors for each category
        newUser.descriptors = new ArrayList<Descriptor>();
        newUser.descriptors.add(new Descriptor(((Spinner)findViewById(R.id.gender_spinner2)).getSelectedItem().toString(), "gender"));
        newUser.descriptors.add(new Descriptor(((Spinner)findViewById(R.id.ethnicity_spinner)).getSelectedItem().toString(), "race"));
        newUser.descriptors.add(new Descriptor(((Spinner)findViewById(R.id.SO_spinner)).getSelectedItem().toString(), "sexual orientation"));
        newUser.descriptors.add(new Descriptor(((Spinner)findViewById(R.id.disability_spinner)).getSelectedItem().toString(), "inclusion supports"));
        newUser.descriptors.add(new Descriptor(((Spinner)findViewById(R.id.anon_spinner)).getSelectedItem().toString(), "anonymous"));

        return newUser;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item){
        final int itemID=item.getItemId();

        // bottom navbar items
        if(itemID==R.id.home_menu_item) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        else if (itemID==R.id.maps_menu_item){
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        else if(itemID==R.id.profile_menu_item){
            // this is the Profile activity, so nothing happens
            return true;
        }
        // end bottom navbar items

        return super.onOptionsItemSelected(item);
    }

    public void openPreferencesActivity(View view) {
        Intent intent = new Intent(this, PreferencesActivity.class);
        startActivity(intent);
    }

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

    //private method of your class
    private int getIndex(Spinner spinner, String myString){
        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                return i;
            }
        }

        return 0;
    }
}