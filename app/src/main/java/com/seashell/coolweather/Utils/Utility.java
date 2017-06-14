package com.seashell.coolweather.Utils;

import android.text.TextUtils;

import com.seashell.coolweather.db.City;
import com.seashell.coolweather.db.County;
import com.seashell.coolweather.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Luquick on 2017/6/13.
 */

public class Utility {

    /**
     * Analytical processing province data from server
     *
     * @param response
     * @return
     */
    public static boolean handleProvinceResponse(String response) {
        LogUtil.d("handleProvinceResponse", response);
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allProvinces = new JSONArray(response);
                for (int j = 0; j < allProvinces.length(); j++) {
                    JSONObject provinceObject = allProvinces.getJSONObject(j);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Analytical processing city data from server
     * @param response
     * @param provinceId
     * @return
     */
    public static boolean handleCityResponse(String response ,int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCity = new JSONArray(response);
                for (int i = 0; i < allCity.length(); i++) {
                    JSONObject cityObject = allCity.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Analytical processing county data from server
     * @param response
     * @param cityId
     * @return
     */
    public static boolean handleCountyResponse(String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCounty = new JSONArray(response);
                for (int i = 0; i < allCounty.length(); i++) {
                    JSONObject countObject = allCounty.getJSONObject(i);
                    County county = new County();
                    county.setWeatherId(countObject.getString("weather_id"));
                    county.setCountyName(countObject.getString("name"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}
