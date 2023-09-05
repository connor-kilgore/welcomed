package org.welcomedhere.welcomed.ui.login;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import android.content.pm.ActivityInfo;

import android.text.InputType;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.widget.AppCompatButton;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.welcomedhere.welcomed.MainActivity;
import org.welcomedhere.welcomed.ForgotPasswordActivity;
import org.welcomedhere.welcomed.R;
import org.welcomedhere.welcomed.User;
import org.welcomedhere.welcomed.data.ProfileManager;
import org.welcomedhere.welcomed.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private ActivityLoginBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        final EditText usernameEditText = binding.email;
        final EditText passwordEditText = binding.password;
        final AppCompatButton loginButton = (AppCompatButton) binding.login;
        final ProgressBar loadingProgressBar = binding.loading;

        TextView signUp = (TextView) findViewById(R.id.sign_up);
        TextView forgotPassword = (TextView) findViewById(R.id.forgot_password_text);

        // set firebase authentication variables
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);


        // check that a user account already exists and that their email is verified
        if(currentUser != null) {
            // We already have a stored Firebase session, so skip login screen
            User user = ProfileManager.retrieveUser(currentUser.getUid());

            // check if user is not within the database
            if(user == null)
            {
                // if user does not exist on db, then create one
                user = new User(auth.getUid(), "", usernameEditText.getText().toString());
                User result = ProfileManager.updateUser(user);
            }

            updateUiWithUser(currentUser);
        }

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                signIn(usernameEditText.getText().toString(), passwordEditText.getText().toString());
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), SignupActivity.class);
                startActivity(intent);
                finish();
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ForgotPasswordActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void signIn(String email, String password)
    {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
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
                            Toast.makeText(getApplicationContext(), "login failed.", Toast.LENGTH_LONG).show();
                            updateUiWithUser(null);
                        }
                    }
                });
    }

    private void updateUiWithUser(FirebaseUser user) {
        if (user!= null)
        {
            if(user.isEmailVerified())
            {
                Toast.makeText(getApplicationContext(), "login successful", Toast.LENGTH_LONG).show();

                // retrieve the user from the db
                User dbUser = ProfileManager.retrieveUser(user.getUid());

                System.out.println(dbUser.acceptData);

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
                    String[] tokenData = {};
                    intent.putExtra("token", tokenData);
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

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}