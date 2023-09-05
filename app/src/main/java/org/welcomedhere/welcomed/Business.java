package org.welcomedhere.welcomed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Business implements Serializable, DatabaseConstants {

    public String googlePlaceID;
    public int businessID;
    public String address;
    public String name;
    public Request request;

    // create a list of reviews
    List<Review> topReviews = new ArrayList<Review>();

    // constructor
    public Business(String googlePlaceID, String address, String name)
    {
        this.googlePlaceID = googlePlaceID;
        this.address = address;
        this.name = name;
        this.businessID =-1;
    }

    public Business(String fromString){
        String[] strings=fromString.split("; ");
        this.googlePlaceID=strings[0].split(": ")[1];
        this.address=strings[1].split(": ")[1];
        this.name=strings[2].split(": ")[1];
        if(strings.length>3){
            this.businessID =Integer.parseInt(strings[3].split(": ")[1]);
        }
    }

    public Business(Business b){
        this.businessID=b.businessID;
        this.googlePlaceID=b.googlePlaceID;
        this.address=b.address;
        this.name=b.name;
    }

    public Business copyFromBusiness(Business b){
        this.businessID=b.businessID;
        this.googlePlaceID=b.googlePlaceID;
        this.address=b.address;
        this.name=b.name;
        return this;
    }

    public void printInfo()
    {
        System.out.println("placeID: " + googlePlaceID + "\naddress: " + address + "\nname: " + name+ "\nbusinessID: "+ businessID);
    }
    @Override
    public String toString(){
        if(this.businessID >=0){
            return "placeID: " + googlePlaceID + "; address: " + address + "; name: " + name + "; businessID: "+ businessID;
        }
        else{
            return "placeID: " + googlePlaceID + "; address: " + address + "; name: " + name;
        }
    }

}
