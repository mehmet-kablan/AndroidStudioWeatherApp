package com.example.mobilprogramlamahavadurumuuygulamasi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;



public class MainActivity extends AppCompatActivity {


    private RelativeLayout homeRL,RLCities,RLCountries;
    private TextView cityNameTV,temperatureTV,conditionTV;
    private RecyclerView weatherRV;
    private ImageView backIV,iconIV;
    private Button backButton,backToCitiesButton;
    private ArrayList<WeatherRVModel> weatherRVModelArrayList;
    private ListView citiesList;
    private WeatherRVAdapter weatherRVAdapter;
    private final int PERMISSION_CODE = 1;
    private String cityName,chosenCountry;
    private ArrayList<String> countries = new ArrayList<String>();
    private ArrayList<String> cities = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_main);
        homeRL = findViewById(R.id.idRLHome);
        RLCities = findViewById(R.id.idRLcities);
        RLCountries = findViewById(R.id.idRLcountries);
        citiesList = findViewById(R.id.idLVcities);
        cityNameTV = findViewById(R.id.idTVCityName);
        temperatureTV = findViewById(R.id.idTVTemperature);
        conditionTV = findViewById(R.id.idTVCondition);
        weatherRV = findViewById(R.id.idRvWeather);
        backIV = findViewById(R.id.idIVBack);
        iconIV = findViewById(R.id.idIVIcon);
        weatherRVModelArrayList = new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(this,weatherRVModelArrayList);
        backButton = findViewById(R.id.idBTNBack);
        backToCitiesButton = findViewById(R.id.idBTNBackCities);
        weatherRV.setAdapter(weatherRVAdapter);
        cityName = "Istanbul/Turkey";


        try {
            listCountries();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"İzin verildi..",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this,"Lütfen uygulamaya izin veriniz...",Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void getWeatherInfo(String cityName){

        String url = "https://api.weatherapi.com/v1/forecast.json?key=8dcfe04346d94c02a84171742231606&q=" + cityName + "&days=7&aqi=no&alerts=no&lang=tr";
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                RLCities.setVisibility(View.GONE);
                RLCountries.setVisibility(View.GONE);
                homeRL.setVisibility(View.VISIBLE);

                weatherRVModelArrayList.clear();
                try {

                    String temperature = response.getJSONObject("current").getString("temp_c");
                    String cityCountry = response.getJSONObject("location").getString("name")+ "/" + response.getJSONObject("location").getString("country");
                    temperatureTV.setText(temperature+"℃");
                    cityNameTV.setText(cityCountry);
                    int isDay = response.getJSONObject("current").getInt("is_day");
                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("https:".concat(conditionIcon)).into(iconIV);
                    conditionTV.setText(condition);
                    if (isDay == 1){
                        backIV.setImageResource(R.drawable.morningbg);
                    }else{
                        backIV.setImageResource(R.drawable.nightbg);
                    }

                    JSONObject forecastObj = response.getJSONObject("forecast");
                    JSONArray forecastDays = forecastObj.getJSONArray("forecastday");
                    for (int i = 0;i<forecastDays.length();i++){
                        JSONObject dayObj = forecastDays.getJSONObject(i).getJSONObject("day");
                        String date = forecastDays.getJSONObject(i).getString("date");
                        String temper = dayObj.getString("avgtemp_c");
                        String img = dayObj.getJSONObject("condition").getString("icon");
                        weatherRVModelArrayList.add(new WeatherRVModel(temper,img,date));

                    }

                    weatherRVAdapter.notifyDataSetChanged();


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this,"Lütfen geçerli bir şehir giriniz...",Toast.LENGTH_SHORT).show();
                try {
                    listCities(chosenCountry);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        requestQueue.add(jsonObjectRequest);

        backToCitiesButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                try {
                    cities.clear();
                    listCities(chosenCountry);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }

    private void listCountries() throws Exception {


        InputStream inputStream = getAssets().open("countries.json");
        int size = inputStream.available();
        byte[] buffer = new byte[size];
        inputStream.read(buffer);
        inputStream.close();

        String json;
        String name;
        String code;
        json = new String(buffer, StandardCharsets.UTF_8);
        JSONArray jsonArray = new JSONArray(json);

        chosenCountry = "";
        countries.clear();
        cities.clear();
        for (int i = 0; i<jsonArray.length();i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            name = jsonObject.getString("name");
            code = jsonObject.getString("alpha2").toUpperCase();
            countries.add(name +" (" + code + ")");
        }

        String[] copyCountries = new String[countries.size()];
        countries.toArray(copyCountries);


        ListView countriesList = (ListView) findViewById(R.id.idLVCountries);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,android.R.id.text1,copyCountries);
        countriesList.setAdapter(dataAdapter);

        countriesList.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String selectedItem = (String) adapterView.getItemAtPosition(position);
                char[] chars = selectedItem.toCharArray();

                String countryCode = String.valueOf(chars[chars.length-3]) + String.valueOf(chars[chars.length-2]);
                RLCountries.setVisibility(View.GONE);
                RLCities.setVisibility(View.VISIBLE);

                try {
                    listCities(countryCode);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                chosenCountry = countryCode;


            }
        });

        RLCities.setVisibility(View.GONE);
        homeRL.setVisibility(View.GONE);
        RLCountries.setVisibility(View.VISIBLE);

    }

    private void listCities(String countryCode) throws Exception{

        InputStream inputStream = getAssets().open("cities.json");
        int size = inputStream.available();
        byte[] buffer = new byte[size];
        inputStream.read(buffer);
        inputStream.close();

        String json;
        json = new String(buffer, StandardCharsets.UTF_8);
        JSONArray jsonArray = new JSONArray(json);


        for (int i = 0; i<jsonArray.length();i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String name = jsonObject.getString("name");
            if (jsonObject.getString("country").equals(countryCode)){
                cities.add(name);
            }


        }
        String[] copyCities = new String[cities.size()];
        cities.toArray(copyCities);

        ArrayAdapter<String> dataAdapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,android.R.id.text1,copyCities);
        citiesList.setAdapter(dataAdapter1);

        citiesList.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String selectedItem = (String) adapterView.getItemAtPosition(position);
                char[] chars = selectedItem.toCharArray();

                RLCities.setVisibility(View.GONE);

                getWeatherInfo(selectedItem + " " + chosenCountry);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                try {
                    listCountries();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });



        homeRL.setVisibility(View.GONE);
        RLCountries.setVisibility(View.GONE);
        RLCities.setVisibility(View.VISIBLE);

    }


}