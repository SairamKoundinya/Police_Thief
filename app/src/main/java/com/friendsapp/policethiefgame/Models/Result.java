package com.friendsapp.policethiefgame.Models;

public class Result {

    private String name, score;
    private int imgid;

    public Result() {

    }

    public Result(String name, String score, int imgid) {

        this.name = name;
        this.score = score;
        this.imgid = imgid;
    }

    public String getName() {
        return name;
    }
    public String getScore() {
        return score;
    }
    public int getImgid() {
        return imgid;
    }
}
