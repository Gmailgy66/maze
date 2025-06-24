package com.caicai.game.role;

import lombok.Data;

import java.util.ArrayList;
@Data
public class Hero {
    public long score;
    public ArrayList<Skill> skills;
    public Hero(){
        this.score = 0;
        this.skills = new ArrayList<>();
    }
}
