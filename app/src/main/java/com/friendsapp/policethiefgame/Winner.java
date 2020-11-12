package com.friendsapp.policethiefgame;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Objects;

public class Winner extends AppCompatActivity {

    @BindView(R.id.tablelayout)
    TableLayout tableLayout;
    @BindView(R.id.wname)
    TextView wname;
    @BindView(R.id.wpoints)
    TextView wpoints;

    private String[] players;
    private int[] scores;
    private int playersCount;

    private SharedPreferences sharedPref;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_winner);

        ButterKnife.bind(this);

        sharedPref = getSharedPreferences("com.friendsapp.policethief.sp", Context.MODE_PRIVATE);
        mediaPlayer = MediaPlayer.create(this, R.raw.gamemusic1);
        mediaPlayer.start();

        playersCount =   getCountOfPlayers();

        players = new String[playersCount];
        scores = new int[playersCount];

        showPoints();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.release();
    }

    private void showPoints() {

        tableLayout.removeAllViews();
        setUpTableData();

        for(int i=0;i<playersCount;i++)
        {
            tableLayout.addView(getTableRow(i));
        }
        presentWinner();
    }

    private void presentWinner() {

        wname.setText(players[0]);
        wpoints.setText(String.valueOf(scores[0]));
    }

    @OnClick(R.id.cgame)
    public void continueGame()
    {
        startActivity(new Intent(this, FirstActivity.class));
        finish();
    }

    private void setUpTableData() {

        for(int i=0;i<playersCount;i++)
        {
            String playerName = sharedPref.getString("player"+(i+1), "Player"+(i+1));
            players[i] = playerName;

            int score = sharedPref.getInt("player"+(i+1)+"score", 0);
            scores[i] = score;
        }

        sortTableData();

    }

    private void sortTableData() {

        for(int i=0;i<playersCount;i++)
        {
            for(int j=i+1;j<playersCount;j++)
            {
                if(scores[j]>scores[i])
                {
                    int scr = scores[i];
                    scores[i] = scores[j];
                    scores[j] = scr;

                    String name = players[i];
                    players[i] = players[j];
                    players[j] = name;
                }
            }
        }
    }


    private View getTableRow(int i) {
        TableRow tableRow ;

        if(i%2==0)
            tableRow = (TableRow) getLayoutInflater().inflate(R.layout.tablerowwhite, null);
        else
            tableRow = (TableRow) getLayoutInflater().inflate(R.layout.tablerowblue, null);

        TextView tv = (TextView) tableRow.getChildAt(0);
        tv.setText(players[i]);

        tv = (TextView) tableRow.getChildAt(1);
        tv.setText(String.valueOf(scores[i]));

        return tableRow;
    }

    private int getCountOfPlayers() {
        return sharedPref.getInt("playersCount",0);
    }

}
