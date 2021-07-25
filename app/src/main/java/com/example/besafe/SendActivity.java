package com.example.besafe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SendActivity extends AppCompatActivity {

    //
    BottomNavigationView bottomNavigationView;

    // Shared preferences
    EditText keyWord, phone;
    Button save_btn;
    SharedPreferences sp;
    String keyWordStr, phoneStr;


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
                        finish();
                        return true;


                    case R.id.recordsActivity:
                        // open the correct activity
                        startActivity(new Intent(getApplicationContext(), RecordsActivity.class));
                        overridePendingTransition(0, 0);
                        finish();
                        return true;

                    case R.id.sendActivity:
                        return true;
                }
                return false;
            }

        });

        // Shared preferences
        keyWord = findViewById(R.id.key_word);
        phone = findViewById(R.id.phone_by_user);
        save_btn = findViewById(R.id.save_btn);

        // Name + Mode
        sp = getSharedPreferences("BeSafeConfiguration", Context.MODE_PRIVATE);

        // Listener save button
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the strings
                keyWordStr = keyWord.getText().toString();
                phoneStr  = phone.getText().toString();

                // Shared Preferences editor
                SharedPreferences.Editor editor = sp.edit();

                // pass the data that user have entered
                editor.putString("safe_word", keyWordStr);
                editor.putString("emergency_phone", phoneStr);
                // commit
                editor.commit();
                // Popup for the user
                Toast.makeText(SendActivity.this, "Information as been saved", Toast.LENGTH_LONG).show();

                Log.d("SHAREDPREFERENCES", "Information as been saved ");
            }
        });
    }
}
