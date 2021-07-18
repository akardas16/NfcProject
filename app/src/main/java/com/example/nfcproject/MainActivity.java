package com.example.nfcproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NfcEvent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;



import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import be.appfoundry.nfclibrary.activities.NfcActivity;
import be.appfoundry.nfclibrary.exceptions.InsufficientCapacityException;
import be.appfoundry.nfclibrary.exceptions.ReadOnlyTagException;
import be.appfoundry.nfclibrary.exceptions.TagNotPresentException;
import be.appfoundry.nfclibrary.tasks.interfaces.AsyncOperationCallback;
import be.appfoundry.nfclibrary.tasks.interfaces.AsyncUiCallback;
import be.appfoundry.nfclibrary.utilities.async.WriteCallbackNfcAsync;
import be.appfoundry.nfclibrary.utilities.interfaces.NfcReadUtility;
import be.appfoundry.nfclibrary.utilities.interfaces.NfcWriteUtility;
import be.appfoundry.nfclibrary.utilities.sync.NfcMessageUtilityImpl;
import be.appfoundry.nfclibrary.utilities.sync.NfcReadUtilityImpl;


public class MainActivity extends NfcActivity {

    private static final String TAG = "helloman";
    Button button1,button2;

    // essential URL structure is built using constants
    Double exchangeRate=null;



    NfcReadUtility mNfcReadUtility = new NfcReadUtilityImpl();
    ProgressDialog mProgressDialog;
    SqliteHelper sqlHelper;
    ArrayList<String> myList;


    AsyncUiCallback mAsyncUiCallback = new AsyncUiCallback() {
        @Override
        public void callbackWithReturnValue(Boolean result) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            if (result) {
                Toast.makeText(MainActivity.this, "values written successful !", Toast.LENGTH_LONG).show();
                for (String messages:getNfcMessages()){
                    myList = new ArrayList<>(Arrays.asList(messages.split(",")));
                    //text1.setText(myList.get(0));
                    //text2.setText(myList.get(1));
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
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    AsyncOperationCallback mAsyncOperationCallback;
    private AsyncTask<Object, Void, Boolean> mTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button1=findViewById(R.id.buttonID1);
        button2=findViewById(R.id.buttonID2);
        Button resetBtn=findViewById(R.id.resetID);



        new JsonTask().execute("https://openexchangerates.org/api/latest.json?app_id=2b2741bac91145038d7bcba4af81aa69&symbols=EUR");

        writeMessage("10,20");
        button1.setOnClickListener(v -> {

            if (myList!=null && myList.size()>=2){
                int a =Integer.parseInt(myList.get(0))-2;
                int b =Integer.parseInt(myList.get(1));
                String BalanceNo="1";
                String OldBalance_USD=myList.get(0);
                String NewBalance_USD= String.valueOf(Integer.parseInt(myList.get(0))-2);
                String NewBalance_EUR=String.valueOf(new DecimalFormat("##.#####").format(Double.parseDouble(NewBalance_USD)*exchangeRate));
                String ExchangeRate=String.valueOf(exchangeRate);
                Intent intent=new Intent(MainActivity.this,SecondActivity.class);
                intent.putExtra("a",String.valueOf(a));
                intent.putExtra("b",String.valueOf(b));
                intent.putExtra("OldBalance_USD",OldBalance_USD);
                intent.putExtra("NewBalance_USD",NewBalance_USD);
                intent.putExtra("NewBalance_EUR",NewBalance_EUR);
                intent.putExtra("ExchangeRate",ExchangeRate);
                intent.putExtra("BalanceNo",BalanceNo);
                startActivity(intent);
            }


        });

        button2.setOnClickListener(v -> {
            if (myList!=null && myList.size()>=2){
                int a =Integer.parseInt(myList.get(0));
                int b =Integer.parseInt(myList.get(1))-5;
                String OldBalance_USD=myList.get(1);
                String BalanceNo="2";
                String NewBalance_USD= String.valueOf(Integer.parseInt(myList.get(1))-5);
                String NewBalance_EUR=String.valueOf(new DecimalFormat("##.#####").format(Double.parseDouble(NewBalance_USD)*exchangeRate));
                String ExchangeRate=String.valueOf(exchangeRate);
                Intent intent=new Intent(MainActivity.this,SecondActivity.class);
                intent.putExtra("BalanceNo",BalanceNo);
                intent.putExtra("a",String.valueOf(a));
                intent.putExtra("b",String.valueOf(b));
                intent.putExtra("OldBalance_USD",OldBalance_USD);
                intent.putExtra("NewBalance_USD",NewBalance_USD);
                intent.putExtra("NewBalance_EUR",NewBalance_EUR);
                intent.putExtra("ExchangeRate",ExchangeRate);
                startActivity(intent);
            }
        });

        resetBtn.setOnClickListener(v -> {
            int a =10;
            int b =20;
            Intent intent=new Intent(MainActivity.this,SecondActivity.class);
            intent.putExtra("a",String.valueOf(a));
            intent.putExtra("b",String.valueOf(b));
            startActivity(intent);

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
        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setTitle(R.string.progressdialog_waiting_for_tag);
        mProgressDialog.setMessage("waiting for Nfc tag to write initial values!");
        mProgressDialog.show();
    }



    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
           // Log.i("ddddffff", "onPostExecute: "+result);
            try {
                JSONObject jsonObject=new JSONObject(result);

               // Log.i("zzzzz", "onPostExecute: "+   jsonObject.get("rates").toString());
                JSONObject object=new JSONObject(jsonObject.get("rates").toString());

                Log.i("zzzzz", "onPostExecute: "+ object.get("EUR").toString());
                exchangeRate= Double.valueOf(new DecimalFormat("#.#####").format(1/object.getDouble("EUR")));
                Log.i("vvvffd", "onPostExecute: "+String.valueOf(exchangeRate));
            } catch (JSONException e) {
                e.printStackTrace();
               // Log.i("zzzzz", "onPostExecute: "+ e.getMessage());
            }


        }
    }




}
