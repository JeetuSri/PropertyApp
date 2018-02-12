package com.example.a10389.propertyapp;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Base64;

import com.google.common.primitives.Bytes;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Blob;

/**
 * Created by 10389 on 12/13/2017.
 */

public class UploadImageToCloud extends AsyncTask<Bitmap,Void,Void> {

    String googleCloudUrl="https://www.googleapis.com/upload/storage/v1/b/image-video/o";
    String imageName;

    public UploadImageToCloud(String userImageName) {
        imageName=userImageName;

    }

    @Override
    protected Void doInBackground(Bitmap... bitmap) {

        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        bitmap[0].compress(Bitmap.CompressFormat.PNG,100,bos);
        byte[] image=bos.toByteArray();

            HttpURLConnection conn = null;
            try {

                URL postUrl = new URL(googleCloudUrl+"?name="+imageName);
                conn = (HttpURLConnection) postUrl.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type","image/jpeg");
                conn.setRequestProperty("uploadType","media");
                conn.connect();
                DataOutputStream dos=new DataOutputStream(conn.getOutputStream());
                dos.write(image);
                dos.close();
                int status=conn.getResponseCode();
                if(status == 200){
                    //
                }

            } catch (IOException e) {
//            Log.i(Tag,"Responce:" +data.toString());
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(conn!=null){
                    conn.disconnect();
                }
            }
        return null;
    }
}
