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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

public class JoinGame extends AppCompatActivity {

    @BindView(R.id.code)
    EditText codeet;

    @BindView(R.id.check_code)
    Button codebtn;

    @BindView(R.id.list_players)
    RecyclerView recyclerView;

    private DatabaseReference myRef;
    private String code, playerName, sound;
    private int playersCount;
    private boolean has;

    private RecyclerView.LayoutManager mLayoutManager;
    private FirebaseRecyclerAdapter adapter;
    private SharedPreferences sharedPreferences;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_game);
        ButterKnife.bind(this);

        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        sharedPreferences = getSharedPreferences("com.friendsapp.policethief.sp", Context.MODE_PRIVATE);
        sound = sharedPreferences.getString("sound", "on");

        sounds();

        playersCount = 0;

        myRef = FirebaseDatabase.getInstance().getReference().child("games");
        setPlayerName();
    }


    private void sounds(){
        if(sound.equals("on")) {
            mediaPlayer = MediaPlayer.create(this, R.raw.gamemusic2);
            mediaPlayer.setLooping(true);
        }
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

    @OnClick(R.id.check_code)
    public void checkCode(View view) {

        code = codeet.getText().toString();

        if(code.isEmpty()){
            toast("Code is empty, enter correct code");
            return;
        }
        checkCodeInDb(code);
    }

    private void checkPlayersCount() {

        Query query = myRef.child(code).child("players");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int count = (int)snapshot.getChildrenCount();
                if(count>9){
                    showDialog("Can't join, maximum 10 players allowed");
                }
                else{
                   // toast("Players low only");
                    Player player = new Player(playerName);
                    myRef.child(code).child("players").push().setValue(player);
                    displayPlayers();
                    storePlayers();
                    codebtn.setEnabled(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                toast(error.getMessage());
            }
        });
    }

    private void checkNameInDb() {

        Query query = myRef.child(code).child("players").orderByChild("name").equalTo(playerName);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    showDialog("Player already exists with name: "+playerName+" in this game, change your name and join back");
                }
                else{
                  //  toast("Name not exists");
                    checkPlayersCount();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                toast(error.getMessage());
            }
        });
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

    private void storePlayers() {

        final SharedPreferences.Editor editor = sharedPreferences.edit();
        final Context context = this;

        myRef.child(code).child("spin").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Iterable<DataSnapshot> players = snapshot.getChildren();
                boolean has = false;
                for(DataSnapshot player : players)
                {
                    String temp = "player"+player.getKey();
                    editor.putString(temp, player.getValue().toString());
                    editor.putInt(temp+"score", 0);
                    playersCount++;
                    has = true;
                }
                if(has) {
                    editor.putString("code", code);
                    editor.putInt("playersCount", playersCount);
                    editor.apply();

                    startActivity( new Intent(context, MainActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                toast(error.getMessage());
            }
        });

    }

    private void checkCodeInDb(String code) {

        Query query = myRef.orderByChild("code").equalTo(code);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    //toast("Code exists");
                    checkNameInDb();
                }
                else{
                    showDialog("No code exists, enter correct code");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                toast(error.getMessage());
            }
        });
    }

    private void setPlayerName() {
        playerName = sharedPreferences.getString("playerName", "User");
    }

    private void showDialog(String msg) {

        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(msg)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void toast(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

}
