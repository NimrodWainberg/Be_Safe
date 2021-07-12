package com.example.besafe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

public class WelcomeAnimation extends AppCompatActivity {

    TextView appname;
    LottieAnimationView lottie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_animation);


        appname = findViewById(R.id.app_name);
        lottie = findViewById(R.id.lottie);

        // animation
        appname.animate().translationY(-1400).setDuration(2700).setStartDelay(0);
        lottie.animate().translationX(2000).setDuration(2000).setStartDelay(2900);

        // Run the animation async using runnable (run without any param on a different thread)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // After the Animation move to main activity
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
            //5 sec
        }, 5000);

    }
}