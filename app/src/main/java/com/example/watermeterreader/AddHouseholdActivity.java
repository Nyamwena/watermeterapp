package com.example.watermeterreader;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.watermeterreader.R;
import com.example.watermeterreader.db.DatabaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AddHouseholdActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etIdNumber, etHouseNumber, etStreetName, etSuburbName;
    private Button btnAddHousehold;
    private DatabaseHelper dbHelper;
    private FloatingActionButton fabBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_household);

        dbHelper = new DatabaseHelper(this);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etIdNumber = findViewById(R.id.etIdNumber);
        etHouseNumber = findViewById(R.id.etHouseNumber);
        etStreetName = findViewById(R.id.etStreetName);
        etSuburbName = findViewById(R.id.etSuburbName);
        btnAddHousehold = findViewById(R.id.btnAddHousehold);

        btnAddHousehold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String firstName = etFirstName.getText().toString().trim();
                String lastName = etLastName.getText().toString().trim();
                String idNumber = etIdNumber.getText().toString().trim();
                String houseNumber = etHouseNumber.getText().toString().trim();
                String streetName = etStreetName.getText().toString().trim();
                String suburbName = etSuburbName.getText().toString().trim();

                if(firstName.isEmpty() || lastName.isEmpty() || idNumber.isEmpty() ||
                        houseNumber.isEmpty() || streetName.isEmpty() || suburbName.isEmpty()){
                    Toast.makeText(AddHouseholdActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                long result = dbHelper.addHousehold(firstName, lastName, idNumber, houseNumber, streetName, suburbName);
                if(result != -1) {
                    Toast.makeText(AddHouseholdActivity.this, "Household added successfully!", Toast.LENGTH_SHORT).show();
                    etFirstName.setText("");
                    etLastName.setText("");
                    etIdNumber.setText("");
                    etHouseNumber.setText("");
                    etStreetName.setText("");
                    etSuburbName.setText("");
                } else {
                    Toast.makeText(AddHouseholdActivity.this, "Failed to add household", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fabBack = findViewById(R.id.fabBack);
        fabBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}