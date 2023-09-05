package org.welcomedhere.welcomed;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {

    public String userID;
    public String name;
    public String email;
    public String phone;
    public Boolean acceptData;

    public operation op;
    public ArrayList<String> preferences;
    public ArrayList<Descriptor> descriptors;

    public enum operation {
        NEW,
        BLANK,
        GET,
        DELETE,
        ADD_SUCCESS,
        DELETE_SUCCESS,
        UPDATE_PREFERENCES,
        GET_PREFERENCES,
        GET_DESCRIPTORS,
    };

    /**
     * Constructor: Takes provided info from a profile customization input.
     */
    public User(String userID, String name, String email)
    {
        this.name = name;
        this.userID = userID;
        this.email = email;
    }

    /**
     *  Constructor for simple queries
     */
    public User(String userID, operation op) {
        this.userID = new String(userID);
        this.op = op;
    }
}

