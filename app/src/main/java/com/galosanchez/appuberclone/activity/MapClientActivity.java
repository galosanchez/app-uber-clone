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
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;

public class MapClientActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private UserController userController;
    private Toolbar toolbar;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationClient;
    private final int LOCATION_REQUEST_CODE = 1;
    private final int SETTINGS_REQUEST_CODE = 2;

    private Marker marker;
    private GeofireController geofireController;
    private LatLng mlatLng;
    private boolean firstTime = true;
    private List<Marker> driversMarker = new ArrayList<>();
    private GoogleMap.OnCameraIdleListener onCameraIdleListener;
    private String placeOrigin;
    private String placeDestination;
    private LatLng originLatLng;
    private LatLng destinationLatLng;
    private EditText editTextDestination;
    private EditText editTextOrigin;
    private ImageView imageViewLocation;
    private boolean stateButtonOrigin = true;
    private boolean stateButtonDestination = false;
    private TokenController tokenController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_client);
        editTextDestination = findViewById(R.id.editTextDestination);
        editTextOrigin = findViewById(R.id.editTextOrigin);
        imageViewLocation = findViewById(R.id.imageViewLocation);
        tokenController = new TokenController();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setSubtitle(R.string.text_client);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentMap);
        mapFragment.getMapAsync(this);

        userController = new UserController();
        geofireController = new GeofireController("active_drivers");

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }

        onCameraIdleListener = new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                try {
                    if (stateButtonOrigin){
                        Geocoder geocoder = new Geocoder(MapClientActivity.this);
                        originLatLng = map.getCameraPosition().target;
                        List<Address> addressList = geocoder.getFromLocation(originLatLng.latitude, originLatLng.longitude, 1);
                        placeOrigin = addressList.get(0).getAddressLine(0);
                        editTextOrigin.setText(placeOrigin);
                    } else if(stateButtonDestination){
                        Geocoder geocoder = new Geocoder(MapClientActivity.this);
                        destinationLatLng = map.getCameraPosition().target;
                        List<Address> addressList = geocoder.getFromLocation(destinationLatLng.latitude, destinationLatLng.longitude, 1);
                        placeDestination = addressList.get(0).getAddressLine(0);
                        editTextDestination.setText(placeDestination);
                    }

                } catch (Exception e) {
                    Log.d("onCameraIdle",e.getMessage());
                }
            }
        };

        editTextOrigin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectOrigin();
            }
        });
        editTextDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDestination();
            }
        });

        generateToken();

    }

    private void selectDestination() {
        if (stateButtonOrigin){
            stateButtonOrigin = false;
            editTextOrigin.setTextColor(getResources().getColor(R.color.white_opt38));
        }
        if (!stateButtonDestination){
            stateButtonDestination = true;
            imageViewLocation.setImageResource(R.drawable.icon_person_destination);
            editTextDestination.setTextColor(getResources().getColor(R.color.white));
            editTextDestination.setHint("Selecciona tu destino");
        }
    }

    private void selectOrigin() {
        if (stateButtonDestination){
            stateButtonDestination = false;
            editTextDestination.setTextColor(getResources().getColor(R.color.white_opt38));
        }
        if (!stateButtonOrigin){
            stateButtonOrigin = true;
            imageViewLocation.setImageResource(R.drawable.icon_person_location);
            editTextOrigin.setTextColor(getResources().getColor(R.color.white));
        }
    }

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            //super.onLocationResult(locationResult);
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {

                    mlatLng = new LatLng(location.getLatitude(), location.getLongitude());

                    /*
                    if (marker!=null){
                        marker.remove();
                    }
                    marker = map.addMarker(new MarkerOptions().position(
                            new LatLng(location.getLatitude(),location.getLongitude()))
                            .title("Tu posición")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_person_location)));

                     */

                    if (firstTime) {
                        map.moveCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                        .zoom(16f)
                                        .build()
                        ));
                        firstTime = false;
                        getActiveDrivers();
                    }


                }
            }
        }
    };

    private void getActiveDrivers() {
        geofireController.getActiveDrivers(mlatLng, 10).addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                // Agregar conductores conectados
                for (Marker marker : driversMarker) {
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(key)) {
                            return;
                        }
                    }

                }
                LatLng driverLatLng = new LatLng(location.latitude, location.longitude);
                Marker marker = map.addMarker(new MarkerOptions().position(driverLatLng).title("Conductor disponible").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_map_car)));
                marker.setTag(key);
                driversMarker.add(marker);
            }

            @Override
            public void onKeyExited(String key) {
                // Eliminar conductores desconectados
                for (Marker marker : driversMarker) {
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(key)) {
                            marker.remove();
                            driversMarker.remove(marker);
                            return;
                        }
                    }

                }

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                // Actualizar posicion de los conductores activos
                for (Marker marker : driversMarker) {
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(key)) {
                            marker.setPosition(new LatLng(location.latitude, location.longitude));
                        }
                    }

                }


            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        MapStyleOptions styleMap = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_night);
        map = googleMap;
        map.setMapStyle(styleMap);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setOnCameraIdleListener(onCameraIdleListener);

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
                        map.setMyLocationEnabled(true);
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
        if (requestCode == SETTINGS_REQUEST_CODE && gpsActived()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            map.setMyLocationEnabled(true);

        } else if (requestCode == SETTINGS_REQUEST_CODE && !gpsActived()) {
            showAlertDialogGPS();
        }

        super.onActivityResult(requestCode, resultCode, data);
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
                    map.setMyLocationEnabled(true);
                }else{
                    showAlertDialogGPS();
                }
            }else {
                permissionLocation();
            }
        }else{
            if (gpsActived()){
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                map.setMyLocationEnabled(true);
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
                                ActivityCompat.requestPermissions(MapClientActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        }).setCancelable(false)
                        .show();
            } else {
                ActivityCompat.requestPermissions(MapClientActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.client_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.itemLogout:
                logOut();
                break;
            case R.id.itemRoute:
                getRoute();
                break;
            case R.id.itemProfile:
                goToProfile();
                break;
            default:

        }
        return super.onOptionsItemSelected(item);
    }

    private void goToProfile() {
        Intent intent = new Intent(MapClientActivity.this, ProfileClientActivity.class);
        startActivity(intent);
    }

    private void getRoute() {
        if (originLatLng != null && destinationLatLng != null){
            Intent intent = new Intent(MapClientActivity.this, RouteActivity.class);
            intent.putExtra("ORIGIN_LAT", originLatLng.latitude);
            intent.putExtra("ORIGIN_LON", originLatLng.longitude);
            intent.putExtra("DESTIN_LAT", destinationLatLng.latitude);
            intent.putExtra("DESTIN_LON", destinationLatLng.longitude);
            intent.putExtra("ORIGIN", placeOrigin);
            intent.putExtra("DESTINATION", placeDestination);
            startActivity(intent);
        }else{
            Toast.makeText(this, "Seleccionar el lugar de partida y el destino para continuar.", Toast.LENGTH_LONG).show();
        }
    }

    private void logOut() {
        userController.signOut();
        Intent intent = new Intent(MapClientActivity.this,MainActivity.class);
        MyFunctions.deleteBackStack(intent);
        startActivity(intent);
    }

    private void generateToken(){
        tokenController.create(userController.getUidCurrentUser());
    }

}