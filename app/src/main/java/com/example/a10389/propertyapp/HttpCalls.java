package com.example.a10389.propertyapp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

/**
 * Created by 10389 on 11/16/2017.
 */

public class HttpCalls extends AsyncTask<String,String,String> {

    String url="http://192.168.2.10:10389/PropertyApp_v2/";
    private int Type;
    Context context;
    DatabaseHelper myDb=new DatabaseHelper(context);
    public HttpCalls(int type) {
        Type=type;
    }

    @Override
    protected String doInBackground(String... strings) {
        String response=null;
        switch (Type){
            case 0: response=postLoginDetails(strings[0],strings[1]);
                    break;
            case 1:response=saveUsers(strings[0]);
                    break;
            case 2:response=getProprties(strings[0],strings[1],strings[2],strings[3],strings[4]);
                    break;
            case 3:response=registerProperties(strings[0],strings[1],strings[2]);
                    break;
            case 4:response=getProptiesForMap(strings[0],strings[1],strings[2],strings[3]);
                    break;
            case 5:response=deleteProperties(strings[0],strings[1]);
                    break;
            case 6:response=searchProperties(strings[0],strings[1]);
                    break;
            case 7:response=updateProperties(strings[0],strings[1]);
                    break;
            case 8:response=logout(strings[0]);
                    break;
        }
        return  response;
    }

