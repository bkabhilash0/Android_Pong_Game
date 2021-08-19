package com.infowithvijay.pong2dgame;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class GameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
    }

    public void startGame(View view) {
        Intent intent = new Intent(GameActivity.this,PongActivity.class);
        startActivity(intent);

    }
}
