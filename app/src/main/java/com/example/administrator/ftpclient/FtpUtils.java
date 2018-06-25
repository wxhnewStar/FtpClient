package com.example.administrator.ftpclient;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class FtpUtils {
    private FTPClient ftpClient;
    private String strencoding;

    /**
     * init ftp servere
     */
    public FtpUtils() {

    }


    public void connectServer(String ip, int port, String userName, String userPwd, String path) {
        ftpClient = new FTPClient();
        try {
            // 连接
            ftpClient.connect(ip, port);
            // 登录
            ftpClient.login(userName, userPwd);
            ftpClient.setControlEncoding("GBK");
            if (path != null && path.length() > 0) {
                // 跳转到指定目录
                ftpClient.changeWorkingDirectory(path);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @throws IOException function:关闭连接
     */
    public void closeServer() {
        if (ftpClient.isConnected()) {
            try {
                ftpClient.logout();
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param path
     * @return function:读取指定目录下的文件名
     * @throws IOException
     */
    public List<wxhFile> getFileList(String path) throws ParseException {
        List<wxhFile> fileLists = new ArrayList<wxhFile>();
        // 获得指定目录下所有文件名
        FTPFile[] ftpFiles = null;
        try {
            ftpFiles = ftpClient.listFiles(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; ftpFiles != null && i < ftpFiles.length; i++) {
            FTPFile file = ftpFiles[i];
            if (file.isFile()) {
                System.out.println("文件夹下面的文件====="+file.getName());
                wxhFile a=new wxhFile(path+file.getName(),file.getName(),1);
                fileLists.add(a);
            }else if(file.isDirectory()){
                String name=file.getName();
                if(name.equals(".")||name.equals("..")) continue;
                System.out.println("文件夹名称为====="+name);
                wxhFile a=new wxhFile(path+file.getName(),file.getName(),0);
                fileLists.add(a);
                }
            }

        return fileLists;
    }

    /**
     * @param fileName
     * @return function:从服务器上读取指定的文件
     * @throws ParseException
     * @throws IOException
     */
    public String readFile(String fileName) throws ParseException {
        InputStream ins = null;
        StringBuilder builder = null;
        try {
            // 从服务器上读取指定的文件
            ins = ftpClient.retrieveFileStream(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(ins, strencoding));
            String line;
            builder = new StringBuilder(150);
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                builder.append(line);
            }
            reader.close();
            if (ins != null) {
                ins.close();
            }
            // 主动调用一次getReply()把接下来的226消费掉. 这样做是可以解决这个返回null问题
            ftpClient.getReply();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    /**
     * @param fileName function:删除文件
     */
    public void deleteFile(String fileName) {
        try {
            ftpClient.deleteFile(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}


class wxhFile {
    String filePath;
    boolean isFile;
    String filename;
    boolean isParent=false;
    wxhFile(String path,String name,int flag){
        if(flag==0) isFile=false;
        else if(flag==1)isFile=true;
        else if(flag==2){
            isParent=true;
        }
        filePath=path;
        filename=name;
    }
    boolean ifFile(){
        return isFile;
    }
}