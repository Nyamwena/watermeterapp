package com.example.watermeterreader.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.watermeterreader.R;
import android.widget.TextView;

public class BillingAdapter extends RecyclerView.Adapter<BillingAdapter.ViewHolder> {
    private Cursor cursor;

    public BillingAdapter(Cursor cursor) {
        this.cursor = cursor;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOwnerName, tvHouseInfo, tvAmount, tvDate;
        public ViewHolder(View itemView) {
            super(itemView);
            tvOwnerName = itemView.findViewById(R.id.tvOwnerName);
            tvHouseInfo  = itemView.findViewById(R.id.tvHouseInfo);
            tvAmount     = itemView.findViewById(R.id.tvAmount);
            tvDate       = itemView.findViewById(R.id.tvDate);
        }
    }

    @Override
    public int getItemCount() {
        return (cursor == null) ? 0 : cursor.getCount();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_billing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (!cursor.moveToPosition(position)) return;

        String firstName   = cursor.getString(cursor.getColumnIndexOrThrow("first_name"));
        String lastName    = cursor.getString(cursor.getColumnIndexOrThrow("last_name"));
        String houseNumber = cursor.getString(cursor.getColumnIndexOrThrow("house_number"));
        double amount      = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
        String date        = cursor.getString(cursor.getColumnIndexOrThrow("billing_date"));
        String suburb      = cursor.getString(cursor.getColumnIndexOrThrow("suburb_name"));
        // Note: include suburb_name in your getBillingList() SELECT if needed

        holder.tvOwnerName.setText(firstName + " " + lastName);
        holder.tvHouseInfo.setText("House " + houseNumber + " — " + suburb);
        holder.tvAmount.setText(String.format("Amount: $%.2f", amount));
        holder.tvDate.setText(date);
    }

    public void swapCursor(Cursor newCursor) {
        if (cursor != null) cursor.close();
        cursor = newCursor;
        notifyDataSetChanged();
    }
}