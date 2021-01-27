package com.friendsapp.policethiefgame.Models;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.friendsapp.policethiefgame.R;

import androidx.recyclerview.widget.RecyclerView;

public class ViewHolderPlayer  extends RecyclerView.ViewHolder {
    private TextView playerName;
    private ImageView propicimg;
    private Context context;


    public ViewHolderPlayer(View itemView, Context context) {
        super(itemView);
        this.context = context;
        playerName = itemView.findViewById(R.id.list_name);
        propicimg = itemView.findViewById(R.id.propic);
    }

    public void setPlayerName(String string) {
        playerName.setText(string);
    }

    public void setPropic(int imgid) {

        propicimg.setImageDrawable(context.getResources().getDrawable(imgid));
    }

}