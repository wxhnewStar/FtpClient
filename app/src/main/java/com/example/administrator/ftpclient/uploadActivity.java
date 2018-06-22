package com.example.administrator.ftpclient;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
        user=intent.getStringExtra("uesr");
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
                //开始上传
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
                    // Get the file instance
                    // File file = new File(path);
                    // Initiate the upload
                }else{
                    Log.e(TAG,"选择失败");
            }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}
