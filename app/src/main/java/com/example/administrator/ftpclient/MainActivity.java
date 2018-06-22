package com.example.administrator.ftpclient;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private  Button login;
    private  Button cancel;
    public static EditText host,port,name,pass;

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
                //信息填写完整,那么就测试是否能够登录，如果能就跳转，不能就提示用户
                    String can[]=new String[4];
                    can[0]=host;
                    can[1]=portStr;
                    can[2]=name;
                    can[3]=pass;
                    LogTask task=new LogTask(this);
                    task.execute(can);
            }
            //清空按钮
        }else if(v==findViewById(R.id.cancel_button)){
            this.name.setText("");
            this.pass.setText("");
            this.host.setText("");
            this.port.setText("");
            Toast tot = Toast.makeText(
                    this,
                    "填写信息已清空",
                    Toast.LENGTH_LONG);
            tot.show();
        }
     }

//处理登录的异步函数
     class LogTask extends AsyncTask<String,Void,Boolean>{
         Context mContext;
         public LogTask(Context ctx){
             mContext=ctx;
         }

         protected Boolean doInBackground(String... Params){
             String host=Params[0];
             String portStr=Params[1];
             String user=Params[2];
             String pass=Params[3];
             System.out.println(Params[1]);
                 int port=21;
                 System.out.println("进入异步函数了");
                 port = Integer.parseInt(portStr);
                 boolean flag=false;
                 FTPManager manager= new FTPManager();
                 try{
                     flag=manager.connect(host,port,user,pass);
                     System.out.println("Okokokokokkookoko连接到了");
                 }catch (Exception e){
                     System.out.println("开启连接出错");
                     e.printStackTrace();
                 }finally {
                     return flag;
                 }
         }

         protected void onPostExecute(Boolean flag){
             System.out.println("处理结果函数内部");
             if(flag){
                 System.out.println("处理结果函数正确分支");
                 Toast tot = Toast.makeText(
                         mContext,
                         "正在跳转",
                         Toast.LENGTH_LONG);
                 tot.show();
                 System.out.print("跳转起来！！！");
                 Intent intent=new Intent(MainActivity.this,chooseActivity.class);
                 //准备进入选择界面并且准备好参数
                 intent.putExtra("host",host.getText().toString());
                 intent.putExtra("user",name.getText().toString());
                 intent.putExtra("pass",pass.getText().toString());
                 intent.putExtra("port",Integer.parseInt(port.getText().toString()));
                 startActivity(intent);
             }else{
                 System.out.println("处理结果函数错误分支");
                 new AlertDialog.Builder(mContext)
                         .setTitle("提示")
                         .setMessage("抱歉，暂时无法连接Ftp服务器，请检查是否服务器是否开启以及信息是否无误！")
                         .setPositiveButton("确定", null)
                         .show();

             }
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
