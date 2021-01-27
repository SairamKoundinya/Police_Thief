package com.friendsapp.policethiefgame.Models;

public class Player {

    public String name;
    public int propicid;

    public Player() {

    }

    public Player(String name, int propicid) {
        this.name = name;
        this.propicid = propicid;
    }

    public String getName() {
        return name;
    }

    public int getPropicid() {
        return propicid;
    }
}
