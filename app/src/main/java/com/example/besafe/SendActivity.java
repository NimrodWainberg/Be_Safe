package com.example.besafe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SendActivity extends AppCompatActivity {

    //
    BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        // For navigation button
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        // Current activity selected is sendActivity
        bottomNavigationView.setSelectedItemId(R.id.sendActivity);
        // Move to the other pages
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.mainActivity:
                        // open the correct activity
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0, 0);
                        return true;


                    case R.id.recordsActivity:
                        // open the correct activity
                        startActivity(new Intent(getApplicationContext(), RecordsActivity.class));
                        overridePendingTransition(0, 0);
                        return true;

                    case R.id.sendActivity:
                        return true;
                }
                return false;
            }

        });
    }
}
