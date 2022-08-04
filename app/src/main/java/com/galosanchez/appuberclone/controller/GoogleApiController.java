package com.galosanchez.appuberclone.controller;

import android.content.Context;

import com.galosanchez.appuberclone.R;
import com.galosanchez.appuberclone.retrofit.IGoogleApi;
import com.galosanchez.appuberclone.retrofit.RetrofitClient;
import com.google.android.gms.maps.model.LatLng;

import retrofit2.Call;

public class GoogleApiController {
    private Context context;
    public GoogleApiController(Context context){
        this.context =context;
    }
    public Call<String> getDiretions(LatLng originLatLng , LatLng destinationLatLng){
        String c="https://maps.googleapis.com/maps/api/directions/json?mode=driving&transit_routing_preferences=less_driving&origin=&destination=&key=";
        String url = "https://maps.googleapis.com";
        String query= "/maps/api/directions/json?mode=driving&transit_routing_preferences=less_driving&" +
                        "origin="+originLatLng.latitude+","+originLatLng.longitude+"&" +
                        "destination="+ destinationLatLng.latitude+","+destinationLatLng.longitude+"&" +
                        "key="+context.getResources().getString(R.string.google_maps_key);
        return RetrofitClient.getClient(url).create(IGoogleApi.class).getDirections(url+query);
    }
}
