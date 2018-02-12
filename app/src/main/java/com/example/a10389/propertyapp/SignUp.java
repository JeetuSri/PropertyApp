package com.example.a10389.propertyapp;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.beloo.widget.chipslayoutmanager.util.log.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mobsandgeeks.saripaar.Rule;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.TextRule;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class SignUp extends AppCompatActivity implements OnMapReadyCallback,
               GoogleApiClient.ConnectionCallbacks,
                GoogleApiClient.OnConnectionFailedListener,Validator.ValidationListener{

    public static final int SAVEUSER=1;
    public static final int UPLOADFILE=7;

    //variables for view
    private Button clickimage,setlocation,btnSignUp;
    TextView latitude,longitude;
    @TextRule(order = 1,minLength = 2,maxLength = 8)
    private EditText fName;
    EditText lName,uName,address,email,mobile,pass;
    ExpandenListView list;

    //variables for form validation
    protected Validator validator;
    protected boolean validated;

    //variables for image
    static final int CAM_REQ = 1;
    String mCurrentPhotoPath;
    private File image;
    ArrayList<HashMap<Bitmap,String>> finalImageListFromAdapter=new ArrayList<>();
    ArrayList<HashMap<Bitmap,String>> imageListToSetAdapter=new ArrayList<>();
    ArrayList<String> imageUrlList;
    HashMap<String,Bitmap> cloudImageList=new HashMap<>();

    //variables for map
    DecimalFormat decimalFormat=new DecimalFormat("#.0000000");
    private LatLng place=new LatLng(0,0);
    GoogleMap map;
    GoogleApiClient mGoogleApiClient;
    com.google.android.gms.maps.MapView mapView;

    //variable for helper class
    CustomImageAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        getSupportActionBar().setTitle("User Registration");

        // cast/initialize variables
        clickimage = (Button) findViewById(R.id.clickbutton);
        clickimage.setTransformationMethod(null);
        btnSignUp = (Button) findViewById(R.id.signUp);
        btnSignUp.setTransformationMethod(null);
        setlocation=(Button)findViewById(R.id.locationbutton);
        setlocation.setTransformationMethod(null);
        list=(ExpandenListView)findViewById(R.id.imageView);
        fName = (EditText) findViewById(R.id.firstName);
        lName = (EditText) findViewById(R.id.lastName);
        uName = (EditText) findViewById(R.id.userName);
        address = (EditText) findViewById(R.id.address);
        email = (EditText) findViewById(R.id.email);
        mobile = (EditText) findViewById(R.id.mobile);
        pass = (EditText) findViewById(R.id.password);
        latitude=(TextView)findViewById(R.id.latitude);
        longitude=(TextView)findViewById(R.id.longitude);
        mapView = (MapView) findViewById(R.id.map);

        imageUrlList=new ArrayList<>();

        validator=new Validator(this);
        validator.setValidationListener(this);

        //method calls
        if (mapView != null) {
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }


        clickImageFromCamera();
        singUp();
        setLocation();

        //setting list to adapter and calling adapter,the response of adapter is set to listView
        adapter=new CustomImageAdapter(SignUp.this,imageListToSetAdapter);
        list.setAdapter(adapter);

        //The result of adapter stored in finalImageListFromAdapter to store data in db
        for(int i=0;i<adapter.getCount();i++){
            finalImageListFromAdapter.add((HashMap<Bitmap, String>) adapter.getItem(i));
        }//end for()

        //changeListener for adapter, if the adapter view is changed then to get final result from adapter
        adapter.setmOnDataChangeListener(new CustomImageAdapter.OnDataChangeListener() {
            @Override
            public void onDataChanged(int Size) {
                finalImageListFromAdapter=new ArrayList<HashMap<Bitmap, String>>();
                for(int i=0;i<Size;i++){
                    finalImageListFromAdapter.add((HashMap<Bitmap, String>) adapter.getItem(i));
                }//end for()
            }
        });//end changeListener()

    }

    //method for form validation
    protected boolean validate(){
        if(validator!=null){
            validator.validate();
        }
        return validated;
    }

    //method to set the selected location
    private void setLocation() {
        setlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapView.setVisibility(View.GONE);
            }
        });//end clickListener()
    }//end setLocation()

    //method to click the image from camera
    public void clickImageFromCamera() {
        clickimage.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                        image = new File(dir, "Image.jpg");
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                            // Create the File where the photo should go
                            File photoFile = null;
                            try {
                                photoFile = createImageFile();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }//end try/catch


                            // Continue only if the File was successfully created
                            if (photoFile != null) {
                                Uri photoURI = FileProvider.getUriForFile(SignUp.this,
                                        "com.example.android.fileprovider",
                                        photoFile);
                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                startActivityForResult(takePictureIntent, CAM_REQ);
                            }//end if()
                        }//end if()
                    }//end onClick()
                });//end clickListener()
         }//end clickImageFromCamera()


    //method to create image file
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPG" + timeStamp + "_";
        File storageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }//end createImageFile()



    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            HashMap<Bitmap,String> map=new HashMap<>();
            Intent click = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(mCurrentPhotoPath);

            click.setData(Uri.fromFile(f));
            this.sendBroadcast(click);
            Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
            Bitmap picture=Bitmap.createScaledBitmap(bitmap,150,150,false);
            list.setVisibility(View.VISIBLE);
            map.put(picture,"original");
            imageListToSetAdapter.add(map);
            adapter.notifyDataSetChanged();
        }//end if()
        //The result of adapter stored in finalImageListFromAdapter to store data in db
        finalImageListFromAdapter=new ArrayList<HashMap<Bitmap, String>>();
        for(int i=0;i<adapter.getCount();i++){
            finalImageListFromAdapter.add((HashMap<Bitmap, String>) adapter.getItem(i));
        }//end for()

    }//end onActivityResult()

    //method to read the image and add image to list for cloudUpload
    private void readImage(Bitmap bitmap) {
        UUID uuid= UUID.randomUUID();
        String userImageName="user-"+String.valueOf(uuid)+".jpg";
        cloudImageList.put(userImageName,bitmap);
        imageUrlList.add(userImageName);
        new UploadImageToCloud(userImageName).execute(bitmap);
    }//end readImage()


    //onClickSubmit method call to submit user form
    public void singUp() {
        btnSignUp.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            validator.validate();
                            for(int i=0;i<finalImageListFromAdapter.size();i++){
                                HashMap<Bitmap,String> hash=finalImageListFromAdapter.get(i);
                                readImage(hash.keySet().iterator().next());
                            }
                            createUser();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }//end try/catch
                    }
                });//end clickListener()
    }//end signUp()


    //method from clickListener to submit the registration form to db through HttpCalls helper class
    public void createUser() throws UnsupportedEncodingException {

        //get the value from editText fileds
        String Fname = fName.getText().toString();
        String Lname = lName.getText().toString();
        String Uname = uName.getText().toString();
        String Address = address.getText().toString();
        String Email = email.getText().toString();
        String Mobile = mobile.getText().toString();
        String Pass = pass.getText().toString();
        Double lat=place.latitude;
        Double lng=place.longitude;

        //form to submit
        StringBuffer data = new StringBuffer();
        data.append("[{\"firstName\":\"" + URLEncoder.encode(Fname, "UTF-8") + "\",");
        data.append("\"lastName\":\"" + URLEncoder.encode(Lname, "UTF-8") + "\",");
        data.append("\"username\":\"" + URLEncoder.encode(Uname, "UTF-8") + "\",");
        data.append("\"address\":\"" + URLEncoder.encode(Address, "UTF-8") + "\",");
        data.append("\"email\":\"" + URLEncoder.encode(Email, "UTF-8") + "\",");
        data.append("\"mobile\":\"" + URLEncoder.encode(Mobile, "UTF-8") + "\",");
        data.append("\"password\":\"" + URLEncoder.encode(Pass, "UTF-8") + "\",");
        data.append("\"currentLatitude\":\"" + URLEncoder.encode(String.valueOf(lat), "UTF-8") + "\",");
        data.append("\"currentLongitude\":\"" + URLEncoder.encode(String.valueOf(lng), "UTF-8") + "\",");
        data.append("\"image\":");
        for(String key:imageUrlList){
            data.append("\""+key+"\"");
        }//end for()
        data.append("}]");
        //call to create User
        String response=new HttpCalls(SAVEUSER).doInBackground(data.toString());
        if(response == "userRegistered"){
            startActivity(new Intent(SignUp.this,MainPage.class));
        }//end if()
    }//end createUser()


    @Override
    public void onMapReady(final GoogleMap googleMap) {
        map = googleMap;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                map.setMyLocationEnabled(true);
            }//end if()
        } else {
            buildGoogleApiClient();
            map.setMyLocationEnabled(true);
        }//end ifelse()
    }//end onMapReady()

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this)
                .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }//end buildGoogleApiClient()

    //method to get the current location when the map is loaded
    private void getCurrentLocation() {
        map.clear();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(location!=null){
            place=new LatLng(location.getLatitude(),location.getLongitude());
        }
        moveMap();
    }//end getCurrentLocation()


    //method to move th map towards current location
    private void moveMap()  {
        map.moveCamera(CameraUpdateFactory.newLatLng(place));
        map.animateCamera(CameraUpdateFactory.zoomTo(12));
        latitude.setText(String.valueOf(decimalFormat.format(place.latitude)));
        longitude.setText(String.valueOf(decimalFormat.format(place.longitude)));

        //onClickListener to select location and place marker
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng loc) {
                place=new LatLng(loc.latitude,loc.longitude);
                map.addMarker(new MarkerOptions().position(place));
                latitude.setText(String.valueOf(decimalFormat.format(place.latitude)));
                longitude.setText(String.valueOf(decimalFormat.format(place.longitude)));
            }
        });//end clickListener()
    }//end moveMap()

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getCurrentLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onValidationSucceeded() {
            validated=true;
    }

    @Override
    public void onValidationFailed(View failedView, Rule<?> failedRule) {
            validated=false;
        if(failedView instanceof EditText){
            EditText ed=(EditText)failedView;
            ed.setError("required field");
        }
    }
}
