package com.example.a10389.propertyapp;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.HttpAuthHandler;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by 10389 on 11/15/2017.
 */

public class ListView extends Fragment implements SearchView.OnQueryTextListener,View.OnClickListener{
    public static final int GETPROP=2;
    public static final int SEARCHPROP=6;
    DatabaseHelper myDb;
    JSONArray productListArray;
    ArrayList<JSONObject> jsonArray=new ArrayList<>();
    ArrayList<HashMap<String,String>> productList=new ArrayList<>();
    ListViewAdapter adapter;
    JSONObject productJsonObject;
    int pageSize=3;
    android.widget.ListView view;
    public static String MODELNAME="modelName";
    public static String BRANDNAME="brandName";
    public static String PRICE="price";
    public static String REMARK="remark";
    public static String IMAGE="image";
    public static String WIDTH="width";
    public static String HEIGHT="height";

    //variables for filter
    private Boolean isFabOpen=false;
    private FloatingActionButton fab,fab1,fab2;
    private Animation fab_open,fab_close,rotate_forward,rotate_backward;
    android.widget.ListView simplelist;

    static String filterValue;
    public static ListView newInstance(String filterData){
        ListView fragment=new ListView();
        filterValue=filterData;
        return fragment;
    }

    public static ListView newInstance(){
        ListView fragment=new ListView();
        return fragment;
    }
    android.widget.ListView listView;
    private View mview;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
             myDb=new DatabaseHelper(getActivity());
            StringBuilder sb=new StringBuilder();
            try {
                JSONObject loggedUser=myDb.getUser();
                String response=new HttpCalls(GETPROP).doInBackground(loggedUser.getString("access_token"),loggedUser.getString("authorities"),loggedUser.getString("user"),String.valueOf(1),String.valueOf(pageSize));
                Log.d("PROPERTIES",response);
                productListArray=new JSONArray(response);
                if(filterValue !=null){
                    JSONObject filter=new JSONObject(filterValue);
                    for(int i=0;i<productListArray.length();i++){
                        HashMap<String,String> map=new HashMap<>();
                        productJsonObject=productListArray.getJSONObject(i);
                        int maxprice=Integer.parseInt(filter.getString("priceMaxValue"));
                        int minprice=Integer.parseInt(filter.getString("priceMinValue"));
                        int maxwidth=Integer.parseInt(filter.getString("widthMaxValue"));
                        int minwidth=Integer.parseInt(filter.getString("widthMinValue"));
                        int maxheight=Integer.parseInt(filter.getString("heightMaxValue"));
                        int minheight=Integer.parseInt(filter.getString("heightMinValue"));
                        int price=Integer.parseInt(productJsonObject.getString("price"));
                        int height=Integer.parseInt(productJsonObject.getString("height"));
                        int width=Integer.parseInt(productJsonObject.getString("width"));
                        if((maxprice > price && minprice <= price) && (maxwidth > width && minwidth <= width ) && (maxheight > height && minheight < height)){
                            map.put("id",productJsonObject.getString("productId"));
                            map.put("brandName",productJsonObject.getString("brandName"));
                            map.put("modelName",productJsonObject.getString("modelName"));
                            map.put("price",productJsonObject.getString("price"));
                            map.put("remark",productJsonObject.getString("remark"));
                            map.put("locationTag",productJsonObject.getString("locationTag"));
                            map.put("width",productJsonObject.getString("width"));
                            map.put("height",productJsonObject.getString("height"));
                            map.put("latitude",productJsonObject.getString("latitude"));
                            map.put("longitude",productJsonObject.getString("longitude"));
                            JSONArray imagelist=productJsonObject.getJSONArray("imageList");

                            for(int j=0;j<imagelist.length();j++){
                                JSONObject image=imagelist.getJSONObject(0);
                                map.put("image",image.getString("shortImage"));
                            }

                            productList.add(map);
                            jsonArray.add(productJsonObject);
                        }
                    }
                }else{
                    for(int i=0;i<productListArray.length();i++){
                    HashMap<String,String> map=new HashMap<>();
                     productJsonObject=productListArray.getJSONObject(i);
                    map.put("id",productJsonObject.getString("productId"));
                    map.put("brandName",productJsonObject.getString("brandName"));
                    map.put("modelName",productJsonObject.getString("modelName"));
                    map.put("price",productJsonObject.getString("price"));
                    map.put("remark",productJsonObject.getString("remark"));
                    map.put("locationTag",productJsonObject.getString("locationTag"));
                    map.put("width",productJsonObject.getString("width"));
                    map.put("height",productJsonObject.getString("height"));
                    map.put("latitude",productJsonObject.getString("latitude"));
                    map.put("longitude",productJsonObject.getString("longitude"));
                    JSONArray imagelist=productJsonObject.getJSONArray("imageList");

                    for(int j=0;j<imagelist.length();j++){
                        JSONObject image=imagelist.getJSONObject(0);
                        map.put("image",image.getString("shortImage"));
                    }
                    productList.add(map);
                    jsonArray.add(productJsonObject);
                }
                }
                Log.d("productJsonObjectLIST",productList.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

   @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       View mview = inflater.inflate(R.layout.navigation_listview,container,false);
       simplelist=(android.widget.ListView) mview.findViewById(R.id.list);
       adapter=new ListViewAdapter(getActivity(),productList,jsonArray);
       simplelist.setAdapter(adapter);


       //for filter
       fab = (FloatingActionButton)mview.findViewById(R.id.fab);
       fab1 = (FloatingActionButton)mview.findViewById(R.id.fab1);
       fab2 = (FloatingActionButton)mview.findViewById(R.id.fab2);
       fab_open = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.fb_open);
       fab_close = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.fab_close);
       rotate_forward = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.rotate_forward);
       rotate_backward = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.rotate_backward);
       fab.setOnClickListener(this);
       fab1.setOnClickListener(this);
       fab2.setOnClickListener(this);
       return mview;

    }

    //animation method for filter buttons
    public void animateFAB(){

        if(isFabOpen){
            fab.setImageResource(R.mipmap.ic_launcher_filter_list);
            fab.startAnimation(rotate_backward);
            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab1.setClickable(false);
            fab2.setClickable(false);
            isFabOpen = false;
        } else {
            fab.setImageResource(R.mipmap.ic_launcher_cancel);
            fab.startAnimation(rotate_forward);
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            fab1.setClickable(true);
            fab2.setClickable(true);
            isFabOpen = true;

        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        simplelist.setOnScrollListener(new PaginationScrollListener(jsonArray.size()) {
            @Override
            protected boolean onLoadMore(int i, int totalItemCount) {
                loadNextDataFromApi(i);
                return true;
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,inflater);
       /* getActivity().getMenuInflater().inflate(R.menu.search,menu);*/
        MenuItem menuItem = menu.findItem(R.id.search_id);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.d("qr",query);

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Log.d("new", newText);
        if (newText == null || newText.trim().isEmpty()) {
            adapter = new ListViewAdapter(getActivity(), productList, jsonArray);
            simplelist.setAdapter(adapter);
        } else {
            searchProperty(newText);
        }
        return false;
    }

    public void searchProperty(String property) {

        try {
            JSONObject loggedUser = myDb.getUser();
            String res = new HttpCalls(SEARCHPROP).doInBackground(loggedUser.getString("access_token"),property);
            Log.d("PROPERTIES", res);
            productListArray = new JSONArray(res);
            productList=new ArrayList<>();
            for (int i = 0; i < productListArray.length(); i++) {
                HashMap<String, String> map = new HashMap<>();
                productJsonObject = productListArray.getJSONObject(i);
                map.put("brandName", productJsonObject.getString("brandName"));
                map.put("modelName", productJsonObject.getString("modelName"));
                map.put("price", productJsonObject.getString("price"));
                map.put("remark", productJsonObject.getString("remark"));
                map.put("locationTag", productJsonObject.getString("locationTag"));
                map.put("width", productJsonObject.getString("width"));
                map.put("height", productJsonObject.getString("height"));
                map.put("latitude", productJsonObject.getString("latitude"));
                map.put("longitude", productJsonObject.getString("longitude"));
                JSONArray imagelist = productJsonObject.getJSONArray("imageList");

                for (int j = 0; j < imagelist.length(); j++) {
                    JSONObject image = imagelist.getJSONObject(0);
                    map.put("image", image.getString("shortImage"));
                }
                productList.add(map);
                jsonArray.add(productJsonObject);
                adapter = new ListViewAdapter(getActivity(), productList, jsonArray);
                simplelist.setAdapter(adapter);
            }
//            Log.d("productJsonObjectLIST", productList.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter.notifyDataSetChanged();
    }

    private void loadNextDataFromApi(int page) {
        try {
            JSONObject loggedUser=myDb.getUser();
            String response=new HttpCalls(GETPROP).doInBackground(loggedUser.getString("access_token"),loggedUser.getString("authorities"),loggedUser.getString("user"),String.valueOf(page),String.valueOf(pageSize));
            Log.d("PROPERTIES",response);
            productListArray=new JSONArray(response);
            if(filterValue !=null){
                JSONObject filter=new JSONObject(filterValue);
                for(int i=0;i<productListArray.length();i++){
                    HashMap<String,String> map=new HashMap<>();
                    productJsonObject=productListArray.getJSONObject(i);
                    int maxprice=Integer.parseInt(filter.getString("priceMaxValue"));
                    int minprice=Integer.parseInt(filter.getString("priceMinValue"));
                    int maxwidth=Integer.parseInt(filter.getString("widthMaxValue"));
                    int minwidth=Integer.parseInt(filter.getString("widthMinValue"));
                    int maxheight=Integer.parseInt(filter.getString("heightMaxValue"));
                    int minheight=Integer.parseInt(filter.getString("heightMinValue"));
                    int price=Integer.parseInt(productJsonObject.getString("price"));
                    int height=Integer.parseInt(productJsonObject.getString("height"));
                    int width=Integer.parseInt(productJsonObject.getString("width"));
                    if((maxprice > price && minprice <= price) && (maxwidth > width && minwidth <= width ) && (maxheight > height && minheight < height)){
                        map.put("id",productJsonObject.getString("productId"));
                        map.put("brandName",productJsonObject.getString("brandName"));
                        map.put("modelName",productJsonObject.getString("modelName"));
                        map.put("price",productJsonObject.getString("price"));
                        map.put("remark",productJsonObject.getString("remark"));
                        map.put("locationTag",productJsonObject.getString("locationTag"));
                        map.put("width",productJsonObject.getString("width"));
                        map.put("height",productJsonObject.getString("height"));
                        map.put("latitude",productJsonObject.getString("latitude"));
                        map.put("longitude",productJsonObject.getString("longitude"));
                        JSONArray imagelist=productJsonObject.getJSONArray("imageList");

                        for(int j=0;j<imagelist.length();j++){
                            JSONObject image=imagelist.getJSONObject(0);
                            map.put("image",image.getString("shortImage"));
                        }
                        productList.add(map);
                        jsonArray.add(productJsonObject);
                        adapter.notifyDataSetChanged();
                    }

                }
            }else{
                for(int i=0;i<productListArray.length();i++){
                    HashMap<String,String> map=new HashMap<>();
                    productJsonObject=productListArray.getJSONObject(i);
                    map.put("id",productJsonObject.getString("productId"));
                    map.put("brandName",productJsonObject.getString("brandName"));
                    map.put("modelName",productJsonObject.getString("modelName"));
                    map.put("price",productJsonObject.getString("price"));
                    map.put("remark",productJsonObject.getString("remark"));
                    map.put("locationTag",productJsonObject.getString("locationTag"));
                    map.put("width",productJsonObject.getString("width"));
                    map.put("height",productJsonObject.getString("height"));
                    map.put("latitude",productJsonObject.getString("latitude"));
                    map.put("longitude",productJsonObject.getString("longitude"));
                    JSONArray imagelist=productJsonObject.getJSONArray("imageList");

                    for(int j=0;j<imagelist.length();j++){
                        JSONObject image=imagelist.getJSONObject(0);
                        map.put("image",image.getString("shortImage"));
                    }
                    productList.add(map);
                    jsonArray.add(productJsonObject);
                    adapter.notifyDataSetChanged();
                }

            }
            Log.d("productJsonObjectLIST",productList.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.fab:

                animateFAB();
                break;
            case R.id.fab1:
                    if(filterValue !=null){
                        Intent intent=new Intent(getActivity(),Filter.class);
                        intent.putExtra("ranges",filterValue);
                        intent.putExtra("viewType","listView");
                        startActivity(intent);
                    }else{
                        Intent intent=new Intent(getActivity(),Filter.class);
                        intent.putExtra("viewType","listView");
                        startActivity(intent);
                    }
                break;
            case R.id.fab2:
               refreshData();

                break;
        }
    }

    private void refreshData() {
        try {
            JSONObject loggedUser = myDb.getUser();
            String response = new HttpCalls(GETPROP).doInBackground(loggedUser.getString("access_token"), loggedUser.getString("authorities"), loggedUser.getString("user"), String.valueOf(1), String.valueOf(pageSize));
            Log.d("PROPERTIES", response);
            productListArray = new JSONArray(response);
            for (int i = 0; i < productListArray.length(); i++) {
                    HashMap<String, String> map = new HashMap<>();
                    productJsonObject = productListArray.getJSONObject(i);
                    map.put("id", productJsonObject.getString("productId"));
                    map.put("brandName", productJsonObject.getString("brandName"));
                    map.put("modelName", productJsonObject.getString("modelName"));
                    map.put("price", productJsonObject.getString("price"));
                    map.put("remark", productJsonObject.getString("remark"));
                    map.put("locationTag", productJsonObject.getString("locationTag"));
                    map.put("width", productJsonObject.getString("width"));
                    map.put("height", productJsonObject.getString("height"));
                    map.put("latitude", productJsonObject.getString("latitude"));
                    map.put("longitude", productJsonObject.getString("longitude"));
                    JSONArray imagelist = productJsonObject.getJSONArray("imageList");

                    for (int j = 0; j < imagelist.length(); j++) {
                        JSONObject image = imagelist.getJSONObject(0);
                        map.put("image", image.getString("shortImage"));
                    }
                    productList.add(map);
                    jsonArray.add(productJsonObject);
                }
            Log.d("productJsonObjectLIST", productList.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter = new ListViewAdapter(getActivity(), productList, jsonArray);
        simplelist.setAdapter(adapter);
    }
}

