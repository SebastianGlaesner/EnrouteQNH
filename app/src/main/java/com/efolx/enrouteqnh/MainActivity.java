package com.efolx.enrouteqnh;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.Switch;
import android.widget.TextView;

import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;


public class MainActivity extends ActionBarActivity implements AdapterView.OnItemClickListener, CompoundButton.OnCheckedChangeListener {

    private static final float METERS_TO_MILES = 0.000621371192f;
    private static final String PREFS_NAME = "EnrouteQNHPreferences";
    private static final String LAST_AIRPORT = "lastairport";
    private static String AUTO_MODE ="automode";
    private ArrayList<Airport> airports;
    private LocationManager locationManager;
    private HashMap<String, Airport> airportMap;
    private Airport selectedAirport;
    private boolean autoMode;
    private Location location;
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        updateLocation();

        loadAirports();
        AutoCompleteTextView tv = (AutoCompleteTextView) findViewById(R.id.airportView);
        ArrayAdapter<Airport> adapter = new ArrayAdapter<Airport>(
                this, android.R.layout.simple_dropdown_item_1line, airports);
        tv.setOnItemClickListener(this);
        tv.setAdapter(adapter);
        settings = getSharedPreferences(PREFS_NAME, 0);
        autoMode = settings.getBoolean(AUTO_MODE, true);
        Switch autoSwitch = (Switch)findViewById(R.id.autoSwitch);
        autoSwitch.setChecked(autoMode);
        autoSwitch.setOnCheckedChangeListener(this);
        updateMode();
    }

    private void updateLocation() {
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, false);
        location = locationManager.getLastKnownLocation(provider);
    }

    private void loadAirports() {
        int ressourceId = R.raw.airports;
        InputStream inputStream = getResources().openRawResource(ressourceId);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        airports = new ArrayList<Airport>();
        airportMap = new HashMap<String, Airport>();
        try {
            String zeile = reader.readLine();
            zeile = reader.readLine();
            while(zeile!=null){
                String[] columns = zeile.split(";");
                if(columns.length==5) {
                    String idString = columns[0];
                    String nameString = columns[1];
                    String longString = columns[3];
                    String latString = columns[2];
                    String elevationString = columns[4];
                    Airport airport = getAirport(idString, nameString, longString, latString, elevationString);
                    airports.add(airport);
                    airportMap.put(airport.toString(),airport);
                }
                zeile = reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Airport getAirport(String idString, String nameString, String longString, String latString, String elevationString) throws ParseException {
        NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
        double longitude = format.parse(longString).doubleValue();
        double latitude = format.parse(latString).doubleValue();
        Location location = new Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        int elevation = Integer.valueOf(elevationString);

        return new Airport(idString, nameString, location, elevation);
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String selection = ((TextView)view).getText().toString();
        selectedAirport = airportMap.get(selection);
        settings.edit().putString(LAST_AIRPORT,selectedAirport.toString()).apply();
        AutoCompleteTextView tv = (AutoCompleteTextView) findViewById(R.id.airportView);
        tv.clearFocus();
        updateAirportView();
    }

    private void updateAirportView() {
        if(selectedAirport!=null){
            findViewById(R.id.airportBox).setVisibility(View.VISIBLE);
            NumberFormat df = DecimalFormat.getInstance(Locale.getDefault());
            df.setMaximumFractionDigits(1);
            TextView distanceTextView = (TextView) findViewById(R.id.distanceTextView);
            float distanceInMeters = location.distanceTo(selectedAirport.getLocation());
            float distanceInMiles = distanceInMeters * METERS_TO_MILES;
            distanceTextView.setText(df.format(distanceInMiles));
            TextView bearingTextView = (TextView) findViewById(R.id.bearingTextView);
            float bearing = location.bearingTo(selectedAirport.getLocation());
            bearingTextView.setText(df.format(bearing));
            TextView elevationTextView = (TextView) findViewById(R.id.elevationTextView);
            elevationTextView.setText(Integer.toString(selectedAirport.getElevation()));

            loadQNH();

        }else{
            findViewById(R.id.airportBox).setVisibility(View.INVISIBLE);
        }
    }

    private void loadQNH() {
        TextView qnhTextView = (TextView) findViewById(R.id.qnhTextView);
        qnhTextView.setText("...");
        qnhTextView.setTextColor(Color.LTGRAY);
        FTPTask ftpTask = new FTPTask(qnhTextView);
        ftpTask.execute(new String[] {selectedAirport.getId()});

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        autoMode=isChecked;
        SharedPreferences.Editor edit = settings.edit();
        edit.putBoolean(AUTO_MODE, isChecked);
        edit.apply();
        updateMode();
    }

    private void updateMode() {
        AutoCompleteTextView tv = (AutoCompleteTextView) findViewById(R.id.airportView);
        if(autoMode){
            tv.setEnabled(false);
            selectClosestAirport();
        }else{
            selectedAirport = airportMap.get(settings.getString(LAST_AIRPORT,null));
            if(selectedAirport!=null){//should always be true
                tv.setText(selectedAirport.toString());
                tv.setEnabled(true);
            }
            tv.setEnabled(true);
        }
    }

    private void selectClosestAirport() {
        Collections.sort(airports,new DistanceComparator(location));
        selectedAirport=airports.get(0);
        AutoCompleteTextView tv = (AutoCompleteTextView) findViewById(R.id.airportView);
        ArrayAdapter<Airport> adapter = (ArrayAdapter<Airport>) tv.getAdapter();
        tv.setAdapter(null);
        tv.setText(selectedAirport.toString());
        tv.setAdapter(adapter);
        updateAirportView();
    }
}
