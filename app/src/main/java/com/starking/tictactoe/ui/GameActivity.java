package com.starking.tictactoe.ui;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.starking.tictactoe.R;
import com.starking.tictactoe.app.Constants;
import com.starking.tictactoe.model.Play;
import com.starking.tictactoe.model.User;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    List<ImageView> casillas;
    TextView tvPlayer1, tvPlayer2;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore db;
    String uid, playId, playerOneName = "", playerTwoName = "", winnerId = "";
    Play play;
    ListenerRegistration listenerJugada = null;
    FirebaseUser firebaseUser;
    String playerName;
    User userPlayer1, userPlayer2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        initViews();
        initGame();

    }

    private void initGame() {

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        uid = firebaseUser.getUid();
        db = FirebaseFirestore.getInstance();

        Bundle extras = getIntent().getExtras();

        playId = extras.getString(Constants.EXTRA_JUGADA_ID);

    }

    private void initViews() {

        tvPlayer1 = findViewById(R.id.textViewPlayer1);
        tvPlayer2 = findViewById(R.id.textViewPlayer2);

        casillas = new ArrayList<>();
        casillas.add((ImageView) findViewById(R.id.imageView0));
        casillas.add((ImageView) findViewById(R.id.imageView1));
        casillas.add((ImageView) findViewById(R.id.imageView2));
        casillas.add((ImageView) findViewById(R.id.imageView3));
        casillas.add((ImageView) findViewById(R.id.imageView4));
        casillas.add((ImageView) findViewById(R.id.imageView5));
        casillas.add((ImageView) findViewById(R.id.imageView6));
        casillas.add((ImageView) findViewById(R.id.imageView7));
        casillas.add((ImageView) findViewById(R.id.imageView8));
    }


    @Override
    protected void onStart() {
        super.onStart();
        jugadaListener();
    }

    private void jugadaListener() {
        listenerJugada = db.collection("plays")
                .document(playId)
                .addSnapshotListener(GameActivity.this, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null){
                            Toast.makeText(GameActivity.this, "Error al obtener los datos de las jugadas.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String source = snapshot != null
                                && snapshot.getMetadata().hasPendingWrites() ? "Local" : "Server";

                        if (snapshot.exists() && source.equals("Server")) {
                            // Parseando DocumentSnapshot a Play
                            play = snapshot.toObject(Play.class);

                            if (playerOneName.isEmpty() || playerTwoName.isEmpty()){
                                //obtener nombres de usuario de la jugada y setear sobre nombres de texto
                                getPlayerNames();

                            }

                            updateUI();

                        }

                        updatePlayersUI();
                    }
                });
    }

    private void updatePlayersUI() {

        if (play.isTurnoJugadorUno()){
            tvPlayer1.setTextColor(getResources().getColor(R.color.colorPrimary));
            tvPlayer2.setTextColor(getResources().getColor(R.color.grayColor));
        }
        else {
            tvPlayer1.setTextColor(getResources().getColor(R.color.grayColor));
            tvPlayer2.setTextColor(getResources().getColor(R.color.colorAccent));
        }

        if (!play.getGanadorId().isEmpty()) {
            winnerId = play.getGanadorId();
            showGameOverDialog();
        }

    }

    private void updateUI() {

        for (int i=0; i<9; i++) {
            int cell = play.getCeldasSeleccionadas().get(i);
            ImageView ivCasillaActual = casillas.get(i);

            if (cell == 0) {
                ivCasillaActual.setImageResource(R.drawable.ic_empty_square);
            }
            else if (cell == 1) {
                ivCasillaActual.setImageResource(R.drawable.ic_player_one);
            }
            else {
                ivCasillaActual.setImageResource(R.drawable.ic_player_two);
            }
        }
    }

    private void getPlayerNames() {

        // Obtener el nombre del player 1

        db.collection("users")
                .document(play.getJugadorUnoId())
                .get()
                .addOnSuccessListener(GameActivity.this, new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        userPlayer1 = snapshot.toObject(User.class);

                        playerOneName = snapshot.get("name").toString();
                        tvPlayer1.setText(playerOneName);

                        if (play.getJugadorUnoId().equals(uid)){
                            playerName = playerOneName;
                        }
                    }
                });

        // Obtener nombre del player 2
        db.collection("users")
                .document(play.getJugadorDosId())
                .get()
                .addOnSuccessListener(GameActivity.this, new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        userPlayer2 = snapshot.toObject(User.class);

                        playerTwoName = snapshot.get("name").toString();
                        tvPlayer2.setText(playerTwoName);

                        if (play.getJugadorDosId().equals(uid)){
                            playerName = playerTwoName;
                        }
                    }
                });

    }

    @Override
    protected void onStop() {
        if (listenerJugada != null){
            listenerJugada.remove();
        }

        super.onStop();
    }

    public void casillaSeleccionada(View view) {
        if (!play.getGanadorId().isEmpty()){
            Toast.makeText(this, "Game is over.", Toast.LENGTH_SHORT).show();
        }
        else {
            if (play.isTurnoJugadorUno() && play.getJugadorUnoId().equals(uid)){
                // Player 1 is playing
                actualizarJugada(view.getTag().toString());
            }
            else if (!play.isTurnoJugadorUno() && play.getJugadorDosId().equals(uid)){
                // Player 2 is playing
                actualizarJugada(view.getTag().toString());
            }
            else {
                Toast.makeText(this, "It's not your turn yet.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void actualizarJugada(String cellNumber) {
        int cellPosition = Integer.parseInt(cellNumber);

        if (play.getCeldasSeleccionadas().get(cellPosition) != 0) {
            Toast.makeText(this, "Select a free cell.", Toast.LENGTH_SHORT).show();
        }
        else {
            if (play.isTurnoJugadorUno()) {
                casillas.get(cellPosition).setImageResource(R.drawable.ic_player_one);
                play.getCeldasSeleccionadas().set(cellPosition, 1);
            } else {
                casillas.get(cellPosition).setImageResource(R.drawable.ic_player_two);
                play.getCeldasSeleccionadas().set(cellPosition, 2);
            }

            if (solutionExists()) {
                play.setGanadorId(uid);
                Toast.makeText(this, "There's a solution.", Toast.LENGTH_SHORT).show();
            }
            else if (tieExists()){
                play.setGanadorId("TIE");
                Toast.makeText(this, "There's a tie.", Toast.LENGTH_SHORT).show();
            }
            else {
                changeTurn();
            }

            // Actualizar en firestores los datos de la jugada

            db.collection("plays")
                    .document(playId)
                    .set(play)
                    .addOnSuccessListener(GameActivity.this, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    }).addOnFailureListener(GameActivity.this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w("ERROR", "Error al guardar la jugada");
                }
            });
        }
    }

    private void changeTurn() {

        // Cambio de turno
        play.setTurnoJugadorUno(!play.isTurnoJugadorUno());

    }

    private boolean tieExists(){
        boolean exists = false;

        // Empate
        boolean isFreeCell = false;
        for (int i = 0; i<9; i++){
            if (play.getCeldasSeleccionadas().get(i) == 0) {
                isFreeCell = true;
                break;
            }
        }

        if (!isFreeCell) {
            exists = true;
        }

        return exists;


    }

    private boolean solutionExists(){
        boolean exists = false;

            List<Integer> selectedCells = play.getCeldasSeleccionadas();
            if (selectedCells.get(0) == selectedCells.get(1)
            && selectedCells.get(1) == selectedCells.get(2)
            && selectedCells.get(2) != 0){  // 0 - 1 - 2
                exists = true;
            }
            else if (selectedCells.get(3) == selectedCells.get(4)
            && selectedCells.get(4) == selectedCells.get(5)
            && selectedCells.get(5) != 0){  // 3 - 4 - 5
                exists = true;
            }
            else if (selectedCells.get(6) == selectedCells.get(7)
                    && selectedCells.get(7) == selectedCells.get(8)
                    && selectedCells.get(8) != 0){  // 6 - 7 - 8
                exists = true;
            }
            else if (selectedCells.get(0) == selectedCells.get(3)
                    && selectedCells.get(3) == selectedCells.get(6)
                    && selectedCells.get(6) != 0){  // 0 - 3 - 6
                exists = true;
            }
            else if (selectedCells.get(1) == selectedCells.get(4)
                    && selectedCells.get(4) == selectedCells.get(7)
                    && selectedCells.get(7) != 0){  // 1 - 4 - 7
                exists = true;
            }
            else if (selectedCells.get(2) == selectedCells.get(5)
                    && selectedCells.get(5) == selectedCells.get(8)
                    && selectedCells.get(8) != 0){  // 2 - 5 - 8
                exists = true;
            }
            else if (selectedCells.get(0) == selectedCells.get(4)
                    && selectedCells.get(4) == selectedCells.get(8)
                    && selectedCells.get(8) != 0){  // 0 - 4 - 8
                exists = true;
            }
            else if (selectedCells.get(0) == selectedCells.get(4)
                    && selectedCells.get(4) == selectedCells.get(6)
                    && selectedCells.get(6) != 0){  // 0 - 4 - 6
                exists = true;
            }

        return exists;
    }

    public void showGameOverDialog(){
        // 1. Instantiate an <code><a href="/reference/android/app/AlertDialog.Builder.html">AlertDialog.Builder</a></code> with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View v = getLayoutInflater().inflate(R.layout.dialog_game_over, null);

        // Obtenemos las referencias a los view components del layout
        TextView tvPoints = v.findViewById(R.id.textViewPuntos);
        TextView tvInformation = v.findViewById(R.id.textViewInfo);
        LottieAnimationView gameOverAnimation = v.findViewById(R.id.animation_view);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setTitle("Game Over");

        builder.setCancelable(false);

        builder.setView(v);

        if (winnerId.equals("EMPATE")) {

            updatePoints(1);
            tvInformation.setText(playerName + " finished your game with a tie.");
            tvPoints.setText("+1 Point");

        }
        else if (winnerId.equals(uid)){

            updatePoints(3);
            tvInformation.setText(playerName + " You won and got 3 points!");
            tvPoints.setText("+3 Points");

        }
        else {

            tvInformation.setText(playerName + " You lost.");
            tvPoints.setText("+0 Points");
            gameOverAnimation.setAnimation("thumbs_down_animation.json");

        }

        gameOverAnimation.playAnimation();

        builder.setPositiveButton("Exit.", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private void updatePoints(int points) {
        User playerUpdate = null;
        if (playerName.equals(userPlayer1.getName())) {
            userPlayer1.setPoints(userPlayer1.getPoints() + points);
            userPlayer1.setGamesPlayed(userPlayer1.getGamesPlayed() + 1);
            playerUpdate = userPlayer1;
        }
        else {
            userPlayer2.setPoints((userPlayer2.getPoints() + points));
            userPlayer2.setGamesPlayed(userPlayer2.getGamesPlayed() + 1);
            playerUpdate = userPlayer2;
        }

        db.collection("users")
                .document(uid)
                .set(playerUpdate)
                .addOnSuccessListener(GameActivity.this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(GameActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

}
