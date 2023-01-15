package com.frizid.timeline.emailAuth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.frizid.timeline.menu.PrivacyActivity;
import com.frizid.timeline.menu.TermsActivity;
import com.google.android.material.snackbar.Snackbar;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.frizid.timeline.MainActivity;
import com.frizid.timeline.R;
import com.parse.facebook.ParseFacebookUtils;

import org.json.JSONException;

import java.util.Arrays;
import java.util.Collection;

import timber.log.Timber;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView terms = findViewById(R.id.terms);
        TextView privacy = findViewById(R.id.privacy);

        privacy.setOnClickListener(v -> {
            startActivity(new Intent(this, PrivacyActivity.class));
        });

        terms.setOnClickListener(v -> {
            startActivity(new Intent(this, TermsActivity.class));
        });

        //Back
        findViewById(R.id.imageView).setOnClickListener(v -> onBackPressed());

        //OnClick
        findViewById(R.id.signUP).setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, SignUpActivity.class)));

        findViewById(R.id.forgot).setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));

        //EditText
        EditText email = findViewById(R.id.email);
        EditText pass = findViewById(R.id.pass);

        //Button
        findViewById(R.id.login).setOnClickListener(v -> {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            String mEmail = email.getText().toString().trim();
            String mPassword = pass.getText().toString().trim();
            if (mEmail.isEmpty()){
                Snackbar.make(v,"Enter your email", Snackbar.LENGTH_LONG).show();
                findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
            }else if(mPassword.isEmpty()){
                Snackbar.make(v,"Enter your password", Snackbar.LENGTH_LONG).show();
                findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
            }else {
                login(mEmail,mPassword);
            }
        });

        //FB Button
        findViewById(R.id.fblogin).setOnClickListener(v -> {
            final ProgressDialog dialog = new ProgressDialog(this);
            dialog.setTitle("Please, wait a moment.");
            dialog.setMessage("Logging in...");
            dialog.show();
            Collection<String> permissions = Arrays.asList("public_profile", "email");
            ParseFacebookUtils.logInWithReadPermissionsInBackground(this, permissions, (user, err) -> {
                dialog.dismiss();
                if (err != null) {
                    Timber.e(err, "done: ");
                    Toast.makeText(this, err.getMessage(), Toast.LENGTH_LONG).show();
                } else if (user == null) {
                    Toast.makeText(this, "The user cancelled the Facebook login.", Toast.LENGTH_LONG).show();
                } else if (user.isNew()) {
                    Toast.makeText(this, "User signed up and logged in through Facebook.", Toast.LENGTH_LONG).show();
                    getUserDetailFromFB();
                } else {
                    Toast.makeText(this, "User logged in through Facebook.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                    findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                    startActivity(intent);
                    finish();
                }
            });
        });
    }
    private void login(String name, String password) {
        ParseUser.logInInBackground(name, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if(e == null){
                    Toast.makeText(LoginActivity.this, "Login successful !", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                    findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                    startActivity(intent);
                    finish();
                }
                else{
                    String msg = e.getMessage();
                    Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
                    findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void getUserDetailFromFB() {
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), (object, response) -> {
            ParseUser user = ParseUser.getCurrentUser();
            try {
                if (object.has("name"))
                    user.setUsername(object.getString("name"));
                if (object.has("email"))
                    user.setEmail(object.getString("email"));
                user.put("name",object.getString("name"));
                user.put("bio","");
                user.put("phone","");
                user.put("status","");
                user.put("location","");
                user.put("type","");
                user.put("link","");
                user.put("count",0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            user.saveInBackground(e -> {
                if (e == null) {
                    Toast.makeText(LoginActivity.this, "First Time Login! Welcome!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                    findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                    startActivity(intent);
                    finish();
                } else
                    Timber.d("Error: %s", e.getMessage());
            });
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "name,email");
        request.setParameters(parameters);
        request.executeAsync();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }
}