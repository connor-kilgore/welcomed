package org.welcomedhere.welcomed.ui.login;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.welcomedhere.welcomed.R;
import org.welcomedhere.welcomed.User;
import org.welcomedhere.welcomed.data.ProfileManager;

public class SignupActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        final Button signup = this.findViewById(R.id.signUp);
        final TextView login = this.findViewById(R.id.login);
        final EditText emailEditText = this.findViewById(R.id.email);
        final EditText passwordEditText = this.findViewById(R.id.password);
        final EditText displayNameEditText = this.findViewById(R.id.first_name);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try
                {
                    trySignup(displayNameEditText.getText().toString(),
                            emailEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
                catch (Exception e)
                {
                    Toast.makeText(getApplicationContext(),
                            "Failed to create new account.",
                            Toast.LENGTH_LONG).show();
                    System.out.println(e);
                }
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void trySignup(String displayName, String email, String password) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        // Send request for new user
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Only proceed if we create the account fine
                        if(task.isSuccessful()) {
                            // TODO: Store display name either with FB or database
                            Log.d("Debug", "Sign up with email success");

                            FirebaseUser user = mAuth.getCurrentUser();

                            String userID = FirebaseAuth.getInstance().getUid();

                            user.sendEmailVerification()
                                .addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        if(task.isSuccessful()) {
                                            Toast.makeText(SignupActivity.this,
                                                    "Verification email sent to " + user.getEmail(),
                                                    Toast.LENGTH_SHORT).show();

                                                // on successful account creation, send a verification email to user and send them to the login page
                                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                                startActivity(intent);
                                                finish();

                                                User newUser = new User(userID, displayName, email);
                                                User result = ProfileManager.updateUser(newUser);
                                        } else {
                                            Toast.makeText(SignupActivity.this,
                                                    "Failed to send verification email.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                        } else {
                            Log.d("Error", task.getException().toString());
                            Toast.makeText(SignupActivity.this,
                                    "Failed to create new account.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}