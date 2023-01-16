package com.frizid.timeline.emailAuth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.frizid.timeline.MainActivity;
import com.frizid.timeline.R;
import com.frizid.timeline.menu.PrivacyActivity;
import com.frizid.timeline.menu.TermsActivity;


@SuppressWarnings("ALL")
public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //Back
        findViewById(R.id.imageView).setOnClickListener(v -> onBackPressed());

        //Text
        findViewById(R.id.login).setOnClickListener(v -> startActivity(new Intent(SignUpActivity.this, LoginActivity.class)));

        TextView terms = findViewById(R.id.terms);
        TextView privacy = findViewById(R.id.privacy);

        privacy.setOnClickListener(v -> startActivity(new Intent(SignUpActivity.this, PrivacyActivity.class)));

        terms.setOnClickListener(v -> startActivity(new Intent(SignUpActivity.this, TermsActivity.class)));

        //EditText
        EditText email = findViewById(R.id.email);
        EditText pass = findViewById(R.id.pass);
        EditText name = findViewById(R.id.name);
        EditText username = findViewById(R.id.username);
        CheckBox checkBox = findViewById(R.id.checkbox);
        //EditText code = findViewById(R.id.code);

        //Button
        findViewById(R.id.signUp).setOnClickListener(v -> {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            String mEmail = email.getText().toString().trim();
            String mPassword = pass.getText().toString().trim();
            String mName = name.getText().toString().trim();
            String mUsername = username.getText().toString().trim();

            if(!checkBox.isChecked()){
                Snackbar.make(v,"Agree to privacy policy & Terms & Conditions", Snackbar.LENGTH_LONG).show();
                findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
            }else
            if (mEmail.isEmpty()){
                Snackbar.make(v,"Enter your email", Snackbar.LENGTH_LONG).show();
                findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
            }else if(mPassword.isEmpty()){
                Snackbar.make(v,"Enter your password", Snackbar.LENGTH_LONG).show();
                findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
            }else if(mName.isEmpty()){
                Snackbar.make(v,"Enter your Name", Snackbar.LENGTH_LONG).show();
                findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
            }else if(mUsername.isEmpty()){
                Snackbar.make(v,"Enter your Username", Snackbar.LENGTH_LONG).show();
                findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
            } else if (mPassword.length()<6){
                Snackbar.make(v,"Password should have minimum 6 characters", Snackbar.LENGTH_LONG).show();
                findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
            }else {
                register(mEmail,mPassword,mName,mUsername);
            }

        });

    }

    private void register(String mEmail, String mPassword, String mName, String mUsername) {

        ParseUser user = new ParseUser();

        user.setEmail(mEmail);
        user.setUsername(mUsername);
        user.setPassword(mPassword);
        user.put("name", mName);
        user.put("bio", "");
        user.put("phone", "");
        user.put("status", ""+System.currentTimeMillis());
        user.put("link", "");
        user.put("location", "");
        user.put("type", "");
        user.put("uri", "");

        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                    Toast.makeText(SignUpActivity.this, "Sign Up Successful !", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                    finish();
                }
                else{
                    findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);

                    Toast.makeText(SignUpActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}