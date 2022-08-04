package com.galosanchez.appuberclone.controller;

import com.galosanchez.appuberclone.domain.ClientBooking;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ClientBookingController {

    private DatabaseReference databaseReference;

    public ClientBookingController(){
        databaseReference = FirebaseDatabase.getInstance().getReference().child("ClientBooking");
    }

    public Task<Void> create(ClientBooking clientBooking){
        return databaseReference.child(clientBooking.getIdClient()).setValue(clientBooking);
    }

    public Task<Void> updateStatus(String idClientBooking, String status){
        Map<String, Object> map = new HashMap<>();
        map.put("status",status);
        return databaseReference.child(idClientBooking).updateChildren(map);
    }

    public DatabaseReference getStatus(String idClientBooking){
        return databaseReference.child(idClientBooking).child("status");
    }
    public DatabaseReference getClientBooking(String idClientBooking){
        return databaseReference.child(idClientBooking);
    }

    public Task<Void> delete(String idClientBooking){
        return databaseReference.child(idClientBooking).removeValue();
    }

}
