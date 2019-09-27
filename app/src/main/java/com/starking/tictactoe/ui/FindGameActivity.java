package com.starking.tictactoe.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.util.Listener;
import com.starking.tictactoe.R;
import com.starking.tictactoe.app.Constants;
import com.starking.tictactoe.model.Play;

public class FindGameActivity extends AppCompatActivity {
    private TextView tvLoadingMessage;
    private ProgressBar progressBar;
    private ScrollView layoutProgressBar, layoutGameMenu;
    private Button btnPlay, btnRanking;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private FirebaseUser firebaseUser;
    private String uid, playId = "";
    private ListenerRegistration listenerRegistration = null;
    private LottieAnimationView animationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_game);

        initViewComponents();
        initFirebase();
        events();

    }

    private void initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        uid = firebaseUser.getUid();
    }

    private void events() {

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeMenuVisibility(false);
                searchGame();
            }
        });

        btnRanking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(FindGameActivity.this, RankingActivity.class);
                startActivity(i);
            }
        });

    }

    private void searchGame() {
        tvLoadingMessage.setText("Searching for game...");

        animationView.playAnimation();

        db.collection("plays")
                .whereEqualTo("jugadorDosId","")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.getResult().size() == 0) {
                            // no existen partidas libres, crear nueva
                            createNewPlay();
                        }
                        else {
                            boolean finded = false;

                            for (DocumentSnapshot docPlay : task.getResult().getDocuments()){

                                if (!docPlay.get("jugadorUnoId").equals(uid)) {
                                    finded = true;
                                    playId = docPlay.getId();
                                    Play play = docPlay.toObject(Play.class);
                                    play.setJugadorDosId(uid);

                                    db.collection("plays")
                                            .document(playId)
                                            .set(play)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    tvLoadingMessage.setText("Free game finded! Starting game...");

                                                    animationView.setRepeatCount(0);
                                                    animationView.setAnimation("checked_animation.json");
                                                    animationView.playAnimation();

                                                    final Handler handler = new Handler();
                                                    final Runnable r = new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            startGame();
                                                        }
                                                    };

                                                    handler.postDelayed(r, 1500);

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            changeMenuVisibility(true);
                                            Toast.makeText(FindGameActivity.this, "There was a trouble while trying to get a game.", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    break;

                                }

                                if (!finded) createNewPlay();

                            }
                        }
                    }
                });
    }

    private void createNewPlay() {
        tvLoadingMessage.setText("Creating new game...");

        Play newPlay = new Play(uid);

        db.collection("plays")
                .add(newPlay)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        playId = documentReference.getId();
                        // tenemos creada la jugada, debemos esperar a otro jugador
                        waitForPlayer();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        changeMenuVisibility(true);
                        Toast.makeText(FindGameActivity.this, "Error while creating new game.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void waitForPlayer() {
        tvLoadingMessage.setText("Waiting for new player...");

        listenerRegistration = db.collection("plays")
                .document(playId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (!documentSnapshot.get("jugadorDosId").equals("")) {
                            tvLoadingMessage.setText("Player finded!");
                            animationView.setRepeatCount(0);
                            animationView.setAnimation("checked_animation.json");
                            animationView.playAnimation();

                            final Handler handler = new Handler();
                            final Runnable r = new Runnable() {
                                @Override
                                public void run() {
                                    startGame();
                                }
                            };

                            handler.postDelayed(r, 1500);
                        }

                    }
                });
    }


    private void startGame() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
        Intent i = new Intent(FindGameActivity.this, GameActivity.class);
        i.putExtra(Constants.EXTRA_JUGADA_ID, playId);
        startActivity(i);
        playId = "";
    }

    private void initViewComponents() {

        animationView = findViewById(R.id.animation_view);
        tvLoadingMessage = findViewById(R.id.textViewLoading);
        progressBar = findViewById(R.id.progressBarPlays);
        layoutGameMenu = findViewById(R.id.gameMenu);
        layoutProgressBar = findViewById(R.id.layoutProgressBar);
        btnPlay = findViewById(R.id.buttonPlay);
        btnRanking = findViewById(R.id.buttonRanking);

        progressBar.setIndeterminate(true);
        tvLoadingMessage.setText("Loading...");

        changeMenuVisibility(true);

    }

    private void changeMenuVisibility(boolean showMenu) {
        layoutProgressBar.setVisibility(showMenu ? View.GONE : View.VISIBLE);
        layoutGameMenu.setVisibility(showMenu ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (playId != ""){
            changeMenuVisibility(false);
            waitForPlayer();
        }
        else {
            changeMenuVisibility(true);
        }
    }

    @Override
    protected void onStop() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }

        if (playId != ""){
            db.collection("plays")
                    .document(playId)
                    .delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            playId = "";
                        }
                    });
        }

        super.onStop();
    }
}
