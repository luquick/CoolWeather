package com.seashell.coolweather.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.seashell.coolweather.R;
import com.seashell.coolweather.Utils.HttpUtil;
import com.seashell.coolweather.Utils.LogUtil;
import com.seashell.coolweather.Utils.Utility;
import com.seashell.coolweather.gson.Forecast;
import com.seashell.coolweather.gson.Weather;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private static final String TAG = WeatherActivity.class.getSimpleName();

    public DrawerLayout mDrawer;
    public SwipeRefreshLayout mSwipeRefresh;
    private Button mBack;
    private ScrollView mWeatherLayout;
    private TextView mTitleCity;
    private TextView mTitleUpdateTime;
    private TextView mDegreeText;
    private TextView mWeatherInfoText;
    private LinearLayout mForecastLayout;
    private TextView mAqiText;
    private TextView mPm25Text;
    private TextView mComfortText;
    private TextView mCarWashText;
    private TextView mSportText;
    private SharedPreferences mPreferences;
    private ImageView mBingPic;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        initWidget();
        parseDataWithCacheOrServer();
    }


    /**
     * Initialize each element
     */
    private void initWidget() {
        mDrawer = (DrawerLayout) this.findViewById(R.id.drawer_layout);
        mBack = (Button) this.findViewById(R.id.nav_button);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.openDrawer(GravityCompat.START);
            }
        });
        mWeatherLayout = (ScrollView) this.findViewById(R.id.weather_sv);
        mTitleCity = (TextView) this.findViewById(R.id.title_city);
        mTitleUpdateTime = (TextView) this.findViewById(R.id.title_update_time);
        mDegreeText = (TextView) this.findViewById(R.id.degree_text);
        mWeatherInfoText = (TextView) this.findViewById(R.id.weather_info_text);
        mForecastLayout = (LinearLayout) this.findViewById(R.id.forecast_layout);
        mAqiText = (TextView) this.findViewById(R.id.aqi_tv);
        mPm25Text = (TextView) this.findViewById(R.id.pm25_tv);
        mComfortText = (TextView) this.findViewById(R.id.comfort_tv);
        mCarWashText = (TextView) this.findViewById(R.id.car_wash_tv);
        mSportText = (TextView) this.findViewById(R.id.sport_tv);
        mBingPic = (ImageView) this.findViewById(R.id.bing_pic_imv);
        mSwipeRefresh = (SwipeRefreshLayout) this.findViewById(R.id.swipe_refresh);
        mSwipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    /**
     * Parsing the weather data from the cache or server
     */
    private void parseDataWithCacheOrServer() {
        final String weatherId;
        loadPicFromCacheWithServer();
        String weatherString = mPreferences.getString("weather", null);
        if (weatherString != null) {
            Weather weather = Utility.handleWeatherResponse(weatherString);
            weatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            //Parsing the weather data from the server
            weatherId = getIntent().getStringExtra("weather_id");
            mWeatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });
    }

    /**
     * Load the image from the cache or server
     */
    private void loadPicFromCacheWithServer() {
        String bingPic = mPreferences.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(mBingPic);
        } else {
            loadBingPic();
        }
    }

    /**
     * load the image from the bing server
     */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(mBingPic);
                    }
                });
            }
        });
    }

    /**
     * Request city weather information based on weather ID
     *
     * @param weatherId
     */
    public void requestWeather(final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "Failed to get the weather information", Toast.LENGTH_SHORT).show();
                        }
                        mSwipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "Failed to get the weather information", Toast.LENGTH_SHORT).show();
                        mSwipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    /**
     * Process and display data in the Weather entity class
     *
     * @param weather
     */
    public void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        mTitleCity.setText(cityName);
        mTitleUpdateTime.setText(updateTime);
        mDegreeText.setText(degree);
        mWeatherInfoText.setText(weatherInfo);
        mForecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(WeatherActivity.this).inflate(R.layout.forecast_item, mForecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_tv);
            TextView inforText = (TextView) view.findViewById(R.id.info_tv);
            TextView maxText = (TextView) view.findViewById(R.id.max_tv);
            TextView minText = (TextView) view.findViewById(R.id.min_tv);

            dateText.setText(forecast.date);
            inforText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            mForecastLayout.addView(view);
        }
        if (weather.aqi != null) {
            mAqiText.setText(weather.aqi.city.aqi);
            mPm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "comfort:" + weather.suggestion.comfort.info;
        String carWash = "car_wash:" + weather.suggestion.carWash.info;
        String sport = "sport suggestion:" + weather.suggestion.sport.info;
        mComfortText.setText(comfort);
        mSportText.setText(sport);
        mCarWashText.setText(carWash);
        mWeatherLayout.setVisibility(View.VISIBLE);
    }

    /**
     * External invocation jumps
     *
     * @param context
     * @param weatherId
     */
    public static void actionStart(Context context, String weatherId) {
        Intent intent = new Intent(context, WeatherActivity.class);
        intent.putExtra("weather_id", weatherId);
        context.startActivity(intent);
    }

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, WeatherActivity.class);
        context.startActivity(intent);
    }
}
