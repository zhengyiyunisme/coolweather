package com.example.coolweather;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class choose_area extends Fragment {
    public static final int LEVER_PROVINCE=0;
    public static final int LEVER_CITY=0;
    public static final int LEVER_COUNTY=0;
    private int currentLever;
    private ArrayAdapter<String> adapter;
    private List<String> dataList;

    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;

    private Button backButton;
    private TextView title;
    private ListView listView;

    private Province selectedProvince;
    private City selectedCity;
    private County selectedCounty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.choose_area,container,false);
        backButton=view.findViewById(R.id.back_button);
        title=view.findViewById(R.id.title_text);
        listView=view.findViewById(R.id.listView);
        adapter=new ArrayAdapter<String>(getContext(),R.layout.choose_area,dataList);
        listView.setAdapter(adapter);
        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLever == LEVER_PROVINCE) {
                    selectedProvince=provinceList.get(position);
                    quryProvince();
                }
                if (currentLever==LEVER_CITY){
                    selectedCity=cityList.get(position);
                    quryCity();
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLever==LEVER_CITY){
                    quryCity();
                }
                if (currentLever == LEVER_COUNTY) {
                    quryCounty();
                }
            }
        });
        quryProvince();
    }

    private void quryCounty() {
        title.setText(selectedCounty.getCountyName());
        backButton.setVisibility(View.VISIBLE);
        countyList=DataSupport.where("cityId=",String.valueOf(selectedCity.getCityCode())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for(int i=0;i<countyList.size();i++){
                dataList.add(countyList.get(i).getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLever=LEVER_COUNTY;
        }else {
            String address = "http://guolin.tech/api/china" + selectedCity.getCityCode();
            quryFromServer(address, "county");
        }
    }

    private void quryCity() {
        title.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        cityList=DataSupport.where("provinceId=",String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for(int i=0;i<cityList.size();i++){
                dataList.add(cityList.get(i).getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLever=LEVER_CITY;
        }else{
            String address="http://guolin.tech/api/china"+selectedProvince.getProvinceCode();
            quryFromServer(address,"city");
        }

    }

    private void quryProvince() {
        title.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList= DataSupport.findAll(Province.class);
        if (provinceList.size() > 0) {
            dataList.clear();
            for(int i=0;i<provinceList.size();i++){
                dataList.add(provinceList.get(i).getProvinceName());
            }
            adapter.notifyDataSetChanged();
            currentLever=LEVER_PROVINCE;
            listView.setSelection(0);
        }
        else{
            String address="http://guolin.tech/api/china";
            quryFromServer(address,"province");
        }

    }

    private void quryFromServer(String address, final String type) {
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean result=false;
                if ("province".equals(type)){
                    result=Utility.handleProvinceResponse(responseText);
                }
                else if("city".equals(type)){
                    result=Utility.handleCityResponse(responseText,selectedProvince.getId());
                }
                else if("county".equals(type)){
                    result= Utility.handleCountyResponse(responseText,selectedCity.getId());
                }
                if (result=true){
                    if ("province".equals(type)){
                        quryProvince();
                    }
                    else if("city".equals(type)){
                        quryCity();
                    }
                    else if("county".equals(type)){
                        quryCounty();
                    }
                }
            }
        });
    }
}
