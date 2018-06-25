package com.example.administrator.ftpclient;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.List;

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
       //verifyStoragePermissions(this);
        int hasReadPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(hasReadPermission!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }
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
        File directory=new File(Environment.getExternalStorageDirectory().getPath().toString()+"/1ftpData");
        boolean  b=false;
        if(!directory.exists()){
            b=directory.mkdir();
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
                 int port=21;
                 port = Integer.parseInt(portStr);
                 boolean flag=false;
                 FTPManager manager= new FTPManager();
                 try{
                     flag=manager.connect(host,port,user,pass);
                     if(!flag) return flag;
                     //连接的时候给系统服务器端设一个文件夹专门存上传的文件
                     manager.createDirectory("/phoneData/");
                 }catch (Exception e){
                     e.printStackTrace();
                 }finally {
                     //最后不管是否连接上了 都关闭一下
                     try {
                         manager.closeFTP();
                     }catch (Exception e1){

                     }
                     return flag;
                 }
         }

         protected void onPostExecute(Boolean flag){
             if(flag){
                 Toast tot = Toast.makeText(
                         mContext,
                         "登录成功",
                         Toast.LENGTH_LONG);
                 tot.show();
                 Intent intent=new Intent(MainActivity.this,chooseActivity.class);
                 //准备进入选择界面并且准备好参数
                 intent.putExtra("host",host.getText().toString());
                 intent.putExtra("user",name.getText().toString());
                 intent.putExtra("pass",pass.getText().toString());
                 intent.putExtra("port",Integer.parseInt(port.getText().toString()));
                 startActivity(intent);
             }else{
                 new AlertDialog.Builder(mContext)
                         .setTitle("提示")
                         .setMessage("抱歉，暂时无法连接Ftp服务器，请检查是否服务器是否开启以及信息是否无误！")
                         .setPositiveButton("确定", null)
                         .show();

             }
         }
     }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==1){
            if(permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                System.out.println("申请权限成功");
            }else{
               System.out.println("申请失败");
            }
        }
    }

}


