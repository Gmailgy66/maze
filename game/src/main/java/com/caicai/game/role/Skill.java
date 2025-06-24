package com.caicai.game.role;

import java.util.Random;

public class Skill {
    public int damage;
    public int cd;
    public int nowCd;

    public static Skill randomSkill() {
        Random random = new Random();
        int cd = random.nextInt(10) + 1; // Random cooldown between 1 and 10
        int damage = random.nextInt(99) + 1; // Random cooldown between 1 and 10
        Skill skill = new Skill(damage, cd, 0);
        return skill;
    }

    public Skill() {
        this.damage = 0;
        this.cd = 0;
        this.nowCd = 0;
    }

    public Skill(int damage, int cd, int nowcd) {
        this.damage = damage;
        this.cd = cd;
        this.nowCd = nowcd;
    }
}
