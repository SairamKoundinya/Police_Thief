package com.friendsapp.policethiefgame.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.friendsapp.policethiefgame.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class Chat extends Fragment {
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.chat_fragment, container, false);

        return root;
    }
}