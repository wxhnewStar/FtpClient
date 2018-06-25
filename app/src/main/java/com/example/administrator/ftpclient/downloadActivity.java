package com.example.administrator.ftpclient;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class downloadActivity extends AppCompatActivity  implements View.OnClickListener{
    private String host,user,pass;
    private int port;
    private Button choose,download;
    private TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        Intent intent=getIntent();
        host=intent.getStringExtra("host");
        user=intent.getStringExtra("user");
        pass=intent.getStringExtra("pass");
        port =intent.getIntExtra("port",21);
        choose=(Button) findViewById(R.id.downloadChoose);
        download=(Button) findViewById(R.id.downloadconfirm);
        choose.setOnClickListener(this);
        download.setOnClickListener(this);
        tv=(TextView) findViewById(R.id.downLoadFilePath);
    }

    @Override
    public void onClick(View view) {
          if(view==findViewById(R.id.downloadconfirm)){
              String filePath=tv.getText().toString();
              if(filePath.equals("")){
                  new AlertDialog.Builder(this)
                          .setTitle("警告")
                          .setMessage("目前选择的文件路径为空，请重新选择你要下载的文件")
                          .setPositiveButton("确定", null)
                          .show();
              }else{
                  //开始上传之前检查是否存在这个文件
                  String portStr = new Integer(port).toString();
                  String can[]=new String[5];
                  can[0]=host;
                  can[1]=portStr;
                  can[2]=user;
                  can[3]=pass;
                  can[4]=filePath;
                  DownTask task=new DownTask(this);
                  task.execute(can);
              }
          }else if (view==findViewById(R.id.downloadChoose)){
              //跳去显示服务器的界面
                    Intent intent=new Intent(downloadActivity.this,selectFileActivity.class);
              intent.putExtra("host",host);
              intent.putExtra("user",user);
              intent.putExtra("pass",pass);
              intent.putExtra("port",port);
              //用0表示这个activity
              startActivityForResult(intent,0);
            }
        }

      public void onActivityResult(int resqCode,int respCode,Intent intent){
        if(respCode==0&&resqCode==0){
            String serverPath=intent.getStringExtra("filepath");
            Toast tot = Toast.makeText(
                    this,
                    "想要下载的文件路径已经找到！",
                    Toast.LENGTH_LONG);
            tot.show();
            tv.setText(serverPath);
        }
      }


    class DownTask extends AsyncTask<String,Long,Boolean> {
        Context mContext;
        ProgressDialog pdialog;

        public DownTask(Context ctx) {
            mContext = ctx;
        }

        protected  void onPreExecute(){
            pdialog=new ProgressDialog(mContext);
            pdialog.setTitle("任务正在执行中");
            pdialog.setMessage("正在下载中，敬请等待...");
            pdialog.setCancelable(false);
            pdialog.setMax(100);
            pdialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pdialog.setIndeterminate(false);
            pdialog.show();
        }

        protected void onProgressUpdate(Long... values){
            long a=Long.valueOf(values[0].toString());
            int value=(int) a;
            pdialog.setProgress(value);
        }

        @Override
        protected Boolean doInBackground(String... Params) {
            FTPClient ftpClient=new FTPClient();
            String host=Params[0];
            String portStr=Params[1];
            String user=Params[2];
            String pass=Params[3];
            String serverPath=Params[4];
            String localPath=Environment.getExternalStorageDirectory().getPath().toString() + "/1ftpData";
            boolean flag=false;
            int port=Integer.parseInt(portStr);
            try {
                ftpClient.setDataTimeout(6000);//设置连接超时时间
                ftpClient.setControlEncoding("utf-8");
                ftpClient.connect(host,port);
                flag=ftpClient.login(user,pass);
                if(!flag) return flag;
                FTPFile[] files = ftpClient.listFiles(serverPath);
                if (files.length == 0) {
                    return false;
                }
                localPath = localPath+"/" + files[0].getName();
                // 接着判断下载的文件是否能断点下载
                long serverSize = files[0].getSize(); // 获取远程文件的长度
                File localFile = new File(localPath);
                long localSize = 0;
                if (localFile.exists()) {
                        File file = new File(localPath);
                        file.delete();

                }
                    boolean newFile = localFile.createNewFile();
                // 进度
                long step = serverSize / 100;
                long process = 0;
                long currentSize = 0;
                // 开始准备下载文件
                ftpClient.enterLocalActiveMode();
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                OutputStream out = new FileOutputStream(localFile, true);
                ftpClient.setRestartOffset(localSize);
                InputStream input = ftpClient.retrieveFileStream(serverPath);
                byte[] b = new byte[1024];
                int length = 0;
                while ((length = input.read(b)) != -1) {
                    out.write(b, 0, length);
                    currentSize = currentSize + length;
                    if (currentSize / step != process) {
                        process = currentSize / step;
                            publishProgress(process);
                    }
                }
                out.flush();
                out.close();
                input.close();
                // 此方法是来确保流处理完毕，如果没有此方法，可能会造成现程序死掉
                if (ftpClient.completePendingCommand()) {
                    return true;
                } else {
                    return false;
                }
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }finally {
                return flag;
            }
        }

        protected void onPostExecute(Boolean flag){
            if(flag){
                Toast tot = Toast.makeText(
                        mContext,
                        "文件下载成功,保存在本地存储目录下的1ftpData文件夹",
                        Toast.LENGTH_LONG);
                tot.show();
            }else{
                new AlertDialog.Builder(mContext)
                        .setTitle("提示")
                        .setMessage("抱歉，下载过程中出现错误，请重新尝试")
                        .setPositiveButton("确定", null)
                        .show();

            }
            pdialog.dismiss();
        }
    }

    }
