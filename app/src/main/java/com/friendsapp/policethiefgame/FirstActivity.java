package com.friendsapp.policethiefgame;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Objects;

public class FirstActivity extends AppCompatActivity {

    @BindView(R.id.playerName)
    TextView playerName;

    private String playerNameStr;

    private SharedPreferences sharedPreferences;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        ButterKnife.bind(this);

        sharedPreferences = getSharedPreferences("com.friendsapp.policethief.sp", Context.MODE_PRIVATE);
        playerNameStr = sharedPreferences.getString("playerName", "");

        displayPlayerName();
        sounds();
    }

    private void sounds(){
        mediaPlayer = MediaPlayer.create(this, R.raw.gamemusic1);
        mediaPlayer.setLooping(true);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mediaPlayer!=null)
        {
            mediaPlayer.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mediaPlayer!=null)
        {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mprelease();
    }

    private void mprelease()
    {
        if(mediaPlayer!=null)
        {
            mediaPlayer.release();
        }
    }

    private void displayPlayerName() {

       String displayName =  getString(R.string.Hi)+" "+playerNameStr;
       playerName.setText(displayName);
    }

    @OnClick(R.id.start_game)
    public void startGame()
    {
        if(playerNameStr.equals(""))
            addName();
        else
            startActivity(new Intent(this, StartGame.class));
    }

    @OnClick(R.id.join_game)
    public void joinGame()
    {
        if(playerNameStr.equals(""))
            addName();
        else
            startActivity(new Intent(this, JoinGame.class));
    }

    @OnClick(R.id.playerName)
    public void edit()
    {
        addName();
    }

    private void addName() {

        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_name, null);

        final EditText editText = (EditText) dialogView.findViewById(R.id.edt_comment);
        Button button1 = (Button) dialogView.findViewById(R.id.buttonSubmit);
        Button button2 = (Button) dialogView.findViewById(R.id.buttonCancel);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogBuilder.dismiss();

                playerNameStr = editText.getText().toString();
                displayPlayerName();

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("playerName", playerNameStr);
                editor.apply();
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogBuilder.dismiss();
            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();

    }

}
