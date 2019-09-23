package com.starking.tictactoe.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.starking.tictactoe.R;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private ScrollView formLogin;
    private ProgressBar pbLogin;
    private FirebaseAuth firebaseAuth;
    private String email, password;
    boolean tryLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViewComponents();

        firebaseAuth = FirebaseAuth.getInstance();

        changeloginFormVisibility(true);
        events();

    }

    private void initViewComponents() {

        etEmail = findViewById(R.id.editTextEmail);
        etPassword = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.buttonLogin);
        pbLogin = findViewById(R.id.progressBarLogin);
        formLogin = findViewById(R.id.formLogin);
        btnRegister = findViewById(R.id.buttonRegister);

    }

    private void events() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = etEmail.getText().toString();
                password = etPassword.getText().toString();

                if(email.isEmpty()){
                    etEmail.setError("Email is required.");
                }
                else if(password.isEmpty()){
                    etPassword.setError("Password is required.");
                }
                else{
                    // TODO: realizar registro en Firebase auth
                    changeloginFormVisibility(false);
                    loginUser();
                }

            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, RegistroActivity.class);
                startActivity(i);
            }
        });
    }

    private void loginUser() {

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        tryLogin = true;
                        if (task.isSuccessful()){
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            updateUI(user);
                        }
                        else {
                            Log.w("TAG", "signInError: ", task.getException());
                            updateUI(null);
                        }
                    }
                });

    }

    private void changeloginFormVisibility(boolean showForm) {
        pbLogin.setVisibility( showForm ? View.GONE : View.VISIBLE);
        formLogin.setVisibility( showForm ? View.VISIBLE : View.GONE);
    }

    private void updateUI(FirebaseUser user) {

        if (user != null){
            // Almacenar la informacion del usuario en FireStore
            // TODO

            // Navegar hacia la siguiente pantalla de la aplicacion
            Intent i = new Intent(LoginActivity.this, FindGameActivity.class);
            startActivity(i);

        }
        else{
            changeloginFormVisibility(true);
            if (tryLogin){
                etPassword.setError("Nombre, Email y/o contraseña incorrectos");
                etPassword.requestFocus();
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        // Comprobamos si previamente el usuario ya ha iniciado sesion en
        // este dispositivo

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        updateUI(currentUser);

    }
}
