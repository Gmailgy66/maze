package com.caicai.game.role;

import java.util.ArrayList;

public class Boss extends Enemy {
    public Skill powerAttack;
    public Boss(){
        this.attack = new Skill();
        this.powerAttack = new Skill();
        this.hp = 0;
    }
}
