package com.example.besafe;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
    String stringkeyWord = "", stringPhone = "";

    // For Lottie Animation
    LottieAnimationView record_on_animation;

    // For calling
    private static final int REQUEST_PHONE_CALL = 1;

    // For sms
    private static final int REQUEST_SEND_SMS = 1;
    String sms_txt = "Help";

    // switch buttons
    SwitchCompat phoneSwitch, messageSwitch;
    static int messageFeature = 0;
    static int phoneFeature = 0;


    // Listview for contact
    private ListView listView_contact;
    private static ArrayList<String> kewWordsHistory = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private Set<String> wordSetList = new HashSet<>();


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

                        Log.d("MATCHWORD", keyWord + " ");
                        // start record
                        handleBtnRecordPressed();
                        Toast.makeText(RecordsActivity.this, " Record as been started with: " + keyWord, Toast.LENGTH_SHORT).show();

                        // check switches
                        if (phoneFeature == 1) {

                            // call to emergency number
                            callEmergency();
                        }

                        if (messageFeature == 1) {

                            // send emergency sms
                            sendEmergency();
                        }
                    }
                } else {
                    Log.d("RECOGNIZER", "Empty results");
                }
            } else {
                Log.d("OK", "Line 63 not ok: ");
                Log.d("RecognizerSpeechIntent", recognizerSpeechIntent + "");

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);
        View parentLayout = findViewById(android.R.id.content);

        // Switch buttons
        phoneSwitch = findViewById(R.id.switchPhone);
        messageSwitch = findViewById(R.id.switchMessage);

        phoneSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    Snackbar.make(parentLayout, getResources().getString(R.string.callFeatureOn), Snackbar.LENGTH_LONG).show();
                    phoneFeature = 1;
                } else {
                    Snackbar.make(parentLayout, getResources().getString(R.string.callFeatureOff), Snackbar.LENGTH_LONG).show();
                    phoneFeature = 0;
                }

            }
        });


        messageSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    Snackbar.make(parentLayout, getResources().getString(R.string.sendFeatureOn), Snackbar.LENGTH_LONG).show();
                    messageFeature = 1;
                } else {
                    Snackbar.make(parentLayout, getResources().getString(R.string.sendFeatureOff), Snackbar.LENGTH_LONG).show();
                    messageFeature = 0;
                }

            }
        });


        // For keyWord, contact number from user
        showKeyWord = findViewById(R.id.showkeyWord);
        showPhone = findViewById(R.id.showPhone);
        // extract the data from sharedPreferences
        sp = getSharedPreferences("BeSafeConfiguration", Context.MODE_PRIVATE);

        // get from shared preferences
        stringkeyWord = sp.getString("safe_word", "");


        // Check if have changed
        if (wordSetList.contains(stringkeyWord)) {
            Log.d("STRINGKEEY", wordSetList + "");
            stringkeyWord = "";

        } else
            wordSetList.add(stringkeyWord);
        Log.d("STRINGKEEY", wordSetList + "");

        // get phone from sp
        stringPhone = sp.getString("emergency_phone", "");


        Log.d("STRINGKEY", stringkeyWord + " ");
        Log.d("STRINGKEY", kewWordsHistory + " ");

        // set text
        if (!stringkeyWord.equals("")) {
            showKeyWord.setText(stringkeyWord);

            // listView messages
            listView_contact = findViewById(R.id.listview_contact);

            // Adding word information in the list to display with time
            String word_History = stringkeyWord + "\n" + (new SimpleDateFormat("EEE, d MMM yyyy HH:mm")
                    .format(Calendar.getInstance().getTime()));
            kewWordsHistory.add(word_History);
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, kewWordsHistory);
            adapter.notifyDataSetChanged();
            listView_contact.setAdapter(adapter);
        }

        if (!stringPhone.equals("")) {
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
                        finish();
                        return true;


                    case R.id.recordsActivity:
                        return true;

                    case R.id.sendActivity:
                        // open the correct activity
                        startActivity(new Intent(getApplicationContext(), SendActivity.class));
                        overridePendingTransition(0, 0);
                        finish();
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
            ActivityCompat.requestPermissions(RecordsActivity.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PHONE_CALL);
        }

        // Sms permission
        if (ContextCompat.checkSelfPermission(RecordsActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(RecordsActivity.this, new String[]{Manifest.permission.SEND_SMS}, REQUEST_SEND_SMS);
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
    public void callEmergency() {
        // check if there is a number
        if (stringPhone == "") {
            Toast.makeText(getApplicationContext(), "Please enter a number first !", Toast.LENGTH_LONG).show();
            stringPhone = "100";
        }

        String s = "tel:" + stringPhone.trim();
        // Intent for calling
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse(s));
        startActivity(intent);

    }

    // For Sending sms to a emergency number
    public void sendEmergency() {
        // check if there is a number
        if (stringPhone == "") {
            Toast.makeText(getApplicationContext(), "Please enter a number first !", Toast.LENGTH_LONG).show();
        }
        if (sms_txt == "") {
            Toast.makeText(getApplicationContext(), "Please enter a message first !", Toast.LENGTH_LONG).show();
            sms_txt = "Help";
        }

        // Initialize sms manager
        SmsManager smsManager = SmsManager.getDefault();
        // send text message
        smsManager.sendTextMessage(stringPhone, null, sms_txt, null, null);
        Toast.makeText(getApplicationContext(), "SMS was sent", Toast.LENGTH_LONG).show();
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

    private void handleBtnRecordPressed() {
        try {
            // play animation
            record_on_animation.playAnimation();
            // TODO
            // change record button image
            record_on.setDrawingCacheBackgroundColor(R.drawable.ic_red_record);

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
        } catch (Exception e) {
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


    // Check if user has microphone
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