    //http post call to logout,response is text message
    private String logout(String access_token) {
        HttpURLConnection conn = null;
        BufferedReader reader;
        String status=null;
        try {

            URL postUrl = new URL(url+"guestUser/logout?access_token="+access_token);
            conn = (HttpURLConnection) postUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type","application/json");
            conn.setDoOutput(true);
            conn.connect();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                status=line;
            }//end while()
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(conn!=null){
                conn.disconnect();
            }
        }//end try/catch/finally
        return status;
    }//end logout()


    //http put call to update the properties
    private String updateProperties(String data, String access_token) {
        HttpURLConnection conn = null;
        try {

            URL postUrl = new URL(url+"agent/product/update?access_token="+access_token);
            conn = (HttpURLConnection) postUrl.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type","application/json");
            conn.setDoOutput(true);
            conn.connect();
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();
            wr.close();
            if(conn.getResponseCode() != HttpURLConnection.HTTP_OK
                    && conn.getResponseCode() != HttpURLConnection.HTTP_CREATED){
                throw  new RuntimeException("Failed : HTTP error code :"+conn.getResponseCode());
            }//end if()
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(conn!=null){
                conn.disconnect();
            }
        }//end try/catch/finally
        return "propertyUpdated";
    }//end updateProperties

   /* http get call to searchProperties
    params:access_token,searchString
    response:list of properties*/
    private String searchProperties(String access_token, String searchString) {
        HttpURLConnection connection = null;
        StringBuilder sb=new StringBuilder();
        try {
            URL productUrl=new URL(url+"guestUser/mapProduct/search/"+searchString+"?access_token="+access_token);
            connection=(HttpURLConnection)productUrl.openConnection();
            connection.setRequestMethod("GET");
            //connection.setRequestProperty("Content-length","0");
            connection.setRequestProperty("Authorization","Basic bXktdHJ1c3RlZC1jbGllbnQ6c2VjcmV0");
            //*connection.setDoInput(true);
            connection.setDoOutput(false);
            connection.setUseCaches(false);
            connection.setAllowUserInteraction(false);
            connection.setReadTimeout(15000);
            connection.setConnectTimeout(15000);
            connection.connect();
            int status=connection.getResponseCode();
            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }//end while()
                    br.close();
                    return sb.toString();
            }//end switch()
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Exception ex) {
                }//end try/catch
            }//end if()
        }//end try/catch/finally
        return sb.toString();
    }//end searchProperties()


  /*  http delete call to delete properties
            params:access_token,propertyObject*/
    private String deleteProperties(String access_token, String productObject) {
        HttpURLConnection connection=null;
        try {
            URL postUrl = new URL(url+"agent/products/delete?access_token="+access_token);
            connection = (HttpURLConnection) postUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type","application/json");
            connection.setDoOutput(true);
            connection.connect();
            OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
            wr.write(productObject);
            wr.flush();
            wr.close();
            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK
                    && connection.getResponseCode() != HttpURLConnection.HTTP_CREATED){
                throw  new RuntimeException("Failed : HTTP error code :"+connection.getResponseCode());
            }//end if()
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(connection!=null){
                connection.disconnect();
            }//end if()
        }//end try/catch/finally
        return "Deleted Successfully";
    }//end deleteProperties()


    /*http get call to get properties for map
    params:latitude,longitude,radius,access_token
    response:list of properties*/
    private String getProptiesForMap(String lat,String lng, String rad,String access_token) {
        double latitude=Double.valueOf(lat);
        double longitude=Double.valueOf(lng);
        int radius=Integer.parseInt(rad);
        HttpURLConnection connection = null;
        StringBuilder sb=new StringBuilder();
        try {
            URL productUrl=new URL(url+"guestUser/mapProduct/"+radius+"/"+latitude+"/"+longitude+"?access_token="+access_token);
            connection=(HttpURLConnection)productUrl.openConnection();
            connection.setRequestMethod("GET");
            //connection.setRequestProperty("Content-length","0");
            connection.setRequestProperty("Authorization","Basic bXktdHJ1c3RlZC1jbGllbnQ6c2VjcmV0");
            //*connection.setDoInput(true);
            connection.setDoOutput(false);
            connection.setUseCaches(false);
            connection.setAllowUserInteraction(false);
            connection.setReadTimeout(15000);
            connection.setConnectTimeout(15000);
            connection.connect();
            int status=connection.getResponseCode();

            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }//end while()
                    br.close();
                    return sb.toString();
            }//end switch()
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Exception ex) {
                }//end try/catch
            }//end if()
        }//end try/catch/finally
        return sb.toString();
    }//end getProptiesForMap



   /* http post call for login
    params:username,password
    response:login details*/
    public String postLoginDetails(String username,String password){
        StringBuffer result=new StringBuffer();
        BufferedReader reader;
        HttpURLConnection connection = null;
        try{
            URL loginUrl=new URL(url+"oauth/token");
            connection= (HttpURLConnection) loginUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization","Basic bXktdHJ1c3RlZC1jbGllbnQ6c2VjcmV0");
            JSONObject params=new JSONObject();
            params.put("grant_type","password");
            params.put("username",username);
            params.put("password",password);
            OutputStream os=connection.getOutputStream();
            BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));
            writer.write(getQuery(params));
            writer.flush();
            writer.close();
            os.close();
            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK
                    && connection.getResponseCode() != HttpURLConnection.HTTP_CREATED){
                throw  new RuntimeException("Failed : HTTP error code :"+connection.getResponseCode());
            }//end if()
            reader=new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String temp;
            while((temp = reader.readLine())!=null ){
                result.append(temp);
                break;
            }//end while()
            reader.close();
            connection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }//end try/catch
        return result.toString();
    }//end postLoginDetails

    //call to make format of parameters
    private static String getQuery(JSONObject params) throws Exception {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){

            String key= itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }//end while()
        return result.toString();
    }//end getQuery()



  /*  http post call to store users in db
            params:user Object*/
    public String saveUsers(String data){
        HttpURLConnection conn = null;
        try {

            URL postUrl = new URL(url+"guestUser/saveUsers");
            conn = (HttpURLConnection) postUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type","application/json");
            conn.setDoOutput(true);
            conn.connect();
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();
            wr.close();
            if(conn.getResponseCode() != HttpURLConnection.HTTP_OK
                    && conn.getResponseCode() != HttpURLConnection.HTTP_CREATED){
                throw  new RuntimeException("Failed : HTTP error code :"+conn.getResponseCode());
            }//end if()
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(conn!=null){
                conn.disconnect();
            }//end if()
        }//end try/catch/finally
        return "userRegistered";
    }//end saveUsers()


    /*http get call to get properties
    params:access_token,authority,loggedUser,pageNo,pageSize
    response:List of properties with pagination*/
    public  String getProprties(String access_token,String authorities,String user,String pageno, String pagesize) {
        int pageNo=Integer.parseInt(pageno);
        int pageSize=Integer.parseInt(pagesize);
        HttpURLConnection connection = null;
        StringBuilder sb=new StringBuilder();
        try {
            URL productUrl=new URL(url+authorities.toLowerCase()+"/allProduct/"+user+"/page/"+pageNo+"/"+pageSize+"/?access_token="+access_token);
            connection=(HttpURLConnection)productUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization","Basic bXktdHJ1c3RlZC1jbGllbnQ6c2VjcmV0");
            connection.setDoOutput(false);
            connection.setUseCaches(false);
            connection.setAllowUserInteraction(false);
            connection.setReadTimeout(15000);
            connection.setConnectTimeout(15000);
            connection.connect();
            int status=connection.getResponseCode();

            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }//end while()
                    br.close();
                    return sb.toString();
            }//end switch()
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }  finally {
            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Exception ex) {
                }//end try/catch
            }//end if()
        }//end try/catch/finally
        return sb.toString();
    }//end getProperties()


   /* http pst call to store properties details in db
    params:PropertyObject,access_token,user*/

    public String registerProperties(String data,String access_token,String user) {
        HttpURLConnection conn = null;
        BufferedReader reader;
        String text="";
        try {

            URL postUrl = new URL(url+"agent/products/?access_token="+access_token+"&currentUsername="+user);
            conn = (HttpURLConnection) postUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type","application/json");
            conn.setDoOutput(true);
            conn.connect();
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();
            wr.close();
            if(conn.getResponseCode() != HttpURLConnection.HTTP_OK
                    && conn.getResponseCode() != HttpURLConnection.HTTP_CREATED){
                throw  new RuntimeException("Failed : HTTP error code :"+conn.getResponseCode());
            }//end if()
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(conn!=null){
                conn.disconnect();
            }//end if()
        }//end try/catch/finally
        return "propertyRegistered";
    }//end registerProperties()


}
