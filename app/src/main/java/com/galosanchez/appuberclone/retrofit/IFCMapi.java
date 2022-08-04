package com.galosanchez.appuberclone.retrofit;

import android.content.Context;

import com.galosanchez.appuberclone.R;
import com.galosanchez.appuberclone.domain.FCMBody;
import com.galosanchez.appuberclone.domain.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMapi {

    String key_server ="YOUR_API_SERVER";

    @Headers({
            "Content-Type:application/json",
            "Authorization:key="+key_server
    })
    @POST("fcm/send")
    Call<FCMResponse> send(@Body FCMBody body);


}
