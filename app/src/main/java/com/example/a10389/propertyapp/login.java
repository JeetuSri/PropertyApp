package com.example.a10389.propertyapp;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

public class login extends Activity {

    public static final int POSTLOGIN=0;

    //variables for view
    EditText name,password;
    Button login,cancel;

    //variables for helper class
    DatabaseHelper myDb;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //logic to display the activity as popup window
        DisplayMetrics dm=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width=dm.widthPixels;
        int height=dm.heightPixels;
        getWindow().setLayout((int)(width*0.7),(int)(height*0.5));

        //intialize or cast variables
        myDb=new DatabaseHelper(this);

        name=(EditText)findViewById(R.id.editname);
        password=(EditText)findViewById(R.id.editpass);
        login=(Button)findViewById(R.id.btnlogn);
        cancel=(Button)findViewById(R.id.btncancel);

        //method calls
        userLogin();
        cancel();
    }


    //method for user login,if login is successfull it will navigate to product page
    public void userLogin(){
        login.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        //post call for login ,response is login details
                        String response=new HttpCalls(POSTLOGIN).doInBackground(name.getText().toString(),password.getText().toString());

                        //store login details in SQLite
                        boolean isAdded=myDb.insertData(response);

                        //conditions for valid credentials and non empty fields
                        if(name.getText().toString().trim().equals("") && password.getText().toString().trim().equals("")){
                                name.setError("username is required!");
                                password.setError("password is required!");
                        }else{
                            try {
                                JSONObject object=new JSONObject(response);
                                if(!(object.getString("authorities").equals("AGENT"))){
                                    Toast toast=Toast.makeText(login.this,object.getString("authorities"),Toast.LENGTH_LONG);
                                    toast.setGravity(Gravity.TOP|Gravity.CENTER,0,0);
                                    toast.show();
                                }else{
                                    Intent intent=new Intent(login.this,ProductDetail.class);
                                    startActivity(intent);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }//end ifelse()
                    }//end onClick()
                });//end clickListener()
        }//end userLogin()


    //method cancel to cancel login,it will navigate to mainpage
    public void cancel(){
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(login.this,MainPage.class));
            }
        });//end clickListener()
    }//end cancel()

}
