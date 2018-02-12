package com.example.a10389.propertyapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 10389 on 11/15/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME="LoginDetail.db";
    public static final String TABLE_NAME="loginDetail";
    public static final String COL_1="access_token";
    public static final String COL_2="authorities";
    public static final String COL_3="expires_in";
    public static final String COL_4="refresh_token";
    public static final String COL_5="scope";
    public static final String COL_6="token_type";
    public static final String COL_7="user";


    public DatabaseHelper(Context context) {
        super(context,DATABASE_NAME,null,1);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " +TABLE_NAME + "(access_token TEXT,authorities TEXT,expires_in INTEGER,refresh_token TEXT," +
                "scope TEXT,token_type TEXT,user TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS"+TABLE_NAME);
        onCreate(db);
    }

    //method to store data in SQLite
    public Boolean insertData(String response){
        long result;
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        try {
            JSONObject obj=new JSONObject(response);
            String msg=obj.getString("access_token");
            Log.d("access_token",msg);
            contentValues.put(COL_1,obj.getString("access_token"));
            contentValues.put(COL_2,obj.getString("authorities"));
            contentValues.put(COL_3,obj.getString("expires_in"));
            contentValues.put(COL_4,obj.getString("refresh_token"));
            contentValues.put(COL_5,obj.getString("scope"));
            contentValues.put(COL_6,obj.getString("token_type"));
            contentValues.put(COL_7,obj.getString("user"));
            result=db.insert(TABLE_NAME,null,contentValues);
            if(result == -1)
                return false;
            else
                return true;

        } catch (JSONException e) {
            e.printStackTrace();
        }//end try/catch
        return  true;
    }//end insertData()

    //method to retrieve data from SQLite
    public JSONObject getUser(){
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor res=db.rawQuery("select * from "+TABLE_NAME,null);
        Log.d("USER",res.toString());
        JSONObject loggedUser=null;
        StringBuffer user=new StringBuffer();
        try {

            if(res.moveToLast()){
                user.append("{\"access_token\":\""+res.getString(0)+"\",");
                user.append("\"authorities\":\""+res.getString(1)+"\",");
                user.append("\"expires_in\":\""+res.getString(2)+"\",");
                user.append("\"refresh_token\":\""+res.getString(3)+"\",");
                user.append("\"scope\":\""+res.getString(4)+"\",");
                user.append("\"token_type\":\""+res.getString(5)+"\",");
                user.append("\"user\":\""+res.getString(6)+"\"}");
            }//end if()
            loggedUser=new JSONObject(user.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }//end try/catch
        return loggedUser;
    }//end getUser()
}
