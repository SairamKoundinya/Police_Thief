package com.friendsapp.policethiefgame.Models;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.friendsapp.policethiefgame.R;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class MyAdapter extends  RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private List<Result> mDataset;
    private Context apc;

    public class MyViewHolder extends RecyclerView.ViewHolder  {

        public TextView point, name, score;
        public CircleImageView img;

        public MyViewHolder(View v) {
            super(v);
            apc = v.getContext();
            point = v.findViewById(R.id.point);
            name = v.findViewById(R.id.result);
            img = v.findViewById(R.id.img);
            score = v.findViewById(R.id.score);
        }

    }

    public MyAdapter(List<Result> myDataset2) {
        mDataset = myDataset2;
    }

    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item, parent, false);
        return new MyAdapter.MyViewHolder(v);

    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        holder.point.setText(mDataset.get(position).getScore());
        holder.name.setText(mDataset.get(position).getName());
        holder.img.setImageDrawable(apc.getResources().getDrawable(mDataset.get(position).getImgid()));
        holder.score.setText("Score: "+mDataset.get(position).getScr());
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
