package org.welcomedhere.welcomed;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;

import org.welcomedhere.welcomed.data.ProfileManager;
import org.welcomedhere.welcomed.models.ReviewModel;

import java.util.ArrayList;

public class ReviewActivity extends AppCompatActivity {

    public static final String USER_DATA = "user_data"; // to get stored preferences
    public static final String ANON_KEY = "anon_key"; // to get anonymous preference
    SharedPreferences userdata;
    private Button saveBtn;
    private Button addBtn;
    private String businessID;
    ReviewModel reviewModel;
    Review review;

    // get trait types
    ArrayList<String> traitTypes;

    ProfileManager manager = new ProfileManager();

    User usr = manager.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        // set top toolbar properties
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView title = findViewById(R.id.title);
        title.setText("review");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        // get stored data
        userdata = getSharedPreferences(USER_DATA, Context.MODE_PRIVATE);
        String anon = userdata.getString(ANON_KEY, null);
        SwitchCompat anonSwitch = findViewById(R.id.toggle);
        if(anon.equals("yes")) {
            anonSwitch.setChecked(true);    // change state to true if anon is yes
        }

        // get the business info of the place being reviewed
        Business businessData = new Business(getIntent().getStringExtra("businessInfo"));

        // set the business info into the page
        TextView businessName = findViewById(R.id.business_name);

        businessName.setText(businessData.name);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // get pre-existing trait preferences
        usr.op = User.operation.GET_PREFERENCES;
        ArrayList<String> preferences = (ArrayList<String>) new Client(usr, "").getObjectFromServer();

        traitTypes = (ArrayList<String>) new Client("traitType").getObjectFromServer();

        this.reviewModel=new ReviewModel();
        reviewModel.BusinessID=businessID;

        // get preferences form
        FlexboxLayout traits = findViewById(R.id.preferences_form);
        ArrayList<String> traitList = new ArrayList<String>();

        // add the pre-existing traits
        for(int index = 0; index < preferences.size(); index++)
        {
            addTraitXML(traits, preferences.get(index), traitList);
            traitList.add(preferences.get(index));
        }

        // set the add trait button
        addBtn = (Button) findViewById(R.id.add_trait);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialog(traits, traitList);
            }
        });

        // set the saveBtn
        saveBtn = (Button) findViewById(R.id.submit_button);

        // set a click listener to send data to database
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // get the text in the editText box
                EditText edit = (EditText) findViewById(R.id.comment_holder);

                // instantiate review class
                review = new Review(businessData.businessID, edit.getText().toString(), manager.getCurrentUid(), "null", "", anonSwitch.isChecked(), traitList);

                // initialize client class
                Client client = new Client(review, "create");
                // run client entry on separate thread
                client.start();

                // move back to the business profile page
                Intent intent = new Intent(view.getContext(), PlaceDetailsActivity.class);
                intent.putExtra("businessInfo", getIntent().getStringExtra("businessInfo"));
                startActivity(intent);
                finish();
            }
        });

    }

    private void createDialog(FlexboxLayout traitBox, ArrayList<String> selectedTraits)
    {

        // Initialize dialog
        Dialog dialog=new Dialog(ReviewActivity.this);

        // set custom dialog
        dialog.setContentView(R.layout.dialog_searchable_spinner);

        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.90);
        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.90);

        // set custom height and width
        dialog.getWindow().setLayout(width, height);

        // set transparent background
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // show dialog
        dialog.show();

        // Initialize and assign variable
        EditText editText=dialog.findViewById(R.id.edit_text);
        ListView listView=dialog.findViewById(R.id.list_view);

        // Initialize array adapter
        final ArrayAdapter<String>[] adapter = new ArrayAdapter[]{new ArrayAdapter<>(ReviewActivity.this, android.R.layout.simple_list_item_1, traitTypes)};

        // set adapter
        listView.setAdapter(adapter[0]);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter[0].getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // check that the chosen value is within the traitTypes list
                if(traitTypes.contains(adapter[0].getItem(position)))
                {
                    // get the traits of the chosen category
                    ArrayList<String> traitsOfCategory = (ArrayList<String>) new Client(adapter[0].getItem(position)).getObjectFromServer();

                    // Initialize array adapter
                    adapter[0] =new ArrayAdapter<>(ReviewActivity.this, android.R.layout.simple_list_item_1, traitsOfCategory);

                    // set adapter
                    listView.setAdapter(adapter[0]);
                }
                // otherwise, assume the chosen value is a specific trait
                else
                {
                    // check that trait does not already exist
                    if(!selectedTraits.contains(adapter[0].getItem(position)))
                    {
                        selectedTraits.add(adapter[0].getItem(position));

                        addTraitXML(traitBox, adapter[0].getItem(position), selectedTraits);
                    }
                    // Dismiss dialog
                    dialog.dismiss();
                }
            }
        });
    }

    private void addTraitXML(FlexboxLayout traitBox, String item, ArrayList<String> selectedTraits)
    {
        // add the view into the xml file
        View traitView = getLayoutInflater().inflate(R.layout.trait_pop_up, null);
        traitBox.addView(traitView);

        // get the text and set it's appropriate trait text
        TextView traitText = traitView.findViewById(R.id.name_textview);
        traitText.setText(item);

        // get the layout and set the appropriate background
        LinearLayout layout = traitView.findViewById(R.id.layout_holder);

        // get the button and set a listener to remove the trait
        Button close = traitView.findViewById(R.id.close_button);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                traitBox.removeView(traitView);
                selectedTraits.remove(item);
            }
        });
    }

    private String getExtra(Bundle savedInstanceState, String key)
    {
        if(savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();
            if(extras == null)
            {
                return "not found";
            }
            else
            {
                return extras.getString(key);
            }
        }
        else
        {
            return savedInstanceState.getString(key);
        }
    }

}

