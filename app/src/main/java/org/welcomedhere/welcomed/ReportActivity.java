package org.welcomedhere.welcomed;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.welcomedhere.welcomed.data.ProfileManager;

import java.util.Objects;

public class ReportActivity extends AppCompatActivity {
    Button reportBtn;
    Report report;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_report); // Replace with your layout resource ID
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        MaterialToolbar toolbar = findViewById(R.id.materialToolbar_maps);
        setSupportActionBar(toolbar);

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
        // get concern type
        Spinner concernSpinner = findViewById(R.id.concern_type_spinner);
        ArrayAdapter<CharSequence> concernAdapter = ArrayAdapter.createFromResource(this,
                R.array.concern_options, R.layout.spinner_item);
        concernAdapter.setDropDownViewResource(R.layout.dropdown_options);
        concernSpinner.setAdapter(concernAdapter);

        // can we contact them
        Spinner yesNoSpinner = findViewById(R.id.contact_spinner);
        ArrayAdapter<CharSequence> yesNoAdapter = ArrayAdapter.createFromResource(this,
                R.array.yes_or_no, R.layout.spinner_item);
        yesNoAdapter.setDropDownViewResource(R.layout.dropdown_options);
        yesNoSpinner.setAdapter(yesNoAdapter);

        // get the business info of the place being reviewed
        Business businessData = new Business(getIntent().getStringExtra("businessInfo"));

        // set the saveBtn
        reportBtn = (Button) findViewById(R.id.submit_button);

        // set a click listener to send data to database
        reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get the text in the editText box
                EditText edit = (EditText) findViewById(R.id.comment_holder);

                // get type of report
                Spinner concern = (Spinner) findViewById(R.id.concern_type_spinner);
                Report.ReportType reportType;
                if(concern.getSelectedItem().toString().equals("ADA"))
                {
                    reportType = Report.ReportType.ADA;
                }
                else if(concern.getSelectedItem().toString().equals("Discrimination"))
                {
                    reportType = Report.ReportType.DISCRIMINATION;
                }
                else
                {
                    reportType = Report.ReportType.OTHER;
                }


                // get contact bool
                Spinner contactSpinner = (Spinner) findViewById(R.id.contact_spinner);
                boolean contact;
                if(contactSpinner.getSelectedItem().toString().equals("Yes"))
                {
                    contact = true;
                }
                else
                {
                    contact = false;
                }

                // get the text body
                EditText textBody = (EditText) findViewById(R.id.comment_holder);

                ProfileManager manager = new ProfileManager();

                // instantiate review class
                report = new Report(businessData.businessID, manager.getCurrentUid(), reportType, contact, textBody.getText().toString());

                report.printInfo();

                // initialize client class
                Client client = new Client(report, "create");
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
}
