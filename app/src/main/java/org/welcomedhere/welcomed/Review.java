package org.welcomedhere.welcomed;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Review implements Serializable {

    public int businessID;
    public String bodyText;
    public String userID;
    public String img_link;
    public String date;
    public String name;
    public boolean anonymous;
    public ArrayList<String> traits;
    public DatabaseConstants.Request request;

    public Review(int businessID, String bodyText, String userID, String img_link, String date, boolean anonymous, ArrayList<String> traits)
    {
        this.businessID = businessID;
        this.bodyText = bodyText;
        this.userID = userID;
        this.img_link = img_link;
        this.date = date;
        this.anonymous = anonymous;
        this.traits = traits;
    }

    // copy constructor
    public Review(Review review)
    {
        this.businessID = review.businessID;
        this.bodyText = review.bodyText;
        this.userID = review.userID;
        this.img_link = review.img_link;
        this.date = review.date;
        this.anonymous = review.anonymous;
        this.traits = new ArrayList<String>(review.traits);
    }


    public void updateBody(String newText)
    {
        this.bodyText = newText;
    }

    public void printInfo()
    {
        System.out.println("businessID: " + businessID + "\nbodyText: " + bodyText + "\nuserID: " + userID + "\nimg_link: " + img_link + "\nanonymous: " + anonymous);
    }
}
