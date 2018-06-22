package com.example.administrator.ftpclient;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class downloadActivity extends AppCompatActivity {
    private String host,user,pass;
    private int port;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        Intent intent=getIntent();
        host=intent.getStringExtra("host");
        user=intent.getStringExtra("uesr");
        pass=intent.getStringExtra("pass");
        port =intent.getIntExtra("port",21);
    }
}
