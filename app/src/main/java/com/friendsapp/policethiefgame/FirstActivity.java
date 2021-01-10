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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public class FirstActivity extends AppCompatActivity {

    @BindView(R.id.playerName)
    TextView playerName;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    private String playerNameStr;
    private boolean came;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        try {

            if(!isTaskRoot() && getIntent().hasCategory(Intent.CATEGORY_LAUNCHER) && getIntent().getAction()!=null && getIntent().getAction().equals(Intent.ACTION_MAIN)){
                finish();
                return;
            }


            ButterKnife.bind(this);

            came = true;
            sharedPreferences = getSharedPreferences("com.friendsapp.policethief.sp", Context.MODE_PRIVATE);
            playerNameStr = sharedPreferences.getString("playerName", "");

            displayPlayerName();
        }
        catch (Exception e)
        {
            toast(e.getMessage());
        }
    }

    private void prgsV()
    {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void prgsIV()
    {
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void displayPlayerName() {

       String displayName =  getString(R.string.Hi)+" "+playerNameStr;
       playerName.setText(displayName);
    }

    @OnClick(R.id.start_game)
    public void startGame()
    {
        goStart();
    }

    private void goStart()
    {
        if(playerNameStr.equals(""))
            addName(1);
        else
            startActivity(new Intent(this, StartGame.class));
    }

    @OnClick(R.id.join_game)
    public void joinGame()
    {
        goJoin();
    }

    private void goJoin()
    {
        if(playerNameStr.equals(""))
            addName(2);
        else
            startActivity(new Intent(this, JoinGame.class));
    }

    private void goCPlay()
    {
        prgsV();
        if(playerNameStr.equals("")) {
            prgsIV();
            addName(3);
        }
        else {
            if(came) {
                came = false;
                startActivity(new Intent(this, ComputerPlay.class));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        came = true;
        prgsIV();
    }

    @OnClick(R.id.leaderboard)
    public void lboard()
    {
        startActivity(new Intent(this, LeaderBoard.class));
    }

    @OnClick(R.id.computerplay)
    public void cplay()
    {
        goCPlay();
    }

    @OnClick(R.id.playerName)
    public void edit()
    {
        addName(0);
    }

    private void addName(int i) {

        try {

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

                    if (!playerNameStr.isEmpty()) {
                        displayPlayerName();

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("playerName", playerNameStr);
                        editor.apply();

                        if (i == 1)
                            goStart();
                        else if (i == 2)
                            goJoin();
                        else if(i == 3)
                            goCPlay();
                    } else {
                        toast("UserName couldn't be empty");
                    }
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
        catch (Exception e)
        {
            toast(e.getMessage());
        }

    }

    private void toast(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}
