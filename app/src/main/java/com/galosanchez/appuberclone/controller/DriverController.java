package com.galosanchez.appuberclone.controller;

import com.galosanchez.appuberclone.domain.Client;
import com.galosanchez.appuberclone.domain.Driver;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class DriverController {

    private DatabaseReference database;

    public DriverController(){
        database = FirebaseDatabase.getInstance().getReference().child("users").child("drivers");
    }

    public Task<Void> create(Driver driver){
        return database.child(driver.getKey()).setValue(driver);
    }

    public DatabaseReference getDriver(String idDriver){
        return database.child(idDriver);
    }

    public Task<Void> update(Driver driver){
        Map<String, Object> map = new HashMap<>();
        map.put("image",driver.getImage());
        map.put("name",driver.getName());
        map.put("vehicleBrand",driver.getVehicleBrand());
        map.put("vehiclePlate",driver.getVehiclePlate());
        return database.child(driver.getKey()).updateChildren(map);
    }

}
