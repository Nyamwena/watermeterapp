package com.example.watermeterreader;



import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.watermeterreader.R;
import com.example.watermeterreader.db.DatabaseHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import android.Manifest;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;


public class UserDashboardActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 100;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;
    private Button btnCapture, btnSaveReading;
    private ImageView ivMeterImage;
    private TextView tvRecognizedText;
    private EditText etHouseNumber;
    private Bitmap capturedBitmap;
    private DatabaseHelper dbHelper;
    private Spinner spinnerHouseNumbers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);
        // Before invoking the camera, check for the CAMERA permission.
        checkCameraPermission();

        dbHelper = new DatabaseHelper(this);

        btnCapture       = findViewById(R.id.btnCapture);
        btnSaveReading   = findViewById(R.id.btnSaveReading);
        ivMeterImage     = findViewById(R.id.ivMeterImage);
        tvRecognizedText = findViewById(R.id.tvRecognizedText);

        spinnerHouseNumbers = findViewById(R.id.spinnerHouseNumbers);
        // 1) Load “House — Suburb” strings
        List<String> houses = dbHelper.getHouseDisplayList();
        if (houses.isEmpty()) {
            Toast.makeText(this,
                    "No households available. Please ask admin to add some.",
                    Toast.LENGTH_LONG).show();
        } else {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    houses
            );
            adapter.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item
            );
            spinnerHouseNumbers.setAdapter(adapter);
        }

//        etHouseNumber    = findViewById(R.id.etHouseNumber);

        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        btnSaveReading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveMeterReading();
            }
        });
    }
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted; request it.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            // Permission is already granted, you may call your camera intent directly.
            dispatchTakePictureIntent();
        }
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Check if a camera activity is available
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "No camera found!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            capturedBitmap = (Bitmap) extras.get("data");
            ivMeterImage.setImageBitmap(capturedBitmap);
            runTextRecognition(capturedBitmap);
        }
    }

    private void runTextRecognition(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        recognizer.process(image)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text visionText) {
                        processTextBlock(visionText);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(UserDashboardActivity.this, "OCR Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void processTextBlock(Text result) {
        StringBuilder recognizedText = new StringBuilder();
        for (Text.TextBlock block : result.getTextBlocks()) {
            recognizedText.append(block.getText()).append("\n");
        }
        String text = recognizedText.toString().trim();
        // Extract only digits from the recognized text
        String parsedReading = text.replaceAll("\\D+", "");
        tvRecognizedText.setText(parsedReading);
    }

    private void saveMeterReading() {
        // 1) Get selected house (e.g. "123 — Springfield")
        String selected = (String) spinnerHouseNumbers.getSelectedItem();
        if (selected == null) {
            Toast.makeText(this, "Please choose a house.", Toast.LENGTH_SHORT).show();
            return;
        }
        String houseNumber = selected.split(" \u2014 ")[0];

        // 2) Read the OCR result
        String readingStr = tvRecognizedText.getText().toString().trim();
        if (readingStr.isEmpty()) {
            Toast.makeText(this, "No meter reading to save.", Toast.LENGTH_SHORT).show();
            return;
        }

        double newReading;
        try {
            newReading = Double.parseDouble(readingStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid reading value", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3) Fetch the *previous* reading BEFORE inserting
        double lastReading = dbHelper.getLastReading(houseNumber);

        // 4) Insert the new reading
        String currentDate = DateFormat.getDateTimeInstance().format(new Date());
        long insertResult = dbHelper.addMeterReading(houseNumber, newReading, currentDate);
        if (insertResult == -1) {
            Toast.makeText(this, "Failed to save meter reading", Toast.LENGTH_SHORT).show();
            return;
        }

        // 5) Calculate consumption and billing
        double consumption = newReading - lastReading;           // kiloliters used
        double rate        = 0.00055;                               // your rate per kL
        double billAmount  = consumption * rate;                 // total bill

        // 6) Store the billing record
        long billResult = dbHelper.addBilling(houseNumber, billAmount, currentDate);
        if (billResult != -1) {
            Toast.makeText(this, String.format(
                            "Saved! Consumed: %.2f kL, Bill: $%.2f",
                            consumption, billAmount),
                    Toast.LENGTH_LONG
            ).show();
        } else {
            Toast.makeText(this, "Failed to save billing record", Toast.LENGTH_SHORT).show();
        }
    }




}
