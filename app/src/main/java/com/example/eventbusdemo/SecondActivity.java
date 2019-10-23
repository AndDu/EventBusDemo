package com.example.eventbusdemo;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SecondActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        EventBus.getDefault().post(new A());
    }

    public void post(View view){
        EventBus.getDefault().post(new User("xxxx", "yyyyyyyy"));
    }
}
