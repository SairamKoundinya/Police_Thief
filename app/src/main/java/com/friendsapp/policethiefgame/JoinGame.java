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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.friendsapp.policethiefgame.Models.Player;
import com.friendsapp.policethiefgame.Models.Propic;
import com.friendsapp.policethiefgame.Models.ViewHolderPlayer;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class JoinGame extends AppCompatActivity {

    @BindView(R.id.code)
    EditText codeet;
    @BindView(R.id.check_code)
    Button codebtn;
    @BindView(R.id.list_players)
    RecyclerView recyclerView;
    @BindView(R.id.checkcodelv)
    LinearLayout checkcodelv;
    @BindView(R.id.gamecodelv)
    LinearLayout gamecodelv;
    @BindView(R.id.gamecode)
    TextView gamecode;
    @BindView(R.id.host)
    TextView host;

    private DatabaseReference myRef, myref2;
    private String code, playerName;
    private int playersCount, propicid;
    private boolean has, added;

    private RecyclerView.LayoutManager mLayoutManager;
    private FirebaseRecyclerAdapter adapter;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_game);

        try {
            ButterKnife.bind(this);

            mLayoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(mLayoutManager);
            sharedPreferences = getSharedPreferences("com.friendsapp.policethief.sp", Context.MODE_PRIVATE);

            playersCount = 0;
            added = false;

            myRef = FirebaseDatabase.getInstance().getReference().child("games");
            setPlayerName();
            setpropic();
        }
        catch (Exception e)
        {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.check_code)
    public void checkCode(View view) {

        try {

            code = codeet.getText().toString();
            if (code.isEmpty()) {
                toast("Code is empty, enter correct code");
                return;
            }
            checkCodeInDb(code);
        }
        catch (Exception e)
        {
            toast(e.getMessage());
        }
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

                    if(!added) {
                        added = true;
                        Player player = new Player(playerName, propicid);
                        myref2 = myRef.child(code).child("players").push();
                        myref2.setValue(player);
                        joined(code);
                        displayPlayers();
                        storePlayers();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                toast(error.getMessage());
            }
        });
    }

    private void joined(String code) {

        checkcodelv.setVisibility(View.GONE);
        gamecodelv.setVisibility(View.VISIBLE);
        gamecode.setText("game code: "+code);

        myRef.child(code).child("host").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.getValue().toString();
                host.setText("You joined, "+name+" will start the game.");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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

                return new ViewHolderPlayer(view, getApplicationContext());
            }
            @Override
            protected void onBindViewHolder(ViewHolderPlayer holder, final int position, Player player) {
                holder.setPlayerName(player.getName());
                holder.setPropic(Propic.propics[player.getPropicid()]);
            }

        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    @OnClick(R.id.quit_game)
    public void quitgame(){
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

    private void storePlayers() {

        final SharedPreferences.Editor editor = sharedPreferences.edit();
        final Context context = this;

        myRef.child(code).child("spin").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Iterable<DataSnapshot> players = snapshot.getChildren();
                boolean has = false;
                boolean there = false;
                for(DataSnapshot player : players)
                {
                    String temp = "player"+player.getKey();

                    String tmpname = player.getValue().toString();
                    int index = tmpname.lastIndexOf(':');
                    String nme = tmpname.substring(0,index);
                    int picid = Integer.parseInt( tmpname.substring(index+1));

                    editor.putString(temp, nme);
                    editor.putInt(temp+"picid", picid);
                    editor.putInt(temp+"score", 0);
                    playersCount++;
                    has = true;
                    if(playerName.equals(nme))
                    {
                        there = true;
                    }
                }
                if(has && there) {
                    editor.putString("code", code);
                    editor.putInt("playersCount", playersCount);
                    editor.apply();

                    startActivity( new Intent(context, PlayActivity.class));
                    finish();
                }
                if(has) {
                    if (!there) {
                        startedgame();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                toast(error.getMessage());
            }
        });

    }

    @OnClick(R.id.back)
    public void back()
    {
        goback();
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
                    showDialog("No code exists, enter correct code or code has been expired");
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

    private void setpropic() {
        propicid = sharedPreferences.getInt("PropicNum", -1);
    }

    @Override
    public void onBackPressed() {

       goback();
    }

    private void goback()
    {
        if(myref2 != null) {
            showLeaveDialog();
        }
        else{
            super.onBackPressed();
        }
    }

    private void showLeaveDialog() {

        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage("Are you sure, you want to leave game")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        myref2.removeValue();
                        JoinGame.super.onBackPressed();
                    }
                })
                .setNegativeButton("NO",null)
                .show();
    }

    private void startedgame() {
        toast("Game was already started!");
        myref2.removeValue();
        JoinGame.super.onBackPressed();
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
