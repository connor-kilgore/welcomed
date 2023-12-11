package org.welcomedhere.welcomed;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.welcomedhere.welcomed.data.ProfileManager;
import org.welcomedhere.welcomed.databinding.ActivityLoginBinding;
import org.welcomedhere.welcomed.ui.login.LoginViewModel;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    User dbUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // initialize content view and orientation
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // login button attempts to log the user in
        ((TextView) findViewById(R.id.login)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // attempt sign in with username and login
                signIn(((EditText)findViewById(R.id.email)).getText().toString(), ((EditText)findViewById(R.id.password)).getText().toString());
            }
        });

        // sign up button sends user to sign up page
        ((TextView) findViewById(R.id.sign_up)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), SignupActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // forgot password button sends user to forgot password page
        ((TextView) findViewById(R.id.forgot_password_text)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ForgotPasswordActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            updateUiWithUser(currentUser);
        }
    }

    private void signIn(String email, String password)
    {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            updateUiWithUser(user);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateUiWithUser(FirebaseUser user) {
        if (user!= null)
        {
            if(user.isEmailVerified())
            {
                Toast.makeText(getApplicationContext(), "Authentication success", Toast.LENGTH_LONG).show();

                // retrieve the user from the db
                dbUser = ProfileManager.retrieveUser(user.getUid());

                // if user does not exist, create a new entry with uid
                if(dbUser == null)
                {
                    dbUser = ProfileManager.updateUser(dbUser);
                }

                // check if user has not set privacy policy yet
                if(dbUser.acceptData == null)
                {
                    // if so, send user to privacy policy page
                    setContentView(R.layout.privacy_policy);

                    // set the agree and decline buttons
                    Button agree = findViewById(R.id.agree);
                    Button decline = findViewById(R.id.decline);

                    // set on click listeners for both
                    agree.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dbUser.acceptData = true;
                            ProfileManager.updateUser(dbUser);

                            Intent intent = new Intent(v.getContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });

                    decline.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dbUser.acceptData = false;
                            ProfileManager.updateUser(dbUser);

                            Intent intent = new Intent(v.getContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
                else
                {
                    // otherwise, assume user has set accept data and go straight to main activity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);

                }
            }
            else
            {
                user.sendEmailVerification()
                        .addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if(task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this,
                                            "Verification email sent to " + user.getEmail(),
                                            Toast.LENGTH_SHORT).show();

                                    User newUser = new User(user.getUid(), "", user.getEmail());
                                    User result = ProfileManager.updateUser(newUser);
                                } else {
                                    Toast.makeText(LoginActivity.this,
                                            "Failed to send verification email.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
    }
}