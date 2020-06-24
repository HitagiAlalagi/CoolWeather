package com.coolweather.android;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.bumptech.glide.Glide;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.service.AutoUpdateService;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;

    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;

    private ImageView bingPicImg;
    public SwipeRefreshLayout swipeRefreshLayout;
    private String weatherId;

    public DrawerLayout drawerLayout;
    private Button navButton;

    private TextView locate_button;
    public LocationClient mLocationClient;
    private TextView locatee;

    public static String currentCity= "";   //当前定位城市
    public Button locateCity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();    //获取DecorView
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        |View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );  //改变系统UI
            getWindow().setStatusBarColor(Color.TRANSPARENT);   //设置顶部透明
        }

        mLocationClient=new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new WeatherActivity.MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());

        setContentView(R.layout.activity_weather);
        //初始化各组件

        locateCity = findViewById(R.id.locatecity);
        locate_button = findViewById(R.id.location_button);
        locatee = findViewById(R.id.locate);

        drawerLayout = findViewById(R.id.drawer_layout);
        navButton = findViewById(R.id.nav_button);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        bingPicImg = findViewById(R.id.bing_pic_img);
        weatherLayout = findViewById(R.id.weather_layout);
        titleCity = findViewById(R.id.title_city);
        titleUpdateTime = findViewById(R.id.title_update_time);
        degreeText = findViewById(R.id.degree_text);
        weatherInfoText = findViewById(R.id.weather_info_text);
        forecastLayout = findViewById(R.id.forecast_layout);
        aqiText = findViewById(R.id.aqi_text);
        pm25Text = findViewById(R.id.pm25_text);
        comfortText = findViewById(R.id.comfort_text);
        carWashText = findViewById(R.id.car_wash_text);
        sportText = findViewById(R.id.sport_text);
        Button newsinfo = (Button)findViewById(R.id.news);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);

        newsinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WeatherActivity.this, NewsActivity.class);
                startActivity(intent);
            }
        });

        if (weatherString != null){
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            weatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        }else {
            //无缓存时去服务器查询数据
            weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);    //暂时将ScrollView设为不可见
            requestWeather(weatherId);
        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        //地图跳转
        locate_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WeatherActivity.this,MapActivity.class);
                startActivity(intent);
            }
        });
        //定位城市
        locateCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestLocation();
                titleCity.setText(currentCity);
                requestWeather1(weatherId);
            }
        });
        requestLocation();

    }

    //初始化定位函数，启动百度定位,获取权限
    public void requestLocation(){
        initLocation();
        mLocationClient.start();
        List<String> permissionList=new ArrayList<>();
        //如果没有启动下面权限，就询问用户让用户打开
        if (ContextCompat.checkSelfPermission(WeatherActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(WeatherActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(WeatherActivity.this,
                Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(WeatherActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()){
            String[] permissions=permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(WeatherActivity.this,permissions,1);
        }
    }

    /*初始化定位函数*/
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(2000);       ////设置发起定位请求的间隔时间为5000ms,设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        // 设置定位模式,一共三种模式，高精度（使用GPS、网络定位，精度最高），低功耗（仅使用网络定位），仅设备（仅使用GPS定位）
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        // 设置坐标系，gcj02表示国测局加密经纬度坐标，bd0911百度加密经纬度坐标，bd09百度加密墨卡托坐标
        option.setCoorType("gcj02");
        mLocationClient.setLocOption(option); //自动开始定位
    }

    /*监听线程，获得当前的经纬度，并显示*/
    private class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation bdLocation) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() { // GPS定位结果
                    StringBuilder currentPosition=new StringBuilder();
                    currentPosition.append("国家:").append(bdLocation.getCountry()).append(" ");
                    currentPosition.append("省:").append(bdLocation.getProvince()).append(" ");
                    currentPosition.append("市:").append(bdLocation.getCity()).append(" ");
                    currentPosition.append("区:").append(bdLocation.getDistrict()).append("  ");
                    locatee.setText(currentPosition);
                    currentCity = bdLocation.getCity(); //获取省份信息

                }
            });
        }
    }

    //停止定位
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
    }

    /**
     * 根据天气Id请求城市天气信息
     */
    public void requestWeather(final String weatherId){
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=391fb9f88dfd4384b1caba0bdc301002";
        //组装地址并发出请求
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);    //将返回数据装换为Weather对象
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather!=null && "ok".equals(weather.status)){
                            //缓存有效的weather对象(实际上缓存的是字符串)
                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);   //显示内容
                        }else{
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }

    /**
     * 处理并展示Weather实体类中的数据
     * @param weather
     */
    private void showWeatherInfo(Weather weather){
        //从Weather对象中获取数据
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split("")[1];   //按24小时计算的时间
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;

        //将数据显示在对应控件上
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);


        switch (weatherInfo){
            case "晴":
                bingPicImg.setImageResource(R.drawable.qing);break;
            case "阴":
                bingPicImg.setImageResource(R.drawable.yin);break;
            case "多云":
                bingPicImg.setImageResource(R.drawable.duoyun);break;
            case "阵雨":
                bingPicImg.setImageResource(R.drawable.zhenyu);break;
            case "雷阵雨":
                bingPicImg.setImageResource(R.drawable.leizhenyu);break;
            case "小雨":
                bingPicImg.setImageResource(R.drawable.xiaoyu);break;
            case "中雨":
                bingPicImg.setImageResource(R.drawable.zhongyu);break;
            case "大雨":
                bingPicImg.setImageResource(R.drawable.dayu);break;
            default:
                Log.d("tag",weatherInfo);break;
        }


        forecastLayout.removeAllViews();
        for (Forecast forecast:weather.forecastList){   //循环处理每天的天气信息
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);

            //加载布局
            TextView dateText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);

            //设置数据
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);

            //添加到父布局
            forecastLayout.addView(view);
        }
        if (weather.aqi != null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }

        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运动建议：" + weather.suggestion.sport.info;

        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);

        weatherLayout.setVisibility(View.VISIBLE);  //将天气信息设置为可见

        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    /**
     * 根据天气Id请求城市天气信息1
     */
    public void requestWeather1(final String weatherId){
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=6a5076fe1fc64df7896af33b25fea851"; // 这里的key设置为第一个实训中获取到的API Key
        // 组装地址并发出请求
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseText = response.body().string();
                final  Weather weather = Utility.handleWeatherResponse(responseText);   // 将返回数据转换为Weather对象
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        requestLocation();
                        if(weather!=null && "ok".equals(weather.status)){
                            //缓存有效的weather对象(实际上缓存的是字符串)
                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo1(weather);   // 显示内容
                        }else{
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);    // 表示刷新事件结束并隐藏刷新进度条
                    }
                });
            }
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }

    /**
     * 处理并展示Weather实体类中的数据
     * @param weather
     */
    private void showWeatherInfo1(Weather weather){
        // 从Weather对象中获取数据
        String cityName = WeatherActivity.currentCity;
        String updateTime = weather.basic.update.updateTime.split(" ")[1]; //按24小时计时的时间
        String degree = weather.now.temperature + "°C";
        String weatherInfo = weather.now.more.info;
        // 将数据显示到对应控件上
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for(Forecast forecast:weather.forecastList){    // 循环处理每天的天气信息
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            // 加载布局
            TextView dateText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);
            // 设置数据
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            // 添加到父布局
            forecastLayout.addView(view);
        }
        if(weather.aqi != null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度: " + weather.suggestion.comfort.info;
        String carWash = "洗车指数: " + weather.suggestion.carWash.info;
        String sport = "运动建议: "+ weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);  // 将天气信息设置为可见

        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }
}
