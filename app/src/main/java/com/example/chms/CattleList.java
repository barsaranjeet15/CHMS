package com.example.chms;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class CattleList extends AppCompatActivity {

    public static String [] cattleImages = null;
    public static String [] cattleIds = null;
    public static String [] cattleNextHeatDates = null;
    public static String[] cattleLastHeatDates = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cattle_list);

        SharedPreferences sharedPreferences = getSharedPreferences("CHMS",MODE_PRIVATE);
        boolean isRegistered = sharedPreferences.getBoolean("isRegistered",false);
        if(!isRegistered)
        {
            Intent intent = new Intent(this, OwnerRegistration.class);
            startActivity(intent);
        }

        CHMSDatabase dbHelper = new CHMSDatabase(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursorCattles = db.rawQuery("SELECT * FROM cattle_profile",new String[]{});
        cattleImages = new String[cursorCattles.getCount()];
        cattleIds = new String[cursorCattles.getCount()];
        cattleNextHeatDates = new String[cursorCattles.getCount()];
        cattleLastHeatDates = new String[cursorCattles.getCount()];
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        if(cursorCattles.getCount() > 0)
        {
            cursorCattles.moveToFirst();
            for(int i=0;!cursorCattles.isAfterLast();i++)
            {
                cattleImages[i] = cursorCattles.getString(cursorCattles.getColumnIndex("cattle_image"));
                Toast.makeText(this, ""+cattleImages[i], Toast.LENGTH_SHORT).show();
                cattleIds[i] = "Cattle id: "+cursorCattles.getString(cursorCattles.getColumnIndex("cuin"));
                //cattleNextHeatDates[i] = "Next heat on: "+cursorCattles.getString(cursorCattles.getColumnIndex("last_heat_on"));
                String lastHeatOn = cursorCattles.getString(cursorCattles.getColumnIndex("last_heat_on"));
                try {
                    Calendar c = Calendar.getInstance();
                    Date date = simpleDateFormat.parse(lastHeatOn);
                    c.setTime(date);
                    c.add(Calendar.DATE, 21);
                    Date nextHeatDate = c.getTime();
                    String nextHeatOn = simpleDateFormat.format(nextHeatDate);
                    cattleNextHeatDates[i] = nextHeatOn;
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                cattleLastHeatDates[i] = lastHeatOn;

                cursorCattles.moveToNext();
            }

            for(int i=0;i<cattleIds.length;i++)
            {
                String currentNextHeat = cattleNextHeatDates[i];
                Date currentNextHeatDate = null;

                long currentMills = 0;
                try {
                    currentNextHeatDate = simpleDateFormat.parse(currentNextHeat);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                currentMills = currentNextHeatDate.getTime();
                for(int j=i;j<cattleIds.length;j++)
                {
                    String checkNextHeat = cattleNextHeatDates[j];
                    Date checkNextHeatDate = null;
                    try {
                        checkNextHeatDate = simpleDateFormat.parse(checkNextHeat);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    long checkMills = checkNextHeatDate.getTime();
                    if(checkMills < currentMills)
                    {
                        String tmpCattleId = cattleIds[i];
                        cattleIds[i] = cattleIds[j];
                        cattleIds[j] = tmpCattleId;

                        String tmpNextHeatDate = cattleNextHeatDates[i];
                        cattleNextHeatDates[i] = cattleNextHeatDates[j];
                        cattleNextHeatDates[j] = tmpNextHeatDate;

                        String tmpCattleImage = cattleImages[i];
                        cattleImages[i] = cattleImages[j];
                        cattleImages[j] = tmpCattleImage;
                    }
                }
                for(i=0;i<cattleNextHeatDates.length;i++)
                {
                    cattleNextHeatDates[i] = "Next heat on: "+ cattleNextHeatDates[i];
                }
            }


            CattleListAdapter adapter = new CattleListAdapter(this,cattleImages,cattleIds,cattleNextHeatDates);
            ListView listView = (ListView)findViewById(R.id.cattleList);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent i = new Intent(getApplicationContext(),CattleProfile.class);
                    startActivity(i);
                }
            });

        }

    }
    public void addCattle(View v)
    {
        Intent i = new Intent(this,AddCattle.class);
        startActivity(i);
    }
}