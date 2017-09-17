package com.example.shashi.hackerearth;

/**
 * Created by shashi on 8/12/2017.
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Created by SS054006 on 6/27/2017.
 */

public class DataProcessActivity extends AsyncTask<String,String,String> {
    String answer=null;
    private ProgressDialog Dialog;
    private Context context;
    ProgressDialog pDialog;
    public DataProcessActivity(Context context)
    {
        //Toast.makeText(context,"downloading data",Toast.LENGTH_SHORT).show();
        this.context=context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog = new ProgressDialog(context,
                ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        pDialog.setTitle("Please wait");
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setMessage("Loading data...");
        pDialog.setIndeterminate(true);
        pDialog.setCancelable(false);
        pDialog.setInverseBackgroundForced(true);
        pDialog.show();
    }

    @Override
    protected String doInBackground(String...args) {
        try {
            URL url = new URL(args[0]);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(100000);
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                Log.e("msg",stringBuilder.toString());
                return stringBuilder.toString();
            }
            finally{
                urlConnection.disconnect();
            }
        }
        catch(Exception e) {
            Log.e("ERROR", e.getMessage(), e);
            return null;
        }
        //return answer;
    }

    @Override
    protected void onPostExecute(String s) {
        //Log.e("message",s);
        pDialog.hide();
    }
}
