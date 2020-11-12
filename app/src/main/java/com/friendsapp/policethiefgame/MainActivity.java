package com.friendsapp.policethiefgame;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;

import com.friendsapp.policethiefgame.Models.Message;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnPageChange;

import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.textclassifier.ConversationActions;
import android.widget.Button;
import android.widget.EditText;

import com.friendsapp.policethiefgame.ui.main.SectionsPagerAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.message)
    EditText editText;

    private DatabaseReference myRef;
    private SharedPreferences sharedPref;
    private MediaPlayer mediaPlayer;
    private String message, playerName, code;
    private int count;
    private final static int chatpos = 2;
    TabLayout tabs;

    @BindView(R.id.coordinator)
    CoordinatorLayout coordinatorLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        myRef = FirebaseDatabase.getInstance().getReference().child("chat");
        sharedPref = getSharedPreferences("com.friendsapp.policethief.sp", Context.MODE_PRIVATE);
        count =1;

        setPlayerName();
        setCode();
        listentochat();

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                clearCount(position);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void makeSound(int id)
    {
        mprelease();

        mediaPlayer = MediaPlayer.create(this, id);
        mediaPlayer.start();
    }

    private void mprelease()
    {
        if(mediaPlayer!=null) {
            mediaPlayer.release();
        }
    }

    private void clearCount(int pos) {
        if(pos == chatpos)
        {
            tabs.getTabAt(chatpos).setText(getString(R.string.chat));
        }
    }


    private void listentochat() {

        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Message message = snapshot.getValue(Message.class);
                String name = message.getName();

                if(!playerName.equals(name)) {
                    Snackbar snackbar = Snackbar.make(coordinatorLayout, name+" messaged..", Snackbar.LENGTH_LONG);
                    snackbar.show();
                    makeSound(R.raw.glass);
                }
                changeTabTitle();
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

    private void changeTabTitle() {

        TabLayout.Tab tab = tabs.getTabAt(chatpos);
        String title = tab.getText().toString();

        if(tabs.getSelectedTabPosition() != chatpos) {
            if (title.equals(getString(R.string.chat))) {
                count = 1;
            }
            tab.setText(getString(R.string.chat) + " (" + count + ")");
            count++;
        }
    }

    @OnClick(R.id.send)
    public void sendMessage() {
        message = editText.getText().toString();

        if(!message.isEmpty())
            saveIndb(message);

        editText.getText().clear();
    }

    private void saveIndb(String msg){

        com.friendsapp.policethiefgame.Models.Message mesobj = new Message(playerName, msg);
        myRef.push().setValue(mesobj);
        makeSound(R.raw.blop);
    }

    @OnClick(R.id.qmsg)
    public void quickMessage() {

        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        View dialogView = getLayoutInflater().inflate(R.layout.template_dialog, null);

        dialogView.findViewById(R.id.hi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveIndb(getString(R.string.hi));
                dialogBuilder.dismiss();
            }
        });
        dialogView.findViewById(R.id.hello).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveIndb(getString(R.string.hello));
                dialogBuilder.dismiss();
            }
        });
        dialogView.findViewById(R.id.playfast).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveIndb(getString(R.string.play_fast));
                dialogBuilder.dismiss();
            }
        });
        dialogView.findViewById(R.id.okay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveIndb(getString(R.string.okay));
                dialogBuilder.dismiss();
            }
        });

        dialogView.findViewById(R.id.impolice).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveIndb(getString(R.string.i_m_police));
                dialogBuilder.dismiss();
            }
        });
        dialogView.findViewById(R.id.imthief).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveIndb(getString(R.string.i_m_thief));
                dialogBuilder.dismiss();
            }
        });
        dialogView.findViewById(R.id.whothief).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveIndb(getString(R.string.who_is_thief));
                dialogBuilder.dismiss();
            }
        });
        dialogView.findViewById(R.id.guessfast).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveIndb(getString(R.string.guess_fast));
                dialogBuilder.dismiss();
            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }

    private void setPlayerName() {
        playerName = sharedPref.getString("playerName", "User");
    }

    private void setCode() {
        code = sharedPref.getString("code", "0");
    }

}