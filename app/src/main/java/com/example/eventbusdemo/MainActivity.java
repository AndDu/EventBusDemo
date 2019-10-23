package com.example.eventbusdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);

    }

    //需要对方法做一个标识，告诉Eventbus只将带有该标识的方法加入EVentBus
    @Subscrible(threadMode = ThreadMode.MAIN)
    public void getMessage(User user) {
        Log.e("getMessage: ", user.toString());
    }
}
