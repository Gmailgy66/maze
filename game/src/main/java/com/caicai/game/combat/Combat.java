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
        // Reset static variables for each combat session
        minTurns = Integer.MAX_VALUE;
        bestActions = new ArrayList<>();

        JSONObject json = loadJson("boss_battle.json");
        if (json == null) {
            HashMap<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", "Failed to load boss configuration");
            return errorMap;
        }

        JSONArray bossArray = json.getJSONArray("B");
        JSONArray skillsArray = json.getJSONArray("PlayerSkills");

        HashMap<String, Object> map = new HashMap<>();
        map.put("PlayerSkills", skillsArray);

        int[] bossHP = new int[bossArray.length()];
        for (int i = 0; i < bossArray.length(); i++) {
            bossHP[i] = bossArray.getInt(i);
        }
        map.put("Boss", bossHP);

        int[][] skills = new int[skillsArray.length()][2];
        for (int i = 0; i < skillsArray.length(); i++) {
            skills[i][0] = skillsArray.getJSONArray(i).getInt(0); // damage
            skills[i][1] = skillsArray.getJSONArray(i).getInt(1); // cooldown
        }

        long startTime = System.currentTimeMillis();
        solve(bossHP, skills);
        long endTime = System.currentTimeMillis();

        map.put("actions", bestActions);
        map.put("PlayerSkills", skills);
        map.put("minTurns", minTurns);
        map.put("solutionTime", endTime - startTime);

        return map;
    }

    public HashMap<String, Object> startWithJson(JSONObject json) {
        // Reset static variables for each combat session
        minTurns = Integer.MAX_VALUE;
        bestActions = new ArrayList<>();

//        JSONObject json = loadJson("boss_battle.json");
        if (json == null) {
            HashMap<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", "Failed to load boss configuration");
            return errorMap;
        }

        JSONArray bossArray = json.getJSONArray("B");
        JSONArray skillsArray = json.getJSONArray("PlayerSkills");

        HashMap<String, Object> map = new HashMap<>();
        map.put("PlayerSkills", skillsArray);

        int[] bossHP = new int[bossArray.length()];
        for (int i = 0; i < bossArray.length(); i++) {
            bossHP[i] = bossArray.getInt(i);
        }
        map.put("Boss", bossHP);

        int[][] skills = new int[skillsArray.length()][2];
        for (int i = 0; i < skillsArray.length(); i++) {
            skills[i][0] = skillsArray.getJSONArray(i).getInt(0); // damage
            skills[i][1] = skillsArray.getJSONArray(i).getInt(1); // cooldown
        }

        long startTime = System.currentTimeMillis();
        solve(bossHP, skills);
        long endTime = System.currentTimeMillis();

        map.put("actions", bestActions);
        map.put("PlayerSkills", skills);
        map.put("minTurns", minTurns);
        map.put("solutionTime", endTime - startTime);

        return map;
    }

    private static void solve(int[] bossHP, int[][] skills) {
        PriorityQueue<State> pq = new PriorityQueue<>();
        int[] initialCooldown = new int[skills.length];
        pq.add(new State(0, bossHP, initialCooldown, new ArrayList<>()));

        Set<String> visited = new HashSet<>();

        while (!pq.isEmpty()) {
            State current = pq.poll();

            // 剪枝：当前回合数不优
            if (current.turn >= minTurns) {
                continue;
            }

            String stateKey = Arrays.toString(current.bossHP) + Arrays.toString(
                    current.cooldowns);
            if (visited.contains(stateKey)) {
                continue;
            }
            visited.add(stateKey);

            // 当前目标 boss
            int currentBoss = 0;
            while (currentBoss < current.bossHP.length && current.bossHP[currentBoss] <= 0) {
                currentBoss++;
            }

            // 已击败全部 boss，记录最优解
            if (currentBoss == current.bossHP.length) {
                if (current.turn < minTurns) {
                    minTurns = current.turn;
                    bestActions = current.actions;
                }
                continue;
            }

            boolean hasValidSkill = false;

            // 尝试释放所有可用技能
            for (int i = 0; i < skills.length; i++) {
                if (current.cooldowns[i] == 0) {
                    hasValidSkill = true;

                    int[] nextBossHP = current.bossHP.clone();
                    int[] nextCooldown = current.cooldowns.clone();
                    List<Integer> nextActions = new ArrayList<>(
                            current.actions);

                    // 释放技能
                    nextBossHP[currentBoss] -= skills[i][0];
                    if (nextBossHP[currentBoss] < 0) {
                        nextBossHP[currentBoss] = 0;
                    }

                    // 冷却推进
                    for (int j = 0; j < nextCooldown.length; j++) {
                        if (nextCooldown[j] > 0) {
                            nextCooldown[j]--;
                        }
                    }
                    nextCooldown[i] = skills[i][1]; // 本轮技能设冷却

                    nextActions.add(i);
                    pq.add(new State(current.turn + 1, nextBossHP, nextCooldown,
                                     nextActions));
                }
            }

            // 如果没有任何技能可以释放，加入“跳过一回合”状态
            if (!hasValidSkill) {
                int[] nextCooldown = current.cooldowns.clone();
                for (int j = 0; j < nextCooldown.length; j++) {
                    if (nextCooldown[j] > 0) {
                        nextCooldown[j]--;
                    }
                }
                // bossHP 不变，actions 不变
                pq.add(new State(current.turn + 1, current.bossHP, nextCooldown,
                                 new ArrayList<>(current.actions)));
            }
        }
    }

    // private static void solve(int[] bossHP, int[][] skills) {
    // PriorityQueue<State> pq = new PriorityQueue<>();
    // int[] initialCooldown = new int[skills.length];
    // pq.add(new State(0, bossHP, initialCooldown, new ArrayList<>()));
    //
    // while (!pq.isEmpty()) {
    // State current = pq.poll();
    //
    // // 已超过当前最优解，剪枝
    // if (current.turn >= minTurns) continue;
    //
    // // 判断当前目标 Boss 是否已被击败
    // int currentBoss = 0;
    // while (currentBoss < current.bossHP.length && current.bossHP[currentBoss] <=
    // 0) {
    // currentBoss++;
    // }
    // if (currentBoss == current.bossHP.length) {
    // // 所有 boss 击败
    // if (current.turn < minTurns) {
    // minTurns = current.turn;
    // bestActions = current.actions;
    // }
    // continue;
    // }
    //
    // // 尝试释放所有技能
    // for (int i = 0; i < skills.length; i++) {
    // if (current.cooldowns[i] == 0) {
    // int[] nextBossHP = current.bossHP.clone();
    // int[] nextCooldown = current.cooldowns.clone();
    // List<Integer> nextActions = new ArrayList<>(current.actions);
    //
    // // 释放技能
    // nextBossHP[currentBoss] -= skills[i][0];
    // if (nextBossHP[currentBoss] < 0) nextBossHP[currentBoss] = 0;
    //
    // // 更新冷却
    // for (int j = 0; j < nextCooldown.length; j++) {
    // if (nextCooldown[j] > 0) nextCooldown[j]--;
    // }
    // nextCooldown[i] = skills[i][1];
    //
    // nextActions.add(i);
    // pq.add(new State(current.turn + 1, nextBossHP, nextCooldown, nextActions));
    // }
    // }
    // }
    // }

    private static JSONObject loadJson(String filename) {
        try {
            InputStream is = Combat.class.getClassLoader()
                                         .getResourceAsStream(filename);
            if (is == null) {
                System.err.println("Cannot find " + filename);
                return null;
            }
            Scanner scanner = new Scanner(is,
                                          StandardCharsets.UTF_8).useDelimiter(
                    "\\A");
            return new JSONObject(scanner.next());
        } catch (Exception e) {
            System.err.println("Failed to load JSON: " + e.getMessage());
            return null;
        }
    }
}
