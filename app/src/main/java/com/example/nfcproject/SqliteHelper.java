package com.example.nfcproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class SqliteHelper extends SQLiteOpenHelper {


    public SqliteHelper(Context context) {
        super(context, "balances.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create Table NfcDetails(uniqId TEXT primary key, BalanceNo TEXT, OldBalance_USD TEXT,NewBalance_USD TEXT,NewBalance_EUR TEXT,ExchangeRate TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("drop Table if exists NfcDetails");
    }

    public Boolean insertuserdata(String uniqId, String BalanceNo, String OldBalance_USD,String NewBalance_USD,String NewBalance_EUR,String ExchangeRate)
    {
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", uniqId);
        contentValues.put("contact", BalanceNo);
        contentValues.put("dob", OldBalance_USD);
        contentValues.put("dob", NewBalance_USD);
        contentValues.put("dob", NewBalance_EUR);
        contentValues.put("dob", ExchangeRate);
        long result=DB.insert("NfcDetails", null, contentValues);
        return result != -1;
    }

    public Cursor getdata ()
    {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from NfcDetails", null);
        return cursor;

    }
}
