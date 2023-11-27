package org.welcomedhere.welcomed;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import org.welcomedhere.welcomed.data.ProfileManager;

import java.util.Objects;

public class ReportActivity extends AppCompatActivity {
    final int PICK_FROM_GALLERY = 1;
    Button reportBtn;
    Report report;
    Uri photoUri = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_report); // Replace with your layout resource ID
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // set top toolbar properties
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView title = findViewById(R.id.title);
        title.setText("report an issue");

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

        // set the business name and address into page
        ((TextView)findViewById(R.id.business_name)).setText(businessData.name);
        ((TextView)findViewById(R.id.business_address)).setText(businessData.address);

        Button changePhoto = findViewById(R.id.add_photo);
        changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    // check if permission is not granted
                    if (ActivityCompat.checkSelfPermission(ReportActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        // if so, ask for permission
                        ActivityCompat.requestPermissions(ReportActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_FROM_GALLERY);
                    } else {
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(galleryIntent, PICK_FROM_GALLERY);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

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
                report = new Report(businessData.businessID, manager.getCurrentUid(), reportType, contact, textBody.getText().toString(), photoUri != null);

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

                // if an image exists, send it to the s3 bucket
                if(photoUri != null)
                {
                    // create imageInfo with the uri and path
                    ImageInfo newProfilePic = new ImageInfo("reportPhotos/" + businessData.businessID + "_" + FirebaseAuth.getInstance().getUid() + ".jpg", photoUri);

                    // send to s3 bucket
                    Client picSendThread = new Client(newProfilePic, "SEND");
                    picSendThread.context = ReportActivity.this;
                    picSendThread.start();
                }
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
                    ActivityCompat.requestPermissions(ReportActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_FROM_GALLERY);
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
                photoUri = data.getData();

                // get the profile picture element
                ImageView reportPhoto = findViewById(R.id.report_photo);

                // set it's image to currentUri
                reportPhoto.setImageURI(photoUri);

                //you got image path, now you may use this
                break;
        }
    }
}
