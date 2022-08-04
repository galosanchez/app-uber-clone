package com.galosanchez.appuberclone.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.galosanchez.appuberclone.utils.MyFunctions;
import com.galosanchez.appuberclone.R;
import com.galosanchez.appuberclone.controller.DriverController;
import com.galosanchez.appuberclone.controller.UserController;
import com.galosanchez.appuberclone.domain.Driver;
import com.galosanchez.appuberclone.domain.User;
import com.galosanchez.appuberclone.fragment.ProgressDialogFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public class RegisterDriverActivity extends AppCompatActivity {

    private ImageView imageViewBack;
    private EditText editTextName, editTextEmail, editTextPassword, editTextConfirmPassword, editTextVehicleBrand, editTextVehiclePlate;
    private Button buttonRegister;
    private ProgressDialogFragment dialogFragment;
    private UserController userController;
    private DriverController driverController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_driver);
        //Bloquear rotación de pantalla
        MyFunctions.screenOrientationPortrait(this);

        // Initialize variables
        userController = new UserController();
        driverController = new DriverController();
        dialogFragment = new ProgressDialogFragment(getSupportFragmentManager(), "ProgressRegister");
        imageViewBack = findViewById(R.id.imageViewBack);
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextVehicleBrand = findViewById(R.id.editTextVehicleBrand);
        editTextVehiclePlate = findViewById(R.id.editTextVehiclePlate);
        buttonRegister = findViewById(R.id.buttonRegister);

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToActivityBack();
            }
        });

    }

    private void goToActivityBack() {
        onBackPressed();
    }

    private void registerUser() {
        String nameUser, emailUser, passwdUser, confirmPasswdUser, vehicleBrand, vehiclePlate;
        nameUser = editTextName.getText().toString();
        emailUser = editTextEmail.getText().toString();
        passwdUser = editTextPassword.getText().toString();
        confirmPasswdUser = editTextConfirmPassword.getText().toString();
        vehicleBrand = editTextVehicleBrand.getText().toString();
        vehiclePlate = editTextVehiclePlate.getText().toString();

        if( !validarDatosUsuario(nameUser, emailUser, passwdUser, confirmPasswdUser,vehicleBrand, vehiclePlate) )
            return;

        User user = new User();
        user.setName(nameUser);
        user.setEmail(emailUser);
        user.setPassword(confirmPasswdUser);
        dialogFragment.showDialog();
        userController.register(user).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    String Uid = userController.getUidCurrentUser();
                    Driver driver = new Driver(Uid, user.getName(), user.getEmail(), vehicleBrand, vehiclePlate);
                    saveUserDatabase(driver);

                } else {
                    // If sign in fails, display a message to the user.

                    // Verificar conexión a Internet
                    if (MyFunctions.conexionInternet(RegisterDriverActivity.this)){
                        String errorCode = userController.getErrorCode(task);
                        //Verificar si existe usuario con el correo
                        if (errorCode.equals("ERROR_EMAIL_ALREADY_IN_USE")){
                            editTextEmail.setError("Correo ya está en uso.");
                            editTextEmail.requestFocus();
                        }else{
                            Toast.makeText(RegisterDriverActivity.this, "Error al registrar usuario"+ errorCode, Toast.LENGTH_LONG).show();
                        }
                    }
                    dialogFragment.closeDialog();
                }
            }
        });

    }

    private void saveUserDatabase(Driver driver) {
        driverController.create(driver).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Intent intent = new Intent(RegisterDriverActivity.this, MapDriverActivity.class);
                    MyFunctions.deleteBackStack(intent);
                    startActivity(intent);
                }else{
                    Toast.makeText(RegisterDriverActivity.this, "Error al guardar datos del usuario", Toast.LENGTH_LONG).show();
                }
                dialogFragment.closeDialog();
            }
        });
    }

    private boolean validarDatosUsuario(String name, String mail, String passwd , String confirmPasswd,String vehicleBrand, String vehiclePlate) {
        if ( name.isEmpty() || mail.isEmpty() || passwd.isEmpty() || vehicleBrand.isEmpty() || vehiclePlate.isEmpty() ) {
            Toast.makeText(this, "Existe campos vacíos.", Toast.LENGTH_LONG).show();
        } else {
            if (passwd.length() < 6 ) {
                editTextPassword.setError("Mínimo 6 caracteres.");
                editTextPassword.requestFocus();
            } else {
                if (!passwd.equals(confirmPasswd)){
                    editTextConfirmPassword.setError("La contraseña no coincide.");
                    editTextConfirmPassword.requestFocus();
                }else{
                    return true;
                }
            }
        }
        return false;
    }
}