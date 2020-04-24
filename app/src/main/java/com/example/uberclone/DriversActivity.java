package com.example.uberclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DriversActivity extends AppCompatActivity implements View.OnClickListener {

    private ArrayList<HashMap<String,String>> nearByData;
    private SimpleAdapter simpleAdapter;
    private ListView listView;
    private Button btnGetRequests;
    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drivers);

        btnGetRequests = findViewById(R.id.btnUpdateListview);
        btnGetRequests.setOnClickListener(this);
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        listView = findViewById(R.id.listviewInfo);
        nearByData = new ArrayList<>();
        simpleAdapter = new SimpleAdapter(this,nearByData,
                android.R.layout.simple_list_item_2,new String[]
                {"usernamePassenger","distance"}
                ,new int[]{android.R.id.text1,android.R.id
                .text2}
                );
        listView.setAdapter(simpleAdapter);
        nearByData.clear();

    }

    @Override
    public void onClick(View v) {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                fetchLocation(location);

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

        else if (Build.VERSION.SDK_INT < 23){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    0,0,locationListener);
            Location now = locationManager.getLastKnownLocation(
                    LocationManager.GPS_PROVIDER);
            fetchLocation(now);
        }

        else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    0,0,locationListener);
            Location now = locationManager.getLastKnownLocation(
                    LocationManager.GPS_PROVIDER);
            fetchLocation(now);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==1000){
            if(grantResults.length>0 && grantResults[0]==PackageManager
                    .PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        0,0,locationListener);
                Location now = locationManager.getLastKnownLocation(
                        LocationManager.GPS_PROVIDER);
                fetchLocation(now);
            }
            else{
                Toast.makeText(this, "Permission denied,Can't operate!", Toast.LENGTH_SHORT).show();
            }
        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    private void fetchLocation(Location location) {

        if (location != null) {
            nearByData.clear();

            final ParseGeoPoint driversGeoPoint = new ParseGeoPoint(
                    location.getLatitude(), location.getLongitude());
            ParseQuery<ParseObject> requests = ParseQuery.getQuery("CarRequests");
            requests.whereEqualTo("done", "no");
            requests.whereNear("location", driversGeoPoint);

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

                        }

                        simpleAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(DriversActivity.this, "There" +
                                "are no rides now", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
