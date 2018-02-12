package com.example.a10389.propertyapp;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;


import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.service.autofill.Validator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mobsandgeeks.saripaar.Rule;
import com.mobsandgeeks.saripaar.annotation.TextRule;
import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.model.ChipInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.Buffer;
import java.sql.Blob;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static android.R.attr.bitmap;
import static android.app.Activity.RESULT_OK;
import static com.example.a10389.propertyapp.SignUp.UPLOADFILE;

/**
 * Created by 10389 on 11/15/2017.
 */

public class Registration extends Fragment implements OnMapReadyCallback,
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener, com.mobsandgeeks.saripaar.Validator.ValidationListener{

    public static final int REGPROP=3;
    static final int CAM_REQ = 1;

    //variables for image
    File image,f;
    public static final String GalleryIntentKey="imageArray";
    ArrayList<HashMap<Bitmap,String>> finalImageListFromAdapter=new ArrayList<>();
    ArrayList<String> myPath=new ArrayList<>();
    ArrayList<String> imagePath=new ArrayList<>();
    ArrayList<HashMap<Bitmap,String>> imageListToSetAdapter=new ArrayList<>();
    ArrayList<String> imageUrlList;
    ArrayList<String> shortImageUrlList;
    HashMap<String,Bitmap> cloudImageList;
    HashMap<String,Bitmap> cloudThumbnailImageList;

    //variavles for view
    private Button button,btnRegister,btngallery,btnlocation;
    private ExpandenListView view;
    ChipsInput locTag;
    @TextRule(order = 1,minLength = 3,maxLength = 8)
    private EditText brandname;
    EditText modelname,price,width,height,remark,attrName,attrDisplName,attrValue;
    private TextView latitude,longitude;
    ArrayList<String> locationTags;
    protected View mview;

    //variables for helper class
    CustomImageAdapter adapter;
    DatabaseHelper myDb;

    //variables for form validation
    protected com.mobsandgeeks.saripaar.Validator validator;
    protected  boolean validated;

    //variables for google map
    int displayMap=0;
    GoogleApiClient mGoogleApiClient;
    DecimalFormat decimalFormat=new DecimalFormat("#.0000000");
    private LatLng place=new LatLng(0,0);
    GoogleMap mMap;
    com.google.android.gms.maps.MapView mapview;
    String mCurrentPhotoPath;


    public static Registration newInstance() {
        Registration fragment = new Registration();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myDb = new DatabaseHelper(getActivity());
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // initialize/cast variables
        View mView = inflater.inflate(R.layout.navigation_registration, container, false);
        this.mview = mView;
        button = (Button) mView.findViewById(R.id.button1);
        button.setTransformationMethod(null);
        btngallery=(Button)mView.findViewById(R.id.gallerybutton);
        btngallery.setTransformationMethod(null);
        btnlocation=(Button)mView.findViewById(R.id.locationbutton);
        btnlocation.setTransformationMethod(null);
        btnRegister = (Button) mView.findViewById(R.id.submit);
        btnRegister.setTransformationMethod(null);
        view = (ExpandenListView) mView.findViewById(R.id.imageView);
        brandname = (EditText) mView.findViewById(R.id.brandName);
        modelname = (EditText) mView.findViewById(R.id.modelName);
        //locTag = (ChipsInput) mView.findViewById(R.id.locationTag);
        price = (EditText) mView.findViewById(R.id.price);
        width = (EditText) mView.findViewById(R.id.width);
        height = (EditText) mView.findViewById(R.id.height);
        remark = (EditText) mView.findViewById(R.id.remark);
        attrName = (EditText) mView.findViewById(R.id.attributeName);
        attrDisplName = (EditText) mView.findViewById(R.id.attributeDisplayName);
        attrValue = (EditText) mView.findViewById(R.id.attributeValue);
        latitude=(TextView)mView.findViewById(R.id.latitude);
        longitude=(TextView)mView.findViewById(R.id.longitude);

         //setting list to adapter and calling adapter,the response of adapter is set to listView
        adapter=new CustomImageAdapter(getActivity(),imageListToSetAdapter);
        locationTags=new ArrayList<>();
        if(adapter !=null){
            view.setAdapter(adapter);
        }
        imageUrlList=new ArrayList<>();
        shortImageUrlList=new ArrayList<>();
        cloudImageList=new HashMap<>();
        cloudThumbnailImageList=new HashMap<>();

        validator=new com.mobsandgeeks.saripaar.Validator(getActivity());
        validator.setValidationListener(this);

        //method calls
        clickImageFromCamera();
        selectImageFromGallery();
        registerProprties();
        setLocation();

        //logic for chipsInput
        final ChipsInput chipsInput = (ChipsInput) mView.findViewById(R.id.locationTag);

        List<com.example.a10389.propertyapp.Tag> locationList = new ArrayList<>();
        com.example.a10389.propertyapp.Tag t1=new com.example.a10389.propertyapp.Tag();
        com.example.a10389.propertyapp.Tag t2=new com.example.a10389.propertyapp.Tag();
        t1.setLocationTag("home");
        t2.setLocationTag("office");
        locationList.add(t1);
        locationList.add(t2);
        chipsInput.setFilterableList(locationList);

        chipsInput.addChipsListener(new ChipsInput.ChipsListener() {
            @Override
            public void onChipAdded(ChipInterface chipInterface, int i) {
                locationTags.add(chipInterface.getInfo().trim());
            }

            @Override
            public void onChipRemoved(ChipInterface chipInterface, int i) {
                String location=locationTags.get(i);
                locationTags.remove(location);
            }

            @Override
            public void onTextChanged(CharSequence charSequence) {

            }
        });

        return mView;
    }

    //button clickListener method to upload image from gallery
    private void selectImageFromGallery() {
        btngallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UploadData();
            }//end onClick()
        });//end clickListener()
    }//end selectImageFromGallery()

    //button clickListener method to set the selected location
    private void setLocation() {
        btnlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((displayMap%2) == 0){
                    mapview.setVisibility(View.GONE);
                    displayMap++;

                }else{
                    mapview.setVisibility(View.VISIBLE);
                    displayMap++;
                }
            }//end onClick()
        });//end clickListener()
    }//end setLocation()


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //google map
        mapview = (com.google.android.gms.maps.MapView) mview.findViewById(R.id.map);
        if (mapview != null) {
            mapview.onCreate(null);
            mapview.onResume();
            mapview.getMapAsync(this);

        }//end if()
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }//end if()
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }//end ifelse()

    }//end onMapReady()

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }//end buildgoogleApiClient()

    //method to get the current location when the map is loaded
    private void getCurrentLocation() {
        mMap.clear();
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }//end if()
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(location!=null){
            place=new LatLng(location.getLatitude(),location.getLongitude());
        }//end if()
               moveMap();
    }//end getCurrentLocation()

    //method to move th map towards current location
    private void moveMap()  {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(place));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
        latitude.setText(String.valueOf(decimalFormat.format(place.latitude)));
        longitude.setText(String.valueOf(decimalFormat.format(place.longitude)));

        //onClickListener to select location and place marker
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng loc) {
                place=new LatLng(loc.latitude,loc.longitude);
                mMap.addMarker(new MarkerOptions().position(place));
                latitude.setText(String.valueOf(decimalFormat.format(place.latitude)));
                longitude.setText(String.valueOf(decimalFormat.format(place.longitude)));
            }
        });//end clickListener()
    }//end moveMap()


    //method to capture image from camera
    public void clickImageFromCamera() {
        button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                        image = new File(dir, "Image.jpg");
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                            // Create the File where the photo should go
                            File photoFile = null;
                            try {
                                photoFile = createImageFile();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }//end try/catch
                            // Continue only if the File was successfully created
                            if (photoFile != null) {
                                Uri photoURI = FileProvider.getUriForFile(getContext(),
                                        "com.example.android.fileprovider",
                                        photoFile);
                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                startActivityForResult(takePictureIntent, CAM_REQ);
                            }//end if()
                        }//end if()
                    }
                });//end clickListener()
    }//end clickImageFromCamera()

    //method to create image file
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
         image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //condition for camera image
        if (requestCode == 1 && resultCode == RESULT_OK) {
            HashMap<Bitmap,String> map=new HashMap<>();
            Intent click = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
           f = new File(mCurrentPhotoPath);

            click.setData(Uri.fromFile(f));
            this.getActivity().sendBroadcast(click);
            Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
            Bitmap picture=Bitmap.createScaledBitmap(bitmap,150,150,false);
            view.setVisibility(View.VISIBLE);
            map.put(picture,"original");
            imageListToSetAdapter.add(map);
                adapter.notifyDataSetChanged();

        }//end if()
        //condition for gallery image
        if (requestCode == 2 && resultCode == RESULT_OK) {
            imagePath = new ArrayList<>();
            ClipData clipData = data.getClipData();
            if(clipData == null) {
                Uri uri=data.getData();
                imagePath.add(String.valueOf(uri));
            }else{
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    ClipData.Item item = clipData.getItemAt(i);
                    imagePath.add(String.valueOf(item.getUri()));
                }//end for()

            }//end ifelse()

            for (int i = 0; i < imagePath.size(); i++) {
                try {
                    HashMap<Bitmap,String> map=new HashMap<>();
                    view.setVisibility(View.VISIBLE);
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), Uri.parse(imagePath.get(i)));
                    Bitmap picture = Bitmap.createScaledBitmap(bitmap, 150, 150, false);
                    map.put(picture,"original");
                    imageListToSetAdapter.add(map);


                    } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }//end try/catch
            }//end for()
            adapter.notifyDataSetChanged();
        }//end if()

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

    //method to read the image,convert image to thumnail and add image to list for cloudUpload
    private void readImage(Bitmap photo){
        UUID uuid= UUID.randomUUID();
        String propertyImageName="property-"+String.valueOf(uuid)+".jpg";
        String shortImageName="propertythumbnail-"+String.valueOf(uuid)+".jpg";
        Bitmap picture=Bitmap.createScaledBitmap(photo,500,500,false);
        cloudImageList.put(propertyImageName,photo);
        cloudThumbnailImageList.put(shortImageName,picture);
        imageUrlList.add(propertyImageName);
        shortImageUrlList.add(shortImageName);
    }//end readImage()

    //method to select image from gallery
    public void UploadData() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra( Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.putExtra(Registration.GalleryIntentKey,myPath.toString());
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Selecte Image"), 2);
        ////Log.d(">>>>>>>>>>>image", String.valueOf(PICK_IMAGE));
    }//end uploadData()

    //method to store properties to db
    public void registerProprties() {
        btnRegister.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            for(int i=0;i<finalImageListFromAdapter.size();i++){
                                HashMap<Bitmap,String> hash=finalImageListFromAdapter.get(i);
                                readImage(hash.keySet().iterator().next());
                            }
                            RegisterProperties();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }//end try/catch
                    }
                });//end clickListener()
    }//end registerProperties()

    //method to submit the properties form to store in database through HttpCalls helper class
    public void RegisterProperties() throws UnsupportedEncodingException, JSONException {

        //get the editField values
        String brandName = brandname.getText().toString();
        String modelName = modelname.getText().toString();
        String Price = price.getText().toString();
        String Width = width.getText().toString();
        String Height = height.getText().toString();
        String Remark = remark.getText().toString();
        String attributeName = attrName.getText().toString();
        String attributeDisplayName = attrDisplName.getText().toString();
        String attributeValue = attrValue.getText().toString();
        int userId = 1;
        Double lat = place.latitude;
        Double lng = place.longitude;

        //form to submit
        StringBuffer data = new StringBuffer();
        data.append("[{\"brandName\":\"" + URLEncoder.encode(brandName, "UTF-8") + "\",");
        data.append("\"modelName\":\"" + URLEncoder.encode(modelName, "UTF-8") + "\",");
        data.append("\"locationTag\":\"" + (locationTags.toString()) + "\",");
        data.append("\"price\":" + URLEncoder.encode(Price.trim(), "UTF-8") + ",");
        data.append("\"width\":\"" + URLEncoder.encode(Width, "UTF-8") + "\",");
        data.append("\"height\":\"" + URLEncoder.encode(Height, "UTF-8") + "\",");
        data.append("\"latitude\":\"" + URLEncoder.encode(String.valueOf(lat), "UTF-8") + "\",");
        data.append("\"longitude\":\"" + URLEncoder.encode(String.valueOf(lng), "UTF-8") + "\",");
        data.append("\"remark\":\"" + URLEncoder.encode(Remark, "UTF-8") + "\",");
        data.append("\"lastModifiedUser\":{\"userId\":" + URLEncoder.encode(String.valueOf(userId), "UTF-8") + "},");
        data.append("\"extraAttributeList\":[{\"attributeName\":\"" + URLEncoder.encode(attributeName, "UTF-8") + "\",");
        data.append("\"attributeDisplayName\":\"" + URLEncoder.encode(attributeDisplayName, "UTF-8") + "\",");
        data.append("\"attributeValue\":\"" + URLEncoder.encode(attributeValue, "UTF-8") + "\"}],");
        data.append("\"imageUrlList\":[");
        boolean firstimage=true;
        boolean firstshortimage=true;

        //imageUrlList simplification
        for(String key:cloudImageList.keySet()){
            Toast.makeText(getActivity(),key,Toast.LENGTH_LONG).show();
            if(firstimage)
                firstimage=false;
            else
                data.append(",");

            data.append("\""+key+"\"");
            new UploadImageToCloud(key).execute(cloudImageList.get(key));

        }//end for()

        //shortImageUrlList simplification
        data.append("],\"shortImageUrlList\":[");
        for(String key:cloudThumbnailImageList.keySet()){
            if(firstshortimage)
                firstshortimage=false;
            else
                data.append(",");

            data.append("\""+key+"\"");
            new UploadImageToCloud(key).execute(cloudThumbnailImageList.get(key));
        }//end for()
        data.append("]}]");

        //call to helper class for storing data in db
        JSONObject loggedUser=myDb.getUser();
        String response=new HttpCalls(REGPROP).doInBackground(data.toString(),loggedUser.getString("access_token"),loggedUser.getString("user"));

    }//end registerProperties()

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

    }

    @Override
    public void onValidationFailed(View failedView, Rule<?> failedRule) {

    }
}

