package com.caicai.game.combat;

import com.caicai.game.role.*;

public class Combat {
    Hero role;
    Boss enemy;
//    boolean isInitialized = false;

    public Combat(Hero role, Boss enemy) {
        this.role = role;
        this.enemy = enemy;
        role.skills.sort((Skill s1, Skill s2) -> s2.damage - s1.damage);
    }

//    public void initialize(Hero role, Boss enemy) {
//        this.isInitialized = true;
//        this.role = role;
//        this.enemy = enemy;
//        role.skills.sort((Skill s1, Skill s2) -> s2.damage - s1.damage);
//    }

    public String next () {
        String info = "";
        // 玩家回合
        int pos = -1;
        for (int i = 0; i < role.skills.size(); i++) {
            if(role.skills.get(i).nowCd == 0 && pos == -1) {
                pos = i;
            }
            if(role.skills.get(i).nowCd > 0) {
                role.skills.get(i).nowCd--;
            }
        }
        info += String.format("玩家造成了%d点伤害\n", role.skills.get(pos).damage);
        enemy.hp -= role.skills.get(pos).damage;
        role.skills.get(pos).nowCd = role.skills.get(pos).cd;
        if (enemy.hp <= 0) {
            // 战斗结束
            info += "you win!\n";
            return info;
        }

        // 敌人回合
        if (enemy.powerAttack.nowCd == 0){
            role.score -= enemy.powerAttack.damage;
            enemy.powerAttack.nowCd = enemy.powerAttack.cd;
            info += String.format("敌人造成了%d点伤害\n", enemy.powerAttack.damage);
        }
        else {
            role.score -= enemy.attack.damage;
            info += String.format("敌人造成了%d点伤害\n", enemy.attack.damage);
        }
        return info;
    }

}
