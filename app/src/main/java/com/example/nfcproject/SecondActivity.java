package com.example.nfcproject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import be.appfoundry.nfclibrary.activities.NfcActivity;
import be.appfoundry.nfclibrary.tasks.interfaces.AsyncOperationCallback;
import be.appfoundry.nfclibrary.tasks.interfaces.AsyncUiCallback;
import be.appfoundry.nfclibrary.utilities.async.WriteCallbackNfcAsync;
import be.appfoundry.nfclibrary.utilities.interfaces.NfcReadUtility;
import be.appfoundry.nfclibrary.utilities.sync.NfcMessageUtilityImpl;
import be.appfoundry.nfclibrary.utilities.sync.NfcReadUtilityImpl;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.nfc.NdefMessage;
import android.nfc.NfcEvent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class SecondActivity extends NfcActivity {

    private static final String TAG = "helloman";
    TextView text1,text2;
    Button shw;
    NfcReadUtility mNfcReadUtility = new NfcReadUtilityImpl();
    ProgressDialog mProgressDialog;
    SqliteHelper sqlHelper;
    ArrayList<String> myList;
    String BalanceNo;
    String OldBalance_USD;
    String NewBalance_USD;
    String NewBalance_EUR;
    String ExchangeRate;

    AsyncUiCallback mAsyncUiCallback = new AsyncUiCallback() {
        @Override
        public void callbackWithReturnValue(Boolean result) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            if (result) {
                Toast.makeText(SecondActivity.this, "values written to tag and SQL database successfully!", Toast.LENGTH_LONG).show();
                if (BalanceNo!=null){
                    sqlHelper.insertuserdata(getSaltString(),BalanceNo,OldBalance_USD,NewBalance_USD,NewBalance_EUR,ExchangeRate);

                }
                for (String messages:getNfcMessages()){
                    myList = new ArrayList<>(Arrays.asList(messages.split(",")));
                    text1.setText(myList.get(0));
                    text2.setText(myList.get(1));
                }
            }

            Log.d(TAG,"Received our result : " + result);

        }

        @Override
        public void onProgressUpdate(Boolean... values) {
            if (values.length > 0 && values[0] && mProgressDialog != null) {
                mProgressDialog.setMessage("Writing");
                Log.d(TAG,"Writing !");
            }
        }

        @Override
        public void onError(Exception e) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            Log.i(TAG,"Encountered an error !",e);
            Toast.makeText(SecondActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    AsyncOperationCallback mAsyncOperationCallback;
    private AsyncTask<Object, Void, Boolean> mTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        Intent intent=getIntent();
        text1=findViewById(R.id.textID1);
        text2=findViewById(R.id.textID2);
        shw=findViewById(R.id.buttonIDd);
        sqlHelper=new SqliteHelper(this);

        BalanceNo=intent.getStringExtra("BalanceNo");
        OldBalance_USD=intent.getStringExtra("OldBalance_USD");
        NewBalance_USD=intent.getStringExtra("NewBalance_USD");
        NewBalance_EUR=intent.getStringExtra("NewBalance_EUR");
        ExchangeRate=intent.getStringExtra("ExchangeRate");


        String a=intent.getStringExtra("a");
        String b=intent.getStringExtra("b");
        writeMessage(a+","+b);

        shw.setOnClickListener(v -> {
            Cursor res = sqlHelper.getdata();
            if(res.getCount()==0){
                Toast.makeText(SecondActivity.this, "No Entry Exists", Toast.LENGTH_SHORT).show();
                return;
            }
            StringBuffer buffer = new StringBuffer();
            while(res.moveToNext()){
                buffer.append("Name :"+res.getString(0)+"\n");
                buffer.append("Contact :"+res.getString(1)+"\n");
                buffer.append("Date of Birth :"+res.getString(2)+"\n\n");
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
            builder.setCancelable(true);
            builder.setTitle("User Entries");
            builder.setMessage(buffer.toString());
            builder.show();
        });


        enableBeam();
    }

    private void writeMessage(String message){
        mAsyncOperationCallback = writeUtility -> {

            //  writeUtility.writeTextToTagFromIntent(text,getIntent());
            return writeUtility.writeTextToTagFromIntent(message, getIntent());
        };
        showDialog();
    }



    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (getNfcAdapter() != null) {
            getNfcAdapter().disableForegroundDispatch(this);
        }
    }

    protected String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 8) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        return new NfcMessageUtilityImpl().createText("Message to beam");
    }

    /**
     * Launched when in foreground dispatch mode
     *
     * @param paramIntent
     *         containing found data
     */
    @Override
    public void onNewIntent(final Intent paramIntent) {
        super.onNewIntent(paramIntent);

        if (mAsyncOperationCallback != null && mProgressDialog != null && mProgressDialog.isShowing()) {
            new WriteCallbackNfcAsync(mAsyncUiCallback, mAsyncOperationCallback).executeWriteOperation();
            mAsyncOperationCallback = null;
        } else {
            for (String data : mNfcReadUtility.readFromTagWithMap(paramIntent).values()) {
                Toast.makeText(this, data, Toast.LENGTH_SHORT).show();
            }
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        if (mTask != null) {
            mTask.cancel(true);
            mTask = null;
        }
    }

    public void showDialog() {
        mProgressDialog = new ProgressDialog(SecondActivity.this);
        mProgressDialog.setTitle(R.string.progressdialog_waiting_for_tag);
        mProgressDialog.setMessage("waiting for Nfc tag to update values!");
        mProgressDialog.show();
    }

}