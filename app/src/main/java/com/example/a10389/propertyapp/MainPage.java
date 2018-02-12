package com.example.a10389.propertyapp;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.StrictMode;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainPage extends Activity {

    //variables for view
    Button btnLogin,btnSignUp,btnSkip;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // casting variables
        btnLogin = (Button)findViewById(R.id.btnlogin);
        btnSignUp = (Button)findViewById(R.id.btnsignup);
        btnSkip = (Button)findViewById(R.id.btnskip);

        //calling functions
        login();
        singUp();
        skip();

    }

    //method to navigate towards login activity
    public void login(){
        btnLogin.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(MainPage.this,login.class));
                    }
                });//end clickListener()
    }//end login()


    //method to navigate towards  user registration activity
    public void singUp(){
        btnSignUp.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(MainPage.this,SignUp.class));

                    }
                });//end clickListener()
    }//end singUp()


    //method skip to navigate direct to product page without login
    public void skip(){
        btnSkip.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });//end clickListener()
    }//end skip()

    //to exit the application
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
