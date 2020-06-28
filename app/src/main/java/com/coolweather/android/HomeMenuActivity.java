package com.coolweather.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.coolweather.android.food_grid.FoodGridActivity;
import com.coolweather.android.food_list.InfoListActivity;

public class HomeMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_menu);
    }

    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.home_btn1:
                intent.setClass(HomeMenuActivity.this, InfoListActivity.class);
                break;
            case R.id.home_btn2:
                intent.setClass(HomeMenuActivity.this, FoodGridActivity.class);
                break;
        }
        startActivity(intent);
    }
}
