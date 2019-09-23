package com.starking.tictactoe;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ScrollView formLogin;
    private ProgressBar pbLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.editTextEmail);
        etPassword = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.buttonLogin);
        pbLogin = findViewById(R.id.progressBarLogin);
        formLogin = findViewById(R.id.formLogin);

        changeloginFormVisibility(true);
        events();
    }

    private void events() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();

                changeloginFormVisibility(false);
            }
        });
    }

    private void changeloginFormVisibility(boolean showForm) {
        pbLogin.setVisibility( showForm ? View.GONE : View.VISIBLE);
        formLogin.setVisibility( showForm ? View.VISIBLE : View.GONE);
    }
}
