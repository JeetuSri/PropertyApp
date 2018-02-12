package com.example.a10389.propertyapp;

import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.model.ChipInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PropertyUpdate extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener{

    public static final int UPDATEPROP=7;

    //Calling helper classes
    DatabaseHelper myDb;
    CustomImageAdapter adapter;

    //variables to hold update data
    String propertyObject;
    JSONObject originalAttributeList;
    JSONArray originalImageList;
    JSONObject propertyToEdit;
    String originalLocationTags;
    String[] obj;

    //variables for view
    EditText brandName,modelName,price,width,height,remark,
            attributeName,attributeDisplayName,attributeValue;
    TextView latitude,longitude;
    ChipsInput chipsInput;
    Button save,delImage,click,pick;
    ExpandenListView listView;
    ArrayList<String> locationTags;

    //variables for map
    com.google.android.gms.maps.MapView mapView;
    GoogleMap map;
    LatLng place= null;
    Marker marker;
    GoogleApiClient mGoogleApiClient;


    //variables for images
    static final int CAM_REQ = 1;
    File image,output;
    ArrayList<String> myPath=new ArrayList<>();
    ArrayList<HashMap<Bitmap,String>> finalImageListFromAdapter=new ArrayList<>();
    ArrayList<String> imagePath=new ArrayList<>();
    ArrayList<HashMap<Bitmap,String>> imageListToSetAdapter=new ArrayList<>();
    ArrayList<String> imageUrlList;
    ArrayList<String> thumnailImageUrlList;
    ArrayList<String> previousImageList;
    ArrayList<JSONObject> deletingImageList;
    HashMap<Bitmap,JSONObject> propertyImageList=new HashMap<>();
    HashMap<String,Bitmap> cloudImageList=new HashMap<>();
    HashMap<String,Bitmap> cloudThumbnailImageList=new HashMap<>();
    String mCurrentPhotoPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_update);
        getSupportActionBar().setTitle("Update Property!");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //get data from intent
        Intent intent=getIntent();
        propertyObject=intent.getStringExtra("data");

        // cast/initialize variables
        myDb=new DatabaseHelper(this);
        brandName=(EditText)findViewById(R.id.updateBrandName);
        modelName=(EditText)findViewById(R.id.updateModelName);
        price=(EditText)findViewById(R.id.updatePrice);
        width=(EditText)findViewById(R.id.updateWidth);
        height=(EditText)findViewById(R.id.updateHeight);
        remark=(EditText)findViewById(R.id.updateRemark);
        attributeName=(EditText)findViewById(R.id.updateAttributeName);
        attributeDisplayName=(EditText)findViewById(R.id.updateAttributeDisplayName);
        attributeValue=(EditText)findViewById(R.id.updateAttributeValue);
        latitude=(TextView)findViewById(R.id.updateLatitude);
        longitude=(TextView)findViewById(R.id.updateLongitude);
        listView=(ExpandenListView) findViewById(R.id.updateListView);
        chipsInput=(ChipsInput)findViewById(R.id.updateLocationTag);
        click=(Button)findViewById(R.id.newPicFromCamera);
        pick=(Button)findViewById(R.id.newPicFromGallery);
        save=(Button)findViewById(R.id.updateSubmit);
        mapView=(com.google.android.gms.maps.MapView)findViewById(R.id.map);


        try {
            propertyToEdit=new JSONObject(propertyObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        imageUrlList=new ArrayList<>();
        thumnailImageUrlList=new ArrayList<>();
        deletingImageList=new ArrayList<>();
        previousImageList=new ArrayList<>();

        //function calls
        if(mapView != null){
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }
        setPreviousValuesToUpdateList();
        captureImageFromCamera();
        selectImagesFromGallery();

        //setting list to adapter and calling adapter,the response of adapter is set to listView
        adapter=new CustomImageAdapter(PropertyUpdate.this,imageListToSetAdapter);
        if(adapter.getCount() == 0){
            listView.setVisibility(View.GONE);
        }
        listView.setAdapter(adapter);

        //The result of adapter stored in finalImageListFromAdapter to store data in db
        for(int i=0;i<adapter.getCount();i++){
            finalImageListFromAdapter.add((HashMap<Bitmap, String>) adapter.getItem(i));
        }

        //changeListener for adapter, if the adapter view is changed then to get final result from adapter
        adapter.setmOnDataChangeListener(new CustomImageAdapter.OnDataChangeListener() {
            @Override
            public void onDataChanged(int Size) {
                finalImageListFromAdapter=new ArrayList<HashMap<Bitmap, String>>();
              for(int i=0;i<Size;i++){
                  finalImageListFromAdapter.add((HashMap<Bitmap, String>) adapter.getItem(i));
              }
            }
        });

        //locationTags to update
        locationTags=new ArrayList<>();
        try {
            originalLocationTags=propertyToEdit.getString("locationTag");
            obj=originalLocationTags.replaceAll("\"","").replace("[","").replace("]","").split(",");
            for(String s:obj){
                Tag t1=new Tag();
                t1.setLocationTag(s);
                chipsInput.addChip(t1);
                locationTags.add(t1.getInfo().trim());
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }
        final List<com.example.a10389.propertyapp.Tag> locationList = new ArrayList<>();
        com.example.a10389.propertyapp.Tag t1=new com.example.a10389.propertyapp.Tag();
        com.example.a10389.propertyapp.Tag t2=new com.example.a10389.propertyapp.Tag();
        t1.setLocationTag("home");
        t2.setLocationTag("office");
        locationList.add(t1);
        locationList.add(t2);
        chipsInput.setFilterableList(locationList);
        //logic for chipsInput*/

        chipsInput.addChipsListener(new ChipsInput.ChipsListener() {
            @Override
            public void onChipAdded(ChipInterface chipInterface, int i) {
                locationTags.add(chipInterface.getInfo());
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
        saveUpdatedProperty();
    }


    //method to load map on view
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map=googleMap;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                map.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            map.setMyLocationEnabled(true);
        }
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                marker.remove();
                Double lat=latLng.latitude;
                Double lng=latLng.longitude;
                place=new LatLng(lat,lng);
                marker=map.addMarker(new MarkerOptions().position(place));
                latitude.setText(String.valueOf(lat));
                longitude.setText(String.valueOf(lng));
            }
        });
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    //method to place property location on map
    private void getLocation() {
        try {
            place = new LatLng(Double.valueOf(propertyToEdit.getString("latitude")),Double.valueOf(propertyToEdit.getString("longitude")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        map.moveCamera(CameraUpdateFactory.newLatLng(place));
        map.animateCamera(CameraUpdateFactory.zoomTo(12));
        marker=map.addMarker(new MarkerOptions().position(place));
    }


    private void setPreviousValuesToUpdateList() {
        try {

            brandName.setText(propertyToEdit.getString("brandName"));
            modelName.setText(propertyToEdit.getString("modelName"));
            price.setText(propertyToEdit.getString("price"));
            width.setText(propertyToEdit.getString("width"));
            height.setText(propertyToEdit.getString("height"));
            remark.setText(propertyToEdit.getString("remark"));
            latitude.setText(propertyToEdit.getString("latitude"));
            longitude.setText(propertyToEdit.getString("longitude"));

            originalAttributeList=propertyToEdit.getJSONArray("extraAttributeList").getJSONObject(0);
            attributeName.setText(originalAttributeList.getString("attributeName"));
            attributeDisplayName.setText(originalAttributeList.getString("attributeDisplayName"));
            attributeValue.setText(originalAttributeList.getString("attributeValue"));



            originalImageList=propertyToEdit.getJSONArray("imageList");
            for(int j=0;j<originalImageList.length();j++) {
                HashMap<Bitmap,String> map=new HashMap<>();
                JSONObject jsonObj = originalImageList.getJSONObject(j);
                String image = jsonObj.getString("shortImage");
                Bitmap bitmap=new DownLoadImageWithURLTask().doInBackground("https://storage.googleapis.com/image-video/" + image);
                int width=150;
                int height=150;
                Bitmap picture=Bitmap.createScaledBitmap(bitmap,width,height,false);
                map.put(picture,"previous");
                imageListToSetAdapter.add(map);
                propertyImageList.put(picture,jsonObj);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void captureImageFromCamera() {
        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                output = new File(dir, "Image.jpg");
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(PropertyUpdate.this,
                                "com.example.android.fileprovider",
                                photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, CAM_REQ);
                    }
                }
            }
        });
    }

    //create the file to store photo
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    //method to select image from gallery
    private void selectImagesFromGallery() {
        pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra( Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.putExtra(Registration.GalleryIntentKey,myPath.toString());
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Selecte Image"), 2);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //this condition for camera image
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Intent click = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(mCurrentPhotoPath);

            click.setData(Uri.fromFile(f));
            sendBroadcast(click);
            Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
            int width=150;
            int height=150;
            Bitmap picture=Bitmap.createScaledBitmap(bitmap,width,height,false);
            HashMap<Bitmap,String> map=new HashMap<>();
            map.put(picture,"original");
            listView.setVisibility(View.VISIBLE);
            imageListToSetAdapter.add(map);
            //adapter.notifyDataSetChanged();
        }

        //this condition for gallery image
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
                }

            }
            for (int i = 0; i < imagePath.size(); i++) {
                try {
                    HashMap<Bitmap,String> map=new HashMap<>();
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(PropertyUpdate.this.getContentResolver(), Uri.parse(imagePath.get(i)));
                    Bitmap picture=Bitmap.createScaledBitmap(bitmap,150,150,false);
                    map.put(picture,"original");
                    listView.setVisibility(View.VISIBLE);
                    imageListToSetAdapter.add(map);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //adapter.notifyDataSetChanged();
        }
        adapter.notifyDataSetChanged();
        finalImageListFromAdapter=new ArrayList<HashMap<Bitmap, String>>();
        for(int i=0;i<adapter.getCount();i++){
            finalImageListFromAdapter.add((HashMap<Bitmap, String>) adapter.getItem(i));
        }
    }

    private void saveUpdatedProperty() {
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(int i=0;i<finalImageListFromAdapter.size();i++){
                    HashMap<Bitmap,String> hash=finalImageListFromAdapter.get(i);
                    if(hash.entrySet().iterator().next().getValue() == "previous"){
                        for(Bitmap key:propertyImageList.keySet()){
                            if(hash.keySet().iterator().next() != key){
                                deletingImageList.add(propertyImageList.get(key));

                            }
                        }
                    }else{
                        readImage(hash.keySet().iterator().next());
                    }
                }

                String brandname = brandName.getText().toString();
                String modelname = modelName.getText().toString();
                String Price = price.getText().toString();
                String Width = width.getText().toString();
                String Height = height.getText().toString();
                String Remark = remark.getText().toString();
                String attrName = attributeName.getText().toString();
                String attrDisplayName = attributeDisplayName.getText().toString();
                String attrValue = attributeValue.getText().toString();
                Double lat = place.latitude;
                Double lng = place.longitude;
                StringBuffer data = new StringBuffer();
                try {
                    data.append("[{\"productId\":"+ URLEncoder.encode(propertyToEdit.getString("productId"),"UTF-8")+",");
                    data.append("\"brandName\":\"" + URLEncoder.encode(brandname, "UTF-8") + "\",");
                    data.append("\"modelName\":\"" + URLEncoder.encode(modelname, "UTF-8") + "\",");
                    data.append("\"locationTag\":\"" +  (locationTags.toString())+ "\",");
                    data.append("\"price\":" + URLEncoder.encode(Price.trim(), "UTF-8") + ",");
                    data.append("\"width\":\"" + URLEncoder.encode(Width, "UTF-8") + "\",");
                    data.append("\"height\":\"" + URLEncoder.encode(Height, "UTF-8") + "\",");
                    data.append("\"latitude\":\"" + URLEncoder.encode(String.valueOf(lat), "UTF-8") + "\",");
                    data.append("\"longitude\":\"" + URLEncoder.encode(String.valueOf(lng), "UTF-8") + "\",");
                    data.append("\"remark\":\"" + URLEncoder.encode(Remark, "UTF-8") + "\",");
                    data.append("\"createdOn\":"+URLEncoder.encode(propertyToEdit.getString("createdOn"),"UTF-8")+",");
                    data.append("\"extraAttributeList\":[{\"extraAttributeId\":"+URLEncoder.encode(originalAttributeList.getString("extraAttributeId"),"UTF-8")+",");
                    data.append("\"attributeName\":\"" + URLEncoder.encode(attrName, "UTF-8") + "\",");
                    data.append("\"attributeDisplayName\":\"" + URLEncoder.encode(attrDisplayName, "UTF-8") + "\",");
                    data.append("\"attributeValue\":\"" + URLEncoder.encode(attrValue, "UTF-8") + "\",");
                    data.append("\"createdOn\":"+URLEncoder.encode(originalAttributeList.getString("createdOn"),"UTF-8")+"}],");
                    data.append("\"imageUrlList\":[");
                    boolean firstimage = true;
                    boolean firstshortimage = true;
                    for (String key : cloudImageList.keySet()) {
                        if (firstimage)
                            firstimage = false;
                        else
                            data.append(",");

                        data.append("\"" + key + "\"");
                        new UploadImageToCloud(key).execute(cloudImageList.get(key));

                    }
                    data.append("],\"shortImageUrlList\":[");
                    for (String key : cloudThumbnailImageList.keySet()) {
                        if (firstshortimage)
                            firstshortimage = false;
                        else
                            data.append(",");

                        data.append("\"" + key + "\"");
                        new UploadImageToCloud(key).execute(cloudThumbnailImageList.get(key));
                    }
                    data.append("],");
                    data.append("\"previousimage\":"+originalImageList+",");
                    data.append("\"deletingImage\":"+deletingImageList+"}]");
                    Log.e("PROPERTYTODISPLAY",data.toString());
                    JSONObject loggedUser=myDb.getUser();
                    String response=new HttpCalls(UPDATEPROP).doInBackground(data.toString(),loggedUser.getString("access_token"));
                }catch (Exception e){

                }
            }
        });
    }

    private void readImage(Bitmap bitmap) {
        UUID uuid= UUID.randomUUID();
        String propertyImageName="property-"+String.valueOf(uuid)+".jpg";
        String propertyThumbnailImageName="propertythumbnail-"+String.valueOf(uuid)+".jpg";
        Bitmap picture=Bitmap.createScaledBitmap(bitmap,500,500,false);
        cloudImageList.put(propertyImageName,bitmap);
        cloudThumbnailImageList.put(propertyThumbnailImageName,picture);
        imageUrlList.add(propertyImageName);
        thumnailImageUrlList.add(propertyThumbnailImageName);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getLocation();
    }



    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent=new Intent(this,ProductDetailedPage.class);
        intent.putExtra("data",propertyObject);
        startActivity(intent);
        return true;
    }
}
