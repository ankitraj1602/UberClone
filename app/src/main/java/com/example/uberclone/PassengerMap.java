package com.example.uberclone;


import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class PassengerMap extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private boolean permissionGranted=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_map);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        askingPermission();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        //locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng latLng = new LatLng(location.getLatitude(),
                        location.getLongitude());
                mMap.clear();
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.addMarker(new MarkerOptions().position(latLng).title("You"));
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



        if(permissionGranted == true){
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
            ==PackageManager.PERMISSION_GRANTED){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    0,0,locationListener);
            Location location1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            updateLocation(location1);
            Toast.makeText(this, "Granted!!", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(this, "Grant Permission!!", Toast.LENGTH_SHORT).show();
        }

        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));


    }

    public void updateLocation(Location loc){
        LatLng latLng = new LatLng(loc.getLatitude(),loc.getLongitude());
        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.addMarker(new MarkerOptions().title("Here, You Are").position(latLng));
    }

    //}
    private void askingPermission() {
        if (Build.VERSION.SDK_INT < 23) {
            permissionGranted = true;
            Toast.makeText(this, "Your phone is 23 less", Toast.LENGTH_SHORT).show();

        } else {
            if (Build.VERSION.SDK_INT >= 23) {
                if(ContextCompat.checkSelfPermission(PassengerMap.this,
 Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
                {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            1000);
                    Toast.makeText(this, "Requesting permission!", Toast.LENGTH_SHORT).show();
                }
                else{
                    permissionGranted = true;
                    Toast.makeText(this, "Other condition met,already " +
                            "gave permission", Toast.LENGTH_SHORT).show();
                }}
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==1000){
            if(grantResults.length>0 && grantResults[0]== RESULT_OK){
                permissionGranted = true;
            }
            else{
                Toast.makeText(this, "Grant Permission for Map Services", Toast.LENGTH_SHORT).show();
            }
        }
    }








}
