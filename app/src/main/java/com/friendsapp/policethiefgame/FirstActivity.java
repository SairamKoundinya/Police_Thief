package com.friendsapp.policethiefgame;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.friendsapp.policethiefgame.Models.Propic;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.Objects;
import java.util.Random;

public class FirstActivity extends AppCompatActivity {

    @BindView(R.id.playerName)
    TextView playerName;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.bottom_sheet)
    LinearLayout layoutBottomSheet;
    @BindView(R.id.propic)
    CircleImageView proimg;

    private String playerNameStr;
    private boolean came;

    private BottomSheetBehavior sheetBehavior;
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
            bottomsheet();
            randompropic();
        }
        catch (Exception e)
        {
            toast(e.getMessage());
        }
    }

    private void randompropic() {

        int picno = sharedPreferences.getInt("PropicNum", -1);

        if(picno == -1) {
            picno = new Random().nextInt(8);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("PropicNum", picno);
            editor.apply();
        }

        proimg.setImageDrawable(getResources().getDrawable(Propic.propics[picno]));
    }

    private void bottomsheet() {

        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @OnClick(R.id.info)
    public void info()
    {
        startActivity(new Intent(this, Attribution.class));
    }

    @OnClick(R.id.propic)
    public void propic()
    {
        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @OnClick(R.id.close)
    public void closeStickers()
    {
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
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

    @OnClick(R.id.imageView1)
    public void propic1()
    {
        changepropic(R.drawable.propic1,0);
    }
    @OnClick(R.id.imageView2)
    public void propic2()
    {
        changepropic(R.drawable.propic2,1);
    }
    @OnClick(R.id.imageView3)
    public void propic3()
    {
        changepropic(R.drawable.propic3,2);
    }
    @OnClick(R.id.imageView4)
    public void propic4()
    {
        changepropic(R.drawable.propic4,3);
    }
    @OnClick(R.id.imageView5)
    public void propic5()
    {
        changepropic(R.drawable.propic5,4);
    }
    @OnClick(R.id.imageView6)
    public void propic6()
    {
        changepropic(R.drawable.propic6,5);
    }
    @OnClick(R.id.imageView7)
    public void propic7()
    {
        changepropic(R.drawable.propic7,6);
    }
    @OnClick(R.id.imageView8)
    public void propic8()
    {
        changepropic(R.drawable.propic8,7);
    }

    @OnClick(R.id.imageView9)
    public void propic9()
    {
        changepropic(R.drawable.propic9,8);
    }
    @OnClick(R.id.imageView10)
    public void propic10()
    {
        changepropic(R.drawable.propic10,9);
    }
    @OnClick(R.id.imageView11)
    public void propic11()
    {
        changepropic(R.drawable.propic11,10);
    }
    @OnClick(R.id.imageView12)
    public void propic12()
    {
        changepropic(R.drawable.propics12,11);
    }
    @OnClick(R.id.imageView13)
    public void propic13()
    {
        changepropic(R.drawable.propic13,12);
    }
    @OnClick(R.id.imageView14)
    public void propic14()
    {
        changepropic(R.drawable.propic14,13);
    }
    @OnClick(R.id.imageView15)
    public void propic15()
    {
        changepropic(R.drawable.propic15,14);
    }
    @OnClick(R.id.imageView16)
    public void propic16()
    {
        changepropic(R.drawable.propic16,15);
    }

    private void changepropic(int propic, int picnum) {

        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        proimg.setImageDrawable(getResources().getDrawable(propic));

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("PropicNum", picnum);
        editor.apply();
    }

}
