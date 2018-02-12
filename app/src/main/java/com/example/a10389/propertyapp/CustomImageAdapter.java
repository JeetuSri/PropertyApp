package com.example.a10389.propertyapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by 10389 on 12/5/2017.
 */

public class CustomImageAdapter extends BaseAdapter{


    OnDataChangeListener mOnDataChangeListener;
    Context context;
    LayoutInflater inflater;
    ImageView image;
    ArrayList<HashMap<Bitmap,String>> data;

    public CustomImageAdapter(Context context, ArrayList<HashMap<Bitmap, String>> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {

        return data.size();
    }

    @Override
    public Object getItem(int i) {

        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View view, ViewGroup parent) {
        Button delete;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View listView = inflater.inflate(R.layout.imagelist, parent, false);

        //get the data with position
        final HashMap<Bitmap, String> result=data.get(i);
        final Bitmap bitmap=result.keySet().iterator().next();

        //set the value to image
        delete = (Button) listView.findViewById(R.id.deletebutton);
        image = (ImageView) listView.findViewById(R.id.productImage);
        image.setImageBitmap(bitmap);

        //delete clickListener to delete the image
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder alert=new AlertDialog.Builder(context);
                alert.setTitle("Are you sure to delete?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialogInterface, int j) {
                        deleteImage(result);
                    }
                });
                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                alert.show();
            }
        });
        return listView;
    }

    public void setmOnDataChangeListener(OnDataChangeListener mOnDataChangeListener) {
        this.mOnDataChangeListener = mOnDataChangeListener;
    }

    //method to delete the image
    private void deleteImage(HashMap<Bitmap, String> result) {
        data.remove(result);
        notifyDataSetChanged();
        if(mOnDataChangeListener !=null){
            mOnDataChangeListener.onDataChanged(data.size());
        }//end if()
    }//end deleteImage()

    public interface OnDataChangeListener{
        public void onDataChanged(int Size);
    }

}