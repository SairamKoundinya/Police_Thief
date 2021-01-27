package com.friendsapp.policethiefgame;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.friendsapp.policethiefgame.Models.Propic;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Winner extends AppCompatActivity {

    @BindView(R.id.tablelayout)
    TableLayout tableLayout;
    @BindView(R.id.wname)
    TextView wname;

    private String[] players;
    private String playerNam, code, winner;
    private int[] scores, picids;
    private int playersCount, pointss;

    private SharedPreferences sharedPref;
    private DatabaseReference myRef;
    private Map<String, Integer> leaders;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_winner);

        try {
            ButterKnife.bind(this);

            sharedPref = getSharedPreferences("com.friendsapp.policethief.sp", Context.MODE_PRIVATE);
            myRef = FirebaseDatabase.getInstance().getReference().child("games");

            playersCount = getCountOfPlayers();
            leaders = new HashMap<>();

            players = new String[playersCount];
            scores = new int[playersCount];
            picids = new int[playersCount];

            setPlayerName();
            setCode();
            showPoints();
            ads();
        }
        catch (Exception e)
        {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void ads() {

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-2971265666261803/6856594374");

        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                mInterstitialAd.show();
            }
        });
    }

    private void setCode() {
        code = sharedPref.getString("code", "0");
        myRef.child(code).removeValue();
    }

    private void addPoints() {

        pointss += sharedPref.getInt("playerPoints", 0);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("playerPoints", pointss);
        editor.apply();

        myRef.child("lboard").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Iterable<DataSnapshot> players = snapshot.getChildren();

                int count = 0;
                int min = Integer.MAX_VALUE;
                String removename = "";
                leaders.clear();
                boolean has = false;
                for (DataSnapshot player : players) {

                    String name = player.getKey();
                    int point = Integer.parseInt(player.getValue().toString());

                    if(min > point) {
                        min = point;
                        removename = name;
                    }
                    count++;
                    leaders.put(name, point);
                    if(name.equals(playerNam))
                        has = true;
                }
                if(has || count<10)
                {
                    leaders.put(playerNam, pointss);
                    myRef.child("lboard").setValue(leaders);
                }
                else{
                    if(pointss>min) {
                        leaders.remove(removename);
                        leaders.put(playerNam, pointss);
                        myRef.child("lboard").setValue(leaders);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void setPlayerName() {
        playerNam = sharedPref.getString("playerName", "User");
    }

    private void showPoints() {

        tableLayout.removeAllViews();
        setUpTableData();

        for(int i=0;i<playersCount;i++)
        {
            tableLayout.addView(getTableRow(i));
            if (players[i].equals(playerNam)) {
                pointss = scores[i]/((i+1)*100);
                addPoints();
            }
        }
        presentWinner();
    }

    private void presentWinner() {

        winner = players[0];
        wname.setText("Winner - " +winner);
    }

    @OnClick(R.id.cgame)
    public void continueGame()
    {
        super.onBackPressed();
    }

    private void setUpTableData() {

        for(int i=0;i<playersCount;i++)
        {
            String playerName = sharedPref.getString("player"+(i+1), "Player"+(i+1));
            players[i] = playerName;

            int picid =  sharedPref.getInt("player" + (i + 1)+"picid", 0);
            picids[i]= picid;

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

                    int ids = picids[i];
                    picids[i] = picids[j];
                    picids[j] = ids;
                }
            }
        }
    }

    private View getTableRow(int i) {

        TableRow tableRow = (TableRow) getLayoutInflater().inflate(R.layout.tablerowwhite, null);

        CircleImageView img = (CircleImageView) tableRow.getChildAt(0);
        img.setImageDrawable(getResources().getDrawable(Propic.propics[picids[i]]));

        TextView tv = (TextView) tableRow.getChildAt(1);
        tv.setText(players[i]);

        tv = (TextView) tableRow.getChildAt(2);
        tv.setText(String.valueOf(scores[i]));

        return tableRow;
    }

    private int getCountOfPlayers() {
        return sharedPref.getInt("playersCount",0);
    }

}

