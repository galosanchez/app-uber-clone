package com.galosanchez.appuberclone.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.galosanchez.appuberclone.R;
import com.galosanchez.appuberclone.controller.GoogleApiController;
import com.galosanchez.appuberclone.utils.DecodePoints;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.material.chip.Chip;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RouteActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private Toolbar toolbar;
    private LatLng originLatLng;
    private LatLng destinationLatLng;

    private GoogleApiController apiController;

    private List<LatLng> polylineList;
    private PolylineOptions polylineOptions;

    private Chip chipTime;
    private Chip chipDistance;
    private String placeOrigin;
    private String placeDestination;
    private String stringDistance;
    private String stringDuration;
    private EditText editTextDestination;
    private EditText editTextOrigin;
    private Button buttonCancel;
    private Button buttonNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Información del viaje");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        chipTime = findViewById(R.id.chipTime);
        chipDistance = findViewById(R.id.chipDistance);
        editTextDestination = findViewById(R.id.editTextDestination);
        editTextOrigin = findViewById(R.id.editTextOrigin);
        buttonCancel = findViewById(R.id.buttonCancel);
        buttonNext = findViewById(R.id.buttonNext);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentMap);
        mapFragment.getMapAsync(this);

        originLatLng = new LatLng(getIntent().getDoubleExtra("ORIGIN_LAT",0),getIntent().getDoubleExtra("ORIGIN_LON",0));
        destinationLatLng = new LatLng(getIntent().getDoubleExtra("DESTIN_LAT",0),getIntent().getDoubleExtra("DESTIN_LON",0));

        placeOrigin = getIntent().getStringExtra("ORIGIN");
        placeDestination = getIntent().getStringExtra("DESTINATION");


        apiController = new GoogleApiController(this);

        editTextDestination.setText(placeDestination);
        editTextOrigin.setText(placeOrigin);

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RouteActivity.this,SearchDriverActivity.class);
                intent.putExtra("ORIGIN_LAT", originLatLng.latitude);
                intent.putExtra("ORIGIN_LON", originLatLng.longitude);
                intent.putExtra("DESTIN_LAT", destinationLatLng.latitude);
                intent.putExtra("DESTIN_LON", destinationLatLng.longitude);
                intent.putExtra("ORIGIN", placeOrigin);
                intent.putExtra("DESTINATION", placeDestination);
                startActivity(intent);
                finish();
            }
        });

    }

    private void drawRoute(){
        apiController.getDiretions(originLatLng,destinationLatLng).enqueue(new Callback<String>() {
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

                    stringDistance = distance.getString("text");
                    stringDuration = duration.getString("text");

                    chipDistance.setText(stringDistance+".");
                    chipTime.setText(stringDuration);

                }catch (Exception e){

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
        map.getUiSettings().setZoomControlsEnabled(false);

        map.addMarker(new MarkerOptions().position(originLatLng).title("Origen").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_person_location)));
        map.addMarker(new MarkerOptions().position(destinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_person_destination)));
        map.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(originLatLng)
                        .zoom(14f)
                .build()
        ));

        drawRoute();

    }
}