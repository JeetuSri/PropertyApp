package com.example.a10389.propertyapp;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class ProductDetail extends  AppCompatActivity {

    public static final int LOGOUT=8;

    //variables for helper class
    DatabaseHelper myDb;

    String username;
    String type;
    String filterData;
    String viewType;
    public ProductDetail() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        Intent intent=getIntent();
        if(intent.getStringExtra("data") != null){
             type=intent.getStringExtra("data");
        }
        if(intent.getStringExtra("filter") !=null){
            filterData=intent.getStringExtra("filter");
            viewType=intent.getStringExtra("viewType");
        }
        //get loggedUser and set the username to toolbar
        myDb=new DatabaseHelper(this);
        JSONObject obj=myDb.getUser();
        try {
            username=obj.getString("user");
        } catch (JSONException e) {
            e.printStackTrace();
        }//end try/catch

        getSupportActionBar().setTitle("Hello ,"+username);

        //bottom navigation
        final BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                        Fragment selectedFragment = null;
                        switch (item.getItemId()) {
                            case R.id.navigation_registration:
                                selectedFragment = Registration.newInstance();
                                break;
                            case R.id.navigation_listview:
                                selectedFragment = ListView.newInstance();
                                break;
                            case R.id.navigation_mapview:
                                selectedFragment = MapView.newInstance();
                                bottomNavigationView.setVisibility(View.GONE);
                                break;
                        }

                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.content, selectedFragment);
                        transaction.setTransition(transaction.TRANSIT_FRAGMENT_OPEN);
                        transaction.commit();
                        return true;
                    }
                });


            //Manually displaying the first fragment - one time only

            if(type !=null){
               bottomNavigationView.setSelectedItemId(R.id.navigation_listview);
                FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
                tr.replace(R.id.content, ListView.newInstance());
                tr.setTransition(tr.TRANSIT_FRAGMENT_OPEN);
                tr.commit();
            }else if(filterData !=null){
                if(viewType.equals("listView")){
                    bottomNavigationView.setSelectedItemId(R.id.navigation_listview);
                    FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
                    tr.replace(R.id.content, ListView.newInstance(filterData));
                    tr.setTransition(tr.TRANSIT_FRAGMENT_OPEN);
                    tr.commit();
                }else{
                    bottomNavigationView.setSelectedItemId(R.id.navigation_mapview);
                    FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
                    tr.replace(R.id.content, MapView.newInstance(filterData));
                    tr.setTransition(tr.TRANSIT_FRAGMENT_OPEN);
                    tr.commit();
                }

            }
            else{
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.content, Registration.newInstance());
                transaction.setTransition(transaction.TRANSIT_FRAGMENT_OPEN);
                transaction.commit();
            }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.optionmenu,menu);
        getMenuInflater().inflate(R.menu.search,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout : JSONObject loggedUser=myDb.getUser();
                try {
                    //call to logout the user
                    String response=new HttpCalls(LOGOUT).doInBackground(loggedUser.getString("access_token"));
                    if(response != null){
                        Intent intent=new Intent(ProductDetail.this,MainPage.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        Toast toast= Toast.makeText(this,response,Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP|Gravity.CENTER,0,0);
                        toast.show();
                    }//end if()
                } catch (JSONException e) {
                    e.printStackTrace();
                }//end try/catch
                return true;

            case R.id.search_id :
                Fragment newFragment=ListView.newInstance();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.content, newFragment);
                transaction.setTransition(transaction.TRANSIT_FRAGMENT_OPEN);
                transaction.commit();
                return true;
            default:return  super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to Exit?")
                .setNegativeButton(R.string.no,null)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finishAffinity();
                        System.exit(0);
                    }
                }).create().show();
    }
}
