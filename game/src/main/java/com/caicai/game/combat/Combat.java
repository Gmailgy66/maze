package com.caicai.game.combat;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Combat {

    static class State implements Comparable<State> {
        int turn;
        int[] bossHP;
        int[] cooldowns;
        List<Integer> actions;

        public State(int turn, int[] bossHP, int[] cooldowns, List<Integer> actions) {
            this.turn = turn;
            this.bossHP = bossHP.clone();
            this.cooldowns = cooldowns.clone();
            this.actions = new ArrayList<>(actions);
        }

        @Override
        public int compareTo(State o) {
            return Integer.compare(this.turn, o.turn);
        }
    }

    static int minTurns = Integer.MAX_VALUE;
    static List<Integer> bestActions = new ArrayList<>();

    public HashMap<String, Object> start() {
        JSONObject json = loadJson("boss_battle.json");
        JSONArray bossArray = json.getJSONArray("B");
        JSONArray skillsArray = json.getJSONArray("PlayerSkills");

        int[] bossHP = new int[bossArray.length()];
        for (int i = 0; i < bossArray.length(); i++) {
            bossHP[i] = bossArray.getInt(i);
        }

        int[][] skills = new int[skillsArray.length()][2];
        for (int i = 0; i < skillsArray.length(); i++) {
            skills[i][0] = skillsArray.getJSONArray(i).getInt(0); // damage
            skills[i][1] = skillsArray.getJSONArray(i).getInt(1); // cooldown
        }

        solve(bossHP, skills);

        // 输出结果
        JSONObject output = new JSONObject();
        output.put("min_turns", minTurns);
        output.put("actions", bestActions);

        HashMap<String, Object> map = new HashMap<>();
        for (String key : output.keySet()) {
            map.put(key, output.get(key));
        }
        return map;
    }

    private static void solve(int[] bossHP, int[][] skills) {
        PriorityQueue<State> pq = new PriorityQueue<>();
        int[] initialCooldown = new int[skills.length];
        pq.add(new State(0, bossHP, initialCooldown, new ArrayList<>()));

        while (!pq.isEmpty()) {
            State current = pq.poll();

            // 已超过当前最优解，剪枝
            if (current.turn >= minTurns) continue;

            // 判断当前目标 Boss 是否已被击败
            int currentBoss = 0;
            while (currentBoss < current.bossHP.length && current.bossHP[currentBoss] <= 0) {
                currentBoss++;
            }
            if (currentBoss == current.bossHP.length) {
                // 所有 boss 击败
                if (current.turn < minTurns) {
                    minTurns = current.turn;
                    bestActions = current.actions;
                }
                continue;
            }

            // 尝试释放所有技能
            for (int i = 0; i < skills.length; i++) {
                if (current.cooldowns[i] == 0) {
                    int[] nextBossHP = current.bossHP.clone();
                    int[] nextCooldown = current.cooldowns.clone();
                    List<Integer> nextActions = new ArrayList<>(current.actions);

                    // 释放技能
                    nextBossHP[currentBoss] -= skills[i][0];
                    if (nextBossHP[currentBoss] < 0) nextBossHP[currentBoss] = 0;

                    // 更新冷却
                    for (int j = 0; j < nextCooldown.length; j++) {
                        if (nextCooldown[j] > 0) nextCooldown[j]--;
                    }
                    nextCooldown[i] = skills[i][1];

                    nextActions.add(i);
                    pq.add(new State(current.turn + 1, nextBossHP, nextCooldown, nextActions));
                }
            }
        }
    }

    private static JSONObject loadJson(String filename) {
        try {
            InputStream is = Combat.class.getClassLoader().getResourceAsStream(filename);
            if (is == null) throw new RuntimeException("Cannot find " + filename);
            Scanner scanner = new Scanner(is, StandardCharsets.UTF_8).useDelimiter("\\A");
            return new JSONObject(scanner.next());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load JSON: " + e.getMessage());
        }
    }
}
