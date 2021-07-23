package com.example.besafe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.FacebookCallback;
import com.facebook.CallbackManager;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class WelcomeActivity extends AppCompatActivity {

    private static final String EMAIL = "email";
    private LoginButton loginButton;
    CallbackManager callbackManager = CallbackManager.Factory.create();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        View parentLayout = findViewById(android.R.id.content);



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
                // Toast.makeText(WelcomeActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                Snackbar.make(parentLayout, "Login Successful", Snackbar.LENGTH_SHORT).show();
                // Go inside the app
                //openRecordPage();
            }

            @Override
            public void onCancel() {
                // App code
                //Popup
               // Toast.makeText(WelcomeActivity.this, "Login Cancel", Toast.LENGTH_SHORT).show();
                Snackbar.make(parentLayout, "Login Cancel", Snackbar.LENGTH_SHORT).show();

            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                //Popup
                //Toast.makeText(MainActivity.this, "Error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                Snackbar.make(parentLayout,  "Error: " + exception.getMessage(), Snackbar.LENGTH_SHORT).show();

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
                    // TODO
//                    // Remove welcome text
//                    helloToApp.setText("");
//                    // User picture
                      String id = object.getString("id");
//                    // User name
                      String name = object.getString("name");
//                    // Show it for the user
//                    textView.setText("Hello " + " " + name);
//                    // using picasso
//                    Picasso.get().load("https://graph.facebook.com/" + id + "/picture?type=large")
//                            .into(imageView);


                    // Move to main activity, new intent with the name, picture
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                    intent.putExtra("Picture_id",id ).putExtra("Name", name);
                    startActivity(intent);
                    // Close this activity
                    finish();

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


    // To know if the user is logged in or logged out
    AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
        @Override
        protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
            // User is not logged in
            if (currentAccessToken == null) {
                LoginManager.getInstance().logOut();
                Toast.makeText(WelcomeActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
               // textView.setText("");
               // imageView.setImageResource(0);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }
}