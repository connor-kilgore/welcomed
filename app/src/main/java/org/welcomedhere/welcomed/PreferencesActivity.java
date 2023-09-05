package org.welcomedhere.welcomed;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.appbar.MaterialToolbar;


import org.w3c.dom.Text;
import org.welcomedhere.welcomed.data.ProfileManager;

import java.util.ArrayList;
import java.util.Objects;

public class PreferencesActivity extends AppCompatActivity {

    ArrayList<String> traitTypes;

    int numTraitsSelected = 0;

    ProfileManager manager = new ProfileManager();

    User usr = manager.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        MaterialToolbar toolbar = findViewById(R.id.materialToolbar_maps2);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // get pre-existing trait preferences
        usr.op = User.operation.GET_PREFERENCES;
        ArrayList<String> preferences = (ArrayList<String>) new Client(usr, "").getObjectFromServer();

        // get traits box
        FlexboxLayout traits = findViewById(R.id.preferences_form);
        ArrayList<String> traitList = new ArrayList<String>();

        TextView traitCounter = findViewById(R.id.trait_counter);

        // get trait types from db
        traitTypes = (ArrayList<String>) new Client("traitType").getObjectFromServer();

        // add the pre-existing traits
        for(int index = 0; index < preferences.size(); index++)
        {
            addTraitXML(traits, preferences.get(index), traitCounter, traitList);
        }

        Button addBtn = findViewById(R.id.add_trait);

        // create trait box listener
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(numTraitsSelected < 10)
                {
                    createDialog(traits, traitList, traitCounter);
                }
            }
        });

        // define save button
        Button saveBtn = findViewById(R.id.save_button);

        // create a listener for save button
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send the preferences off to the db
                usr.op = User.operation.UPDATE_PREFERENCES;
                usr.preferences = traitList;
                Client client = new Client(usr, "updatePreferences");
                client.start();
            }
        });
    }

    // link the multiautocomplete with the array options, and allow users to see options when tapping element
    @SuppressLint("ClickableViewAccessibility")
    public void setupAutoCompleteTextView(MultiAutoCompleteTextView view, int arrayResId) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_options, getResources().getStringArray(arrayResId));
        view.setAdapter(adapter);
        view.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.showDropDown();
            }
        });



        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    view.showDropDown();
                }
                return false;
            }
        });
    }

    private void createDialog(FlexboxLayout traitBox, ArrayList<String> selectedTraits, TextView traitCounter)
    {

        // Initialize dialog
        Dialog dialog=new Dialog(PreferencesActivity.this);

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
        final ArrayAdapter<String>[] adapter = new ArrayAdapter[]{new ArrayAdapter<>(PreferencesActivity.this, android.R.layout.simple_list_item_1, traitTypes)};

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
                    adapter[0] =new ArrayAdapter<>(PreferencesActivity.this, android.R.layout.simple_list_item_1, traitsOfCategory);

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

                        addTraitXML(traitBox, adapter[0].getItem(position), traitCounter, selectedTraits);
                    }
                    // Dismiss dialog
                    dialog.dismiss();
                }
            }
        });
    }

    private void addTraitXML(FlexboxLayout traitBox, String item, TextView traitCounter, ArrayList<String> selectedTraits)
    {
        // add the view into the xml file
        View traitView = getLayoutInflater().inflate(R.layout.trait_pop_up, null);
        traitBox.addView(traitView);

        // get the text and set it's appropriate trait text
        TextView traitText = traitView.findViewById(R.id.name_textview);
        traitText.setText(item);

        // get the layout and set the appropriate background
        LinearLayout layout = traitView.findViewById(R.id.layout_holder);

        numTraitsSelected++;
        traitCounter.setText(numTraitsSelected + "/10");

        // get the button and set a listener to remove the trait
        Button close = traitView.findViewById(R.id.close_button);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                traitBox.removeView(traitView);
                selectedTraits.remove(item);

                numTraitsSelected--;
                traitCounter.setText(numTraitsSelected + "/10");
            }
        });
    }

}
