package com.galosanchez.appuberclone.controller;

import com.galosanchez.appuberclone.domain.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

public class UserController {

    private FirebaseAuth mAuth;

    public UserController(){
        mAuth = FirebaseAuth.getInstance();
    }

    public Task<AuthResult> loginWithEmailAndPassword(User user){
        return mAuth.signInWithEmailAndPassword(user.getEmail(), user.getPassword());
    }

    public Task<AuthResult> register(User user){
        return mAuth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword());
    }

    public String getUidCurrentUser(){
        return mAuth.getInstance().getCurrentUser().getUid();
    }

    public String getErrorCode(Task<AuthResult> task){
        return  ((FirebaseAuthException) task.getException()).getErrorCode();
    }

    public void signOut(){
        mAuth.signOut();
    }

    public  boolean existSession(){
        boolean exist = false;
        if (mAuth.getCurrentUser() != null){
            exist = true;
        }
        return  exist;
    }



}
