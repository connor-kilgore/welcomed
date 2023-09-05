package org.welcomedhere.welcomed.models;

import java.time.OffsetDateTime;
import java.util.List;

public class ReviewModel {
    public String UserID;
    public String BusinessID;

    public OffsetDateTime dateTime;
    public String reviewText;
    public String image_link;
    public int helpfulRating;
    public List<String> tags;

    public List<AccessibilityRating> accommodations;
    public List<AccessibilityRating> refusals;
    public int karma;


}
