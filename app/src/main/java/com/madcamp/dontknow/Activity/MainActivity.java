package com.madcamp.dontknow.Activity;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.madcamp.dontknow.Fragment.*;
import com.madcamp.dontknow.R;
import android.support.design.widget.TabLayout;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    public static String myTel=null;
    public static String url = "http://52.231.68.157:8080/api";



    GeoDataClient mGeoDataClient;
    PlaceDetectionClient mPlaceDetectionClient;
    FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean mLocationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;




    public Tab1_Fragment tab1_fragment = new Tab1_Fragment();
    public Tab2_ParentFragment tab2_parentFragment = new Tab2_ParentFragment();
    public Tab3_Fragment tab3_fragment = new Tab3_Fragment();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }




    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    private void getDeviceLocation() {
//        final RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        System.out.println(mLocationPermissionGranted);
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            Location new_loc = task.getResult();
//                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
//                                    new LatLng(mLastKnownLocation.getLatitude(),
//                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            final double lat = new_loc.getLatitude();
                            final double lng = new_loc.getLongitude();
//                            System.out.println(Double.toString(yourlat)+", "+Double.toString(yourlng));

                            Volley.newRequestQueue(getApplicationContext()).add(new StringRequest(Request.Method.PUT, url+"/profiles/"+MainActivity.myTel, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    System.out.println("Location uploaded : "+lat+", "+lng);
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    System.out.println(error);
                                }
                            }) {
                                protected Map<String, String> getParams() {
                                    Map<String, String> MyData = new HashMap<String, String>();
                                    MyData.put("loc", Double.toString(lat)+", "+Double.toString(lng));
                                    return MyData;
                                }
                            });

                        } else {
//                            Log.d(TAG, "Current location is null. Using defaults.");
//                            Log.e(TAG, "Exception: %s", task.getException());
//                            mMap.moveCamera(CameraUpdateFactory
//                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
//                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    class LocThread extends Thread {
        @Override
        public void run() {
            super.run();
            while(true) {
                getDeviceLocation();
                try {
                    sleep(1000 * 5);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("Got location");
            }
        }
    }

    protected void Initialize() {
        TabLayout tab = findViewById(R.id.tab);
        ViewPager viewPager = findViewById(R.id.view_pager);
        mainAdapter adapter = new mainAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tab.setupWithViewPager(viewPager);



        mGeoDataClient = Places.getGeoDataClient(this, null);
        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);
        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLocationPermission();

        new LocThread().start();

    }


    public void joinButtonClick(View view){
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://52.231.68.157:8080/api/register";
        final String user_tel = ((EditText)findViewById(R.id.editText1)).getText().toString();
        final String user_name = ((EditText)findViewById(R.id.editText2)).getText().toString();
        final String user_pw = ((EditText)findViewById(R.id.editText3)).getText().toString();
        final String user_code = ((EditText)findViewById(R.id.editText4)).getText().toString();
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response", response);
                        if(response.equals("OK")){
                            //TODO
                            //change visibility
                            MainActivity.myTel = user_tel;
                            findViewById(R.id.pre_frame).setVisibility(View.INVISIBLE);
                            findViewById(R.id.main_frame).setVisibility(View.VISIBLE);
                            Initialize();
                        }
                        else if(response.equals("NO")){
                            Toast.makeText(getApplicationContext(), "same phone number already exists", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.default_image);
                ByteArrayOutputStream baos = null;
                try {
                    baos = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.PNG, 100, baos);
                } finally {
                    if (baos != null) {
                        try {
                            baos.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                String str = Base64.encodeToString(baos.toByteArray(),Base64.DEFAULT);
                params.put("mainImage",str);
                params.put("background1",str);
                params.put("background2",str);
                params.put("background3",str);
                params.put("background4",str);
                params.put("background5",str);
                JSONArray arr = new JSONArray();
                params.put("registered",arr.toString());
                params.put("tel", user_tel);
                params.put("name", user_name);
                params.put("pw", user_pw);
                params.put("code", user_code);
                params.put("loc", "0, 0");
                return params;
            }
        };
        queue.add(postRequest);
    }

    public void loginButtonClick(View view){
        RequestQueue queue = Volley.newRequestQueue(this);

        final String user_tel = ((EditText)findViewById(R.id.editText5)).getText().toString();
        final String user_pw = ((EditText)findViewById(R.id.editText6)).getText().toString();
        String url = "http://52.231.68.157:8080/api/login/"+user_tel+"/"+user_pw;
        StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response", response);
                        if(response.equals("OK")){
                            MainActivity.myTel = user_tel;
                            findViewById(R.id.pre_frame).setVisibility(View.INVISIBLE);
                            findViewById(R.id.main_frame).setVisibility(View.VISIBLE);
                            Initialize();
                        }
                        else if(response.equals("NO")){
                            Toast.makeText(getApplicationContext(), "wrong login information", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                }
        );
        queue.add(postRequest);
    }

    class mainAdapter extends FragmentPagerAdapter {


        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch(position) {
                case 0 : return "Page1";
                case 1 : return "Page2";
                case 2 : return "Page3";
                default : return null;
            }
        }

        public mainAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0 : return tab1_fragment;
                case 1 : return tab2_parentFragment;
                case 2 : return tab3_fragment;
                default : return null;
            }
        }
    }
}