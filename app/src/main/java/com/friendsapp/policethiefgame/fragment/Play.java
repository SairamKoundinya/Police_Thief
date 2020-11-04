package com.friendsapp.policethiefgame.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.friendsapp.policethiefgame.Models.MyAdapter;
import com.friendsapp.policethiefgame.Models.Result;
import com.friendsapp.policethiefgame.R;
import com.friendsapp.policethiefgame.Winner;
import com.github.aakira.expandablelayout.ExpandableLinearLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Play extends Fragment {

    @BindView(R.id.status1)
    TextView status1;
    @BindView(R.id.status2)
    TextView status2;
    @BindView(R.id.distributebutton)
    Button button;
    @BindView(R.id.listview)
    ListView playerslist;
    @BindView(R.id.distributetimer)
    TextView dtimer;
    @BindView(R.id.findtimer)
    TextView ftimer;
    @BindView(R.id.police)
    TextView policetv;
    @BindView(R.id.thief)
    TextView thieftv;
    @BindView(R.id.hide)
    TextView hide;
    @BindView(R.id.expansion)
    ImageView expansion;
    @BindView(R.id.content)
    ExpandableLinearLayout content;
    @BindView(R.id.my_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.policeimg)
    ImageView policeimg;
    @BindView(R.id.thiefimg)
    ImageView thiefimg;
    @BindView(R.id.lvtext)
    LinearLayout lvtext;

    private DatabaseReference myRef;
    private SharedPreferences sharedPreferences;
    private CountDownTimer disTimer, findTimer;
    private String[] commonnames;
    private int[] imageids;

    private List<Result> myDataset2;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private String currentPlayer, playerName, temp;
    private Map<String, String> chits;
    private List<Integer> assigned, cmnasg;
    private ArrayAdapter<String> arrayAdapter;
    private List<String> playernames;
    private String code, policename, thiefname;
    private int playersCount, track, pid, tid, rounds, roundCount;
    private int[] score;
    private boolean expan;
    private int cid;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.play_fragment, container, false);
        ButterKnife.bind(this, root);

        track =1;
        roundCount = 1;
        rounds = 2;
        expan = true;
        assigned = new ArrayList<>();
        cmnasg = new ArrayList<>();
        playernames = new ArrayList<>();
        myDataset2 = new ArrayList<>();
        chits = new HashMap<>();

        sharedPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences("com.friendsapp.policethief.sp", Context.MODE_PRIVATE);
        myRef = FirebaseDatabase.getInstance().getReference().child("games");
