package com.seashell.coolweather.gson;

/**
 * Air quality indexes
 * Created by Luquick on 2017/6/15.
 */

public class AQI {
    public AQICity city;

    public class AQICity {
        public String aqi;
        public String pm25;
    }
}
