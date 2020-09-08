package com.example.weatherdemo;

import androidx.annotation.NonNull;

public class Utility {

    @NonNull
    public static int getIconResourceForWeather(String des) {
        if (des == "haze") {
            return R.drawable.haze_s;
        } else if (des == "sunny") {
            return R.drawable.sun_s;
        } else if (des == "rain") {
            return R.drawable.light_ss;
        } else if (des == "snow") {
            return R.drawable.snow_s;
        } else if (des == "cloudy") {
            return R.drawable.cloud_s;
        }
        return R.drawable.haze_s;
    }
}
