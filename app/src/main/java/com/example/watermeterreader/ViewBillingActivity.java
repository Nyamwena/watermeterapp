package com.example.watermeterreader;

import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.example.watermeterreader.R;
import com.example.watermeterreader.db.DatabaseHelper;
import java.util.ArrayList;

public class ViewBillingActivity extends AppCompatActivity {

    private ListView lvBilling;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_billing);

        lvBilling = findViewById(R.id.lvBilling);
        dbHelper = new DatabaseHelper(this);

        loadBillingData();
    }

    private void loadBillingData() {
        ArrayList<String> billingList = new ArrayList<>();
        Cursor cursor = dbHelper.getBillingList();
        if(cursor != null && cursor.moveToFirst()){
            do {
                // Get billing details (ID, House Number, Amount, Date) and household owner's name if available
                int billingId = cursor.getInt(0);
                String houseNumber = cursor.getString(1);
                double amount = cursor.getDouble(2);
                String billingDate = cursor.getString(3);
                String firstName = cursor.getString(4);
                String lastName = cursor.getString(5);

                String record = "ID: " + billingId + "\nHouse: " + houseNumber +
                        "\nOwner: " + (firstName != null ? firstName + " " + lastName : "N/A") +
                        "\nAmount: $" + amount + "\nDate: " + billingDate;
                billingList.add(record);
            } while(cursor.moveToNext());
            cursor.close();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, billingList);
        lvBilling.setAdapter(adapter);
    }
}