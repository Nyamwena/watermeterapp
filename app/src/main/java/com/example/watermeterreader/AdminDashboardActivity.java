package com.example.watermeterreader;


import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.example.watermeterreader.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AdminDashboardActivity extends AppCompatActivity {

    private Button btnAddUser, btnAddHousehold, btnViewBilling ;
    private FloatingActionButton fabBack, fabLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        btnAddUser = findViewById(R.id.btnAddUser);
        btnAddHousehold = findViewById(R.id.btnAddHousehold);
        btnViewBilling = findViewById(R.id.btnViewBilling);

        btnAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminDashboardActivity.this, AddUserActivity.class);
                startActivity(intent);
            }
        });

        btnAddHousehold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminDashboardActivity.this, AddHouseholdActivity.class);
                startActivity(intent);
            }
        });

        btnViewBilling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminDashboardActivity.this, ViewBillingActivity.class);
                startActivity(intent);
            }
        });

        fabBack = findViewById(R.id.fabBack);
        fabLogout = findViewById(R.id.fabLogout);

        fabBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();  // Navigates back
            }
        });

        fabLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Logout: navigate back to LoginActivity and clear activity stack
                Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}