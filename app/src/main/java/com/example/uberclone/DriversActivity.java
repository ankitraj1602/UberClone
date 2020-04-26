package com.example.uberclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DriversActivity extends AppCompatActivity implements View.OnClickListener {

    private ArrayList<HashMap<String,String>> nearByData;
    private SimpleAdapter simpleAdapter;
    private ListView listView;
    private Button btnGetRequests;
    private ArrayList<String> usernameArray;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private boolean firstTimeListviewSorted=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drivers);

        btnGetRequests = findViewById(R.id.btnUpdateListview);
        btnGetRequests.setOnClickListener(this);
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        listView = findViewById(R.id.listviewInfo);
        nearByData = new ArrayList<>();
        usernameArray = new ArrayList<>();
        simpleAdapter = new SimpleAdapter(this,nearByData,
                android.R.layout.simple_list_item_2,new String[]
                {"usernamePassenger","distance"}
                ,new int[]{android.R.id.text1,android.R.id
                .text2}
                );
        listView.setAdapter(simpleAdapter);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                 updatedLists(location);//I dont know why but this one is being called
                //three times or something..
                Log.i("Times","Printed times");

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if(Build.VERSION.SDK_INT>=23 && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED )
        {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                    ,1000);
        }
        else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    0,0,locationListener);
//            Location now = locationManager.getLastKnownLocation(
//            LocationManager.GPS_PROVIDER);
//            fetchLocation(now);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.passenger_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.logOutPassItem:
                if(ParseUser.getCurrentUser()!=null){
                    ParseUser.getCurrentUser().logOutInBackground(new LogOutCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e==null){
                                Intent intentBack = new Intent (DriversActivity.this,
                                        MainActivity.class);
                                startActivity(intentBack);
                                finish();
                            }
                        }
                    });
                }
                break;
        }



        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
//        if(firstTimeListviewSorted==true){
//            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//            if(location!=null){fetchLocation(location);}
//            //listView.setAdapter(simpleAdapter);
//            firstTimeListviewSorted=false;
//            Toast.makeText(this, "First done", Toast.LENGTH_SHORT).show();
//        }
//        else if(firstTimeListviewSorted==false) {
//            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//
//            if (location != null) {
//                updatedLists(location);
//
//            }
//            }
        }

        private void updatedLists(Location location){
            if (location != null) {
//                ParseQuery<ParseObject> requests1 = ParseQuery.getQuery("CarRequests");
////                requests1.whereEqualTo("done", "yes");
////                requests1.whereContainedIn("username",usernameArray);
////                requests1.findInBackground(new FindCallback<ParseObject>() {
////                    @Override
////                    public void done(List<ParseObject> objects, ParseException e) {
////                        if(objects.size()>0 && e==null){
////                            for(ParseObject one:objects){
////                                String name = one.get("username").toString();
////                                int index =  usernameArray.indexOf(name);
////                                usernameArray.remove(index);
////                                HashMap<String,String> map = new HashMap<>();
////                                map.put("usernamePassenger",name);
////                                ParseGeoPoint loc = (ParseGeoPoint)one.get("location");
////                               // map.put("",loc+"");
////                                int index1 = nearByData.indexOf(map);
////                                nearByData.remove(index1);
////                            }
////                            simpleAdapter.notifyDataSetChanged();
////                        }
////                    }
////                });


                final ParseGeoPoint driversGeoPoint = new ParseGeoPoint(
                        location.getLatitude(), location.getLongitude());
                ParseQuery<ParseObject> requests = ParseQuery.getQuery("CarRequests");
                requests.whereEqualTo("done", "no");
                requests.whereNear("location", driversGeoPoint);
                requests.whereNotContainedIn("username",usernameArray);

                requests.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (e == null && objects.size() > 0) {
                            for (ParseObject singleRequest : objects) {

                                ParseGeoPoint loc = (ParseGeoPoint) singleRequest.get("location");
                                HashMap<String, String> dataMap = new HashMap<>();
                                dataMap.put("usernamePassenger", singleRequest.get("username")
                                        .toString());
                                double distanceBetween = loc.distanceInKilometersTo(
                                        driversGeoPoint);
                                distanceBetween = Math.round(distanceBetween * 10) / 10;
                                dataMap.put("distance", "" + distanceBetween);

                                nearByData.add(dataMap);
                                usernameArray.add(singleRequest.get("username")+"");


                            }

                            simpleAdapter.notifyDataSetChanged();
                        }
                    }
                });

            }
        }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==1000){
            if(grantResults.length>0 && grantResults[0]==PackageManager
                    .PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                       0,0,locationListener);
//                Location now = locationManager.getLastKnownLocation(
//                        LocationManager.GPS_PROVIDER);
//                fetchLocation(now);
            }
            else{
                Toast.makeText(this, "Permission denied,Can't operate!", Toast.LENGTH_SHORT).show();
            }
        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

//    private void fetchLocation(Location location) {
//
//        if (location != null) {
//            //nearByData.clear();
//
//            final ParseGeoPoint driversGeoPoint = new ParseGeoPoint(
//                    location.getLatitude(), location.getLongitude());
//            ParseQuery<ParseObject> requests = ParseQuery.getQuery("CarRequests");
//            requests.whereEqualTo("done", "no");
//            requests.whereNear("location", driversGeoPoint);
//            requests.whereNotContainedIn("username",usernameArray);
//
//
//            requests.findInBackground(new FindCallback<ParseObject>() {
//                @Override
//                public void done(List<ParseObject> objects, ParseException e) {
//                    if (e == null && objects.size() > 0) {
//                        for (ParseObject singleRequest : objects) {
//
//                            ParseGeoPoint loc = (ParseGeoPoint) singleRequest.get("location");
//                            HashMap<String, String> dataMap = new HashMap<>();
//                            dataMap.put("usernamePassenger", singleRequest.get("username")
//                                    .toString());
//                            double distanceBetween = loc.distanceInKilometersTo(
//                                    driversGeoPoint);
//                            distanceBetween = Math.round(distanceBetween * 10) / 10;
//                            dataMap.put("distance", "" + distanceBetween);
//
//                            nearByData.add(dataMap);
//                            usernameArray.add(singleRequest.get("username").toString());
//
//
//                        }
//
//                        listView.setAdapter(simpleAdapter);
//                    } else {
//                        Toast.makeText(DriversActivity.this, "There" +
//                                "are no rides now", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });
//        }
//    }
}
