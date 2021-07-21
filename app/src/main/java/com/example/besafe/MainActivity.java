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


    CallbackManager callbackManager = CallbackManager.Factory.create();
    private static final String EMAIL = "email";
    private LoginButton loginButton;
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
        helloToApp = (TextView) findViewById(R.id.welcome_txt);

        language_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] language = {"ENGLISH", "עברית"};

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
                        if(language[which].equals("ENGLISH")){

                            context = LocaleHelper.setLocale(MainActivity.this,"en");
                            resources = context.getResources();

                            helloToApp.setText(resources.getString(R.string.welcome_txt));
                        }

                        if (language[which].equals("עברית")){

                            context = LocaleHelper.setLocale(MainActivity.this,"iw-rlL");
                            resources = context.getResources();

                            helloToApp.setText(resources.getString(R.string.welcome_txt));
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


        imageView = findViewById(R.id.profilePic);
        textView = findViewById(R.id.profileName);

        FacebookSdk.sdkInitialize(getApplicationContext());

        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList(EMAIL));
        loginButton.setPermissions(Arrays.asList("user_gender, user_friends"));
        // If you are using in a fragment, call loginButton.setFragment(this);

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                //Popup
                Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                // Go inside the app
                //openRecordPage();
            }

            @Override
            public void onCancel() {
                // App code
                //Popup
                Toast.makeText(MainActivity.this, "Login Cancel", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                //Popup
                Toast.makeText(MainActivity.this, "Error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        // Get information from facebook about user
        GraphRequest graphRequest = new GraphRequest().newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                Log.d("Demo", object.toString());
                // Obtain info
                try {
                    // Remove welcome text
                    helloToApp.setText("");
                    // User picture
                    String id = object.getString("id");
                    // User name
                    String name = object.getString("name");
                    // Show it for the user
                    textView.setText("Hello " + " " + name);
                    // using picasso
                    Picasso.get().load("https://graph.facebook.com/" + id + "/picture?type=large")
                            .into(imageView);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        // For passing information between two activities (from facebook to mainActivity)
        Bundle bundle = new Bundle();
        bundle.putString("fields", "gender, name, id, first_name, last_name");

        // pass the bundle
        graphRequest.setParameters(bundle);
        // execute async (on a different intent in the background)
        graphRequest.executeAsync();

    }

    // Open Record activity
    public void openRecordPage() {
        Intent intent = new Intent(this, RecordsActivity.class);
        startActivity(intent);
    }

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
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }
}