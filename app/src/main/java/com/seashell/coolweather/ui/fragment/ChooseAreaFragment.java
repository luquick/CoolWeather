package com.seashell.coolweather.ui.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.seashell.coolweather.MainActivity;
import com.seashell.coolweather.R;
import com.seashell.coolweather.Utils.HttpUtil;
import com.seashell.coolweather.Utils.LogUtil;
import com.seashell.coolweather.Utils.Utility;
import com.seashell.coolweather.db.City;
import com.seashell.coolweather.db.County;
import com.seashell.coolweather.db.Province;
import com.seashell.coolweather.gson.Weather;
import com.seashell.coolweather.ui.WeatherActivity;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Luquick on 2017/6/13.
 */

public class ChooseAreaFragment extends Fragment {
    private static final String TAG = ChooseAreaFragment.class.getSimpleName();

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog mProgressDialog;
    private TextView mTitle;
    private Button mBack;
    private ListView mListView;
    private List<String> mDatas = new ArrayList<>();
    private ArrayAdapter<String> mAdapter;

    private List<Province> provinceList;
    private List<City> mCityList;
    private List<County> mCountyList;

    /**
     * The selected province
     */
    private Province mSelectedProvince;

    /**
     * The selected city
     */
    private City mSelectedCity;


    /**
     * The currently selected level
     */
    private int mCurrentLevel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        mTitle = (TextView) view.findViewById(R.id.title_tv);
        mBack = (Button) view.findViewById(R.id.back_btn);
        mAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, mDatas);
        mListView = (ListView) view.findViewById(R.id.list_v);
        mListView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mCurrentLevel == LEVEL_PROVINCE) {
                    mSelectedProvince = provinceList.get(position);
                    queryCities();
                } else if (mCurrentLevel == LEVEL_CITY) {
                    mSelectedCity = mCityList.get(position);
                    queryCounties();
                } else if (mCurrentLevel == LEVEL_COUNTY) {
                    String weatherId = mCountyList.get(position).getWeatherId();
                    if (getActivity() instanceof MainActivity) {
                        WeatherActivity.actionStart(getContext(), weatherId);
                        getActivity().finish();
                    } else if (getActivity() instanceof WeatherActivity) {
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.mDrawer.closeDrawers();
                        activity.mSwipeRefresh.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }
                }
            }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentLevel == LEVEL_COUNTY) {
                    queryCities();
                } else if (mCurrentLevel == LEVEL_CITY) {
                    queryProvince();
                }
            }
        });

        queryProvince();
    }

    /**
     * Query all province across the country. first from the local database query.
     * if you don't have to go from the server
     */
    private void queryProvince() {
        mTitle.setText("China");
        mBack.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0) {
            mDatas.clear();
            for (Province province : provinceList) {
                mDatas.add(province.getProvinceName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            mCurrentLevel = LEVEL_PROVINCE;
        } else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }


    /**
     * Query is selected within the province of all cities. priority from the local database query
     * if not checked. will form the server.
     */
    private void queryCities() {
        mTitle.setText(mSelectedProvince.getProvinceName());
        mBack.setVisibility(View.VISIBLE);
        LogUtil.d(TAG, "provinceId = " + mSelectedProvince.getId());
        mCityList = DataSupport.where("provinceId=?", String.valueOf(mSelectedProvince.getId())).find(City.class);
        if (mCityList.size() > 0) {
            mDatas.clear();
            for (City city : mCityList) {
                mDatas.add(city.getCityName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            mCurrentLevel = LEVEL_CITY;
        } else {
            int provinceCode = mSelectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }


    /**
     * Query is selected within the city of all counties. priority from the local database query
     * if not checked. will form the server.
     */
    private void queryCounties() {
        mTitle.setText(mSelectedCity.getCityName());
        mBack.setVisibility(View.VISIBLE);
        LogUtil.d(TAG, "cityId = " + mSelectedCity.getId());
        mCountyList = DataSupport.where("cityId=?", String.valueOf(mSelectedCity.getId())).find(County.class);
        if (mCountyList.size() > 0) {
            mDatas.clear();
            for (County county : mCountyList) {
                mDatas.add(county.getCountyName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            mCurrentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = mSelectedProvince.getProvinceCode();
            int cityCode = mSelectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }

    }

    /**
     * Query the province and county data from the server based on the incoming address and type
     *
     * @param address
     * @param type
     */
    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);

                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, mSelectedProvince.getId());

                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText, mSelectedCity.getId());

                }
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvince();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                //Go back to the main thread processing logic
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "fail to load", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * Show progress dialog
     */
    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }


    /**
     * Close dialog
     */
    private void closeProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }
}
