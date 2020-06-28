package com.coolweather.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;



public class foodhome extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.foodhome);
        Button btn5 = (Button) findViewById(R.id.home_btn5);
        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(foodhome.this, HomeMenuActivity.class);
                startActivity(i);
            }
        });
    }


}


