package com.example.uberclone;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class PassengerMap extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Button btnRequestCab;
    private boolean rideCanBeCancelled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_map);


        //locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnRequestCab = findViewById(R.id.btnRequestCab);
        btnRequestCab.setOnClickListener(this);

        //if there is an existing ride,then btnRequst will show option to cancel request
        ParseQuery<ParseObject> rides = ParseQuery.getQuery("CarRequests");
        rides.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
        rides.whereEqualTo("done","no");//give those rides who are not dealt with and are not cancelled

        rides.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(objects.size()>0 && e==null){
                    rideCanBeCancelled = true;
                    btnRequestCab.setText("Cancel Request");
                }
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.passenger_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logOutPassItem:
                ParseUser.getCurrentUser().logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e==null){
                            finish();
                        }
                    }
                });
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                updateLocation(location);
//                LatLng latLng = new LatLng(location.getLatitude(),
//                        location.getLongitude());
//                mMap.clear();
//                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//                mMap.addMarker(new MarkerOptions().position(latLng).title("You"));
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

        if (Build.VERSION.SDK_INT < 23) {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        } else if (Build.VERSION.SDK_INT >= 23) {

            if (ContextCompat.checkSelfPermission(PassengerMap.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(PassengerMap.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);


            } else {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location currentPassengerLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateLocation(currentPassengerLocation);


            }
        }




//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));




    }

    public void updateLocation(Location loc){
        LatLng latLng = new LatLng(loc.getLatitude(),loc.getLongitude());
        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
        mMap.addMarker(new MarkerOptions().title("Here, You Are").position(latLng));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            if (ContextCompat.checkSelfPermission(PassengerMap.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location currentPassengerLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateLocation(currentPassengerLocation);

            }
        }

    }

    @Override
    public void onClick(View view) {

        if(rideCanBeCancelled == false) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission
                    .ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Location now = locationManager.getLastKnownLocation(
                        LocationManager.GPS_PROVIDER);
                Toast.makeText(this, "sending request", Toast.LENGTH_SHORT).show();
                ParseObject requests = new ParseObject("CarRequests");
                requests.put("username", ParseUser.getCurrentUser().getUsername());
                ParseGeoPoint location = new ParseGeoPoint(now.getLatitude(), now.getLongitude());
                requests.put("location", location);
                requests.put("done", "no");

                requests.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            btnRequestCab.setText("Cancel Request");
                            Toast.makeText(PassengerMap.this, "sending request", Toast.LENGTH_SHORT).show();
                            rideCanBeCancelled = true;

                        }
                    }
                });

            } else {
                Toast.makeText(this, "Unknown error,Please try again", Toast.LENGTH_SHORT).show();
            }
        }

        else{
            if(rideCanBeCancelled == true){
                ParseQuery<ParseObject> rides = ParseQuery.getQuery("CarRequests");
                rides.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
                rides.whereEqualTo("done","no");//only cancel those rides which are in server and are not dealt with.

                rides.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if(objects.size()>0 && e==null){
                            btnRequestCab.setText("REQUEST CAB");
                            rideCanBeCancelled=false;
                            for(ParseObject singleRide:objects){
                                singleRide.put("done","cancelled");
                                singleRide.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if(e==null){
                                            Toast.makeText(PassengerMap.this, "Cancelled Ride!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            }

                        }
                    }
                });
            }
        }
    }
}









