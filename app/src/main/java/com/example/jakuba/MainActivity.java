package com.example.jakuba;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnNews = findViewById(R.id.btnNewspap);
        Button btnMusic = findViewById(R.id.btnMusicPlayer);
        Button btnRecords = findViewById(R.id.btnRecords);
        btnNews.setOnClickListener(this);
        btnMusic.setOnClickListener(this);
        btnRecords.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;

        switch (v.getId()) {
            case R.id.btnNewspap:
                intent = new Intent(this, NewspaperActivity.class);
                break;
            case R.id.btnMusicPlayer:
                intent = new Intent(this, YoutubeMenuActivity.class);
                break;
            case R.id.btnRecords:
                intent = new Intent(this, RecordsActivity.class);
                break;

                default:
        }
        if (intent != null) {
            startActivity(intent);
        }

    }
}
