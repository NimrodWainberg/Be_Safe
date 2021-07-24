package com.example.besafe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {


    private ImageView imageView;
    private TextView textView;
    BottomNavigationView bottomNavigationView;
    // For language translate
    TextView language_dialog, helloToApp;
    boolean language_selected = true;
    Context context;
    Resources resources;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("MAINACTIVITY", "MAINACTIVITY ");


        // Translate dialog
        language_dialog = (TextView) findViewById(R.id.dialog_language);
        // helloToApp = (TextView) findViewById(R.id.welcome_txt);

        language_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] language = {"English", "עברית"};

                int checkedItem;

                // check if the language has changed
                if (language_selected) {
                    checkedItem = 0;
                } else {
                    checkedItem = 1;
                }

                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                // choose a language from dialog
                builder.setTitle("Select a language").setSingleChoiceItems(language, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Show the language in the button
                        language_dialog.setText(language[which]);

                        // use the suitable strings.xml
                        if(language[which].equals("English")){

                            context = LocaleHelper.setLocale(MainActivity.this,"en");
                            resources = context.getResources();
                            Log.d("Language", "English ");

                        // TODO
                        //    helloToApp.setText(resources.getString(R.string.welcome_txt));
                        }

                        if (language[which].equals("עברית")){

                            Log.d("Language", "Hebrew ");

                            context = LocaleHelper.setLocale(MainActivity.this,"iw-rlL");
                            resources = context.getResources();

                        //    helloToApp.setText(resources.getString(R.string.welcome_txt));
                        }
                    }
                })
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                // create and show
                builder.create().show();
            }
        });

        // For navigation button
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        Log.d("BOTTOM", bottomNavigationView+"");
        // Main Activity selected is home
        bottomNavigationView.setSelectedItemId(R.id.home);
        // Move to the other pages
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Log.d("ITEMID", "TEST");
                Log.d("ITEMID", item.getItemId()+"");
                switch (item.getItemId()) {

                    case R.id.mainActivity:
                        return true;


                    case R.id.recordsActivity:
                        // open the correct activity
                        startActivity(new Intent(getApplicationContext(), RecordsActivity.class));
                        overridePendingTransition(0, 0);
                        return true;

                    case R.id.sendActivity:
                        // open the correct activity
                        startActivity(new Intent(getApplicationContext(), SendActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }

        });


        //Facebook

        // Show user facebook login
        String name = getIntent().getStringExtra("Name");
        String pictureId = getIntent().getStringExtra("Picture_id");
        imageView = findViewById(R.id.profilePic);
        textView = findViewById(R.id.profileName);

        // Show it for the user
        // using picasso
        Picasso.get().load("https://graph.facebook.com/" + pictureId + "/picture?type=large").into(imageView);
        textView.setText("Hello " + " " + name);


        // To know if the user is logged in or logged out
        AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                // User is not logged in
                if (currentAccessToken == null) {
                    LoginManager.getInstance().logOut();
                    Toast.makeText(MainActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
                     textView.setText("");
                     imageView.setImageResource(0);

                     // After login move to the login activity
                    Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
                    startActivity(intent);
                    // kill this activity
                     finish();
                }
            }
        };
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.help_menu, menu);
        return true;
    }



}