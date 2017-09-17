/*
Name - shashi shekhar
NIT Jamshedpur
NewsX android app developed for API Level 19 or higher
*/
package com.example.shashi.hackerearth;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import static com.example.shashi.hackerearth.R.id.info;
import static com.example.shashi.hackerearth.R.id.parent;

public class MainActivity extends AppCompatActivity {

    private JSONArray completeInfo;
    private JSONObject singleInfo;
    private SQLiteDatabase myDB;
    private String[] checkField = new String[]{"PUBLISHER", "TITLE", "TIMESTAMP", "URL"};
    ArrayList<HashMap<String, String>> infoArray = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> lessInfoArray = new ArrayList<HashMap<String, String>>();
    private int start, end, max_limit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myDB = new FeedReaderDbHelper(this).getWritableDatabase();
        start = 0;
        end = 20;
        max_limit = 1000;
        try {
            String response = new DataProcessActivity(this).execute("http://starlord.hackerearth.com/newsjson").get();

            //Toast.makeText(this,response,Toast.LENGTH_LONG).show();
            if (response != null) {
                makeFront(response);

                ListView listView = (ListView) findViewById(R.id.mylist);
                LayoutInflater inflater = getLayoutInflater();
                ViewGroup footer = (ViewGroup) inflater.inflate(R.layout.listview_footer, listView, false);
                listView.addFooterView(footer, null, false);
                ViewGroup header = (ViewGroup) inflater.inflate(R.layout.listview_header, listView, false);
                listView.addHeaderView(header, null, false);
                makeListView();
            } else {
                //Toast.makeText(this, "Connect to the internet", Toast.LENGTH_SHORT).show();
                boolean isData=getDetails("FeedNews");
                if(isData){

                    ListView listView = (ListView) findViewById(R.id.mylist);
                    LayoutInflater inflater = getLayoutInflater();
                    ViewGroup footer = (ViewGroup) inflater.inflate(R.layout.listview_footer, listView, false);
                    listView.addFooterView(footer, null, false);
                    ViewGroup header = (ViewGroup) inflater.inflate(R.layout.listview_header, listView, false);
                    listView.addHeaderView(header, null, false);
                    makeListView();
                }else{
                    Toast.makeText(this,"Connect to the internet atleast once",Toast.LENGTH_SHORT).show();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if(v.getId()==R.id.mylist){
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            menu.setHeaderTitle("Add to favourite");
            menu.add(Menu.NONE, 0, 0, "Yes");
            menu.add(Menu.NONE, 1, 1, "No");
            
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        String option = item.toString();
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        //Toast.makeText(this,String.valueOf(info.position), Toast.LENGTH_SHORT).show();
        //Toast.makeText(this,option, Toast.LENGTH_SHORT).show();
        if(option.equals("Yes")){
            int pos = info.position-1;
            HashMap<String,String>hm =infoArray.get(pos);
            ContentValues cv = new ContentValues();
            cv.put("ID",String.valueOf(info.position));
            cv.put("TITLE",hm.get("TITLE"));
            cv.put("PUBLISHER",hm.get("PUBLISHER"));
            cv.put("URL",hm.get("URL"));
            cv.put("TIMESTAMP",hm.get("TIMESTAMP"));
            //Toast.makeText(this,hm.get("TITLE"), Toast.LENGTH_SHORT).show();
            boolean rowFound = checkTable("Favourite", "ID", String.valueOf(info.position));
            if (!rowFound) {
                long count=myDB.insert("Favourite",null,cv);
                Log.e("Msg", String.valueOf(count));
                Toast.makeText(this,"added to favourite", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this,"already in favourites", Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    // function to be performed when show more button will be clicked
    public void showMore(View view) {
        ListView listView = (ListView) findViewById(R.id.mylist);
        int i;
        if (start < max_limit) {
            for (i = start; i < end && i < max_limit; i++) {
                lessInfoArray.add(infoArray.get(i));
            }
            start = i;
            end = i + 20;
        } else {
            Toast.makeText(this, "No more news available", Toast.LENGTH_SHORT).show();
        }
        SimpleAdapter simpleAdapter = new SimpleAdapter(this, lessInfoArray, R.layout.activity_main, checkField, new int[]{R.id.tv1, R.id.tv2, R.id.tv3, R.id.tv4});
        listView.setAdapter(simpleAdapter);
        EditText ed = (EditText) findViewById(R.id.filterText);
        ed.setText("");
    }

    //function to display only the favourite news articles
    public void favOnly(View view) {
        Cursor csr;
        ArrayList<HashMap<String, String>> tempinfoArray = new ArrayList<HashMap<String, String>>();
        csr = myDB.rawQuery("select * from Favourite",null);
        if (csr != null){
            csr.moveToFirst();
            int totalRow = csr.getCount();
            while(!csr.isAfterLast())
            {
                int columnCount = csr.getColumnCount();
                HashMap<String, String> hm = new HashMap<String, String>();
                for(int i=0;i<columnCount;i++)
                {
                    String colName = csr.getColumnName(i);
                    String colValue = csr.getString(i);

                    if (Arrays.asList(checkField).contains(colName)) {
                        hm.put(colName, colValue);
                    }

                }
                tempinfoArray.add(hm);
                csr.moveToNext();
            }
            ListView listView=(ListView)findViewById(R.id.mylist);
            SimpleAdapter simpleAdapter = new SimpleAdapter(this, tempinfoArray, R.layout.activity_main, checkField, new int[]{R.id.tv1, R.id.tv2, R.id.tv3, R.id.tv4});
            listView.setAdapter(simpleAdapter);
            Button br = (Button)findViewById(R.id.footer);
            br.setVisibility(View.GONE);
            Button bt = (Button)findViewById(R.id.home);
            bt.setVisibility(View.VISIBLE);
            EditText ed = (EditText) findViewById(R.id.filterText);
            ed.setVisibility(View.GONE);
            Spinner sp=(Spinner)findViewById(R.id.optionChooser);
            sp.setVisibility(View.GONE);
        }
    }
    //function to jump from favorite view to home view
    public void viewHome(View view)
    {

        makeListView();
        Button bt = (Button)findViewById(R.id.home);
        bt.setVisibility(View.GONE);
        EditText ed = (EditText) findViewById(R.id.filterText);
        ed.setVisibility(View.VISIBLE);
        Spinner sp=(Spinner)findViewById(R.id.optionChooser);
        sp.setVisibility(View.VISIBLE);
        Button br = (Button)findViewById(R.id.footer);
        br.setVisibility(View.VISIBLE);
    }
    //function to filter the data
    public void filterData(View view) {
        Toast.makeText(this, "going to filter data", Toast.LENGTH_SHORT).show();
    }

    //action to be performed when a list item will be clicked
    public class ListClickHandler implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
            TextView listText = (TextView) view.findViewById(R.id.tv4);
            try {
                boolean connected = false;
                ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                    //we are connected to a network
                    connected = true;
                }
                if(!connected){
                    Toast.makeText(MainActivity.this,"for full story connect to internet",Toast.LENGTH_SHORT).show();
                }else{
                    String res = new ResponseCode().execute(listText.getText().toString()).get();
                    if (!res.equals("404")) {
                        //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(listText.getText().toString()));
                        Intent browserIntent = new Intent(MainActivity.this, BrowserView.class);
                        browserIntent.putExtra("url", listText.getText().toString());
                        startActivity(browserIntent);
                    } else {
                        Toast.makeText(MainActivity.this, "Page not found", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

    }

    //function to make the view
    public void makeFront(String response) throws JSONException {
        ContentValues cv = new ContentValues();
        completeInfo = new JSONArray(response);
        for (int i = 0; i < completeInfo.length(); i++) {
            singleInfo = completeInfo.getJSONObject(i);
            HashMap<String, String> hm = new HashMap<String, String>();
            Iterator<String> key = singleInfo.keys();
            while (key.hasNext()) {
                String keyTerm = key.next();
                if (Arrays.asList(checkField).contains(keyTerm)) {
                    String keyData = singleInfo.getString(keyTerm);
                    cv.put(keyTerm, keyData);
                    if (keyTerm.equals("TIMESTAMP")) {
                        Date date = new Date(Long.parseLong(keyData));
                        DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                        format.setTimeZone(TimeZone.getTimeZone("Etc/IST"));
                        String formatted = format.format(date);
                        keyData = formatted;
                    }
                    hm.put(keyTerm, keyData);
                }

            }
            infoArray.add(hm);
            boolean rowFound = checkTable("FeedNews", "ID", singleInfo.getString("ID"));
            if (!rowFound) {
                    long count=myDB.insert("FeedNews",null,cv);
                    Log.e("Msg", String.valueOf(count));
            }
            //hm.clear();
        }

    }

    //function to check whether there is record available in the table or not
    public boolean checkTable(String table, String column_name, String column_value) {
        String[] projection = {
                column_name,
        };

        String selection = column_name + " = ?";
        String[] selectionArgs = {column_value};

        Cursor cursor = myDB.query(
                table,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );
        return (cursor.getCount() > 0);
    }
    //function to get detail from the table
    public boolean getDetails(String table){
        Cursor csr;
        csr = myDB.rawQuery("select * from "+table,null);
        if (csr != null){
            csr.moveToFirst();
            int totalRow = csr.getCount();
            while(!csr.isAfterLast())
            {
                int columnCount = csr.getColumnCount();
                HashMap<String, String> hm = new HashMap<String, String>();
                for(int i=0;i<columnCount;i++)
                {
                    String colName = csr.getColumnName(i);
                    String colValue = csr.getString(i);

                    if (Arrays.asList(checkField).contains(colName)) {

                        if (colName.equals("TIMESTAMP")) {
                            Date date = new Date(Long.parseLong(colValue));
                            DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                            format.setTimeZone(TimeZone.getTimeZone("Etc/IST"));
                            String formatted = format.format(date);
                            colValue = formatted;
                        }
                        hm.put(colName, colValue);
                    }

                }
                infoArray.add(hm);
                csr.moveToNext();
            }
            return true;
        }
        else{
            return false;
        }
    }
    //function to implement the listview
    public void makeListView()
    {
        int i;
        for (i = start; i < end; i++) {
            lessInfoArray.add(infoArray.get(i));
        }
        start = i;
        end = i + 20;
        ListView listView = (ListView) findViewById(R.id.mylist);

        //listView.setAdapter(new ArrayAdapter(this,R.layout.activity_main,R.id.textview,countryArray));
        SimpleAdapter simpleAdapter = new SimpleAdapter(this, lessInfoArray, R.layout.activity_main, checkField, new int[]{R.id.tv1, R.id.tv2, R.id.tv3, R.id.tv4});
        listView.setAdapter(simpleAdapter);
        listView.setOnItemClickListener(new ListClickHandler());
        registerForContextMenu(listView);
        EditText ed = (EditText) findViewById(R.id.filterText);
        ed.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Toast.makeText(MainActivity.this,s,Toast.LENGTH_SHORT).show();
                ArrayList<HashMap<String, String>> tempInfoArray = new ArrayList<HashMap<String, String>>();
                Spinner sp = (Spinner) findViewById(R.id.optionChooser);
                String key = sp.getSelectedItem().toString();
                int i;
                for (i = 0; i < end; i++) {
                    if (infoArray.get(i).get(key.toUpperCase()).toLowerCase().contains(s.toString().toLowerCase())) {
                        tempInfoArray.add(infoArray.get(i));
                    }
                }
                ListView listView = (ListView) findViewById(R.id.mylist);
                SimpleAdapter simpleAdapter = new SimpleAdapter(MainActivity.this, tempInfoArray, R.layout.activity_main, checkField, new int[]{R.id.tv1, R.id.tv2, R.id.tv3, R.id.tv4});
                listView.setAdapter(simpleAdapter);
                findViewById(R.id.filterText).requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}
class ResponseCode extends AsyncTask<String,String,String> {



    @Override
    protected String doInBackground(String...args) {
        try {
            URL url = new URL(args[0]);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                return String.valueOf(urlConnection.getResponseCode());
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
    }
}
class FeedReaderDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FeedReader.db";
    private static final String SQL_CREATE_ENTRIES ="CREATE TABLE FeedNews (ID TEXT PRIMARY KEY,TITLE TEXT,URL TEXT,PUBLISHER TEXT,CATEGORY TEXT,HOSTNAME TEXT,TIMESTAMP TEXT)";
    private static final String SQL_DELETE_ENTRIES ="DROP TABLE IF EXISTS FeedNews";
    private static final String SQL_CREATE_ENTRIES2="CREATE TABLE Favourite(ID TEXT PRIMARY KEY,TITLE TEXT,PUBLISHER TEXT,URL TEXT,TIMESTAMP TEXT)";
    private static final String SQL_DELETE_ENTRIES2="DROP TABLE IF EXISTS Favourite";
    public FeedReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES2);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_DELETE_ENTRIES2);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}