package org.welcomedhere.welcomed;

import java.io.Serializable;

public class KarmaListing implements Serializable {

    int reviewID;
    int businessID;
    String userID;

    public DatabaseConstants.Request request;

    public KarmaListing(int reviewID, int businessID, String userID)
    {
        this.reviewID = reviewID;
        this.businessID = businessID;
        this.userID = userID;
    }
}
