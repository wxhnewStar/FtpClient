package com.example.administrator.ftpclient;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;

public class selectFileActivity extends AppCompatActivity implements AdapterView.OnItemClickListener ,View.OnClickListener{
     String host,user,pass;
     int port;
     Button bt;
    ListView listView;
    TextView textview;
    String currentParent="";
    List<wxhFile> currentFiles=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_file);
        Intent intent =getIntent();
        //接收数据
        host=intent.getStringExtra("host");
        user=intent.getStringExtra("user");
        pass=intent.getStringExtra("pass");
        port =intent.getIntExtra("port",21);
        listView =(ListView) findViewById(R.id.list);
        textview=(TextView) findViewById(R.id.path);
        bt=(Button) findViewById(R.id.parent);
        bt.setOnClickListener(this);
        listView.setOnItemClickListener(this);
        String portStr = new Integer(port).toString();
        String can[]=new String[5];
        can[0]=host;
        can[1]=portStr;
        can[2]=user;
        can[3]=pass;
        can[4]="";
        GetTask task=new GetTask(this);
        task.execute(can);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
           if(currentFiles.get(position).isFile){
               //找好了那就返回啦
               System.out.println("这次选中的是文件，开始返回啦");
               Intent intent=getIntent();
               intent.putExtra("filepath",currentFiles.get(position).filePath);
               selectFileActivity.this.setResult(0,intent);
               selectFileActivity.this.finish();
           }else {

               System.out.println("这次选中的是文件夹，继续选择");
               String can[]=new String[5];
               can[0]=host;
               can[1]=new Integer(port).toString();
               can[2]=user;
               can[3]=pass;
               can[4]=currentFiles.get(position).filePath+"/";
               //在点击进入新文件夹之前记住现在的父亲路径是谁
               if(!(currentParent.equals(""))){
                   String  s=currentFiles.get(position).filePath+"/" ;
                   int length=s.length();
                   int fir=s.indexOf('/');
                   if(fir==length-1){
                       currentParent="";
                   }else {
                       s=s.substring(0, length-1);
                       s=s.substring(0,s.lastIndexOf('/')+1);
                       currentParent=s;
                   }
               }
               //System.out.println("访问之前的父亲路径是："+currentParent);
               GetTask task=new GetTask(this);
               task.execute(can);
           }
    }

    @Override
    public void onClick(View view) {
        //如果目前文件列表没有加载过且parent路径为空，说明是在最前面
        if(currentParent.equals("")&&currentFiles==null){
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("这已经是最前面的文件夹了！")
                    .setPositiveButton("确定", null)
                    .show();
        }else{
          //  System.out.println("返回："+currentParent);
            String portStr = new Integer(port).toString();
            String can[]=new String[5];
            can[0]=host;
            can[1]=portStr;
            can[2]=user;
            can[3]=pass;
            can[4]=currentParent;
            GetTask task=new GetTask(this);
            task.execute(can);
        }
    }

    class GetTask extends AsyncTask<String,Void,List<wxhFile>> {
        Context mContext;
        public GetTask(Context ctx){
            mContext=ctx;
        }

        protected List<wxhFile> doInBackground(String... Params){
            String host=Params[0];
            String portStr=Params[1];
            String user=Params[2];
            String pass=Params[3];
            String path=Params[4];
            FtpUtils util=new FtpUtils();
            List <wxhFile> list=null;
            int port=21;
            System.out.println("进入异步函数了");
            port = Integer.parseInt(portStr);
            try{
                util.connectServer(host,port,user,pass,"");
                list=util.getFileList(path);
                //附带父亲文件夹的路径进去
                list.add(new wxhFile(path,"",2));
            }catch (Exception e){
                System.out.println("开启连接出错");
                e.printStackTrace();
            }finally {
                try{
                    util.closeServer();
                }catch(Exception e){
                    e.printStackTrace();;
                }
                return list;
            }
        }

        protected void onPostExecute(List<wxhFile> list){
            String parentPath=null;
            for(int i=0;i<list.size();i++){
                if(list.get(i).isParent) {
                    parentPath=list.get(i).filePath;
                    list.remove(i);
                }
            }
            System.out.println("处理结果函数内部");
            if(list==null||list.size()==0){
                System.out.println("空文件夹或者访问出错");
                new AlertDialog.Builder(mContext)
                        .setTitle("提示")
                        .setMessage("这个文件夹是个空文件夹！")
                        .setPositiveButton("确定", null)
                        .show();

            }else{
                System.out.println("找到了！！");
                for(int i=0;i<list.size();i++){
                    System.out.println("文件名字："+list.get(i).filePath+"是不是文件："+list.get(i).isFile);
                }
                //更新当前父亲路径以及文件list
                currentFiles=list;
                //调用刷新来显示我们的列表
                inflateListView(list,parentPath);
            }
        }
    }

    private void inflateListView (List<wxhFile> list,String parentPath){
        List<Map<String, Object>> listItems=new ArrayList<Map<String,Object>>();
        for (int i=0;i<list.size();i++){
            Map<String,Object> listitem=new HashMap<String,Object>();
            if(list.get(i).isFile) {
                listitem.put("icon",R.mipmap.document);
            }else{
                listitem.put("icon",R.mipmap.folder);
            }
            listitem.put("filename",list.get(i).filename);
            listItems.add(listitem);
        }
        //简单适配器的配置
        SimpleAdapter simpleAdapter= new SimpleAdapter(this ,listItems,R.layout.line,new String[]{"icon","filename"}
        ,new int[]{R.id.file_icon,R.id.file_name});
         listView.setAdapter(simpleAdapter);
        textview.setText("当前路径为："+parentPath);

    }
}
