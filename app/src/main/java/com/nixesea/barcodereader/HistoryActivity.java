package com.nixesea.barcodereader;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    ListView lView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        lView = findViewById(R.id.listView);
        SQLiteDatabase myDB =
                openOrCreateDatabase("moon.db", MODE_PRIVATE, null);
        Log.i("MY", "t");
        Cursor myCursor =
                myDB.rawQuery("select URI, time from user", null);
        ArrayList<String> list = new ArrayList<>();
        Log.i("MY", "after arraylist");

        String info = "";
        while (myCursor.moveToNext()) {
            Log.i("MY", "while");
            info = myCursor.getString(0) + myCursor.getString(1);
            list.add(info);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, list);

        lView.setAdapter(adapter);

        lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position,
                                    long id) {
                String s = ((String) ((TextView) itemClicked).getText());
                if (s.contains("http")){
                    Uri address = Uri.parse(s);
                    Intent intent = new Intent(Intent.ACTION_VIEW, address);
                    startActivity(intent);
                }else {
                    Toast.makeText(getApplicationContext(),"It's not a URI",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



}
