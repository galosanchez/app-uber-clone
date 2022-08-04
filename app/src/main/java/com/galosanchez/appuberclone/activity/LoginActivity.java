package com.galosanchez.appuberclone.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.galosanchez.appuberclone.utils.MyFunctions;
import com.galosanchez.appuberclone.controller.UserController;
import com.galosanchez.appuberclone.domain.User;
import com.galosanchez.appuberclone.fragment.ProgressDialogFragment;
import com.galosanchez.appuberclone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public class LoginActivity extends AppCompatActivity {

    private TextView textViewRegister;
    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private ProgressDialogFragment dialogFragment;
    private UserController userController;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Bloquear rotación de pantalla
        MyFunctions.screenOrientationPortrait(this);

        // Initialize variables
        sharedPreferences = getSharedPreferences("TYPE_USER",MODE_PRIVATE);
        userController = new UserController();
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.textViewRegister);
        dialogFragment = new ProgressDialogFragment(getSupportFragmentManager(), "ProgressLogin");

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToRegister();
            }
        });


    }

    private void loginUser() {
        String email, password;
        email = editTextEmail.getText().toString();
        password = editTextPassword.getText().toString();
        // si existe un error retorna
        if(!validarCredenciales(email, password)){
            return;
        }
        dialogFragment.showDialog();
        User user = new User(email, password);
        userController.loginWithEmailAndPassword(user).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    // Sign in success, update UI with the signed-in user's information
                    Intent intent;
                    String typeUser = sharedPreferences.getString("USER", null);
                    if (typeUser.equals("driver")){
                        intent = new Intent(LoginActivity.this, MapDriverActivity.class);
                    }else if (typeUser.equals("client")){
                        intent = new Intent(LoginActivity.this, MapClientActivity.class);
                    }else return;
                    MyFunctions.deleteBackStack(intent);
                    startActivity(intent);
                }else{
                    // If sign in fails, display a message to the user.

                    // Verificar conexión a Internet
                    if (MyFunctions.conexionInternet(LoginActivity.this)){
                        String errorCode = userController.getErrorCode(task);
                        switch (errorCode){
                            case "ERROR_WRONG_PASSWORD":
                            case "ERROR_USER_NOT_FOUND":
                            case "ERROR_INVALID_EMAIL":
                                Toast.makeText(LoginActivity.this, "El usuario o contraseña son incorrectos.", Toast.LENGTH_LONG).show();
                                break;
                            default:
                                Toast.makeText(LoginActivity.this, "Error al iniciar sesión.", Toast.LENGTH_LONG).show();
                                break;
                        }
                    }
                }
                dialogFragment.closeDialog();
            }
        });
    }

    private boolean validarCredenciales(String email, String password) {
        if (!email.isEmpty() && !password.isEmpty()) {
            if (password.length() > 5) {
                return true;
            }
        }
        Toast.makeText(this, "Correo o contraseña inválido.", Toast.LENGTH_LONG).show();
        return false;
    }

    private void goToRegister() {
        Intent intent;
        String typeUser = sharedPreferences.getString("USER", null);
        if (typeUser.equals("driver")){
            intent = new Intent(LoginActivity.this, RegisterDriverActivity.class);
        }else if (typeUser.equals("client")){
            intent = new Intent(LoginActivity.this, RegisterClientActivity.class);
        }else return;
        startActivity(intent);
    }
}