package com.example.a;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class PlayerName extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_name);

        final EditText playerNameText = findViewById(R.id.playerName);
        final AppCompatButton startGameBtn = findViewById(R.id.startGame);

        startGameBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                final String getPlayerName =playerNameText.getText().toString();
                if(getPlayerName.isEmpty()){
                    Toast.makeText(PlayerName.this, "please enter player name", Toast.LENGTH_SHORT).show();
                }else{
                    // creating intent to open AfterPlayerName
                    Intent intent = new Intent(PlayerName.this, AfterPlayerName.class);
                    //Adding player name along with intent
                    intent.putExtra("playerName", getPlayerName);
                    // opens mainActivity
                    startActivity(intent);
                    //Destroy this activity
                    finish();
                }
            }
        });

    }
}