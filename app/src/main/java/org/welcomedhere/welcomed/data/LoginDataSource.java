package org.welcomedhere.welcomed.data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

import org.welcomedhere.welcomed.data.model.LoggedInUser;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {


    public Result<LoggedInUser> login(String username, String password) {
        final Exception taskException[] = {null};

        try {
            // Get instance for user session
            FirebaseAuth mAuth = FirebaseAuth.getInstance();

            // Attempt to authenticate with given data
            Task wait = mAuth.signInWithEmailAndPassword(username, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // log if successful if we need, else should be fine
                                String str = String.format("Logged in user %s", username);
                                Log.d("Success", str);
                            }
                        }
                    });

            while(!wait.isComplete())
            {
                if(wait.isCanceled())
                {
                    throw new FirebaseAuthException("1", "Login was timed out or cancelled.");
                }
            }

            LoggedInUser currentUser =
                    new LoggedInUser(
                            mAuth.getUid(),
                            mAuth.getCurrentUser().getDisplayName());
            return new Result.Success<>(currentUser);
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
    }
}