package com.friendsapp.policethiefgame;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.friendsapp.policethiefgame.Models.Player;
import com.friendsapp.policethiefgame.Models.Propic;
import com.friendsapp.policethiefgame.Models.ViewHolderPlayer;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class StartGame extends AppCompatActivity {

    @BindView(R.id.list_players)
    RecyclerView recyclerView;
    DatabaseReference myRef;

    @BindView(R.id.code)
    TextView codetv;

    @BindView(R.id.roundsvalue)
    TextView roundstv;

    private String code, playerName;
    private boolean has;
    private int rounds, propicid;
    private Map<String,String> players;

    private RecyclerView.LayoutManager mLayoutManager;
    private FirebaseRecyclerAdapter adapter;

    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_game);

        try {
            ButterKnife.bind(this);

            mLayoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(mLayoutManager);

            sharedPreferences = getSharedPreferences("com.friendsapp.policethief.sp", Context.MODE_PRIVATE);

            players = new HashMap<>();

            myRef = FirebaseDatabase.getInstance().getReference().child("games");

            setPlayerName();
            setpropic();
            generateRandomCode();
            fixRounds();
        }
        catch (Exception e)
        {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void fixRounds() {

        rounds = sharedPreferences.getInt("rounds", 5);
        roundstv.setText(String.valueOf(rounds));
    }

    @OnClick(R.id.decrementbtn)
    public void dcrmnt()
    {
        if((rounds-1) >0){
            rounds--;
            roundstv.setText(String.valueOf(rounds));
        }
    }

    @OnClick(R.id.incrementbtn)
    public void icrmnt()
    {
        if((rounds+1) <26){
            rounds++;
            roundstv.setText(String.valueOf(rounds));
        }
    }

    @OnClick(R.id.copy_code)
    public void copyCode()
    {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Code", code);
        Objects.requireNonNull(clipboardManager).setPrimaryClip(clipData);

        toast("Code copied to clipboard");
    }

    @OnClick(R.id.share_code)
    public void shareCode()
    {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);

        String shareBody = "Join my game using code: \""+code+"\". \nDownload [Police Thief Online Multiplayer Game] here -> https://play.google.com/store/apps/details?id=com.friendsapp.policethiefgame \nOpen game and go to Join Game and enter Game code and play with your friends :)";
        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Police Thief Game");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);

        startActivity(Intent.createChooser(intent, "share using"));
    }

    private void setPlayerName() {
        playerName = sharedPreferences.getString("playerName", "User");
    }

    private void setpropic() {
        propicid = sharedPreferences.getInt("PropicNum", 0);
    }

    private void generateRandomCode() {

        if(code == null) {
            code = getRandomCode();

            while (checkCode(code)) {
                code = getRandomCode();
            }

            Player player = new Player(playerName, propicid);
            myRef.child(code).child("players").push().setValue(player);
            myRef.child(code).child("code").setValue(code);
            myRef.child(code).child("host").setValue(playerName);

            codetv.setText("game code: " + code);
            displayPlayers();
        }
    }

    private String getRandomCode()
    {
        return String.format(getString(R.string.format), new Random().nextInt(10000));
    }

    private boolean checkCode(String code) {

        has = false;

        Query query = myRef.orderByChild("code").equalTo(code);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                    has = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                toast(error.getMessage());
            }
        });
        return has;
    }

    private void displayPlayers() {
        Query query = myRef.child(code).child("players");

        FirebaseRecyclerOptions<Player> options =
                new FirebaseRecyclerOptions.Builder<Player>()
                        .setQuery(query, Player.class).build();

        adapter = new FirebaseRecyclerAdapter<Player, ViewHolderPlayer>(options) {
            @Override
            public ViewHolderPlayer onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.playeritem, parent, false);

                return new ViewHolderPlayer(view, getApplicationContext());
            }
            @Override
            protected void onBindViewHolder(ViewHolderPlayer holder, final int position, Player player) {
                String nme = player.getName();
                holder.setPlayerName(nme);

                int picid = player.getPropicid();
                holder.setPropic(Propic.propics[picid]);

                if(position == 0)
                    players.clear();

                players.put(String.valueOf(position+1), nme+":"+picid);
            }

        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onBackPressed() {
        showLeaveDialog();
    }

    private void showLeaveDialog() {

        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage("Are you sure, you want to stop game")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        myRef.child(code).removeValue();
                        StartGame.super.onBackPressed();
                    }
                })
                .setNegativeButton("NO",null)
                .show();
    }


    @OnClick(R.id.back)
    public void back()
    {
        showLeaveDialog();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(adapter!=null)
            adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(adapter!=null)
            adapter.stopListening();
    }

    @OnClick(R.id.play_game)
    public void playgame()
    {
        try {
            int playersCount = players.size();
            if ((playersCount < 3) || (playersCount > 10)) {
                showMinPlayersDialog();
                return;
            }

            myRef.child(code).child("spin").setValue(players);
            myRef.child(code).child("rounds").setValue(rounds);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            for (int i = 0; i < playersCount; i++) {
                editor.putInt("player" + (i + 1) + "score", 0);
                String tmpname = players.get(String.valueOf(i + 1));
                if (tmpname != null) {
                    int index = tmpname.lastIndexOf(':');
                    String nme = tmpname.substring(0,index);
                    int picid = Integer.parseInt( tmpname.substring(index+1));

                    editor.putString("player" + (i + 1), nme);
                    editor.putInt("player" + (i + 1)+"picid", picid);
                }
            }
            editor.putString("code", code);
            editor.putInt("playersCount", playersCount);
            editor.apply();

            startActivity(new Intent(this, PlayActivity.class));
            finish();
        }
        catch (Exception e)
        {
            toast(e.getMessage());
        }
    }

    private void showMinPlayersDialog() {

        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage("Minimum 3 players required and Maximum 10 players allowed to start game")
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }


    private void toast(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
