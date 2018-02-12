package com.example.a10389.propertyapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static com.example.a10389.propertyapp.R.id.filter;


public class MapView extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener ,GoogleMap.OnMarkerClickListener,GoogleMap.OnInfoWindowClickListener,View.OnClickListener{

    public static final int GETPROPFORMAP=4;
    JSONObject obj=null;
    //variables for helper class
    DatabaseHelper myDb;
    MyMarker myMarker;

    //variables for view
    ProgressDialog dialog;
    View view;
    com.google.android.gms.maps.MapView mview;

    //variables for map
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;

    //variables for filter
    static  String filterValues;
    private Boolean isFabOpen=false;
    private FloatingActionButton fab,fab1,fab2;
    private Animation fab_open,fab_close,rotate_forward,rotate_backward;
    android.widget.ListView simplelist;

    //variables for marker
    private Marker marker;
    public HashMap<Marker,MyMarker>  hashMarkers=new HashMap<Marker, MyMarker>();
    public  HashMap<Marker,JSONObject> detailInfo=new HashMap<>();
    private ArrayList<MyMarker> arraylist=new ArrayList<MyMarker>();

    //variables for circle
    Circle circle;
    private int radius=5000;

    //variables for location
    Location mlocation;
    LatLng latLng=new LatLng(0,0);

    public static MapView newInstance() {
        MapView fragment = new MapView();
        return fragment;
    }

