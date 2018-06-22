package com.example.administrator.ftpclient;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.net.URISyntaxException;

/**
 * Created by Administrator on 2018/6/22 0022.
 */
/**
 * 一个专门用来对选择的文件路径处理的类
 */

public class FileUtils {
    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { MediaStore.MediaColumns.DATA };
            Cursor cursor = null;
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                cursor.moveToFirst();
                int column_index = cursor.getColumnIndexOrThrow(projection[0]);
           String filepath;
           filepath=cursor.getString(column_index);
           cursor.close();
           return  filepath;
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

}
