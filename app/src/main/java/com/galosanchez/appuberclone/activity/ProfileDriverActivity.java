package com.galosanchez.appuberclone.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.galosanchez.appuberclone.R;
import com.galosanchez.appuberclone.controller.DriverController;
import com.galosanchez.appuberclone.controller.UserController;
import com.galosanchez.appuberclone.domain.Client;
import com.galosanchez.appuberclone.domain.Driver;
import com.galosanchez.appuberclone.utils.CompressorBitmapImage;
import com.galosanchez.appuberclone.utils.FileUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;

public class ProfileDriverActivity extends AppCompatActivity {

    private EditText editTextName;
    private EditText editTextVehicleBrand;
    private EditText editTextVehiclePlate;
    private ImageView imageViewPhoto;
    private Button buttonSave;
    private Toolbar toolbar;
    private DriverController driverController;
    private UserController userController;
    private File imageFile;
    private final int GALERY_REQUEST = 1;
    private ProgressDialog progressDialog;
    private String newName;
    private String newVehicleBrand;
    private String newVehiclePlate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_driver);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Perfil");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editTextName = findViewById(R.id.editTextName);
        editTextVehicleBrand = findViewById(R.id.editTextVehicleBrand);
        editTextVehiclePlate = findViewById(R.id.editTextVehiclePlate);
        imageViewPhoto = findViewById(R.id.imageViewPhoto);
        buttonSave = findViewById(R.id.buttonSave);
        progressDialog = new ProgressDialog(this);
        driverController = new DriverController();
        userController = new UserController();

        getDriverInfo();

        imageViewPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatePhoto();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });
    }

    private void updatePhoto() {
        // Abrir galeria
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,GALERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALERY_REQUEST && resultCode == RESULT_OK){
            try {
                imageFile = FileUtil.from(this,data.getData());
                imageViewPhoto.setImageBitmap(BitmapFactory.decodeFile(imageFile.getAbsolutePath()));
            }catch (Exception e){

            }

        }
    }


    private void saveData() {
        newName = editTextName.getText().toString();
        newVehicleBrand = editTextVehicleBrand.getText().toString();
        newVehiclePlate = editTextVehiclePlate.getText().toString();
        if (!newName.isEmpty() && imageFile != null){
            progressDialog.setMessage("Espere un momento...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            saveStorage();
        }else{
            Toast.makeText(this, "Ingrese el nombre y una imágen.", Toast.LENGTH_LONG).show();
        }
    }

    private void saveStorage() {
        byte[] imageBytes = CompressorBitmapImage.getImageCompress(this, imageFile.getPath(),500,500);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("drivers_images").child(userController.getUidCurrentUser()).child("profile.jpg");
        UploadTask uploadTask = storageReference.putBytes(imageBytes);
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()){
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String image = uri.toString();
                            Driver driver = new Driver();
                            driver.setImage(image);
                            driver.setName(newName);
                            driver.setVehicleBrand(newVehicleBrand);
                            driver.setVehiclePlate(newVehiclePlate);
                            driver.setKey(userController.getUidCurrentUser());
                            driverController.update(driver).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(ProfileDriverActivity.this, "Datos actualizados", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            });
                        }
                    });
                }else {
                    Log.e("firebase", "Error getting data", task.getException());
                }
            }
        });


    }

    private void getDriverInfo(){
        driverController.getDriver(userController.getUidCurrentUser()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String name = snapshot.child("name").getValue().toString();
                    String vehicleBrand = snapshot.child("vehicleBrand").getValue().toString();
                    String vehiclePlate = snapshot.child("vehiclePlate").getValue().toString();
                    String image = "";
                    if (snapshot.hasChild("image")){
                        image = snapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(imageViewPhoto);
                    }
                    editTextName.setText(name);
                    editTextVehicleBrand.setText(vehicleBrand);
                    editTextVehiclePlate.setText(vehiclePlate);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}