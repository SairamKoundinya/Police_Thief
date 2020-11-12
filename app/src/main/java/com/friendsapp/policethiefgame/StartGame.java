package com.friendsapp.policethiefgame;

import androidx.annotation.NonNull;
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
import com.friendsapp.policethiefgame.Models.ViewHolderPlayer;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

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
    private int rounds;
    private Map<String,String> players;

    private RecyclerView.LayoutManager mLayoutManager;
    private FirebaseRecyclerAdapter adapter;

    private SharedPreferences sharedPreferences;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_game);
        ButterKnife.bind(this);

        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        sharedPreferences = getSharedPreferences("com.friendsapp.policethief.sp", Context.MODE_PRIVATE);

        players = new HashMap<>();

        myRef = FirebaseDatabase.getInstance().getReference().child("games");

        setPlayerName();
        generateRandomCode();
        fixRounds();
        sounds();
    }

    private void sounds(){
        mediaPlayer = MediaPlayer.create(this, R.raw.gamemusic2);
        mediaPlayer.setLooping(true);
    }

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
        if(mediaPlayer!=null)
        {
            mediaPlayer.release();
        }
    }

    private void fixRounds() {

        rounds = sharedPreferences.getInt("rounds", 2);
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
        if((rounds+1) <6){
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

        String shareBody = "Join my game using code: "+code+", download app here https://play.google.com/store/apps/details?id=com.friendsapp.policethiefgame";
        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Police Thief Game");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);

        startActivity(Intent.createChooser(intent, "share using"));
    }

    private void setPlayerName() {
        playerName = sharedPreferences.getString("playerName", "User");
    }

    private void generateRandomCode() {

        code = getRandomCode();

        while(checkCode(code)){
            code = getRandomCode();
        }

        Player player = new Player(playerName);
        myRef.child(code).child("players").push().setValue(player);
        myRef.child(code).child("code").setValue(code);

        codetv.setText(code);
        displayPlayers();
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

                return new ViewHolderPlayer(view);
            }
            @Override
            protected void onBindViewHolder(ViewHolderPlayer holder, final int position, Player player) {
                holder.setPlayerName(player.getName());
                players.put(String.valueOf(position+1), player.getName());
            }

        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
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
        int playersCount = players.size();
        if(playersCount < 3 || playersCount>10)
        {
            showMinPlayersDialog();
            return;
        }

        myRef.child(code).child("spin").setValue(players);
        myRef.child(code).child("rounds").setValue(rounds);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        for(int i=0;i<playersCount;i++)
        {
            editor.putInt("player"+(i+1)+"score", 0);
            editor.putString("player"+(i+1), players.get(String.valueOf(i+1)));
        }
        editor.putString("code", code);
        editor.putInt("playersCount", playersCount);
        editor.apply();

        startActivity( new Intent(this, MainActivity.class));
        finish();
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
