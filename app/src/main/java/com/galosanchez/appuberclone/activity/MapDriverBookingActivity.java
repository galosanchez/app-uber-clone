package com.galosanchez.appuberclone.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.galosanchez.appuberclone.R;
import com.galosanchez.appuberclone.controller.ClientBookingController;
import com.galosanchez.appuberclone.controller.ClientController;
import com.galosanchez.appuberclone.controller.GeofireController;
import com.galosanchez.appuberclone.controller.GoogleApiController;
import com.galosanchez.appuberclone.controller.TokenController;
import com.galosanchez.appuberclone.controller.UserController;
import com.galosanchez.appuberclone.utils.DecodePoints;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapDriverBookingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private UserController userController;
    private ClientController clientController;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationClient;
    private final int LOCATION_REQUEST_CODE = 1;
    private final int SETTINGS_REQUEST_CODE = 2;
    private LatLng mlatLng;
    private GeofireController geofireController;
    private ClientBookingController clientBookingController;
    private LatLng originLatLng;
    private LatLng destinationLatLng;

    private GoogleApiController apiController;
    private List<LatLng> polylineList;
    private PolylineOptions polylineOptions;

    private Marker marker;

    private TextView textViewName;
    private TextView textViewEmail;
    private EditText editTextOrigin;
    private EditText editTextDestination;
    private Button buttonNext;
    private ImageView imageViewPhoto;

    private String extraIdClient;
    private boolean firstTime = true;
    private String stateButtonNext ="start";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_driver_booking);

        userController = new UserController();
        clientController = new ClientController();
        clientBookingController = new ClientBookingController();
        geofireController = new GeofireController("drivers_working");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentMap);
        mapFragment.getMapAsync(this);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }

        textViewName = findViewById(R.id.textViewName);
        textViewEmail = findViewById(R.id.textViewEmail);
        editTextOrigin = findViewById(R.id.editTextOrigin);
        editTextDestination = findViewById(R.id.editTextDestination);
        buttonNext = findViewById(R.id.buttonNext);
        imageViewPhoto = findViewById(R.id.imageViewPhoto);

        extraIdClient = getIntent().getStringExtra("idClient");
        apiController = new GoogleApiController(this);
        getClient();

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (stateButtonNext.equals("start")){
                    startBooking();
                }else if (stateButtonNext.equals("finish")){
                    finishBooking();
                }
            }
        });

    }

    private void startBooking() {
        clientBookingController.updateStatus(extraIdClient,"start").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                stateButtonNext = "finish";
                buttonNext.setTextColor(getResources().getColor(R.color.green_500));
                buttonNext.setText("Finalizar");

                map.clear();
                map.addMarker(new MarkerOptions().position(destinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_person_destination)));
                drawRoute(destinationLatLng);
            }
        });
    }

    private void finishBooking() {
        clientBookingController.updateStatus(extraIdClient,"finish");
        if (fusedLocationClient != null){
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        geofireController.removeLocation(userController.getUidCurrentUser());
        Toast.makeText(this, "Viaje finalizado", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MapDriverBookingActivity.this,MapDriverActivity.class);
        startActivity(intent);
        finish();
    }

    private double getDistanceBetween(LatLng clientLatLng, LatLng driverLatLng){
        double distance = 0;
        Location clientLocation = new Location("");
        Location driverLocation = new Location("");
        clientLocation.setLatitude(clientLatLng.latitude);
        clientLocation.setLongitude(clientLatLng.longitude);
        driverLocation.setLatitude(driverLatLng.latitude);
        driverLocation.setLongitude(driverLatLng.longitude);
        distance = clientLocation.distanceTo(driverLocation);
        return distance;
    }

    private void getCLientBooking() {
        clientBookingController.getClientBooking(extraIdClient).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String destino = snapshot.child("destination").getValue().toString();
                    String origin = snapshot.child("origin").getValue().toString();
                    double destinoLat = Double.parseDouble(snapshot.child("destinationLat").getValue().toString());
                    double destinoLng = Double.parseDouble(snapshot.child("destinationLng").getValue().toString());
                    double originLat = Double.parseDouble(snapshot.child("originLat").getValue().toString());
                    double originLng = Double.parseDouble(snapshot.child("originLng").getValue().toString());
                    originLatLng = new LatLng(originLat,originLng);
                    destinationLatLng = new LatLng(destinoLat,destinoLng);
                    editTextOrigin.setText(origin);
                    editTextDestination.setText(destino);
                    map.addMarker(new MarkerOptions().position(originLatLng).title("Recoger aquí").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_person_location)));
                    drawRoute(originLatLng);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getClient() {
        clientController.getClient(extraIdClient).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String email = snapshot.child("email").getValue().toString();
                    String name = snapshot.child("name").getValue().toString();
                    String image = "";
                    if (snapshot.hasChild("image")){
                        image = snapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(imageViewPhoto);
                    }
                    textViewName.setText(name);
                    textViewEmail.setText(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            //super.onLocationResult(locationResult);
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {

                    mlatLng = new LatLng(location.getLatitude(),location.getLongitude());

                    if (marker!=null){
                        marker.remove();
                    }
                    marker = map.addMarker(new MarkerOptions().position(
                                    new LatLng(location.getLatitude(),location.getLongitude()))
                            .title("Tu posición")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_map_car)));


                    if (firstTime) {
                        map.moveCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                        .zoom(16f)
                                        .build()
                        ));
                        firstTime = false;
                        getCLientBooking();
                    }
                    updateLocation();

                }
            }
        }
    };

    private boolean isCloseClient = false;
    private void updateLocation() {
        if (userController.existSession() && mlatLng != null){
            geofireController.saveLocation(userController.getUidCurrentUser(),mlatLng);
            if (!isCloseClient){
                if (originLatLng != null && mlatLng != null){
                    double distance = getDistanceBetween(originLatLng,mlatLng); //metros
                    if (distance <= 200){
                        isCloseClient = true;
                        buttonNext.setVisibility(View.VISIBLE);
                        Toast.makeText(this, "Te encuentras cerca del cliente.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        MapStyleOptions styleMap = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_night);
        map = googleMap;
        map.setMapStyle(styleMap);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setZoomControlsEnabled(true);

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(5);

        startLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (gpsActived()) {
                        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                    } else {
                        showAlertDialogGPS();
                    }
                } else {
                    permissionLocation();
                }
            } else {
                permissionLocation();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST_CODE && gpsActived()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        } else{
            showAlertDialogGPS();
        }
    }

    private void showAlertDialogGPS(){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Permiso de GPS")
                .setMessage("Es necesario que el GPS esté activo.")
                .setPositiveButton("Configuraciones", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),SETTINGS_REQUEST_CODE);
                    }
                }).setCancelable(false)
                .show();
    }

    private boolean gpsActived(){
        boolean isActive = false;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            isActive = true;
        }
        return isActive;
    }

    private void startLocation(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                if (gpsActived()){
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                }else{
                    showAlertDialogGPS();
                }
            }else {
                permissionLocation();
            }
        }else{
            if (gpsActived()){
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            }else{
                showAlertDialogGPS();
            }
        }
    }

    private void permissionLocation(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Permiso de ubicación")
                        .setMessage("Esta aplicación requiere de los permisos de ubicación para poder continuar.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapDriverBookingActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        }).setCancelable(false)
                        .show();
            } else {
                ActivityCompat.requestPermissions(MapDriverBookingActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

    private void disconnect(){
        if (fusedLocationClient != null){
            fusedLocationClient.removeLocationUpdates(locationCallback);
            if (userController.existSession()){
                geofireController.removeLocation(userController.getUidCurrentUser());
            }
        }else {
            Toast.makeText(this, "No te puedes desconectar", Toast.LENGTH_SHORT).show();
        }
    }

    private void drawRoute(LatLng latLng){
        apiController.getDiretions(mlatLng,latLng).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body());
                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                    JSONObject route = jsonArray.getJSONObject(0);
                    JSONObject polylines = route.getJSONObject("overview_polyline");
                    String points = polylines.getString("points");
                    polylineList = DecodePoints.decodePoly(points);
                    polylineOptions = new PolylineOptions();
//                    polylineOptions.color(Color.rgb(65,205,125));
                    polylineOptions.color(Color.rgb(48,182,105));
                    polylineOptions.width(10f);
                    polylineOptions.startCap(new SquareCap());
                    polylineOptions.jointType(JointType.ROUND);
                    polylineOptions.addAll(polylineList);
                    map.addPolyline(polylineOptions);

                    JSONArray legs = route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject distance = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration");

                }catch (Exception e){
                    Log.d("RouteActivity", e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }


}