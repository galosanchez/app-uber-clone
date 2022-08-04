package com.galosanchez.appuberclone.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.galosanchez.appuberclone.R;
import com.galosanchez.appuberclone.controller.ClientBookingController;
import com.galosanchez.appuberclone.controller.DriverController;
import com.galosanchez.appuberclone.controller.GeofireController;
import com.galosanchez.appuberclone.controller.GoogleApiController;
import com.galosanchez.appuberclone.controller.TokenController;
import com.galosanchez.appuberclone.controller.UserController;
import com.galosanchez.appuberclone.utils.DecodePoints;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
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
import com.google.android.libraries.places.api.Places;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapClientBookingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private UserController userController;
    private ClientBookingController clientBookingController;

    private Marker markerDriver;
    private GeofireController geofireController;
    private boolean firstTime = true;
    private String placeOrigin;
    private String placeDestination;
    private LatLng originLatLng;
    private LatLng destinationLatLng;
    private LatLng driverLatLng;


    private static int AUTOCOMPLETE_REQUEST_CODE = 1;
    private ImageView imageViewLocation;
    private boolean stateButtonOrigin = true;
    private boolean stateButtonDestination = false;
    private TokenController tokenController;

    private TextView textViewName;
    private TextView textViewEmail;
    private TextView textViewState;
    private EditText editTextOrigin;
    private EditText editTextDestination;
    private ImageView imageViewPhoto;
    private GoogleApiController apiController;
    private List<LatLng> polylineList;
    private PolylineOptions polylineOptions;
    private DriverController driverController;

    private ValueEventListener listener;

    private String idDriver;
    private ValueEventListener listenerStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_client_booking);

        tokenController = new TokenController();
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentMap);
        mapFragment.getMapAsync(this);

        userController = new UserController();
        geofireController = new GeofireController("drivers_working");
        clientBookingController = new ClientBookingController();
        apiController = new GoogleApiController(this);
        driverController = new DriverController();

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }

        textViewName = findViewById(R.id.textViewName);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewState = findViewById(R.id.textViewState);
        editTextOrigin = findViewById(R.id.editTextOrigin);
        editTextDestination = findViewById(R.id.editTextDestination);
        imageViewPhoto = findViewById(R.id.imageViewPhoto);

        getStatus();

        getCLientBooking();

    }

    private void getStatus() {
        listenerStatus = clientBookingController.getStatus(userController.getUidCurrentUser()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String status = snapshot.getValue().toString();
                    if (status.equals("accept")){
                        textViewState.setText("Estado: aceptado");
                    }else if (status.equals("start")){
                        textViewState.setText("Estado: iniciado");
                        startBooking();
                    }else if (status.equals("finish")){
                        textViewState.setText("Estado: finalizado");
                        finishBooking();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void finishBooking() {
        Toast.makeText(this, "Viaje finalizado", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void startBooking() {
        map.clear();
        map.addMarker(new MarkerOptions().position(destinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_person_destination)));
        drawRoute(destinationLatLng);
    }

    private void getCLientBooking() {
        clientBookingController.getClientBooking(userController.getUidCurrentUser()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String destino = snapshot.child("destination").getValue().toString();
                    String origin = snapshot.child("origin").getValue().toString();
                    idDriver = snapshot.child("idDriver").getValue().toString();
                    double destinoLat = Double.parseDouble(snapshot.child("destinationLat").getValue().toString());
                    double destinoLng = Double.parseDouble(snapshot.child("destinationLng").getValue().toString());
                    double originLat = Double.parseDouble(snapshot.child("originLat").getValue().toString());
                    double originLng = Double.parseDouble(snapshot.child("originLng").getValue().toString());
                    originLatLng = new LatLng(originLat,originLng);
                    destinationLatLng = new LatLng(destinoLat,destinoLng);
                    editTextOrigin.setText(origin);
                    editTextDestination.setText(destino);
                    //map.addMarker(new MarkerOptions().position(originLatLng).title("Recoger aquí").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_person_location)));
                    getDriver(idDriver);
                    getDriverLocation(idDriver,originLatLng);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getDriver(String idDriver) {
        driverController.getDriver(idDriver).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String name = snapshot.child("name").getValue().toString();
                    String email = snapshot.child("email").getValue().toString();
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

    private void getDriverLocation(String idDriver, LatLng latLngOr) {
        listener = geofireController.getDriverLocation(idDriver).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    double lat = Double.parseDouble(snapshot.child("0").getValue().toString());
                    double lng = Double.parseDouble(snapshot.child("1").getValue().toString());
                    driverLatLng = new LatLng(lat,lng);
                    if (markerDriver != null){
                        markerDriver.remove();
                    }
                    markerDriver = map.addMarker(new MarkerOptions()
                            .position(new LatLng(lat,lng))
                            .title("Tu conductor")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_map_car)));
                    if (firstTime){
                        map.moveCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        .target(driverLatLng)
                                        .zoom(16f)
                                        .build()
                        ));
                        map.addMarker(new MarkerOptions().position(latLngOr).title("Recoger aquí").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_person_location)));
                        firstTime = false;
                        drawRoute(originLatLng);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void drawRoute(LatLng latLng){
        apiController.getDiretions(driverLatLng,latLng).enqueue(new Callback<String>() {
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

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        MapStyleOptions styleMap = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_night);
        map = googleMap;
        map.setMapStyle(styleMap);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setZoomControlsEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null){
            geofireController.getDriverLocation(idDriver).removeEventListener(listener);
        }
        if (listenerStatus != null){
            clientBookingController.getStatus(userController.getUidCurrentUser()).removeEventListener(listenerStatus);
        }
    }
}