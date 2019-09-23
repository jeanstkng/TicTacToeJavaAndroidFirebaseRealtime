package com.starking.tictactoe.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.starking.tictactoe.R;
import com.starking.tictactoe.model.User;

public class RegistroActivity extends AppCompatActivity {

    EditText etName, etEmail, etPassword;
    Button btnRegistro;
    String name, email, password;
    FirebaseAuth firebaseAuth;
    ProgressBar pbRegistro;
    FirebaseFirestore db;
    ScrollView formRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        initViewComponents();

        firebaseAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();

        changeRegisterFormVisibility(true);
        events();

    }

    private void initViewComponents() {
        etName = findViewById(R.id.editTextName);
        etEmail = findViewById(R.id.editTextEmail);
        etPassword = findViewById(R.id.editTextPassword);
        btnRegistro = findViewById(R.id.buttonRegister);
        pbRegistro = findViewById(R.id.progressBarRegister);
        formRegister = findViewById(R.id.formRegister);
    }


    private void events() {

        btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = etName.getText().toString();
                email = etEmail.getText().toString();
                password = etPassword.getText().toString();

                if(name.isEmpty()){
                    etName.setError("Username is required.");
                }
                else if(email.isEmpty()){
                    etEmail.setError("Email is required.");
                }
                else if(password.isEmpty()){
                    etPassword.setError("Password is required.");
                }
                else{
                    // TODO: realizar registro en Firebase auth
                    createUser();
                }
            }
        });

    }

    private void createUser() {
        changeRegisterFormVisibility(false);
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            updateUI(user);
                        }
                        else{
                            Toast.makeText(RegistroActivity.this, "Error en el registro.", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });

    }

    private void updateUI(FirebaseUser user) {

        if (user != null){
            // Almacenar la informacion del usuario en FireStore
            User nuevoUsuario = new User(name, 0, 0);

            db.collection("users")
                    .document(user.getUid())
                    .set(nuevoUsuario)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            // Navegar hacia la siguiente pantalla de la aplicacion
                            finish();
                            Intent i = new Intent(RegistroActivity.this, FindGameActivity.class);
                            startActivity(i);
                        }
                    });


        }
        else{
            changeRegisterFormVisibility(true);
            etPassword.setError("Nombre, Email y/o contraseña incorrectos");
            etPassword.requestFocus();
        }

    }

    private void changeRegisterFormVisibility(boolean showForm) {
        pbRegistro.setVisibility( showForm ? View.GONE : View.VISIBLE);



        formRegister.setVisibility( showForm ? View.VISIBLE : View.GONE);
    }

}
