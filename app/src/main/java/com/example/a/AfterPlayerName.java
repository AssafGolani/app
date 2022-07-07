package com.example.a;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AfterPlayerName extends AppCompatActivity {

    private LinearLayout playerOneLayout, playerTwoLayout;
    private ImageView imgOne, imgTwo, imgThree, imgFour, imgFive, imgSix, imgSeven, imgEight, imgNine;
    private TextView playerOne, playerTwo;

    // winning combination
    private final List<int[]> combinationsList = new ArrayList<>();
    private final List<String> selectedBoxes = new ArrayList<>();
    // player unique Id
    private String playerUniqueId = "0";

    // getting firebase db reference from URL
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://myapp-c1ffb-default-rtdb.firebaseio.com/");

    private boolean opponentFound = false;
    private String opponentUniqueId = "0";

    //values must be match or waiting. when a user create a new connection and he is waiting for other to join then the balue will be waiting
    private String status = "matching";

    // player turn
    private String playerTurn = "";

    // connection id in which player has joined to play the game
    private String connectionId = "";

    //generation value event listener for firebase db
    // turnsEventListener listens for the players turn and wonEventListener listens if the player won
    ValueEventListener turnsEventListener, wonEventListener;

    // selected boxes by players empty fields will be replace by player ids
    private final String[] boxesSelectedBy = {"","","","","","","","",""};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_player_name);

        playerOneLayout = findViewById(R.id.player1_layout);
        playerTwoLayout = findViewById(R.id.player2_layout);
        playerOne = findViewById(R.id.player1);
        playerTwo = findViewById(R.id.player2);
        imgOne = findViewById(R.id.image1);
        imgTwo = findViewById(R.id.image2);
        imgTwo = findViewById(R.id.image2);
        imgThree = findViewById(R.id.image3);
        imgFour = findViewById(R.id.image4);
        imgFive = findViewById(R.id.image5);
        imgSix = findViewById(R.id.image6);
        imgSeven = findViewById(R.id.image7);
        imgEight = findViewById(R.id.image8);
        imgNine = findViewById(R.id.image9);
        // getting PlayerName from PlayerName.class
        final String getPlayerName = getIntent().getStringExtra("playerName");

        // generating winning combinations
        combinationsList.add(new int[]{0,1,2});
        combinationsList.add(new int[]{3,4,5});
        combinationsList.add(new int[]{6,7,8});
        combinationsList.add(new int[]{0,3,6});
        combinationsList.add(new int[]{2,5,8});
        combinationsList.add(new int[]{1,4,7});
        combinationsList.add(new int[]{0,4,8});
        combinationsList.add(new int[]{2,4,6});

        //showing progress dialog while waiting for match
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Waiting for opponent");
        progressDialog.show();

        // generate player unique id. Player will be identified by this id
        playerUniqueId = String.valueOf(System.currentTimeMillis());
        // setting player name to the TextView
        playerTwo.setText(getPlayerName);

        databaseReference.child("connections").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check if opponent exists
                if (!opponentFound){

                    //check if there are others in the firebase realtime db
                    if(snapshot.hasChildren()){
                        // check all connections if other users are also wait for a user to play the match
                        for(DataSnapshot connections: snapshot.getChildren()){
                            // getting connection unique id
                            String conId = connections.getKey();
                            // 2 players are required to play the game, if getPlayerCount is 1 it means other players is waiting for opponent to play the game
                            // else if getPlayerCount is 2 it means this connection has completed with 2 players.
                            int getPlayerCount = (int) connections.getChildrenCount();
                            // after created a new connection waiting for other to join
                            if(status.equals("waiting")){

                                // if playerCount is 2 means other player joined the match
                                if(getPlayerCount == 2){
                                    playerTurn = playerUniqueId;
                                    applyPlayerTurn(playerTurn);

                                    // true when player found to play the match
                                    boolean playerFound = false;

                                    // getting player in connection
                                    for (DataSnapshot players : connections.getChildren()){
                                        String getPlayerUniqueId = players.getKey();
                                        // check if player id match with use who created connection
                                        if (getPlayerUniqueId != null){
                                            if (getPlayerUniqueId.equals(playerUniqueId)){
                                                playerFound = true;
                                            }
                                            else if (playerFound){
                                                String getOpponentPlayerName = players.child(" player name").getValue(String.class);
                                                opponentUniqueId = players.getKey();

                                                //set opponent playername to the textView
                                                playerTwo.setText(getOpponentPlayerName);
                                                // assign conenction id
                                                connectionId = conId;
                                                opponentFound = true;
                                                // adding turn listener and won listener to the db ref
                                                databaseReference.child("turns").child(connectionId).addListenerForSingleValueEvent(turnsEventListener);
                                                databaseReference.child("won").child(connectionId).addValueEventListener(wonEventListener);

                                                // hide progressDialog
                                                if (progressDialog.isShowing()) progressDialog.dismiss();

                                                // once the connection has made remove connectionListener from db ref
                                                databaseReference.child("connections").removeEventListener(this);
                                            }
                                        }

                                    }
                                }else{ // in case user has not created the connection because of other connections are available to join
                                    // checking if the connection has 1 player and need 1 more player
                                    if (getPlayerCount == 1){
                                        connections.child(playerUniqueId).child("player_name").getRef().setValue(getPlayerName);
                                        // getting both players
                                        for (DataSnapshot players: connections.getChildren()){
                                            String getOpponentPlayerName = players.child("player_name").getValue(String.class);
                                            opponentUniqueId = players.getKey();

                                            //first turn for who created the connection
                                            playerTurn = opponentUniqueId;
                                            if (playerTurn != null){
                                                applyPlayerTurn(playerTurn);
                                                playerTwo.setText(getOpponentPlayerName);
                                                connectionId = conId;
                                                opponentFound = true;

                                                databaseReference.child("turns").child(connectionId).addListenerForSingleValueEvent(turnsEventListener);
                                                databaseReference.child("won").child(connectionId).addValueEventListener(wonEventListener);

                                                // hide progressDialog
                                                if (progressDialog.isShowing()) progressDialog.dismiss();

                                                // once the connection has made remove connectionListener from db ref
                                                databaseReference.child("connections").removeEventListener(this);

                                                break;
                                            }

                                        }
                                    }
                                }
                            }
                        }
                        // check if opponent is not found and user is not waiting for the opponent anymore then create a new connection
                        if (!opponentFound && status.equals("waiting")){
                            String connectionUniqueId = String.valueOf(System.currentTimeMillis());
                            //add first player to the connection and wait for other to complete the connection
                            snapshot.child(connectionUniqueId).child(playerUniqueId).child("player_name").getRef().setValue(getPlayerName);

                            status = "waiting";
                        }
                    }
                    else{  // if there is no connection available in the firebase db the create a new connection
                        String connectionUniqueId = String.valueOf(System.currentTimeMillis());
                        //add first player to the connection and wait for other to complete the connection
                        snapshot.child(connectionUniqueId).child(playerUniqueId).child("player_name").getRef().setValue(getPlayerName);

                        status = "waiting";
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        turnsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // getting all turns of the connection
                for (DataSnapshot data : snapshot.getChildren()){
                    if (data.getChildrenCount() == 2){
                        // getting box position selected by the user
                        final int getBoxPosition = Integer.parseInt(data.child("box position").getValue(String.class));
                        // getting player id who selected the box
                        final String getPlayerId = data.child("player_id").getValue(String.class);

                        // check if user has not selected the box before
                        if (!selectedBoxes.contains(String.valueOf(getBoxPosition))){

                            selectedBoxes.add(String.valueOf(getBoxPosition));
                            if (getBoxPosition == 1){
                                selectBox(imgOne, getBoxPosition, getPlayerId);
                            }
                            else if (getBoxPosition == 2){
                                selectBox(imgTwo, getBoxPosition, getPlayerId);
                            }
                            else if (getBoxPosition == 3){
                                selectBox(imgThree, getBoxPosition, getPlayerId);
                            }
                            else if (getBoxPosition == 4){
                                selectBox(imgFour, getBoxPosition, getPlayerId);
                            }
                            else if (getBoxPosition == 5){
                                selectBox(imgFive, getBoxPosition, getPlayerId);
                            }
                            else if (getBoxPosition == 6){
                                selectBox(imgSix, getBoxPosition, getPlayerId);
                            }
                            else if (getBoxPosition == 7){
                                selectBox(imgSeven, getBoxPosition, getPlayerId);
                            }
                            else if (getBoxPosition == 8){
                                selectBox(imgEight, getBoxPosition, getPlayerId);
                            }
                            else if (getBoxPosition == 9){
                                selectBox(imgNine, getBoxPosition, getPlayerId);
                            }

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        wonEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // check if a user has won the match
                if (snapshot.hasChild("player_id")){
                    String getWinPlayerId = snapshot.child("player_id").getValue(String.class);
                    final WinDialog winDialog;
                    if(getWinPlayerId.equals(playerUniqueId)){
                        // show win dialog
                        winDialog = new WinDialog(AfterPlayerName.this, "you won the game");
                    }else{
                        winDialog = new WinDialog(AfterPlayerName.this, "Opponent lost the game");
                    }
                    winDialog.setCancelable(false);
                    winDialog.show();

                    // remove listeners from db
                    databaseReference.child("turns").child(connectionId).removeEventListener(turnsEventListener);
                    databaseReference.child("won").child(connectionId).removeEventListener(wonEventListener);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        imgOne.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // check if the box is not selected before
                if (!selectedBoxes.contains("1") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)v).setImageResource(R.drawable.close_thick);

                    // send selected box positions and player id to db
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(selectedBoxes.size() + 1)).child("box_position").setValue("1");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(selectedBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);

                    // change player turn
                    playerTurn =  opponentUniqueId;
                }
            }
        });

        imgTwo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // check if the box is not selected before
                if (!selectedBoxes.contains("2") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)v).setImageResource(R.drawable.close_thick);
                    // send selected box positions and player id to db
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(selectedBoxes.size() + 1)).child("box_position").setValue("2");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(selectedBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);
                    // change player turn
                    playerTurn =  opponentUniqueId;
                }
            }
        });

        imgThree.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // check if the box is not selected before
                if (!selectedBoxes.contains("3") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)v).setImageResource(R.drawable.close_thick);
                    // send selected box positions and player id to db
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(selectedBoxes.size() + 1)).child("box_position").setValue("3");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(selectedBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);
                    // change player turn
                    playerTurn =  opponentUniqueId;
                }
            }
        });

        imgFour.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // check if the box is not selected before
                if (!selectedBoxes.contains("4") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)v).setImageResource(R.drawable.close_thick);
                    // send selected box positions and player id to db
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(selectedBoxes.size() + 1)).child("box_position").setValue("4");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(selectedBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);
                    // change player turn
                    playerTurn =  opponentUniqueId;
                }
            }
        });

        imgFive.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // check if the box is not selected before
                if (!selectedBoxes.contains("5") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)v).setImageResource(R.drawable.close_thick);
                    // send selected box positions and player id to db
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(selectedBoxes.size() + 1)).child("box_position").setValue("5");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(selectedBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);
                    // change player turn
                    playerTurn =  opponentUniqueId;
                }
            }
        });

        imgSix.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // check if the box is not selected before
                if (!selectedBoxes.contains("6") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)v).setImageResource(R.drawable.close_thick);
                    // send selected box positions and player id to db
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(selectedBoxes.size() + 1)).child("box_position").setValue("6");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(selectedBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);
                    // change player turn
                    playerTurn =  opponentUniqueId;
                }
            }
        });

        imgSeven.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // check if the box is not selected before
                if (!selectedBoxes.contains("7") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)v).setImageResource(R.drawable.close_thick);
                    // send selected box positions and player id to db
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(selectedBoxes.size() + 1)).child("box_position").setValue("7");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(selectedBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);
                    // change player turn
                    playerTurn =  opponentUniqueId;
                }
            }
        });

        imgEight.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // check if the box is not selected before
                if (!selectedBoxes.contains("8") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)v).setImageResource(R.drawable.close_thick);
                    // send selected box positions and player id to db
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(selectedBoxes.size() + 1)).child("box_position").setValue("8");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(selectedBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);
                    // change player turn
                    playerTurn =  opponentUniqueId;
                }
            }
        });

        imgNine.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // check if the box is not selected before
                if (!selectedBoxes.contains("9") && playerTurn.equals(playerUniqueId)){
                    ((ImageView)v).setImageResource(R.drawable.close_thick);
                    // send selected box positions and player id to db
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(selectedBoxes.size() + 1)).child("box_position").setValue("9");
                    databaseReference.child("turns").child(connectionId).child(String.valueOf(selectedBoxes.size() + 1)).child("player_id").setValue(playerUniqueId);
                    // change player turn
                    playerTurn =  opponentUniqueId;
                }
            }
        });

    }

    private void applyPlayerTurn(String playerTurn) {
        if (playerTurn.equals(playerUniqueId)){
            playerOneLayout.setBackgroundResource(R.drawable.round_back_dark_purple);
            playerTwoLayout.setBackgroundResource(R.drawable.round_back_purple_20);
        }
        else{
            playerTwoLayout.setBackgroundResource(R.drawable.round_back_dark_purple);
            playerOneLayout.setBackgroundResource(R.drawable.round_back_purple_20);
        }
    }

    private void selectBox(ImageView imageView, int selectedBoxPosition, String selectedByPlayer){
        boxesSelectedBy[selectedBoxPosition -1] = selectedByPlayer;
        if(selectedByPlayer.equals(playerUniqueId)){
            imageView.setImageResource(R.drawable.close_thick);
            playerTurn = opponentUniqueId;
        }else{
            imageView.setImageResource(R.drawable.circle);
            playerTurn = opponentUniqueId;

        }

        applyPlayerTurn(playerTurn);
        // checking weather play has won the match
        if (checkPlayerWin(selectedByPlayer)){
            // sending won player unique id to firebase so opponent can be notified
            databaseReference.child("won").child(connectionId).child("player_id").setValue(selectedByPlayer);
        }
        // over the game if there is no box left to be selected
        if (selectedBoxes.size() == 9){
            final WinDialog winDialog =  new WinDialog(this, "it's a draw ");
            winDialog.setCancelable(false);
            winDialog.show();
        }
    }

    private boolean checkPlayerWin(String playerId){
        boolean isPlayerWon = false;
        // compare player turn with every winning combination
        for (int i=0; i< combinationsList.size(); i++){
            final int[] combination = combinationsList.get(i);
            // checking last three turn of user
            if (boxesSelectedBy[combination[0]].equals(playerId)
                    && boxesSelectedBy[combination[1]].equals(playerId)
                    && boxesSelectedBy[combination[2]].equals(playerId)
            )
                isPlayerWon = true;
        }
        return isPlayerWon;
    }

}