/*
        setPlayerName();
        setPlayersCount();
        setCode();
        setRounds();
        makeTimer();
        distribute();
        listentoChits();
        presentlistview();
        listenToSelection();
        expansionpanel();
        setupImageids();
        recyclerViewsetup();
*/
        return root;
    }

    private void recyclerViewsetup() {

        mLayoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        recyclerView.setLayoutManager(mLayoutManager);
    }

    private void setupImageids() {

        commonnames = new String[]{
                "Actor", "Athlete", "Businessman", "Engineer", "Doctor", "Minister", "Teacher", "Farmer"
        };
        imageids = new int[]{
                R.drawable.actor, R.drawable.athlete, R.drawable.businessman, R.drawable.engineer,
                R.drawable.doctor, R.drawable.minister, R.drawable.teacher, R.drawable.farmer,
                R.drawable.police, R.drawable.thief
        };
    }

    private void expansionpanel() {

        expansion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                content.toggle();
                if(expan) {
                    expansion.setImageDrawable(getActivity().getDrawable(R.drawable.expandown));
                    hide.setText(R.string.show);
                    expan = false;
                }
                else {
                    expansion.setImageDrawable(getActivity().getDrawable(R.drawable.expanup));
                    hide.setText(R.string.hide);
                    expan = true;
                }

            }
        });
    }

    private void makeTimer() {

        int millis= 1000;

        disTimer =  new CountDownTimer(30*millis, millis) {
            @Override
            public void onTick(long millisUntilFinished) {
                String countdown = millisUntilFinished/millis+"s";
                dtimer.setText(countdown);
            }

            @Override
            public void onFinish() {

                if(currentPlayer.equals(playerName))
                {
                    button.setEnabled(false);
                    status1.setText(R.string.autoD);
                    setupChits();
                }
            }
        };

        findTimer =  new CountDownTimer(+90*millis, millis) {
            @Override
            public void onTick(long millisUntilFinished) {
                String countdown = millisUntilFinished/millis+"s";
                ftimer.setText(countdown);
            }

            @Override
            public void onFinish() {

                if(policename.equals(playerName))
                {
                    myRef.child(code).child("selection").setValue(policename);
                }
            }
        };

    }

    private void setRounds() {

        myRef.child(code).child("rounds").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                rounds = Integer.valueOf(snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void distribute() {

        if(track > playersCount) {

            if(roundCount < rounds)
            {
                toast("Round "+roundCount+" completed");
                roundCount++;
                track = 1;
            }
            else{
                startActivity(new Intent(getContext(), Winner.class));
                getActivity().finish();
            }
        }

        currentPlayer = sharedPreferences.getString("player"+track,"empty");
        disTimer.start();

        if(currentPlayer.equals(playerName))
        {
            status1.setText(R.string.UrTurn);
            button.setEnabled(true);
        }
        else{
            String temp = currentPlayer+ " turn to distribute chits";
            status1.setText(temp);
        }
    }

    private void listentoChits() {

        myRef.child(code).child("chits").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Iterable<DataSnapshot> players = snapshot.getChildren();
                boolean has = false;
                myDataset2.clear();
                for(DataSnapshot player : players)
                {
                    temp = player.getKey();
                    int id = Integer.parseInt(Objects.requireNonNull(temp).substring(0,1));
                    String designation = temp.substring(1);

                    if(designation.equals("Police"))
                    {
                        policename = player.getValue().toString();
                        pid = id;
                    }
                    else if(designation.equals("Thief"))
                    {
                        thiefname = player.getValue().toString();
                        tid = id;
                    }
                    else if(designation.substring(0,6).equals("Common"))
                    {
                        int num = Integer.parseInt(designation.substring(6));
                        score[id] = (num+1)*100;
                        if(player.getValue().toString().equals(playerName))
                        {
                            cid = id;
                        }
                        myDataset2.add(new Result(player.getValue().toString(), "+"+score[id], imageids[num]));
                    }
                    has = true;
                }
                if(has)
                {
                    if(policename.equals(playerName))
                    {
                        status2.setText("Guess who is thief");
                        policetv.setText("You're the police");
                        thieftv.setText("who is thief?");
                        thiefimg.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.thief));
                    }
                    else if(thiefname.equals(playerName))
                    {
                        status2.setText(policename+" will guess the thief");
                        policetv.setText(policename+" is the police");
                        thieftv.setText("You're the thief");
                        thiefimg.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.thief));
                    }
                    else
                    {
                        status2.setText(policename+" will guess the thief");
                        policetv.setText(policename+" is the police");
                        thieftv.setText("You're the "+commonnames[cid]);
                        thiefimg.setImageDrawable(getActivity().getResources().getDrawable(imageids[cid]));
                    }
                    status1.setText(currentPlayer+" distributed");
                    playerslist.setVisibility(View.VISIBLE);
                    lvtext.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    disTimer.cancel();
                    findTimer.start();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                    toast(error.getMessage());
            }
        });
    }

    private void presentlistview() {

        for(int i=0;i<playersCount;i++)
        {
            String name = sharedPreferences.getString("player"+(i+1),"empty");

            if(!playerName.equals(name))
                playernames.add(name);
        }
        arrayAdapter = new ArrayAdapter<>(Objects.requireNonNull(getActivity()), R.layout.playeritem, playernames);
        playerslist.setAdapter(arrayAdapter);

        playerslist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(playerName.equals(policename)) {
                    String selected = arrayAdapter.getItem(position);
                    myRef.child(code).child("selection").setValue(selected);
                }
            }
        });
    }

    private void listenToSelection() {

        myRef.child(code).child("selection").setValue("");
        myRef.child(code).child("selection").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                    String select = Objects.requireNonNull(snapshot.getValue()).toString();
                    if(!select.equals(""))
                    {
                        if(thiefname.equals(select))
                        {
                            myDataset2.add(new Result(policename, "+"+1000, imageids[8]));
                            myDataset2.add(new Result(thiefname, "+"+0, imageids[9]));
                            score[pid] = 1000;
                            score[tid] = 0;
                        }
                        else{
                            myDataset2.add(new Result(policename, "+"+0, imageids[8]));
                            myDataset2.add(new Result(thiefname, "+"+1000, imageids[9]));
                            score[pid] = 0;
                            score[tid] = 1000;
                        }

                        policetv.setText("");
                        thieftv.setText("");
                        thiefimg.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.thief));
                        findTimer.cancel();
                        playerslist.setVisibility(View.GONE);
                        lvtext.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        mAdapter = new MyAdapter(myDataset2);
                        recyclerView.setAdapter(mAdapter);
                        updateScores();
                        track++;
                        distribute();
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                    toast(error.getMessage());
            }
        });
    }

    private void updateScores() {

        SharedPreferences.Editor editor = sharedPreferences.edit();
        for(int i=0; i<playersCount; i++)
        {
            int scr = sharedPreferences.getInt("player"+(i+1)+"score", 0);
            scr += score[i];
            editor.putInt("player"+(i+1)+"score", scr);
        }
        editor.apply();
    }

    @OnClick(R.id.distributebutton)
    public void distributeChits() {
        button.setEnabled(false);
        status1.setText(R.string.YouD);
        setupChits();
    }

    private void setupChits() {

        assigned.clear();
        cmnasg.clear();

        Random random = new Random();
        int police = random.nextInt(playersCount);

        assigned.add(police);
        chits.put(police+"Police", sharedPreferences.getString("player"+(police+1),"empty"));

        while(true)
        {
            int thief = random.nextInt(playersCount);
            if(!assigned.contains(thief))
            {
                assigned.add(thief);
                chits.put(thief+"Thief", sharedPreferences.getString("player"+(thief+1),"empty"));
                break;
            }
        }

        for(int i =0; i<playersCount; i++)
        {
            if(assigned.contains(i)) continue;
            while(true) {
                int common = random.nextInt(8);
                if (!cmnasg.contains(common)) {
                    cmnasg.add(common);
                    chits.put(i+"Common" + common , sharedPreferences.getString("player" + (i + 1), "empty"));
                    break;
                }
            }
        }

        myRef.child(code).child("chits").setValue(chits);
    }

    private void setPlayerName() {
        playerName = sharedPreferences.getString("playerName", "User");
    }

    private void setPlayersCount() {
        playersCount = sharedPreferences.getInt("playersCount", 0);
        score = new int[playersCount];
    }

    private void setCode() {
        code = sharedPreferences.getString("code", "0");
    }

    private void toast(String message)
    {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }
}

