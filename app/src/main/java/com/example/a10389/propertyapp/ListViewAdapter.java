package com.example.a10389.propertyapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.DialogPreference;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.Inflater;

/**
 * Created by 10389 on 11/20/2017.
 */

public class ListViewAdapter extends BaseAdapter{
    public static final int DELETEPROP=5;
    Context context;
    LayoutInflater inflater;
    DatabaseHelper myDb;
    ArrayList<JSONObject> jsonArray;
    ImageView image,delete,detail;
    ArrayList<HashMap<String, String>> data;
    HashMap<String, String> result = new HashMap<String, String>();

    public ListViewAdapter(Context context, ArrayList<HashMap<String, String>> data, ArrayList<JSONObject> jsonArray) {
        this.context = context;
        this.data = data;
        this.jsonArray=jsonArray;

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
        TextView brandName;
        TextView modelName;
        TextView price;
        TextView remark,width,height;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View listView = inflater.inflate(R.layout.sublistview, parent, false);

        //get data with respect to position
        result = data.get(i);

        // cast variables
        brandName = (TextView) listView.findViewById(R.id.brandName);
        modelName = (TextView) listView.findViewById(R.id.modelName);
        price = (TextView) listView.findViewById(R.id.price);
        remark = (TextView) listView.findViewById(R.id.remark);
        image = (ImageView) listView.findViewById(R.id.image);
        width=(TextView)listView.findViewById(R.id.width);
        height=(TextView)listView.findViewById(R.id.height);
        delete=(ImageView)listView.findViewById(R.id.deleteicon);
        detail=(ImageView)listView.findViewById(R.id.detailicon);
        myDb=new DatabaseHelper(context);

        //set the data to view
        brandName.setText(result.get(ListView.BRANDNAME));
        modelName.setText(result.get(ListView.MODELNAME));
        price.setText(result.get(ListView.PRICE));
        width.setText(result.get(ListView.WIDTH));
        height.setText(result.get(ListView.HEIGHT));
        remark.setText(result.get(ListView.REMARK));
        DownLoadImageWithURLTask imageTask = new DownLoadImageWithURLTask(image);
        imageTask.execute("https://storage.googleapis.com/image-video/" + result.get(ListView.IMAGE));

        //onclickListener to navigate to detail page
        listView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context,ProductDetailedPage.class);
                intent.putExtra("data",jsonArray.get(i).toString());
                context.startActivity(intent);
            }
        });//end onClickListener()

        //onClickListener to delete the property
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder alert=new AlertDialog.Builder(context);
                alert.setTitle("Are you sure to delete?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialogInterface, int j) {
                        deleteProperty(jsonArray.get(i));
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

        });//end onClickListener()

        //onclickListener to navigate to detail page
        detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context,ProductDetailedPage.class);
                intent.putExtra("data",jsonArray.get(i).toString());
                context.startActivity(intent);
            }
        });//end onClickListener()

        return listView;
    }

    //delete property from view and also from db
    private void deleteProperty(JSONObject jsonObject)  {
        JSONObject loggedUser=myDb.getUser();
        StringBuffer productObject=new StringBuffer();
        productObject.append("[");
        productObject.append(jsonObject);
        productObject.append("]");

        try {
            String response=new HttpCalls(DELETEPROP).doInBackground(loggedUser.getString("access_token"),productObject.toString());
            context.startActivity(new Intent(context,ListView.class));
        } catch (JSONException e) {
            e.printStackTrace();
        }//end try/catch
    }//end deleteProperty()


}
