package com.galosanchez.appuberclone.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.galosanchez.appuberclone.utils.MyFunctions;
import com.galosanchez.appuberclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private Button buttonDriver, buttonClient;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Bloquear rotación de pantalla
        MyFunctions.screenOrientationPortrait(this);

        // Initialize variables
        sharedPreferences = getApplicationContext().getSharedPreferences("TYPE_USER", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        buttonDriver = findViewById(R.id.buttonDriver);
        buttonClient = findViewById(R.id.buttonClient);

        buttonDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToLogin(editor,"driver");
            }
        });

        buttonClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToLogin(editor,"client");
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser != null){
            Intent intent;
            String typeUser = sharedPreferences.getString("USER", null);
            if (typeUser.equals("driver")){
                intent = new Intent(MainActivity.this, MapDriverActivity.class);
            }else if (typeUser.equals("client")){
                intent = new Intent(MainActivity.this, MapClientActivity.class);
            }else return;
            MyFunctions.deleteBackStack(intent);
            startActivity(intent);
        }
    }

    private void goToLogin(SharedPreferences.Editor editor, String typeUser) {
        editor.putString("USER",typeUser);
        editor.apply();
        Intent intent = new Intent(MainActivity.this,LoginActivity.class);
        startActivity(intent);
    }
}