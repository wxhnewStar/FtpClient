package com.example.administrator.ftpclient;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class chooseActivity extends AppCompatActivity  implements View.OnClickListener{
      private String host,user,pass;
      private int port;
      private Button upload,download;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);
        Intent intent=getIntent();
         host=intent.getStringExtra("host");
        user=intent.getStringExtra("user");
        pass=intent.getStringExtra("pass");
        port =intent.getIntExtra("port",21);
        upload=(Button)findViewById(R.id.upload);
        download=(Button) findViewById(R.id.download);
        upload.setOnClickListener(this);
        download.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view==findViewById(R.id.upload)){
            Intent intent=new Intent(chooseActivity.this,uploadActivity.class);
            //准备进入选择界面并且准备好参数
            intent.putExtra("host",host);
            intent.putExtra("user",user);
            intent.putExtra("pass",pass);
            intent.putExtra("port",port);
            startActivity(intent);
        }else if(view==findViewById(R.id.download)){
            Intent intent=new Intent(chooseActivity.this,downloadActivity.class);
            //准备进入选择界面并且准备好参数
            intent.putExtra("host",host);
            intent.putExtra("user",user);
            intent.putExtra("pass",pass);
            intent.putExtra("port",port);
            startActivity(intent);
        }
    }
}
