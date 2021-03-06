package com.example.weatherdemo;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import retrofit.ApiClient;
import retrofit.ApiInterface;
import retrofit.Daily;
import retrofit.Data;
import retrofit.Today;
import retrofit.WeatherData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends WearableActivity {

    private static final String TAG = "Demo";
    // Data
    Today today;
    Daily[] dailyData;

    // UI
    TextView location;
    TextView todayTemp;
    TextView todayWindSpeed;
    TextView description;
    LottieAnimationView[] mAV = new LottieAnimationView[5];
    TextView[] days = new TextView[6];
    TextView[] temps = new TextView[6];
    TextView[] rains = new TextView[6];
    TextView[] winds = new TextView[6];
    ImageView[] imgs = new ImageView[6];

    int flag = 0;

    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sp = getSharedPreferences("MyPref", MODE_PRIVATE);

        // Enables Always-on
        setAmbientEnabled();

        // iniatialize UI Component
        iniatializeUI();

        // initial animation
        initAnimation();

        // getWeather Data
         getWeatherData();

         // set Data to UI
        //showData();

    }

    String getWeekDay(String ts) {
        long timeStamp = Long.parseLong(ts);
        SimpleDateFormat sdf = new SimpleDateFormat("E");
        Date dateFormat = new java.util.Date(timeStamp*1000L);
        String weekday = sdf.format(dateFormat);
        Log.d(TAG, "getWeekDay: "+weekday);
        return  weekday;
    }

    void iniatializeUI() {
        // initialize Today
        initializeUIToday();
        initializeUIForecast();
        initForecastImage();
    }

    void initializeUIToday() {
        // Animation View
        mAV[0] = (LottieAnimationView) findViewById(R.id.animationView1);
        mAV[1] = (LottieAnimationView) findViewById(R.id.animationView2);
        mAV[2] = (LottieAnimationView) findViewById(R.id.animationView3);
        mAV[3] = (LottieAnimationView) findViewById(R.id.animationView4);
        mAV[4] = (LottieAnimationView) findViewById(R.id.animationView5);

        // location
        location = (TextView) findViewById(R.id.location);
        // today Data
        todayTemp = (TextView) findViewById(R.id.todayTemp);
        todayWindSpeed = (TextView) findViewById(R.id.todayWindSpeed);
        //Description
        description = (TextView) findViewById(R.id.description);

        // Tap on animation View
        mAV[0].setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                Log.d(TAG, "onTouch: 1st Tapped.");
                if(event.getAction() == MotionEvent.ACTION_UP){
                    // image released
                    nextAnimation();
                }

                return true;
            }
        });

        mAV[1].setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                Log.d(TAG, "onTouch: 2nd Tapped.");
                if(event.getAction() == MotionEvent.ACTION_UP){
                    // image released
                    nextAnimation();
                }
                return true;
            }
        });

        mAV[1].setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                Log.d(TAG, "onTouch: 3rd Tapped.");
                if(event.getAction() == MotionEvent.ACTION_UP){
                    // image released
                    nextAnimation();
                }
                return true;
            }
        });

        mAV[2].setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                Log.d(TAG, "onTouch: 3rd Tapped.");
                if(event.getAction() == MotionEvent.ACTION_UP){
                    // image released
                    nextAnimation();
                }
                return true;
            }
        });

        mAV[3].setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                Log.d(TAG, "onTouch: 4th Tapped.");
                if(event.getAction() == MotionEvent.ACTION_UP){
                    // image released
                    nextAnimation();
                }
                return true;
            }
        });

        mAV[4].setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                Log.d(TAG, "onTouch: 5th Tapped.");
                if(event.getAction() == MotionEvent.ACTION_UP){
                    // image released
                    nextAnimation();
                }
                return true;
            }
        });

    }

    void nextAnimation() {
        mAV[flag].setVisibility(View.GONE);
        flag = (flag+1)%5;
        mAV[flag].setVisibility(View.VISIBLE);
    }

    void initializeUIForecast() {

        days[0] = (TextView) findViewById(R.id.day1);
        days[1] = (TextView) findViewById(R.id.day2);
        days[2] = (TextView) findViewById(R.id.day3);
        days[3] = (TextView) findViewById(R.id.day4);
        days[4] = (TextView) findViewById(R.id.day5);
        days[5] = (TextView) findViewById(R.id.day6);

        temps[0] = (TextView) findViewById(R.id.temp1);
        temps[1] = (TextView) findViewById(R.id.temp2);
        temps[2] = (TextView) findViewById(R.id.temp3);
        temps[3] = (TextView) findViewById(R.id.temp4);
        temps[4] = (TextView) findViewById(R.id.temp5);
        temps[5] = (TextView) findViewById(R.id.temp6);

        rains[0] = (TextView) findViewById(R.id.rain1);
        rains[1] = (TextView) findViewById(R.id.rain2);
        rains[2] = (TextView) findViewById(R.id.rain3);
        rains[3] = (TextView) findViewById(R.id.rain4);
        rains[4] = (TextView) findViewById(R.id.rain5);
        rains[5] = (TextView) findViewById(R.id.rain6);

        winds[0] = (TextView) findViewById(R.id.wind1);
        winds[1] = (TextView) findViewById(R.id.wind2);
        winds[2] = (TextView) findViewById(R.id.wind3);
        winds[3] = (TextView) findViewById(R.id.wind4);
        winds[4] = (TextView) findViewById(R.id.wind5);
        winds[5] = (TextView) findViewById(R.id.wind6);
    }

    void initAnimation() {
        for(int i=0;i<5;i++) {
            mAV[i].setVisibility(View.GONE);
        }
        mAV[0].setVisibility(View.VISIBLE);
    }

    void initForecastImage() {
        imgs[0] = (ImageView)findViewById(R.id.img1);
        imgs[1] = (ImageView)findViewById(R.id.img2);
        imgs[2] = (ImageView)findViewById(R.id.img3);
        imgs[3] = (ImageView)findViewById(R.id.img4);
        imgs[4] = (ImageView)findViewById(R.id.img5);
        imgs[5] = (ImageView)findViewById(R.id.img6);

        setImageIcon(imgs[0],1);
        setImageIcon(imgs[1],2);
        setImageIcon(imgs[2],3);
        setImageIcon(imgs[3],4);
        setImageIcon(imgs[4],5);
        setImageIcon(imgs[5],6);
    }

    void setImageIcon(ImageView img, int id) {
        if(id==1)  img.setImageResource(R.drawable.sun_s);
        if(id==2)   img.setImageResource(R.drawable.cloud_s);
        if(id==3)  img.setImageResource(R.drawable.storm_s);
        if(id==4)  img.setImageResource(R.drawable.light_ss);
        if(id==5)  img.setImageResource(R.drawable.snow_s);
        if(id==6)  img.setImageResource(R.drawable.haze_s);
    }

    void showData() {
        Log.d(TAG, "showData: ");

        if(dailyData==null) {
            Log.d(TAG, "showData: No Daily Data");
            return;
        }
        
        if(today==null) {
            Log.d(TAG, "showData: No Current Data");
            return;
        }

        Log.d(TAG, "showData: Set Data");

        todayTemp.setText( today.getTemp()+"°c" );
        todayWindSpeed.setText( today.getWindSpeed()+" m/s" );

        WeatherData[] weather = today.getWeatherData();
        String weatherCondition = weather[0].getMain();

        description.setText(weather[0].getDescription());

//        for(int i=0;i<5;i++) {
//            mAV[i].setVisibility(View.GONE);
//        }
//        switch (weatherCondition) {
//            case "Clear":
//                mAV[0].setVisibility(View.VISIBLE);
//                break;
//            case "Clouds":
//                mAV[1].setVisibility(View.VISIBLE);
//                break;
//            case "Rain":
//                mAV[2].setVisibility(View.VISIBLE);
//                break;
//            case "Snow":
//                mAV[3].setVisibility(View.VISIBLE);
//                break;
//            default:
//                mAV[4].setVisibility(View.VISIBLE);
//        }

        Log.d(TAG, "showData: DailyData size: " + dailyData.length);
        // ForCast Data
        for(int i=0;i<6;i++) {
              int tem = getValue(dailyData[i+1].getTemp().getMax());
              temps[i].setText( tem + "°c" );
              winds[i].setText( dailyData[i+1].getWind_speed() + " m/s" );
              rains[i].setText( dailyData[i+1].getRain() + " mm" );
        }

        for(int i=0;i<6;i++) {
            String weekDay = getWeekDay( dailyData[i+1].getTimeStamp() );
            days[i].setText(weekDay);
        }
    }

    int getValue(String t) {
        double tt = Double.parseDouble(t);
        int tem = (int) Math.round(tt);
        return tem;
    }

    private void getWeatherData(){

        Log.d(TAG, "getWeatherData: ");

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<Data> call = apiInterface.getWeatherData();
        Log.d(TAG, "getWeatherData: "+call.request().url());
        call.enqueue(new Callback<Data>() {

            @Override
            public void onResponse(Call<Data> call, Response<Data> response) {
                Log.d(TAG, "onResponse: Success");
                Log.d(TAG, "onResponse: "+response);

                if(response.body()==null) {
                    Log.d(TAG, "onResponse: Response is Null.");
                } else {
                    Log.d(TAG, "onResponse: Have Data");
                    today = response.body().getTodayData();
                    dailyData = response.body().getDailyData();
                    showData();
                }

                // Debug
//                WeatherData []tem = response.body().getTodayData().getWeatherData();
//                Daily[] dailyData = response.body().getDailyData();
//
//                if(tem.length > 0)  {
//                    Log.d(TAG,tem[0].getMain() +" ::" );
//                    Log.d(TAG,tem[0].getDescription() +" ::" );
//                }
//
//
//                Log.d(TAG, "onResponse: Size: "+dailyData.length);
//
//                if(dailyData.length>0) {
//                    int size = dailyData.length;
//                    for(int i=0;i<size;i++) {
//                        Log.d(TAG, "onResponse: Max = " + dailyData[i].getTemp().getMax() );
//                        Log.d(TAG, "onResponse: Min = " + dailyData[i].getTemp().getMin() );
//                        Log.d(TAG, "onResponse: Wind Speed = " + dailyData[i].getWind_speed() );
//                        Log.d(TAG, "onResponse: Rain = " + dailyData[i].getRain() );
//                    }
//                }
            }

            @Override
            public void onFailure(Call<Data> call, Throwable t) {
                Log.d(TAG, "onFailure: couldn't fetch Data");
            }
        });
    }
}