    public static MapView newInstance(String filterValue) {
        MapView fragment = new MapView();
        filterValues=filterValue;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_navigation_mapview, container, false);
        myDb=new DatabaseHelper(getActivity());
        //for filter
        fab = (FloatingActionButton)view.findViewById(R.id.main);
        fab1 = (FloatingActionButton)view.findViewById(R.id.filter);
        fab2 = (FloatingActionButton)view.findViewById(R.id.refresh);
        fab_open = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.fb_open);
        fab_close = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.rotate_backward);
        fab.setOnClickListener(this);
        fab1.setOnClickListener(this);
        fab2.setOnClickListener(this);
         dialog=new ProgressDialog(getActivity());
        dialog.setTitle("Loading");
        dialog.setMessage("Wait...While Loading");
        dialog.setCancelable(true);
        dialog.show();
        return view;
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
        //google map
        mview = (com.google.android.gms.maps.MapView) view.findViewById(R.id.map);
        if (mview != null) {
            mview.onCreate(null);
            mview.onResume();
            mview.getMapAsync(this);
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

        //mapClickListener to get location of click event and move the circle to that location taking location as center
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng loc) {
                latLng=new LatLng(loc.latitude,loc.longitude);
                circle.remove();
                drawCircle();
                placePropertiesonMap();
            }
        });//end clickListener()

        //infowindow clickListener to navigate to detail page
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent intent=new Intent(getActivity(),ProductDetailedPage.class);
                intent.putExtra("data",detailInfo.get(marker).toString());
                startActivity(intent);
                marker.hideInfoWindow();
            }
        });//end clickListener()
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mlocation = location;
        if (marker != null) {
            marker.remove();
        }
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("current position");
        marker = mMap.addMarker(markerOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
    }

    //method to get the current location on map
    private void getCurrentLocation() {
        mMap.clear();
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }//end if()
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(location!=null){
            latLng=new LatLng(location.getLatitude(),location.getLongitude());
        }//end if()
        moveMap();
    }//end getCurrentLocation()

    //method to move map based on current location
    private void moveMap()  {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
        drawCircle();
        placePropertiesonMap();
        dialog.dismiss();
    }//end movemap()

    //place the properties on map with markers
    private void placePropertiesonMap() {
        try {

            JSONObject loggedUser=myDb.getUser();
            String response=new HttpCalls(GETPROPFORMAP).doInBackground(String.valueOf(latLng.latitude),String.valueOf(latLng.longitude),String.valueOf(radius),loggedUser.getString("access_token"));
            JSONArray array=new JSONArray(response);
            Log.e("LENGTH", String.valueOf(array.length()));
            if(filterValues !=null){
                JSONObject filteredValues=new JSONObject(filterValues);
                for(int i=0;i<array.length();i++){
                    obj=array.getJSONObject(i);
                    int maxprice=Integer.parseInt(filteredValues.getString("priceMaxValue"));
                    int minprice=Integer.parseInt(filteredValues.getString("priceMinValue"));
                    int maxwidth=Integer.parseInt(filteredValues.getString("widthMaxValue"));
                    int minwidth=Integer.parseInt(filteredValues.getString("widthMinValue"));
                    int maxheight=Integer.parseInt(filteredValues.getString("heightMaxValue"));
                    int minheight=Integer.parseInt(filteredValues.getString("heightMinValue"));
                    int price=Integer.parseInt(obj.getString("price"));
                    int height=Integer.parseInt(obj.getString("height"));
                    int width=Integer.parseInt(obj.getString("width"));
                    if((maxprice > price && minprice <= price) && (maxwidth > width && minwidth <= width ) && (maxheight > height && minheight < height))
                        {
                            latLng = new LatLng(Double.valueOf(obj.getString("latitude")), Double.valueOf(obj.getString("longitude")));
                            MarkerOptions options = null;

                            //condition to place different markers
                            if ((Integer.parseInt(obj.getString("productId")) % 2) == 0) {
                                options = new MarkerOptions();
                                options.position(latLng);
                                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                            } else {
                                options = new MarkerOptions();
                                options.position(latLng);
                                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                            }//end if/else

                            Marker currentMarker = mMap.addMarker(options);
                            myMarker = new MyMarker();
                            myMarker.setBrandName(obj.getString("brandName"));
                            myMarker.setModelName(obj.getString("modelName"));
                            myMarker.setPrice(obj.getString("price"));
                            myMarker.setLatitude(Double.valueOf(obj.getString("latitude")));
                            myMarker.setLongitude(Double.valueOf(obj.getString("longitude")));
                            JSONArray imagelist = obj.getJSONArray("imageList");
                            if (imagelist.length() > 0) {
                                myMarker.setImage(imagelist.getJSONObject(0).getString("shortImage"));
                            } else {
                                myMarker.setImage(null);
                            }//end ifelse()
                            arraylist.add(myMarker);

                            hashMarkers.put(currentMarker, myMarker);
                            detailInfo.put(currentMarker, obj);
                            mMap.setInfoWindowAdapter(new CustomAdapter());
                        }//end if()
                    }//end for()
                    } else{
                    for (int i = 0; i < array.length(); i++) {
                        obj = array.getJSONObject(i);
                        latLng = new LatLng(Double.valueOf(obj.getString("latitude")), Double.valueOf(obj.getString("longitude")));
                        MarkerOptions options = null;

                        //condition to place different markers
                        if ((Integer.parseInt(obj.getString("productId")) % 2) == 0) {
                            options = new MarkerOptions();
                            options.position(latLng);
                            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                        } else {
                            options = new MarkerOptions();
                            options.position(latLng);
                            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        }//end if/else

                        Marker currentMarker = mMap.addMarker(options);
                        myMarker = new MyMarker();
                        myMarker.setBrandName(obj.getString("brandName"));
                        myMarker.setModelName(obj.getString("modelName"));
                        myMarker.setPrice(obj.getString("price"));
                        myMarker.setLatitude(Double.valueOf(obj.getString("latitude")));
                        myMarker.setLongitude(Double.valueOf(obj.getString("longitude")));
                        JSONArray imagelist = obj.getJSONArray("imageList");
                        if (imagelist.length() > 0) {
                            myMarker.setImage(imagelist.getJSONObject(0).getString("shortImage"));
                        } else {
                            myMarker.setImage(null);
                        }//end ifelse()
                        arraylist.add(myMarker);

                        hashMarkers.put(currentMarker, myMarker);
                        detailInfo.put(currentMarker, obj);
                        mMap.setInfoWindowAdapter(new CustomAdapter());
                    }//end for()
                }
            setUpMap();
        } catch (JSONException e) {
            e.printStackTrace();
        }//end try/catch
    }//end placePropertiesonMap()


        //method to show the infowindow on clicking marker
    private void setUpMap() {
        if(mMap!=null){
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
                    marker.showInfoWindow();
                    return true;
                }
            });
        }//end if()
    }//end setUpMap()

    //method to drawCircle by taking radius and latlng
    private void drawCircle() {
        circle=mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(radius)
                .fillColor(Color.rgb(135,192,229))
                .strokeColor(Color.rgb(214,137,16))
                .strokeWidth(4)
                .clickable(true)
        );
    }//end drawCircle()

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

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
    public boolean onMarkerClick(Marker marker) {
        return true;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if(marker.isInfoWindowShown()){
            marker.hideInfoWindow();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.main:

                animateFAB();
                break;
            case R.id.filter:
                if(filterValues !=null){
                    Intent intent=new Intent(getActivity(),Filter.class);
                    intent.putExtra("ranges",filterValues);
                    intent.putExtra("viewType","mapView");
                    startActivity(intent);
                }else{
                    Intent intent=new Intent(getActivity(),Filter.class);
                    intent.putExtra("viewType","mapView");
                    startActivity(intent);
                }
                break;
            case R.id.refresh:
                //refreshData();
                break;
        }
    }

    //customAdpater class for infowindow
    public class CustomAdapter implements GoogleMap.InfoWindowAdapter {

        private JSONObject product;
        Context context;
        private View.OnClickListener onClickListener;

        //HashMap<Marker,MyMarker> hashMarkers;
        public CustomAdapter() {
        }

        @Override
        public View getInfoWindow(Marker marker) {
           return null;
        }

        @Override
        public View getInfoContents(final Marker marker) {


            View view = getLayoutInflater().inflate(R.layout.custom_infowindow, null);

            MyMarker mymarker = hashMarkers.get(marker);
            product = detailInfo.get(marker);
            TextView brandName = (TextView) view.findViewById(R.id.brandName);
            TextView modelName = (TextView) view.findViewById(R.id.modelName);
            TextView price = (TextView) view.findViewById(R.id.price);
            ImageView image = (ImageView) view.findViewById(R.id.image);

            //set the details to infowindow
            brandName.setText(mymarker.getBrandName());
            modelName.setText(mymarker.getModelName());
            price.setText(mymarker.getPrice());
            DownLoadImageWithURLTask imageTask = new DownLoadImageWithURLTask();
            Bitmap response = imageTask.doInBackground("https://storage.googleapis.com/image-video/" + mymarker.getImage());
            int width = 100;
            int height = 100;
            if (response != null) {
                response = Bitmap.createScaledBitmap(response, width, height, true);
                image.setImageBitmap(response);
            }//end if()
            return view;
        }

    }
}


