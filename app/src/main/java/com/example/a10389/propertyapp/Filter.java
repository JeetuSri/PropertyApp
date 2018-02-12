package com.example.a10389.propertyapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.yahoo.mobile.client.android.util.rangeseekbar.RangeSeekBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static android.R.attr.filter;
import static android.R.attr.max;
import static android.R.attr.min;

public class Filter extends AppCompatActivity {

    Button submit,reset;
    RangeSeekBar<Integer> rangeSeekBar;
    RangeSeekBar<Integer> rangeSeekBar1;
    RangeSeekBar<Integer> rangeSeekBar2;
    Integer priceMaxVal=100000,priceMinVal=0,widthMaxVal=1000,widthMinVal=0,heightMaxVal=1000,heightMinVal=0;
    android.widget.ListView simplelist;
    TextView min,max,widthmax,widthmin,heightmax,heightmin;
    DisplayMetrics dm;
    String filterValues;
    String viewType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        submit=(Button)findViewById(R.id.submitBtn);
        reset=(Button)findViewById(R.id.reset);
        simplelist = (android.widget.ListView) findViewById(R.id.list);
        min=(TextView)findViewById(R.id.textView4);
        max=(TextView)findViewById(R.id.textView5);
        widthmin=(TextView)findViewById(R.id.textView6);
        widthmax=(TextView)findViewById(R.id.textView7);
        heightmin=(TextView)findViewById(R.id.textView8);
        heightmax=(TextView)findViewById(R.id.textView9);


      /*get the pop up*/

        getSupportActionBar().hide();
        dm=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width=dm.widthPixels;
        final int height=dm.heightPixels;
        getWindow().setLayout((int)(width*0.9),(int)(height*0.8));

        /*End of pop up*/

        /*Rang Seek Bar*/

        rangeSeekBar=(RangeSeekBar<Integer>)findViewById(R.id.rangeSeekbar);
        rangeSeekBar1=(RangeSeekBar<Integer>)findViewById(R.id.rangeSeekbar1);
        rangeSeekBar2=(RangeSeekBar<Integer>)findViewById(R.id.rangeSeekbar2);
        rangeSeekBar.setRangeValues(priceMinVal,priceMaxVal);
        rangeSeekBar1.setRangeValues(widthMinVal,widthMaxVal);
        rangeSeekBar2.setRangeValues(heightMinVal,heightMaxVal);

        Intent intent=getIntent();
        viewType=intent.getStringExtra("viewType");
        if(intent.getStringExtra("ranges") !=null){
          filterValues=intent.getStringExtra("ranges");
            try{
                JSONObject object=new JSONObject(filterValues);
                rangeSeekBar.setSelectedMinValue(object.getInt("priceMinValue"));
                rangeSeekBar.setSelectedMaxValue(object.getInt("priceMaxValue"));
                rangeSeekBar1.setSelectedMinValue(object.getInt("widthMinValue"));
                rangeSeekBar1.setSelectedMaxValue(object.getInt("widthMaxValue"));
                rangeSeekBar2.setSelectedMinValue(object.getInt("heightMinValue"));
                rangeSeekBar2.setSelectedMaxValue(object.getInt("heightMaxValue"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        /*Define function */
        reset();
        Rang();

        /*End function*/

        /*rangebar get Max Min Value*/
        rangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
                priceMinVal=minValue;
                priceMaxVal=maxValue;
                min.setText(String.valueOf(minValue));
                max.setText(String.valueOf(maxValue));

            }
        });

        rangeSeekBar1.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
                widthMinVal=minValue;
                widthMaxVal=maxValue;
                widthmin.setText(String.valueOf(minValue));
                widthmax.setText(String.valueOf(maxValue));
            }
        });

        rangeSeekBar2.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
                heightMaxVal=maxValue;
                heightMinVal=minValue;
                heightmin.setText(String.valueOf(minValue));
                heightmax.setText(String.valueOf(maxValue));
                Log.d("MaxValue", String.valueOf(maxValue));
                Log.d("MinValue",minValue.toString());
            }
        });

        /*End of Max Min function*/


    }

    public void reset(){
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rangeSeekBar.setSelectedMinValue(0);
                rangeSeekBar1.setSelectedMinValue(0);
                rangeSeekBar2.setSelectedMinValue(0);
                rangeSeekBar.setSelectedMaxValue(100000);
                rangeSeekBar1.setSelectedMaxValue(1000);
                rangeSeekBar2.setSelectedMaxValue(1000);

            }
        });
    }


    public void Rang() {
        submit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                StringBuffer data=new StringBuffer();
                data.append("{\"priceMaxValue\":\""+priceMaxVal+"\",");
                data.append("\"priceMinValue\":\""+priceMinVal+"\",");
                data.append("\"widthMaxValue\":\""+widthMaxVal+"\",");
                data.append("\"widthMinValue\":\""+widthMinVal+"\",");
                data.append("\"heightMaxValue\":\""+heightMaxVal+"\",");
                data.append("\"heightMinValue\":\""+heightMinVal+"\"}");

                Intent intent=new Intent(Filter.this,ProductDetail.class);
                intent.putExtra("filter",data.toString());
                intent.putExtra("viewType",viewType);
                startActivity(intent);
            }
        });

    }
}
