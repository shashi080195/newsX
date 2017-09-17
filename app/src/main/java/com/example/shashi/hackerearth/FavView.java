package com.example.shashi.hackerearth;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class FavView extends AppCompatActivity {

    private SQLiteDatabase myDB;
    ArrayList<HashMap<String, String>> infoArray = new ArrayList<HashMap<String, String>>();
    private String[] checkField = new String[]{"PUBLISHER", "TITLE", "TIMESTAMP", "URL"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fav_view);
        myDB = new FeedReaderDbHelper(this).getWritableDatabase();
        boolean isData=getDetails("Favourite");
        if(isData)
        {
            makeListView();
        }

    }
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
    public void makeListView()
    {

        ListView listView = (ListView) findViewById(R.id.myfavlist);
        LayoutInflater inflater = getLayoutInflater();

        //listView.setAdapter(new ArrayAdapter(this,R.layout.activity_main,R.id.textview,countryArray));
        SimpleAdapter simpleAdapter = new SimpleAdapter(this,infoArray, R.layout.activity_fav_view, checkField, new int[]{R.id.fv1, R.id.fv2, R.id.fv3, R.id.fv4});
        listView.setAdapter(simpleAdapter);


    }
}

