package com.galosanchez.appuberclone.controller;

import com.galosanchez.appuberclone.domain.Client;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ClientController {

    private DatabaseReference database;

    public ClientController(){
        database = FirebaseDatabase.getInstance().getReference().child("users").child("clients");
    }

    public Task<Void> create(Client client){
        return database.child(client.getKey()).setValue(client);
    }

    public DatabaseReference getClient(String idClient){
        return database.child(idClient);
    }

    public Task<Void> update(Client client){
        Map<String, Object> map = new HashMap<>();
        map.put("image",client.getImage());
        map.put("name",client.getName());
        return database.child(client.getKey()).updateChildren(map);
    }

}
