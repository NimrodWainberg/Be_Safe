package com.example.besafe;

import android.Manifest;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.util.ArrayList;

import static android.Manifest.permission.RECORD_AUDIO;

public class RecordsActivity extends AppCompatActivity {

    private static int MICROPHONE_PERMISSION_CODE = 200;
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    BottomNavigationView bottomNavigationView;
    // For voice commands
    private SpeechRecognizer speechRecognizer;
    private Intent intentRecognizer;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);
        intentRecognizer = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        // We can specified a specific language
        intentRecognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {
                //Popup
                Toast.makeText(RecordsActivity.this, " Start Voice recognition", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {
                //Popup
                Toast.makeText(RecordsActivity.this, " Start Voice recognition", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle results) {

                //Popup
                Toast.makeText(RecordsActivity.this, " Start Voice recognition", Toast.LENGTH_SHORT).show();

                Log.d("ONRESULT", "STARTVOICERECOGNITION ");

                // give the list of the results
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String string = "";
                if (matches != null) {
                    // Get the string from the start
                    string = matches.get(0);
                    // write the text in the text view
                    textView.setText(string);

                    // Check for specific word
                    if(string.toLowerCase().equals("bilbi")){
                        // TODO: Call to police

                    }
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });

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
    }

    // Voice Recognition button pressed
    public void startRecognizeButton(View view) {
        // Start speechRecognize
        speechRecognizer.startListening(intentRecognizer);

    }

    // Voice Recognition button stopped pressed
    public void stopRecognizeButton(View view) {
        // Start speechRecognize
        speechRecognizer.stopListening();
    }

    // Record audio
    public void btnRecordPressed(View v) {

        handleBtnRecordPressed();
    }

    private void handleBtnRecordPressed(){
        try {
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
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;

        Toast.makeText(this, "Recording is stopped", Toast.LENGTH_LONG).show();
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

