package com.example.weatherpremiumapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout homeRL;
    private ProgressBar loadingPB;
    private TextView cityNameTv,temperatureTv,conditionTv;
    private RecyclerView weatherRV;
    private TextInputEditText cityEdit;
    private ImageView backIV,searchIv;
    private com.airbnb.lottie.LottieAnimationView iconIV;
    private ArrayList<WeatherRVModel> weatherRVModelArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE =1;
    private String cityName;
    String path="";
   int counter=0;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_main);
        loadingPB = findViewById(R.id.idPBLoading);
        cityNameTv = findViewById(R.id.idTVCityName);
        temperatureTv = findViewById(R.id.idTVTemperature);
        conditionTv = findViewById(R.id.idTVCondition);
        weatherRV = findViewById(R.id.idRvWeather);
        cityEdit = findViewById(R.id.idEdtCity);
        backIV = findViewById(R.id.idIVBack);
        iconIV = findViewById(R.id.idIVIcon);
        homeRL = findViewById(R.id.idRLHome);
        searchIv = findViewById(R.id.idIVSearch);
        weatherRVModelArrayList = new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(this,weatherRVModelArrayList);
        weatherRV.setAdapter(weatherRVAdapter);
        temperatureTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(counter%2==0) {
                    String gettemp = temperatureTv.getText().toString().substring(0,temperatureTv.getText().toString().length()-2);
                    float fahreneit = (Float.parseFloat(gettemp) * 9 / 5) + 32;
                    temperatureTv.setText(String.format("%.1f", fahreneit)+"°F");
                    counter++;
                }
                else
                {
                    String gettemp = temperatureTv.getText().toString().substring(0,temperatureTv.getText().toString().length()-2);
                    double celsius = (5/9.00)*(Float.parseFloat(gettemp)-32);
                    temperatureTv.setText(String.format("%.1f", celsius)+"°C");
                    counter++;
                }
            }
        });


        locationManager =(LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{ Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

//
//       Toast.makeText(this, "Reached Location", Toast.LENGTH_SHORT).show();

        if(location!= null){
            cityName = getCityName(location.getLongitude(), location.getLatitude());
//        Toast.makeText(this, cityName, Toast.LENGTH_SHORT).show();
            getWeatherInfo(cityName);
        }

        searchIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counter=0;
                String city= Objects.requireNonNull(cityEdit.getText()).toString();
                if (city.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please enter city name", Toast.LENGTH_SHORT).show();
                }else{
                    cityNameTv.setText(cityName);
                    getWeatherInfo(city);
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_CODE){
            if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Please provide the permissions", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double longitude, double latitude){
//       cityName = "Not Found";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(latitude,longitude,10);
            for (Address adr : addresses){
                if (adr!=null){
                    String city= adr.getLocality();
                    System.out.println(city);
                    if (city!=null && !city.equals("")){
                        return city;
                    }else {
                        Log.d("TAG","CITY NOT FOUND");
                        Toast.makeText(this, "User City Not Found..", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return cityName;
    }
    private void getWeatherInfo(String cityName){
        String url= "https://api.weatherapi.com/v1/forecast.json?key=9698bd600d894808a8441627221812&q=" + cityName + "&days=1&aqi=yes&alerts=yes";
        System.out.println(url);
//        Toast.makeText(this, "flag"+cityName, Toast.LENGTH_SHORT).show();
        cityNameTv.setText(cityName);
//        Toast.makeText(this, cityName, Toast.LENGTH_SHORT).show();
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.GONE);
                homeRL.setVisibility(View.VISIBLE);
//                Toast.makeText(MainActivity.this, "aa gya yahan tak", Toast.LENGTH_SHORT).show();
                weatherRVModelArrayList.clear();

                try {
//                    Toast.makeText(MainActivity.this, "hit hit hit", Toast.LENGTH_SHORT).show();
                    String temperature = response.getJSONObject("current").getString("temp_c");
                    temperatureTv.setText(temperature + "°C");
                    int isDay = response.getJSONObject("current").getInt("is_day");
                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
//                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
//                    Picasso.get().load("https:".concat(conditionIcon)).into(iconIV);
                    conditionTv.setText(condition);
                    if(condition.equals("Overcast")) {
                        iconIV.setAnimation(R.raw.oo);
                        iconIV.resumeAnimation();
                    }
                    else if(condition.equals("Sunny")) {
                        iconIV.setAnimation(R.raw.ss);
                        iconIV.resumeAnimation();
                    }
                    else if(condition.equals("Mist")) {
                        iconIV.setAnimation(R.raw.dd);
                        iconIV.resumeAnimation();
                    }
                    else {
                        iconIV.setAnimation(R.raw.cc);

                        iconIV.resumeAnimation();
                    }
                    if (isDay==1){
                        //Morning
                        if(condition.equals("Sunny"))
                            path="https://images.unsplash.com/photo-1586078074298-05dca4848695?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8MjR8fHN1bm55fGVufDB8fDB8fA%3D%3D&auto=format&fit=crop&w=600&q=60";
                        else if(condition.equals("Mist"))
                            path="https://images.unsplash.com/photo-1485236715568-ddc5ee6ca227?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8Mnx8bWlzdHxlbnwwfHwwfHw%3D&auto=format&fit=crop&w=600&q=60";
                        else if(condition.equals("Overcast"))
                         path="https://images.unsplash.com/photo-1441920007991-ae5ddfade6d1?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8MTZ8fG92ZXJjYXN0JTIwZGF5fGVufDB8fDB8fA%3D%3D&auto=format&fit=crop&w=600&q=60";
                        else if(condition.equals("Partly cloudy"))
                            path="https://images.unsplash.com/photo-1445297983845-454043d4eef4?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8Mnx8cGFydGx5JTIwY2xvdWR5fGVufDB8fDB8fA%3D%3D&auto=format&fit=crop&w=600&q=60";
                        else if(condition.equals("Clear"))
                            path="https://images.unsplash.com/photo-1503435538086-21e860401a47?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8Mnx8Y2xlYXIlMjBkYXl8ZW58MHx8MHx8&auto=format&fit=crop&w=600&q=60";
                        else
                            path="https://images.unsplash.com/photo-1491961713439-c6a3e7368eff?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8Nnx8ZGF5fGVufDB8fDB8fA%3D%3D&auto=format&fit=crop&w=600&q=60";
                        Picasso.get().load(path).into(backIV);
                    }else{
                            if(condition.equals("Overcast"))
                                path="https://images.unsplash.com/photo-1470432581262-e7880e8fe79a?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=872&q=80";
                            else if(condition.equals("Mist"))
                                path="https://images.unsplash.com/photo-1514791376975-4b8607d32b8e?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8NHx8Zm9nZ3klMjBuaWdodHxlbnwwfHwwfHw%3D&auto=format&fit=crop&w=600&q=60";
                            else if(condition.equals("Clear"))
                             path="https://images.unsplash.com/photo-1573166331058-c9e7ef82687d?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8Mnx8Y2xlYXIlMjBuaWdodHxlbnwwfHwwfHw%3D&auto=format&fit=crop&w=600&q=60";
                           else if(condition.equals("Partly cloudy"))
                               path="https://images.unsplash.com/photo-1500740516770-92bd004b996e?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8MXx8cGFydGx5JTIwY2xvdWR5JTIwbmlnaHR8ZW58MHx8MHx8&auto=format&fit=crop&w=600&q=60";
                            else
                                path="https://images.unsplash.com/photo-1590418606746-018840f9cd0f?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8MTB8fG5pZ2h0fGVufDB8fDB8fA%3D%3D&auto=format&fit=crop&w=600&q=60";
                           Picasso.get().load(path).into(backIV);
                    }


                    JSONObject forecastObj = response.getJSONObject("forecast");
                    JSONObject forecast0=forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray = forecast0.getJSONArray("hour");

                    int i;
                    for (i = 0; i<hourArray.length(); i++){
                        JSONObject hourObj = hourArray.getJSONObject(i);
                        String time= hourObj.getString("time");
                        String temper= hourObj.getString("temp_c");
                        String img= hourObj.getJSONObject("condition").getString("icon");
                        String wind= hourObj.getString("wind_kph");
                        if(counter%2==0)
                        weatherRVModelArrayList.add(new WeatherRVModel(time,temper,img,wind));

                    }
                    weatherRVAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
//                    Toast.makeText(MainActivity.this, "chal hat", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error.getMessage());
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }
}