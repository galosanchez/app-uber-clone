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
import com.galosanchez.appuberclone.fragment.ProgressDialogFragment;
import com.galosanchez.appuberclone.R;
import com.galosanchez.appuberclone.controller.ClientController;
import com.galosanchez.appuberclone.controller.UserController;
import com.galosanchez.appuberclone.domain.Client;
import com.galosanchez.appuberclone.domain.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public class RegisterClientActivity extends AppCompatActivity {

    private ImageView imageViewBack;
    private EditText editTextName, editTextEmail, editTextPassword, editTextConfirmPassword;
    private Button buttonRegister;
    private ProgressDialogFragment dialogFragment;
    private UserController userController;
    private ClientController clientController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_client);
        //Bloquear rotación de pantalla
        MyFunctions.screenOrientationPortrait(this);

        // Initialize variables
        userController = new UserController();
        clientController = new ClientController();
        dialogFragment = new ProgressDialogFragment(getSupportFragmentManager(), "ProgressRegister");
        imageViewBack = findViewById(R.id.imageViewBack);
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
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
        String nameUser, emailUser, passwdUser, confirmPasswdUser;
        nameUser = editTextName.getText().toString();
        emailUser = editTextEmail.getText().toString();
        passwdUser = editTextPassword.getText().toString();
        confirmPasswdUser = editTextConfirmPassword.getText().toString();

        if( !validarDatosUsuario(nameUser, emailUser, passwdUser, confirmPasswdUser) )
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
                    Client client = new Client(Uid, user.getName(), user.getEmail());
                    saveUserDatabase(client);

                } else {
                    // If sign in fails, display a message to the user.

                    // Verificar conexión a Internet
                    if (MyFunctions.conexionInternet(RegisterClientActivity.this)){
                        String errorCode = userController.getErrorCode(task);
                        //Verificar si existe usuario con el correo
                        if (errorCode.equals("ERROR_EMAIL_ALREADY_IN_USE")){
                            editTextEmail.setError("Correo ya está en uso.");
                            editTextEmail.requestFocus();
                        }else{
                            Toast.makeText(RegisterClientActivity.this, "Error al registrar usuario"+ errorCode, Toast.LENGTH_LONG).show();
                        }
                    }
                    dialogFragment.closeDialog();
                }
            }
        });

    }

    private void saveUserDatabase(Client client) {
        clientController.create(client).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Intent intent = new Intent(RegisterClientActivity.this, MapClientActivity.class);
                    MyFunctions.deleteBackStack(intent);
                    startActivity(intent);
                }else{
                    Toast.makeText(RegisterClientActivity.this, "Error al guardar datos del usuario", Toast.LENGTH_LONG).show();
                }
                dialogFragment.closeDialog();
            }
        });
    }

    private boolean validarDatosUsuario(String name, String mail, String passwd , String confirmPasswd) {
        if ( name.isEmpty() || mail.isEmpty() || passwd.isEmpty()) {
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