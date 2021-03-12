This is the Custom Navigation Drawer Activity..
  package com.e.weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.ui.AppBarConfiguration;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

import static android.provider.UserDictionary.Words.APP_ID;

public class FourthActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;

    final String App_ID="c8cae2a8362fff414a47f345c37607e5";
    final String WEATHER_URL="https://openweathermap.org/data/2.5/weather";
    final long MIN_TIME=5000;
    final String MIN_DISTANCE="1000";
    final String REQUEST_CODE="101";

    String Location_Provider= LocationManager.GPS_PROVIDER;

    TextView NameOfCity,weatherState,Thermo;
    ImageView mweatherIcon;

    RelativeLayout mCityFinder;

    LocationManager mLocationManager;
    LocationListener mLocationListener;
    private Map<String, List<String>> params;
    private Object AsyncHttpClient;

    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fourth);

        //Assignment of Variables
        DrawerLayout drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawer.closeDrawers();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        weatherState = findViewById(R.id.weatherCondition);
        Thermo=findViewById(R.id.Temp);
        mweatherIcon=findViewById(R.id.weatherIcon);
        mCityFinder=findViewById(R.id.citysearch);
        NameOfCity=findViewById(R.id.cityName);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawer,toolbar,R.id.open_drawer,R.id.close_drawer);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        GraphView graphView = (GraphView) findViewById(R.id.graphView);

        LineGraphSeries<DataPoint> series=new LineGraphSeries<DataPoint>(new DataPoint[]{
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 4),
                new DataPoint(3, 6),
                new DataPoint(4, 8),
                new DataPoint(5, 10),

        });
        graphView.addSeries(series);


        mCityFinder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(FourthActivity.this,cityFinder.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.nav_city:
                getSupportFragmentManager().beginTransaction().replace(R.id.Fragment_container,new CityFragment()).commit();
                break;
            case R.id.nav_Temperature:
                getSupportFragmentManager().beginTransaction().replace(R.id.Fragment_container,new TemperatureFragment()).commit();
                break;
            case R.id.nav_Time:
                getSupportFragmentManager().beginTransaction().replace(R.id.Fragment_container,new TimeZoneFragment()).commit();
                break;
        }
        //drawer.closeDrawers();
        return true;
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }

   /* @Override
    public void onResume() {
        super.onResume();
        getWeatherForCurrentLocation();
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        Intent mIntent = getIntent();
        String city = mIntent.getStringExtra("city");
        if(city!=null) {
            getWeatherForNewCity(city);
        }
        else {
            getWeatherForCurrentLocation();
        }
    }

    private void getWeatherForNewCity(String city) {
        RequestParams params = new RequestParams();
        params.put("q",city);
        params.put("appid",APP_ID);
        letsdoSomeNetworking(params);

    }

    private void getWeatherForCurrentLocation() {
        mLocationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mLocationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                String Latitude=String.valueOf(location.getLatitude());
                String Longitude=String.valueOf(location.getLongitude());

                RequestParams params = new RequestParams();
                params.put("lat",Latitude);
                params.put("lon",Longitude);
                params.put("appid",App_ID);
                letsdoSomeNetworking(params);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                //not able to get location
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Integer.parseInt(REQUEST_CODE));
            return;
        }

        mLocationManager.requestLocationUpdates(Location_Provider,MIN_TIME, Float.parseFloat(MIN_DISTANCE),mLocationListener);
    }


    public void onRequestPermissionsResult(String requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(Integer.parseInt(requestCode), permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(FourthActivity.this, "Locationget Succesfully",Toast.LENGTH_SHORT).show();
                getWeatherForCurrentLocation();
            }
            else {

            }
        }
    }

    private void letsdoSomeNetworking(RequestParams params)
    {
        com.loopj.android.http.AsyncHttpClient client = new AsyncHttpClient();
        RequestHandle data_get_success = client.get(WEATHER_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Toast.makeText(FourthActivity.this, "Data Get Success", Toast.LENGTH_SHORT).show();

                weatherData weatherD = weatherData.fromJson(response);
                updateUI(weatherD);
                //super.onSuccess(statusCode, headers, response);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                //super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    private void updateUI(weatherData weather) {
        Thermo.setText(weather.getmTemp());
        NameOfCity.setText(weather.getMcity());
        weatherState.setText(weather.getMweatherType());
        int resourceID=getResources().getIdentifier(weather.getMicon(),"drawable",getPackageName());
        mweatherIcon.setImageResource(resourceID);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mLocationManager!=null)
        {
            mLocationManager.removeUpdates(mLocationListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.third, menu);
        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

}
