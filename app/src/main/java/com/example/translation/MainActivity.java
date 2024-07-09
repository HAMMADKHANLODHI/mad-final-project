package com.example.translation;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText edtLanguage;
    private TextView translateLanguageTV;
    private Button translateLanguageBtn;
    private Button buttonLogout;
    private Spinner languageSpinner;

    private FirebaseTranslator translator;
    private static final String TAG = "MainActivity";
    private Map<String, Integer> languageMap;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        firebaseAuth = FirebaseAuth.getInstance();

        edtLanguage = findViewById(R.id.idEdtLanguage);
        translateLanguageTV = findViewById(R.id.idTVTranslatedLanguage);
        translateLanguageBtn = findViewById(R.id.idBtnTranslateLanguage);
        languageSpinner = findViewById(R.id.languageSpinner);
        buttonLogout = findViewById(R.id.buttonLogout);

        setupLanguageSpinner();

        translateLanguageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = edtLanguage.getText().toString();
                if (!input.isEmpty()) {
                    String selectedLanguage = languageSpinner.getSelectedItem().toString();
                    int targetLanguageCode = languageMap.get(selectedLanguage);
                    setupTranslator(targetLanguageCode);
                    downloadModel(input);
                } else {
                    Toast.makeText(MainActivity.this, "Please enter text to translate", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Logout button click listener
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });
    }

    private void setupLanguageSpinner() {
        languageMap = new HashMap<>();
        languageMap.put("German", FirebaseTranslateLanguage.DE);
        languageMap.put("Hindi", FirebaseTranslateLanguage.HI);
        languageMap.put("Urdu", FirebaseTranslateLanguage.UR);
        // Add more languages as needed

        // Create a custom adapter with custom item layout
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, new ArrayList<>(languageMap.keySet())) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.WHITE); // Set your color here
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.BLACK); // Set your color here
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);
    }

    private void setupTranslator(int targetLanguageCode) {
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(FirebaseTranslateLanguage.EN)
                .setTargetLanguage(targetLanguageCode)
                .build();
        translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);
    }

    private void downloadModel(String input) {
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();

        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        translateLanguage(input);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Failed to download language model.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void translateLanguage(String input) {
        translator.translate(input)
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String translatedText) {
                        translateLanguageTV.setText(translatedText);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Failed to translate text.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void logoutUser() {
        firebaseAuth.signOut();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }
}
