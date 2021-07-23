package com.example.besafe;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.util.ArrayList;

import static android.Manifest.permission.RECORD_AUDIO;

public class RecordsActivity extends AppCompatActivity {

    // For recording
    private static int MICROPHONE_PERMISSION_CODE = 200;
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    ImageButton record_on, record_stop, record_play;

    BottomNavigationView bottomNavigationView;

    // For voice recognition
    private SpeechRecognizer speechRecognizer;
    private Intent intentRecognizer;
    private TextView textView;
    final int recognizerSpeechIntent = 1;

    // For keyWord, contact number from user
     TextView showKeyWord, showPhone;
     SharedPreferences sp;
     String stringkeyWord, stringPhone;

     // For Lottie Animation
     LottieAnimationView record_on_animation;

     // For calling
    private static final int REQUEST_PHONE_CALL = 1;



    // Getting the data from voice recognition
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("ONACTIVITY", "onActivityResult: ");

        // Speech recognizer
        // check if we receive the data (string from speechRecognizer)
        if (data != null) {

            Log.d("DATA", "data not null ");

            // check if the result was ok
            if (requestCode == recognizerSpeechIntent && resultCode == RESULT_OK) {

                Log.d("OK", "Line 63 was ok");

                // string array, store the words from speechRecognizer into an array
                final ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                // check that we have there any strings
                if (!results.isEmpty()) {
                    Log.d("RECOGNIZER", results.toString());
                    // get the word  from the array
                    final String keyWord = results.get(0);
                    // Popup
                    Toast.makeText(RecordsActivity.this, " The key word is:" + keyWord, Toast.LENGTH_SHORT).show();

                    // Write the text inside
                    textView.setText(keyWord);

                    // TODO: make a call/ send message to emergency contact


                    // check for key word
                    if (stringkeyWord == "") {
                        stringkeyWord = "hello";
                    }

                    if (keyWord.equalsIgnoreCase(stringkeyWord)) {

                        Log.d("MATCHWORD", keyWord +" ");
                        // start record
                        handleBtnRecordPressed();
                        Toast.makeText(RecordsActivity.this, " Record as been started with: " + keyWord, Toast.LENGTH_SHORT).show();

                        // call to emergency number
                        callEmergency();
                    }
                } else {
                    Log.d("RECOGNIZER", "Empty results");
                }
            } else {
                Log.d("OK", "Line 63 not ok: ");
                Log.d("RecognizerSpeechIntent", recognizerSpeechIntent +"");

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        // For keyWord, contact number from user
        showKeyWord = findViewById(R.id.showkeyWord);
        showPhone = findViewById(R.id.showPhone);
        // extract the data from sharedPreferences
        sp = getSharedPreferences("BeSafeConfiguration", Context.MODE_PRIVATE);
        stringkeyWord = sp.getString("safe_word", "");
        stringPhone = sp.getString("emergency_phone", "");

        // set text
        if (stringkeyWord != null) {
            showKeyWord.setText(stringkeyWord);
        }

        if (stringkeyWord != null) {
            showPhone.setText(stringPhone);
        }

        // Lottie Animation
        record_on_animation = findViewById(R.id.record_animation);


        // For voice recognition
        intentRecognizer = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        // put Extra- add more parameters to the function
        // We can specified a specific language
        // intentRecognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Language for voice recognition is EN
        intentRecognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en")
          // for 1 word
         .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);//.putExtra(RecognizerIntent.);


        // for voice recognition
        textView = findViewById(R.id.voiceRecognition_txt);

        // For navigation button
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        // Current activity selected is Records
        bottomNavigationView.setSelectedItemId(R.id.recordsActivity);
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

        // Check if there is a microphone request permission for using it
        if (isMicrophonePresent()) {
            getMicrophonePermission();
        }


        // Calling permission
        if (ContextCompat.checkSelfPermission(RecordsActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(RecordsActivity.this, new String[]{Manifest.permission.CALL_PHONE},REQUEST_PHONE_CALL);
        }
    }

    // Voice Recognition button pressed
    public void startRecognizeButton(View view) {
        // TODO
        Log.d("STARTRECOGNITION", "startRecognizeButton: ");
        //
        startActivityForResult(intentRecognizer, recognizerSpeechIntent);

        // Start speechRecognize
        //speechRecognizer.startListening(intentRecognizer);

    }

    // For calling to a emergency number
    public void callEmergency(){
        // check if there is a number
        if (stringPhone == null) {
            Toast.makeText(getApplicationContext(), "Please enter a number first !", Toast.LENGTH_LONG).show();
        } else {
            String s = "tel:" + stringPhone;
            // Intent for calling
           Intent intent = new Intent(Intent.ACTION_CALL);
           intent.setData(Uri.parse(s));
           startActivity(intent);
        }
    }

    // Voice Recognition button stopped pressed
    public void stopRecognizeButton(View view) {
        // TODO
        Log.d("STOPRECOGNITION", "stopRecognizeButton: ");
        // stop speechRecognize
        speechRecognizer.stopListening();
    }

    // Record audio
    public void btnRecordPressed(View v) {
        handleBtnRecordPressed();
    }

    private void handleBtnRecordPressed(){
        try {
            // play animation
            record_on_animation.playAnimation();
            // TODO
            // change record button image
            record_on.setBackgroundResource(R.drawable.ic_red_record);

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            // Save recording file
            mediaRecorder.setOutputFile(getRecordingFilePath());
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            // We have to use try for prepare
            mediaRecorder.prepare();
            mediaRecorder.start();

            // Popup to inform that the record as been started
            Toast.makeText(this, "Recording is started", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Stop recording
    public void btnStopPressed(View v) {
        //TODO
        Log.d("STOPRegularRecord", "btnStopPressed: ");

        // stop animation
        //record_on_animation.;


        try {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;

            Toast.makeText(this, "Recording is stopped", Toast.LENGTH_LONG).show();
        }catch (Exception e) {
            e.printStackTrace();
        }


    }

    // Play the record
    public void btnPlayPressed(View v) {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(getRecordingFilePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            Toast.makeText(this, "Recording is playing", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean isMicrophonePresent() {
        if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Give permission for microphone use
     * If there is not a permission, request
     */
    private void getMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MICROPHONE_PERMISSION_CODE);
        }
    }

    // Save record file
    // Path to record file
    private String getRecordingFilePath() {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(musicDirectory, "testRecordingFile" + ".mp3");
        return file.getPath();
    }
}

