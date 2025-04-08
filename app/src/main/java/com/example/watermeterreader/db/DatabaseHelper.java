package com.example.watermeterreader.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "meter_reading_db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users table
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT," +
                "password TEXT," +
                "role TEXT)");

        // Create Household table with normalized owner details and extra fields
        db.execSQL("CREATE TABLE household (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "first_name TEXT," +
                "last_name TEXT," +
                "id_number TEXT," +
                "house_number TEXT," +
                "street_name TEXT," +
                "suburb_name TEXT)");

        // Create Meter Reading table
        db.execSQL("CREATE TABLE meter_reading (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "house_number TEXT," +
                "reading_value REAL," +
                "reading_date TEXT)");

        // Create Billing table
        db.execSQL("CREATE TABLE billing (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "house_number TEXT," +
                "amount REAL," +
                "billing_date TEXT)");

        // Insert a default admin user for testing
        ContentValues cv = new ContentValues();
        cv.put("username", "admin");
        cv.put("password", "admin123");
        cv.put("role", "admin");
        db.insert("users", null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS household");
        db.execSQL("DROP TABLE IF EXISTS meter_reading");
        db.execSQL("DROP TABLE IF EXISTS billing");
        onCreate(db);
    }

    // --- User methods ---
    public boolean validateUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE username=? AND password=?", new String[]{username, password});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public String getUserRole(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT role FROM users WHERE username=?", new String[]{username});
        if (cursor != null && cursor.moveToFirst()) {
            String role = cursor.getString(0);
            cursor.close();
            return role;
        }
        return null;
    }

    // Method to add a new user
    public long addUser(String username, String password, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);
        values.put("role", role);
        return db.insert("users", null, values);
    }

    // --- Household methods ---
    // New method for adding household with normalized fields
    public long addHousehold(String firstName, String lastName, String idNumber,
                             String houseNumber, String streetName, String suburbName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("first_name", firstName);
        values.put("last_name", lastName);
        values.put("id_number", idNumber);
        values.put("house_number", houseNumber);
        values.put("street_name", streetName);
        values.put("suburb_name", suburbName);
        return db.insert("household", null, values);
    }

    // Retrieve house numbers for the drop-down (Spinner)
    public List<String> getHouseNumbers() {
        List<String> houseNumbers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT house_number FROM household", null);
        if(cursor.moveToFirst()){
            do{
                houseNumbers.add(cursor.getString(0));
            } while(cursor.moveToNext());
        }
        cursor.close();
        return houseNumbers;
    }

    // --- Meter Reading methods ---
    public long addMeterReading(String houseNumber, double readingValue, String readingDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("house_number", houseNumber);
        values.put("reading_value", readingValue);
        values.put("reading_date", readingDate);
        return db.insert("meter_reading", null, values);
    }

    public double getLastReading(String houseNumber) {
        double reading = 0.0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT reading_value FROM meter_reading WHERE house_number=? ORDER BY id DESC LIMIT 1", new String[]{houseNumber});
        if (cursor.moveToFirst()) {
            reading = cursor.getDouble(0);
        }
        cursor.close();
        return reading;
    }

    // --- Billing methods ---
    public long addBilling(String houseNumber, double amount, String billingDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("house_number", houseNumber);
        values.put("amount", amount);
        values.put("billing_date", billingDate);
        return db.insert("billing", null, values);
    }

    public void calculateAndSaveBill(String houseNumber, double newReading, String billingDate) {
        // Retrieve the last reading
        double lastReading = getLastReading(houseNumber);
        double consumption = newReading - lastReading;
        double rate = 1.50; // example rate
        double billAmount = consumption * rate;
        addBilling(houseNumber, billAmount, billingDate);
    }

    // Retrieve billing list joining billing and household details
    public Cursor getBillingList() {
        SQLiteDatabase db = this.getReadableDatabase();
        // Joining billing with household to get owner names
        return db.rawQuery("SELECT billing.id, billing.house_number, billing.amount, billing.billing_date, " +
                "household.first_name, household.last_name " +
                "FROM billing LEFT JOIN household ON billing.house_number = household.house_number", null);
    }
}
