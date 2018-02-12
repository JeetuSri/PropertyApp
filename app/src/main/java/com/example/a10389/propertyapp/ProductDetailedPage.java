package com.example.a10389.propertyapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProductDetailedPage extends AppCompatActivity implements BaseSliderView.OnSliderClickListener{

   //variables for view
    HashMap<String,File> hashMapForFile;
    String productObject=null;
    SliderLayout sliderLayout;
    TextView productBrandName,productModelName,productLocation,
            productPrice,productWidth,productHeight,productLat,productLong;
    Button editbtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detailed_page);
        getSupportActionBar().setTitle("Property Detail");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //get ProductObject through Intent
        Intent myIntent=getIntent();
        productObject=myIntent.getStringExtra("data");

        //cast variables
        sliderLayout=(SliderLayout) findViewById(R.id.prodimage);
        productBrandName=(TextView)findViewById(R.id.prodBrandName);
        productModelName=(TextView)findViewById(R.id.prodModelName);
        productLocation=(TextView)findViewById(R.id.prodLocation);
        productPrice=(TextView)findViewById(R.id.prodPrice);
        productWidth=(TextView)findViewById(R.id.prodWidth);
        productHeight=(TextView)findViewById(R.id.prodHeight);
        productLat=(TextView)findViewById(R.id.prodLatitude);
        productLong=(TextView)findViewById(R.id.prodLongitude);
        editbtn=(Button)findViewById(R.id.editbutton);

        //method calls
        moveToUpdatePage();
        setContents();

    }

    //button clickListener method to move UpdatePage
    private void moveToUpdatePage() {
        editbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ProductDetailedPage.this,PropertyUpdate.class);
               intent.putExtra("data",productObject);
                startActivity(intent);

            }
        });//end clickListener()
    }//end moveToUpdatePage

    //method set the values to view to show Detail
    private void setContents() {

        try {
            JSONObject product=new JSONObject(productObject);
            productBrandName.setText(product.getString("brandName"));
            productModelName.setText(product.getString("modelName"));
            productLocation.setText(product.getString("locationTag"));
            productPrice.setText(product.getString("price"));
            productWidth.setText(product.getString("width"));
            productHeight.setText(product.getString("height"));
            productLat.setText(product.getString("latitude"));
            productLong.setText(product.getString("longitude"));
            JSONArray array=product.getJSONArray("imageList");
            hashMapForFile = new HashMap<String,File>();

            for(int j=0;j<array.length();j++) {
                JSONObject jsonObj = array.getJSONObject(j);
                String image = jsonObj.getString("shortImage");
                Bitmap bitmap=new DownLoadImageWithURLTask().doInBackground("https://storage.googleapis.com/image-video/"+image);
                File file=new File(Environment.getExternalStorageDirectory()+File.separator+""+bitmap+".jpg");
                FileOutputStream bytes=new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,bytes);
                hashMapForFile.put( String.valueOf(j),file);
            }

            if(hashMapForFile.size() == 0){
                sliderLayout.setVisibility(View.GONE);
            }else{
                //Slider Layout for images
                for(String name : hashMapForFile.keySet()){
                    TextSliderView textSliderView=new TextSliderView(this);
                    textSliderView
                            .image(hashMapForFile.get(name))
                            .setScaleType(BaseSliderView.ScaleType.Fit)
                            .setOnSliderClickListener(this);
                    sliderLayout.addSlider(textSliderView);
                }
                sliderLayout.setPresetTransformer(SliderLayout.Transformer.DepthPage);
                sliderLayout.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
                sliderLayout.stopAutoCycle();

            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }//end try/catch
    }//end setContents

    @Override
    protected void onStop() {
        sliderLayout.stopAutoCycle();
        super.onStop();
    }

    @Override
    public void onSliderClick(BaseSliderView slider) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent=new Intent(this,ProductDetail.class);
        intent.putExtra("data","first");
        startActivity(intent);
        return true;
    }
}

