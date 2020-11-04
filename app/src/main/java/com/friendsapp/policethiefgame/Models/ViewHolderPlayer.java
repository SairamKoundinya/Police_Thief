package com.friendsapp.policethiefgame.Models;

import android.view.View;
import android.widget.TextView;


import com.friendsapp.policethiefgame.R;

import androidx.recyclerview.widget.RecyclerView;

public class ViewHolderPlayer  extends RecyclerView.ViewHolder {
    private TextView playerName;

    public ViewHolderPlayer(View itemView) {
        super(itemView);
        playerName = itemView.findViewById(R.id.list_name);
    }

    public void setPlayerName(String string) {
        playerName.setText(string);
    }

}