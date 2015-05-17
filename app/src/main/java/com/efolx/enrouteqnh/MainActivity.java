package com.efolx.enrouteqnh;

import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int ressourceId = R.raw.airports;
        InputStream inputStream = getResources().openRawResource(ressourceId);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        ArrayList<Airport> list = new ArrayList<Airport>();
        try {
            String zeile = reader.readLine();
            zeile = reader.readLine();
            while(zeile!=null){
                String[] columns = zeile.split(";");
                Location location = new Location("");
                double longitude = Double.valueOf(columns[2]);
                double latitude = Double.valueOf(columns[3]);
                location.setLatitude(latitude);
                location.setLongitude(longitude);
                int elevation = Integer.valueOf(columns[4]);
                Airport airport = new Airport(columns[0],columns[1],location, elevation);
                list.add(airport);
                zeile = reader.readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.sort(list);
        Iterator<Airport> iterator = list.iterator();
        String[] apArray = new String[list.size()];
        int i =0;
        while(iterator.hasNext()){
            Airport next = iterator.next();
            apArray[i] = next.getId();
        }
        View view = findViewById(R.id.listView);
        ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,apArray);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
