package com.example.a10389.propertyapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by 10389 on 11/20/2017.
 */

public class DownLoadImageWithURLTask extends AsyncTask<String,String,Bitmap> {
    ImageView view;
    public DownLoadImageWithURLTask() {

    }

    public DownLoadImageWithURLTask(ImageView image) {
        view=image;
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        String fileToPath=strings[0];
        Bitmap bitmap=null;
        try {
            InputStream in=new URL(fileToPath).openStream();
            bitmap= BitmapFactory.decodeStream(in);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        int width=200;
        int height=200;
        if(bitmap!=null){
            bitmap=Bitmap.createScaledBitmap(bitmap,width,height,true);
            view.setImageBitmap(bitmap);
        }
    }
}
