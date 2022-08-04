package com.galosanchez.appuberclone.controller;

import androidx.annotation.NonNull;

import com.galosanchez.appuberclone.domain.Token;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class TokenController {

    DatabaseReference databaseReference;

    public TokenController() {
        this.databaseReference = FirebaseDatabase.getInstance().getReference().child("Tokens");
    }


    public void create(String userID){
        if (userID == null) return;
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (task.isSuccessful()) {
                            // Get new FCM registration token
                            Token token = new Token(task.getResult());
                            databaseReference.child(userID).setValue(token);
                        }
                    }
                });
    }

    public DatabaseReference getToken(String idUser){
        return databaseReference.child(idUser);

    }

}
