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
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.galosanchez.appuberclone.controller.TokenController;
import com.galosanchez.appuberclone.utils.MyFunctions;
import com.galosanchez.appuberclone.R;
import com.galosanchez.appuberclone.controller.GeofireController;
import com.galosanchez.appuberclone.controller.UserController;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class MapDriverActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private UserController userController;
    private Toolbar toolbar;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationClient;
    private final int LOCATION_REQUEST_CODE = 1;
    private final int SETTINGS_REQUEST_CODE = 2;
    private boolean isConnect = false;
    private LatLng mlatLng;
    private GeofireController geofireController;
    private boolean firstTime = true;

    private Marker marker;
    private TokenController tokenController;
    private  ValueEventListener listener;
    private Menu menuTitle;
    private MenuItem itemState;
    private MenuItem itemStateIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_driver);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setSubtitle(R.string.text_driver);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentMap);
        mapFragment.getMapAsync(this);

        userController = new UserController();
        geofireController = new GeofireController("active_drivers");
        tokenController = new TokenController();
        uidTemporal = userController.getUidCurrentUser();

        generateToken();
        isDriverWorking();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null){
            geofireController.isDriverWoiking(uidTemporal).removeEventListener(listener);
        }
    }

    private void isDriverWorking() {
        listener = geofireController.isDriverWoiking(userController.getUidCurrentUser()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    disconnect();
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
                    }
                    
                    updateLocation();
                    
                }
            }
        }
    };

    private void updateLocation() {
        if (userController.existSession() && mlatLng != null){
            geofireController.saveLocation(userController.getUidCurrentUser(),mlatLng);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        MapStyleOptions styleMap = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_night);
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setMapStyle(styleMap);
        map.getUiSettings().setZoomControlsEnabled(true);

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(5);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (gpsActived()) {
                        // Connect
                        connect();
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
            // Connect
            connect();
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
                    // Connect
                    connect();
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                }else{
                    showAlertDialogGPS();
                }
            }else {
                permissionLocation();
            }
        }else{
            if (gpsActived()){
                // Connect
                connect();
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
                                ActivityCompat.requestPermissions(MapDriverActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        }).setCancelable(false)
                        .show();
            } else {
                ActivityCompat.requestPermissions(MapDriverActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.driver_menu, menu);
        menuTitle = menu;
        super.onCreateOptionsMenu(menu);
        itemState = menuTitle.findItem(R.id.itemState);
        itemStateIcon = menuTitle.findItem(R.id.app_bar_state);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.itemLogout:
                logOut();
                break;
            case R.id.itemState:
                changeState();
                break;
            case R.id.itemProfile:
                goToProfile();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    private void goToProfile() {
        Intent intent = new Intent(MapDriverActivity.this,ProfileDriverActivity.class);
        startActivity(intent);
    }

    private void changeState() {
        if (isConnect){
            disconnect();
        }else {
            startLocation();
        }
    }

    private String uidTemporal;
    private void logOut() {
        disconnect();
        userController.signOut();
        Intent intent = new Intent(MapDriverActivity.this,MainActivity.class);
        MyFunctions.deleteBackStack(intent);
        startActivity(intent);
    }

    private void connect(){
        itemState.setTitle(R.string.text_disconnect);
        itemStateIcon.setIcon(R.drawable.ic_state_on);
        isConnect = true;
        //map.setMyLocationEnabled(true);
    }

    private void disconnect(){
        if (fusedLocationClient != null){
            itemState.setTitle(R.string.text_connect);
            itemStateIcon.setIcon(R.drawable.ic_state_off);
            isConnect = false;
            fusedLocationClient.removeLocationUpdates(locationCallback);
            if (userController.existSession()){
                geofireController.removeLocation(userController.getUidCurrentUser());
            }
        }else {
            Toast.makeText(this, "No te puedes desconectar", Toast.LENGTH_SHORT).show();
        }
    }

    private void generateToken(){
        tokenController.create(userController.getUidCurrentUser());
    }

}