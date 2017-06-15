package com.seashell.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * basic
 * Created by Luquick on 2017/6/15.
 */

public class Basic {
    @SerializedName("city")
    public String cityName;
    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update {
        @SerializedName("loc")
        public String updateTime;
    }
}
