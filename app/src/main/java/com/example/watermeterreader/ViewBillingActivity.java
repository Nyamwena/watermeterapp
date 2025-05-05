package com.example.watermeterreader;

import android.database.Cursor;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.watermeterreader.R;
import com.example.watermeterreader.db.DatabaseHelper;
import com.example.watermeterreader.ui.BillingAdapter;

public class ViewBillingActivity extends AppCompatActivity {
    private RecyclerView rvBilling;
    private BillingAdapter adapter;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_billing);

        rvBilling = findViewById(R.id.rvBilling);
        dbHelper  = new DatabaseHelper(this);

        // Layout manager + divider
        rvBilling.setLayoutManager(new LinearLayoutManager(this));
        rvBilling.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // Load data
        Cursor cursor = dbHelper.getBillingList();
        adapter = new BillingAdapter(cursor);
        rvBilling.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close cursor to prevent leaks
        adapter.swapCursor(null);
    }
}