package com.friendsapp.policethiefgame.Models;

public class Result {

    private String name, score;
    private int imgid, scr;

    public Result() {

    }

    public Result(String name, String score, int imgid, int scr) {

        this.name = name;
        this.score = score;
        this.imgid = imgid;
        this.scr = scr;
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
    public int getScr() {
        return scr;
    }
}
