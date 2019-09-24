package com.starking.tictactoe.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.starking.tictactoe.R;

public class FindGameActivity extends AppCompatActivity {
    private TextView tvLoadingMessage;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_game);

        initViewComponents();

    }

    private void initViewComponents() {

        tvLoadingMessage = findViewById(R.id.textViewLoading);
        progressBar = findViewById(R.id.progressBarPlays);

        progressBar.setIndeterminate(true);
        tvLoadingMessage.setText("Loading...");

    }
}
