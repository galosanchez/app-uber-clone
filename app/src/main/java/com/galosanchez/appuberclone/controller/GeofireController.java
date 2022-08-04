package com.galosanchez.appuberclone.controller;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GeofireController {

    private DatabaseReference database;
    private GeoFire geoFire;

    public GeofireController(String reference){
        database = FirebaseDatabase.getInstance().getReference().child(reference);
        geoFire = new GeoFire(database);
    }

    public void saveLocation(String keyDriver, LatLng latLng){
        geoFire.setLocation(keyDriver, new GeoLocation(latLng.latitude, latLng.longitude));
    }

    public void removeLocation(String keyDriver){
        geoFire.removeLocation(keyDriver);
    }

    public GeoQuery getActiveDrivers(LatLng latLng, double radious){
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude), radious);
        geoQuery.removeAllListeners();
        return geoQuery;
    }

    public DatabaseReference isDriverWoiking (String idDriver){
        return FirebaseDatabase.getInstance().getReference().child("drivers_working").child(idDriver);
    }

    public DatabaseReference getDriverLocation(String idDiver){
        return database.child(idDiver).child("l");
    }

}
