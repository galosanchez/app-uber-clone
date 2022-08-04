package com.galosanchez.appuberclone.controller;

import com.galosanchez.appuberclone.domain.FCMBody;
import com.galosanchez.appuberclone.domain.FCMResponse;
import com.galosanchez.appuberclone.retrofit.IFCMapi;
import com.galosanchez.appuberclone.retrofit.RetrofitClient;

import retrofit2.Call;

public class NotificationController {
    private  String url = "https://fcm.googleapis.com";

    public NotificationController(){

    }

    public Call<FCMResponse> sendNotification(FCMBody body){
        return RetrofitClient.getClientObject(url).create(IFCMapi.class).send(body);
    }

}
