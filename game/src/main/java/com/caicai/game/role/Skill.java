package com.caicai.game.role;

public class Skill {
    public int damage;
    public int cd;
    public int nowCd;

    public Skill() {
        this.damage = 0;
        this.cd = 0;
        this.nowCd = 0;
    }

    public Skill(int i, int i1, int i2) {
        this.damage = i;
        this.cd = i1;
        this.nowCd = i2;
    }
}
