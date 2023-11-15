package org.welcomedhere.welcomed;

import android.graphics.Bitmap;
import android.media.Image;

import org.welcomedhere.welcomed.Business;

public class NearbyPlace extends Business {

    public double distance;
    public Bitmap placeImgBM;
    private int status = 0;

    public NearbyPlace(String googlePlaceID, String address, String name, double distance, Bitmap placeImgBM) {
        super(googlePlaceID, address, name);
        this.distance = distance;
        this.placeImgBM = placeImgBM;
    }
}
