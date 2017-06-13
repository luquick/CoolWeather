package com.seashell.coolweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Luquick on 2017/6/13.
 */

public class County extends DataSupport {

    private int countyId;
    private String countyName;
    private int countyCode;
    private int cityId;

    public int getCountyId() {
        return countyId;
    }

    public void setCountyId(int countyId) {
        this.countyId = countyId;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public int getCountyCode() {
        return countyCode;
    }

    public void setCountyCode(int countyCode) {
        this.countyCode = countyCode;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    @Override
    public String toString() {
        return "County{" +
                "countyId=" + countyId +
                ", countyName='" + countyName + '\'' +
                ", countyCode=" + countyCode +
                ", cityId=" + cityId +
                '}';
    }
}
