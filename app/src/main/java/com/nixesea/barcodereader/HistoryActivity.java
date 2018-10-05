package com.nixesea.barcodereader;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    SQLiteDatabase myDB;
    final static String name_DB = "QRCode_history.db";
    ListView lView;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        myDB.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        lView = findViewById(R.id.listView);
        myDB = openOrCreateDatabase(name_DB, MODE_PRIVATE, null);


        try{
            Cursor myCursor =
                    myDB.rawQuery("select id, URI, time from user", null);
            ArrayList<String> list = new ArrayList<>();

            while (myCursor.moveToNext()) {
                String info =  myCursor.getString(0) + myCursor.getString(1) + myCursor.getString(2);
                list.add(info);
            }

            final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, list);
            lView.setAdapter(adapter);
//            Toast.makeText(getApplicationContext(),"names = " + Arrays.toString(myCursor.getColumnNames()),Toast.LENGTH_LONG).show();


            lView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    String[] s = ((String) ((TextView) view).getText()).split("\n");

                    int i = myDB.delete("user","id='" + s[0] + "'", null);

//                    myDB.delete("user","time = " + s[s.length - 1],null);
                    Toast.makeText(getApplicationContext(),"i = " + i,Toast.LENGTH_LONG).show();

                    return false;
                }
            });

            lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View itemClicked, int position,
                                        long id) {
                    String[] s = ((String) ((TextView) itemClicked).getText()).split("\n");

//                    String[] subStr;
//                    subStr = s.split("\n");
//                    for (String aSubStr : subStr) {
                    String aSubStr = s[1];
//                        if(validateUrl(aSubStr)){
                            Uri address;
                            address = Uri.parse(aSubStr);
                            if(!aSubStr.startsWith("http") && !aSubStr.startsWith("https")){
                                address = Uri.parse("http://" + aSubStr);
//                            }
                            Intent intent = new Intent(Intent.ACTION_VIEW, address);
                            startActivity(intent);
//                            break;
//                        }else {
//                            Toast.makeText(getApplicationContext(),"Это не URL-ссылка",Toast.LENGTH_SHORT).show();
//                        }
                    }
                }
            });
        }catch(Exception e){
            Toast.makeText(getApplicationContext(),"Нет записей в истории",Toast.LENGTH_SHORT).show();
        }
    }

    public boolean validateUrl(String address){
        return android.util.Patterns.WEB_URL.matcher(address).matches();
    }
}
