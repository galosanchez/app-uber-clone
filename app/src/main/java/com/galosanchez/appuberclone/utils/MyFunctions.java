package com.galosanchez.appuberclone.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class MyFunctions {

    public static boolean conexionInternet(Activity activity){
        ConnectivityManager connectivityManager = (ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            return true;// Si hay conexión a Internet en este momento
        } else {
            Toast.makeText(activity, "Sin conexión a Internet.", Toast.LENGTH_LONG).show();
            return false;// No hay conexión a Internet en este momento
        }
    }

    // Bloquear rotación de pantalla en vertical
    public static void screenOrientationPortrait(Activity activity){
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
    // Eliminar la pila de actividades
    public static void deleteBackStack(Intent intent){
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }

}
