package com.example.max.uberclone;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class DriverLocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationListener locationListener;
    LocationManager locationManager;
    Intent intent;
    Button acceptRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_location);

        intent = getIntent();
        acceptRequest = (Button) findViewById(R.id.acceptRequest);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    public void acceptRequest(View view){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Requests");
        query.whereEqualTo("username", intent.getStringExtra("username"));
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null){
                    if (objects.size()>0){
                        for (ParseObject object:objects){
                            object.put("driverUsername", ParseUser.getCurrentUser().getUsername());
                            object.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e==null){
                                        Double riderLat = intent.getDoubleExtra("riderLat", 0);
                                        Double riderLon = intent.getDoubleExtra("riderLon", 0);
                                        Double driverLat = intent.getDoubleExtra("driverLat", 0);
                                        Double driverLon = intent.getDoubleExtra("driverLon", 0);


                                        Intent directionIntent = new Intent(android.content.Intent.ACTION_VIEW,
                                                Uri.parse("http://maps.google.com/maps?saddr="+ driverLat+","+driverLon+"&daddr="+riderLat+","+ riderLon));
                                        startActivity(directionIntent);
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });

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

        RelativeLayout mapLayout = (RelativeLayout)findViewById(R.id.map_layout);
        mapLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Double riderLat = intent.getDoubleExtra("riderLat", 0);
                Double riderLon = intent.getDoubleExtra("riderLon", 0);
                Double driverLat = intent.getDoubleExtra("driverLat", 0);
                Double driverLon = intent.getDoubleExtra("driverLon", 0);


                //mMap.clear();
                LatLng yourLocation = new LatLng(driverLat, driverLon);
                LatLng riderLocation = new LatLng(riderLat, riderLon);

                ArrayList<Marker> markers = new ArrayList<>();
                markers.add(mMap.addMarker(new MarkerOptions().position(yourLocation).title("Your Location")));
                markers.add(mMap.addMarker(new MarkerOptions().position(riderLocation).title("Rider Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))));

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (Marker marker : markers) {
                    builder.include(marker.getPosition());
                }
                LatLngBounds bounds = builder.build();

                int padding = 70; // offset from edges of the map in pixels
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

                mMap.animateCamera(cu);




            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(Context.LOCATION_SERVICE, 0, 0, locationListener);
                }
            }
        }
    }

    public void updateMap(Location location){

    }
}
