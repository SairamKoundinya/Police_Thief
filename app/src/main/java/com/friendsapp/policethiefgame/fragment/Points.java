package com.friendsapp.policethiefgame.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.friendsapp.policethiefgame.R;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import butterknife.BindView;
import butterknife.ButterKnife;

public class Points  extends Fragment {

    @BindView(R.id.tablelayout)
    TableLayout tableLayout;

    private String[] players;
    private int[] scores;
    private int playersCount;

    private SharedPreferences sharedPref;
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.points_fragment, container, false);
        ButterKnife.bind(this, root);

        sharedPref = Objects.requireNonNull(getActivity()).getSharedPreferences("com.friendsapp.policethief.sp", Context.MODE_PRIVATE);

        playersCount = getCountOfPlayers();

        players = new String[playersCount];
        scores = new int[playersCount];

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        showPoints();
    }

    private void showPoints() {


        tableLayout.removeAllViews();
        setUpTableData();

        for(int i=0;i<playersCount;i++)
        {
            tableLayout.addView(getTableRow(i));
        }
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
