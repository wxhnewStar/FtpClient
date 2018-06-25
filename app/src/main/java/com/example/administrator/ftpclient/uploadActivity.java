package com.example.administrator.ftpclient;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
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
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;

public class uploadActivity extends AppCompatActivity implements View.OnClickListener{
    private String host,user,pass;
    private int port;
    private Button choose,upload;
    private TextView tv;
    private static final int FILE_SELECT_CODE = 0;
    private static final String TAG = "ChooseFile";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        Intent intent=getIntent();
        host=intent.getStringExtra("host");
        user=intent.getStringExtra("user");
        pass=intent.getStringExtra("pass");
        port =intent.getIntExtra("port",21);
        choose=(Button)findViewById(R.id.uploadChoose);
        upload=(Button)findViewById(R.id.uploadconfirm);
        tv=(TextView) findViewById(R.id.uploadFilePath);
        choose.setOnClickListener(this);
        upload.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view==findViewById(R.id.uploadChoose)){
            showFileChooser();
        }
        else if(view==findViewById(R.id.uploadconfirm)){
            String filePath=tv.getText().toString();
            if(filePath.equals("")){
                new AlertDialog.Builder(this)
                        .setTitle("警告")
                        .setMessage("目前选择的文件路径为空，请选择您要上传的文件")
                        .setPositiveButton("确定", null)
                        .show();
            }else{
                //开始上传之前检查是否存在这个文件
                File file=new File(filePath);
                if(file.exists()) {
                    String portStr = new Integer(port).toString();
                    String can[]=new String[5];
                    can[0]=host;
                    can[1]=portStr;
                    can[2]=user;
                    can[3]=pass;
                    can[4]=filePath;
                    UpTask task=new UpTask(this);
                    task.execute(can);
                }
                else{
                    new AlertDialog.Builder(this)
                            .setTitle("警告")
                            .setMessage("目前选择的文件路径无法找到对应文件，请再次尝试选择")
                            .setPositiveButton("确定", null)
                            .show();
                }
            }
        }
    }


    //跳转到选择文件界面
    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult( Intent.createChooser(intent, "Select a File to Upload"), FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    //继承的回调函数，以处理得到文件的路径
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    Log.d(TAG, "File Uri: " + uri.toString());
                    // Get the path
                    try{
                        String path = FileUtils.getPath(this, uri);
                        Log.d(TAG, "File Path: " + path);
                        tv.setText(path);
                    }catch (URISyntaxException e){
                        Log.d(TAG,"地址解析出错");
                    }
                }else{
                    Log.e(TAG,"选择失败");
            }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    class UpTask extends AsyncTask<String,Long,Boolean>{
        Context mContext;
        ProgressDialog pdialog;
        public UpTask(Context ctx){
            mContext=ctx;
        }

        protected Boolean doInBackground(String... Params){
            String host=Params[0];
            String portStr=Params[1];
            String user=Params[2];
            String pass=Params[3];
            String filePath=Params[4];
            String serverPath="/phoneData";
            int port=21;
            Boolean flag=false;
            System.out.println("进入上传异步函数了");
            port = Integer.parseInt(portStr);
            FTPClient ftpClient=new FTPClient();
            try{
                ftpClient.setDataTimeout(6000);//设置连接超时时间
                ftpClient.setControlEncoding("utf-8");
                ftpClient.connect(host,port);
                System.out.println("工作文件夹为:"+ftpClient.printWorkingDirectory());
                flag=ftpClient.login(user,pass);
                if(!flag) return  flag;
                ftpClient.changeWorkingDirectory(serverPath);
                File localFile = new File(filePath);
                System.out.println("本地文件存在，名称为：" + localFile.getName());
                System.out.println("服务器文件存放路径：" + serverPath +"/"+ localFile.getName());
                String fileName = localFile.getName();
                // 如果本地文件存在，服务器文件也在，上传文件，这个方法中也包括了断点上传
                long localSize = localFile.length(); // 本地文件的长度
                FTPFile[] files = ftpClient.listFiles(fileName);
                long serverSize = 0;
                if (files.length == 0) {
                    System.out.println("服务器文件不存在");
                    serverSize = 0;
                } else {
                    serverSize = files[0].getSize(); // 服务器文件的长度
                }
                if (localSize <= serverSize) {
                    if (ftpClient.deleteFile(fileName)) {
                        System.out.println("服务器文件存在,删除文件,开始重新上传");
                        serverSize = 0;
                    }
                }
                RandomAccessFile raf = new RandomAccessFile(localFile, "r");
                // 进度
                long step = localSize / 100;
                long process = 0;
                int currentSize = 0;
                // 好了，正式开始上传文件
                ftpClient.enterLocalPassiveMode();
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.setRestartOffset(serverSize);
                raf.seek(serverSize);
                OutputStream output = ftpClient.appendFileStream(fileName);
                byte[] b = new byte[1024];
                int length = 0;
                while ((length = raf.read(b)) != -1) {
                    output.write(b, 0, length);
                    currentSize = currentSize + length;
                    if (currentSize / step != process) {
                        process = currentSize / step;
                        if (process % 5 == 0) {
                            publishProgress(process);
                        }
                    }
                }
                output.flush();
                output.close();
                raf.close();
                if (ftpClient.completePendingCommand()) {
                    System.out.println("文件上传成功");
                    return true;
                } else {
                    System.out.println("文件上传失败");
                    return false;
                }
            }catch (Exception e){
                System.out.println("上传过程中出错");
                e.printStackTrace();
                return  false;
            }finally {
                try{
                   if(ftpClient.isConnected())  ftpClient.disconnect();
                }catch (Exception e1){

                }
                return flag;
            }
        }


        protected  void onPreExecute(){
            pdialog=new ProgressDialog(mContext);
            pdialog.setTitle("任务正在执行中");
            pdialog.setMessage("正在上传中，敬请等待...");
            pdialog.setCancelable(false);
            pdialog.setMax(100);
            pdialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pdialog.setIndeterminate(false);
            pdialog.show();
        }

        protected void onProgressUpdate(Long... values){
            long a=Long.valueOf(values[0].toString());
            System.out.println("显示进度为"+a);
            int value=(int) a;
            pdialog.setProgress(value);
        }

        protected void onPostExecute(Boolean flag){
            System.out.println("处理结果函数内部");
            if(flag){
                Toast tot = Toast.makeText(
                        mContext,
                        "文件上传成功",
                        Toast.LENGTH_LONG);
                tot.show();
            }else{
                System.out.println("处理结果函数错误分支");
                new AlertDialog.Builder(mContext)
                        .setTitle("提示")
                        .setMessage("抱歉，上传过程中出现错误，请重新尝试")
                        .setPositiveButton("确定", null)
                        .show();

            }
            pdialog.dismiss();
        }
    }
}
