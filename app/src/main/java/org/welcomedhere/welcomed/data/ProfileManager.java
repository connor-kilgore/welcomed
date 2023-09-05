package org.welcomedhere.welcomed.data;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;

import org.welcomedhere.welcomed.Client;
import org.welcomedhere.welcomed.User;

public class ProfileManager {
    public static boolean newBlankUser(String userID) {
        User user = new User(userID, User.operation.BLANK);
        Client client = new Client(user, "");

        client.start();
        while(client.isAlive()) {}
        return client.profileResult.op == User.operation.ADD_SUCCESS;
    }
    /**
     * Queries the database to verify a user exists, then send a query to delete that user.
     * @param userID ID of user to be removed
     * @return User deleted, if any.
     */
    public static User deleteUser(String userID) {
        User user = new User(userID, User.operation.DELETE);
        Client client = new Client(user, "");

        client.start();
        // wait for completion
        while(client.isAlive()) {}

        return client.profileResult;
    }

    /**
     * Queries the database for a given userID, and returns the data if it finds it.
     * @param userID ID of user to be located
     * @return Formatted information for given user.
     */
    public static User retrieveUser(String userID) {
        User user = new User(userID, User.operation.GET);
        Client client = new Client(user, "GET");
        Log.d("Connection:", "Awaiting thread completion...");
        client.start();
        // wait for completion
        while(client.isAlive()) {}
        Log.d("Connection", "Thread finished");
        return client.profileResult;
    }

    /**
     * Updates user entry in the database with new info or creates
     * @param updatedUser new user information
     * @return
     */
    public static User updateUser(User updatedUser) {
        // TODO
        updatedUser.op = User.operation.NEW;

        Client client = new Client(updatedUser, "");
        Log.d("Connection:", "Awaiting thread completion...");
        client.start();
        // wait for completion
        while(client.isAlive()) {}
        Log.d("Connection", "Thread finished");
        return client.profileResult;
    }

    public static String getCurrentUid() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getUid();
        return uid;
    }

    public static User getCurrentUser() {
        return retrieveUser(getCurrentUid());
    }
}
