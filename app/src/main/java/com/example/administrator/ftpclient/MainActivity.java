package com.example.administrator.ftpclient;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.FontResourcesParserCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private  Button login;
    private  Button cancel;
    public static EditText host,port,name,pass;

    public Handler handler = new Handler() {

        @Override
        public void publish(LogRecord logRecord) {

        }

        @Override
        public void flush() {

        }

        @Override
        public void close() throws SecurityException {

        }

        public void handleMessage(Message msg) {
            String str = (String) msg.obj;
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("提示")
                    .setMessage("抱歉，暂时无法连接Ftp服务器，请检查是否服务器是否开启以及信息是否无误！")
                    .setPositiveButton("确定", null)
                    .show();
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //申请权限
        verifyStoragePermissions(this);
       //初始化组件
       login=(Button) findViewById(R.id.login_button);
       cancel=(Button) findViewById(R.id.cancel_button);
       host=(EditText) findViewById(R.id.login_host);
        port=(EditText) findViewById(R.id.login_port);
       name=(EditText) findViewById(R.id.login_id);
       pass=(EditText) findViewById(R.id.login_password);
       //添加点击函数
       login.setOnClickListener(this);
       cancel.setOnClickListener(this);

    }

   // 动态申请读写权限的函数，在初始化的时候调用
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }



     public void onClick(View v){
        //登录按钮
        if(v==findViewById(R.id.login_button)) {
            String host = this.host.getText().toString();
            String portStr = this.port.getText().toString();
            String name = this.name.getText().toString();
            String pass = this.pass.getText().toString();
            if (host.equals("") || portStr.equals("") || name.equals("") || pass.equals("")) {
                new AlertDialog.Builder(this)
                        .setTitle("警告")
                        .setMessage("信息未填写完整！")
                        .setPositiveButton("确定", null)
                        .show();
            } else {
                int port = 21;
                //信息填写完整,那么就测试是否能够登录，如果能就跳转，不能就提示用户
                try{
                    port = Integer.parseInt(portStr);
                }catch(NumberFormatException e) {
                    new AlertDialog.Builder(this)
                            .setTitle("警告")
                            .setMessage("端口格式错误，应该为数字！")
                            .setPositiveButton("确定", null)
                            .show();
                }finally {
                    test t=new test(this,host,port,name,pass);
                    new AlertDialog.Builder(this)
                            .setTitle("提示")
                            .setMessage("没事，我只是试一下行不行而已")
                            .setPositiveButton("确定", null)
                            .show();
                    t.start();
                }

            }
            //清空按钮
        }else if(v==findViewById(R.id.cancel_button)){

        }
     }
}

class test extends Thread{
    private Context tx;
    private  String host,user,pass;
    private int port;
    test(Context tx,String host,int port,String user,String pass){
        this.tx=tx;
        this.host=host;
        this.pass=pass;
        this.user=user;
        this.port=port;
    }
    public void run(){
        FTPManager manager= new FTPManager();
        try{
            if(manager.connect(host,port,user,pass)){
                System.out.println("Okokokokokkookoko连接到了");
                Toast tot = Toast.makeText(
                        tx,
                        "正在跳转",
                        Toast.LENGTH_LONG);
                tot.show();
            }else{
                System.out.println("不行啊不行啊");
                new AlertDialog.Builder(tx)
                        .setTitle("提示")
                        .setMessage("抱歉，暂时无法连接Ftp服务器，请检查是否服务器是否开启以及信息是否无误！")
                        .setPositiveButton("确定", null)
                        .show();
            }
        }catch (Exception e){
            System.out.println("开启连接出错");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        try{
            manager.uploadFile("he","/test/");
        }catch (Exception e){
            System.out.println("下载出错");
        }
    }
}
