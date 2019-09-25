package com.coolweather.android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
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

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.WeatherForecast;
import com.coolweather.android.gson.WeatherLifestyle;
import com.coolweather.android.gson.WeatherNow;
import com.coolweather.android.util.HandleResult;
import com.coolweather.android.util.HttpUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    public SwipeRefreshLayout swipeRefresh;

    public DrawerLayout drawerLayout;

    private ScrollView weatherLayout;

    private Button navButton;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout forecastLayout,bigForecastLayout;

    private TextView humText;

    private TextView visText;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;

    private ImageView bingPicImg;

    public String mWeatherId;


    private WeatherForecast weatherForecast;
    private WeatherLifestyle weatherLifestyle;
    private WeatherNow weatherNow;

    private HandleResult handleForecast = new HandleResult() {
        @Override
        public void OnSuccess(String s) {
            Gson gson = new Gson();
            weatherForecast = gson.fromJson(s,WeatherForecast.class);
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("config",MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("forecast",s);
            editor.apply();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showWeatherForecast();
                }
            });
        }

        @Override
        public void OnFailed() {
            Log.d("<<<","fAILED");
            Looper.prepare();
            Toast.makeText(getApplicationContext(),"网络连接错误",Toast.LENGTH_SHORT).show();
            Looper.loop();
        }
    };

    private HandleResult handleLifestyle = new HandleResult() {
        @Override
        public void OnSuccess(String s) {
            Gson gson = new Gson();
            weatherLifestyle = gson.fromJson(s,WeatherLifestyle.class);
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("config",MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("lifestyle",s);
            editor.apply();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showWeatherLifestyle();
                }
            });
        }

        @Override
        public void OnFailed() {
            Looper.prepare();
            Toast.makeText(getApplicationContext(),"网络连接错误",Toast.LENGTH_SHORT).show();
            Looper.loop();
            Log.d("<<<","fAILED");
        }
    };

    private HandleResult handleNow = new HandleResult() {
        @Override
        public void OnSuccess(String s) {
            Gson gson = new Gson();
            weatherNow = gson.fromJson(s,WeatherNow.class);
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("config",MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("now",s);
            editor.apply();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showWeatherNow();
                }
            });
        }

        @Override
        public void OnFailed() {
            Log.d("<<<","fAILED");
            Looper.prepare();
            Toast.makeText(getApplicationContext(),"网络连接错误",Toast.LENGTH_SHORT).show();
            Looper.loop();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);

        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        humText = (TextView) findViewById(R.id.aqi_text);
        visText = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        bigForecastLayout = findViewById(R.id.big_forecast_layout);
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navButton = (Button) findViewById(R.id.nav_button);

        SharedPreferences preferences = getSharedPreferences("pic",MODE_PRIVATE);
        String bingPic = preferences.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            loadBingPic();
        }

        SharedPreferences prefs = getSharedPreferences("config",MODE_PRIVATE);
        String forecast = prefs.getString("forecast",null);
        String now = prefs.getString("now",null);
        String lifestyle = prefs.getString("lifestyle",null);
    /*    if (forecast != null && now!=null &&lifestyle!=null) {
            // 有缓存时直接解析天气数据
            handleForecast.OnSuccess(forecast);
            handleLifestyle.OnSuccess(lifestyle);
            handleNow.OnSuccess(now);
           // Weather weather = Utility.handleWeatherResponse(weatherString);
           // mWeatherId = weather.basic.weatherId;
           // showWeatherInfo(weather);
        } else { */
            // 无缓存时去服务器查询天气
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
        requestWeather(mWeatherId);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        // }
       // if (forecast != null && now!=null &&lifestyle!=null)
       // showWeatherInfo();


    }

    public void requestWeather(String id) {
        HttpUtil.snedRequest(id,"lifestyle",handleLifestyle);
        HttpUtil.snedRequest(id,"now",handleNow);
        HttpUtil.snedRequest(id,"forecast",handleForecast);
    }

    private void showWeatherInfo(){
        showWeatherNow();
        showWeatherForecast();
        showWeatherLifestyle();
    }

    private void showWeatherLifestyle() {
        List<WeatherLifestyle.HeWeather6Bean.LifestyleBean> lifestyleBeans = weatherLifestyle.getHeWeather6().get(0).getLifestyle();
        String cmf = "舒适度： "+lifestyleBeans.get(0).getTxt();
        String dress = "穿衣建议： "+lifestyleBeans.get(1).getTxt();
        String flu = "感冒预测： "+lifestyleBeans.get(2).getTxt();
        comfortText.setText(cmf);
        carWashText.setText(dress);
        sportText.setText(flu);

    }

    private void showWeatherForecast() {
        List<WeatherForecast.HeWeather6Bean.DailyForecastBean> dailyForecasts = weatherForecast.getHeWeather6().get(0).getDaily_forecast();
        for (WeatherForecast.HeWeather6Bean.DailyForecastBean bean : dailyForecasts){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(bean.getDate());
            infoText.setText(bean.getCond_txt_d());
            maxText.setText(bean.getTmp_max());
            minText.setText(bean.getTmp_min());
            forecastLayout.addView(view);
        }
        if (dailyForecasts.isEmpty())bigForecastLayout.setVisibility(View.INVISIBLE);
        weatherLayout.setVisibility(View.VISIBLE);
        swipeRefresh.setRefreshing(false);
    }

    private void showWeatherNow() {
        WeatherNow.HeWeather6Bean weather6Bean=weatherNow.getHeWeather6().get(0);
        String cityName = weather6Bean.getBasic().getLocation() + " " +weather6Bean.getBasic().getParent_city();
        String updateTime = weather6Bean.getUpdate().getLoc().split(" ")[1];
        String degree = weather6Bean.getNow().getTmp()+"℃";
        String weatherInfo = weather6Bean.getNow().getCond_txt();
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        humText.setText(weather6Bean.getNow().getHum());
        visText.setText(weather6Bean.getNow().getVis());
    }
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("pic",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }
}
