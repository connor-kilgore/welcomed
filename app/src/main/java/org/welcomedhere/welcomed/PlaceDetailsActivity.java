package org.welcomedhere.welcomed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.welcomedhere.welcomed.data.ProfileManager;

import java.util.ArrayList;
import java.util.Objects;

public class PlaceDetailsActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    Business businessData;

    private Button reviewBtn;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);
        MaterialToolbar toolbar = findViewById(R.id.materialToolbar_maps);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navbar_profile2);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // get businessData from previous page
        businessData=new Business(getIntent().getStringExtra("businessInfo"));

        AppCompatButton leaveFeedbackButton = findViewById(R.id.decline);
        leaveFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ReportActivity.class);
                intent.putExtra("businessInfo", getIntent().getStringExtra("businessInfo"));
                startActivity(intent);
                finish();
            }
        });

        // get the top 5 rated traits from reviews
        businessData.request = DatabaseConstants.Request.GET_TOP_TRAITS;
        ArrayList<TraitValue> topTraits = (ArrayList<TraitValue>) new Client(businessData, "").getObjectFromServer();
        FlexboxLayout traitBox = findViewById(R.id.positive_traits);

        // first check that topTraits came back not null
        if(topTraits != null)
        {
            for(int index = 0; index < topTraits.size(); index++)
            {
                // check that trait is not null
                if(topTraits.get(index) != null)
                {
                    // get the text popup
                    View traitListPopup = getLayoutInflater().inflate(R.layout.trait_list, null);
                    LinearLayout layout = traitListPopup.findViewById(R.id.layout_holder);
                    TextView traitText = traitListPopup.findViewById(R.id.name_textview);

                    // set appropriate trait
                    traitText.setText(topTraits.get(index).trait);

                    // add trait to the box
                    traitBox.addView(traitListPopup);

                    // set trait background to blue
                    layout.setBackground(ContextCompat.getDrawable(traitListPopup.getContext(), R.drawable.blue_button));
                }

                System.out.println(topTraits.get(index).trait + ", " + topTraits.get(index).value);
            }
        }



        // TODO: find a way to make this on a different thread
        businessData.request = DatabaseConstants.Request.REVIEW_POPULATE;
        Review[] reviews = (Review[]) new Client(businessData, "").getObjectFromServer();

        // for each entry, add to the xml file with properly formatted review
        LinearLayout linearLayout = findViewById(R.id.review_section);

        // add the returned reviews to the businesses review list
        for(int index = 0; index < reviews.length; index++)
        {
            // check that review is not null
            if(reviews[index] != null)
            {
                ProfileManager manager = new ProfileManager();

                // alter the new review to match review data
                User user = manager.retrieveUser(reviews[index].userID);
                // check that user returned is not null
                if(user != null)
                {

                    boolean anonymous = false;

                    // check if user is an anonymous poster
                    for(int inner = 0; inner < user.descriptors.size(); inner++)
                    {
                        if(user.descriptors.get(inner).descriptorType.equals("anonymous"))
                        {
                            if(user.descriptors.get(inner).descriptor.equals("Yes"))
                            {
                                anonymous = true;
                            }
                        }
                    }

                    // add the review to the business review list
                    businessData.topReviews.add(reviews[index]);

                    // add the view into the xml file
                    View view = getLayoutInflater().inflate(R.layout.review_box, null);
                    linearLayout.addView(view);

                    // get the elements of the review box
                    TextView reviewBody = view.findViewById(R.id.textbox);
                    TextView date = view.findViewById(R.id.date);
                    TextView name = view.findViewById(R.id.name);
                    TextView karmaCount = view.findViewById(R.id.karma_count);

                    // get the current review's reviewID
                    reviews[index].request = DatabaseConstants.Request.GET_REVIEW_ID;
                    Client reviewClient = new Client(reviews[index], "getReviewID");
                    int reviewID = (int) reviewClient.getObjectFromServer();

                    // create a karma listing
                    KarmaListing listing = new KarmaListing(reviewID, reviews[index].businessID, manager.getCurrentUid());
                    listing.request = DatabaseConstants.Request.RETRIEVE;

                    // get karma listing data on review for current user
                    int userKarmaRating = (int) new Client(listing, "getKarma").getObjectFromServer();

                    ImageButton upVote = view.findViewById(R.id.upvote_button);
                    ImageButton downVote = view.findViewById(R.id.downvote_button);

                    // set buttons based on userKarmaRating
                    if(userKarmaRating == 0)
                    {
                        upVote.setTag("unpressed");
                        upVote.setImageResource(R.drawable.baseline_thumb_up_24);
                        downVote.setTag("unpressed");
                        downVote.setImageResource(R.drawable.baseline_thumb_down_24);
                    }
                    else if(userKarmaRating == 1)
                    {
                        upVote.setTag("pressed");
                        upVote.setImageResource(R.drawable.highlight_thumb_up_24);
                        downVote.setTag("unpressed");
                        downVote.setImageResource(R.drawable.baseline_thumb_down_24);
                    }
                    else
                    {
                        upVote.setTag("unpressed");
                        upVote.setImageResource(R.drawable.baseline_thumb_up_24);
                        downVote.setTag("pressed");
                        downVote.setImageResource(R.drawable.highlight_thumb_down_24);
                    }

                    // populate review box with traits
                    TextView traitReviewText = view.findViewById(R.id.good_trait_1);
                    for(int inner = 0; inner < reviews[index].traits.size(); inner++)
                    {
                        traitReviewText.setText(traitReviewText.getText() + reviews[index].traits.get(inner));
                        if(inner != reviews[index].traits.size() - 1)
                        {
                            traitReviewText.setText(traitReviewText.getText() + ", ");
                        }
                    }

                    // get the reviews total karma
                    reviews[index].request = DatabaseConstants.Request.GET_REVIEW_KARMA;
                    final int[] totalReviewKarma = {(int) reviewClient.getObjectFromServer()};

                    // set total review karma into karmaCount text
                    karmaCount.setText(totalReviewKarma[0] + "");

                    // set appropriate username, text and date
                    reviewBody.setText(reviews[index].bodyText);
                    date.setText(reviews[index].date);
                    if(anonymous)
                    {
                        name.setText("anonymous");
                    }
                    else
                    {
                        name.setText(user.name);
                    }

                    // set onclicklisteners for the upvote and downvote buttons
                    int finalIndex = index;

                    upVote.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(upVote.getTag() == "unpressed")
                            {
                                upVote.setTag("pressed");
                                upVote.setImageResource(R.drawable.highlight_thumb_up_24);
                                totalReviewKarma[0]++;

                                if(downVote.getTag() == "pressed")
                                {
                                    downVote.setTag("unpressed");
                                    downVote.setImageResource(R.drawable.baseline_thumb_down_24);
                                    totalReviewKarma[0]++;
                                }

                                listing.request = DatabaseConstants.Request.UPVOTE;
                            }
                            // otherwise, assume it is pressed
                            else
                            {
                                upVote.setTag("unpressed");
                                upVote.setImageResource(R.drawable.baseline_thumb_up_24);
                                totalReviewKarma[0]--;

                                listing.request = DatabaseConstants.Request.REMOVE_VOTE;
                            }

                            // initialize client class
                            Client client = new Client(listing, "updateKarma");

                            // set total review karma into karmaCount text
                            karmaCount.setText(totalReviewKarma[0] + "");

                            // run client entry on separate thread
                            client.start();
                        }
                    });

                    downVote.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(downVote.getTag() == "unpressed")
                            {
                                downVote.setTag("pressed");
                                downVote.setImageResource(R.drawable.highlight_thumb_down_24);
                                totalReviewKarma[0]--;

                                if(upVote.getTag() == "pressed")
                                {
                                    upVote.setTag("unpressed");
                                    upVote.setImageResource(R.drawable.baseline_thumb_up_24);
                                    totalReviewKarma[0]--;
                                }

                                listing.request = DatabaseConstants.Request.DOWNVOTE;
                            }
                            // otherwise, assume it is pressed
                            else
                            {
                                downVote.setImageResource(R.drawable.baseline_thumb_down_24);
                                downVote.setTag("unpressed");
                                totalReviewKarma[0]++;

                                listing.request = DatabaseConstants.Request.REMOVE_VOTE;
                            }

                            // initialize client class
                            Client client = new Client(listing, "updateKarma");

                            karmaCount.setText(totalReviewKarma[0] + "");

                            // run client entry on separate thread
                            client.start();
                        }
                    });

                }
                // TODO: delete the review if the user returned is null
            }
        }


        //set text for business name and address
        TextView businessName= (TextView) findViewById(R.id.business_name);
        businessName.setText(businessData.name);
        TextView businessAddress= (TextView) findViewById(R.id.business_address);
        businessAddress.setText(businessData.address);

        // get the top 3 reviews


        // set the reviewBtn
        reviewBtn = (Button) findViewById(R.id.agree);

        // set a click listener to send data to database
        reviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // run client entry on separate thread
                Intent intent = new Intent(view.getContext(), ReviewActivity.class);
                intent.putExtra("businessInfo", getIntent().getStringExtra("businessInfo"));
                startActivity(intent);
                finish();
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
        else if (itemID==R.id.profile_menu_item){
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        // end bottom navbar items

        return super.onOptionsItemSelected(item);
    }
}