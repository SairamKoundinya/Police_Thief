package com.friendsapp.policethiefgame.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.friendsapp.policethiefgame.Models.Message;
import com.friendsapp.policethiefgame.Models.MsgAdapter;
import com.friendsapp.policethiefgame.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Chat extends Fragment {

    @BindView(R.id.chatlist)
    ListView listView;

    private SharedPreferences sharedPref;
    private DatabaseReference myRef;
    private String playerName, code;
    private List<Message> messages;
    private MsgAdapter msgAdapter;
//    private ArrayAdapter<String> arrayAdapter;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.chat_fragment, container, false);

        ButterKnife.bind(this, root);

        messages = new ArrayList<>();
        sharedPref = Objects.requireNonNull(getActivity()).getSharedPreferences("com.friendsapp.policethief.sp", Context.MODE_PRIVATE);
        myRef = FirebaseDatabase.getInstance().getReference().child("chat");

        setPlayerName();
        setCode();

        msgAdapter =  new MsgAdapter(getActivity(), messages, playerName);
        //arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.playeritem, messages);
        listView.setAdapter(msgAdapter);
        listentochat();

        return root;
    }



    private void listentochat() {

        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Toast.makeText(getApplicationContext(), snapshot.getValue().toString(),Toast.LENGTH_LONG).show();
                Message message = snapshot.getValue(Message.class);
                messages.add(message);
                msgAdapter.notifyDataSetChanged();
               // messages.add(snapshot.getValue().toString());
               // arrayAdapter.notifyDataSetChanged();
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setPlayerName() {
        playerName = sharedPref.getString("playerName", "User");
    }

    private void setCode() {
        code = sharedPref.getString("code", "0");
    }

}