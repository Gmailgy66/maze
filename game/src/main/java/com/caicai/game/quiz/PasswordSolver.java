
package com.caicai.game.quiz;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

public class PasswordSolver {
    private static final byte[] FIXED_SALT = new byte[]{
            (byte) 0xb2, 0x53, 0x22, 0x65, 0x7d, (byte) 0xdf, (byte) 0xb0, (byte) 0xfe,
            (byte) 0x9c, (byte) 0xde, (byte) 0xde, (byte) 0xfe, (byte) 0xf3, 0x1d, (byte) 0xdc, 0x3e
    };

    private static String targetHash;
    private static List<int[]> clues;

//    public int solveFromFile(File filename) throws Exception {
//        ObjectMapper mapper = new ObjectMapper();
//        JsonNode root = mapper.readTree(filename);
//        targetHash = root.get("L").asText();
//
//        clues = new ArrayList<>();
//        for (JsonNode clue : root.get("C")) {
//            int[] c = new int[clue.size()];
//            for (int i = 0; i < clue.size(); i++) c[i] = clue.get(i).asInt();
//            clues.add(c);
//        }
//
//        // 三种方式分别求出尝试次数
//        int tries1 = runBacktrackingCountOnly(0, 1);
//        int tries2 = runBacktrackingCountOnly(9, -1);
//        int tries3 = runCluePriorityCountOnly();
//
//        // 返回最小尝试次数
//        return Math.min(tries1, Math.min(tries2, tries3));
//    }
//
//    // 简化版本：只返回尝试次数，不记录日志
//    private int runBacktrackingCountOnly(int startDigit, int step) throws Exception {
//        int[] tries = {0};
//        boolean[] found = {false};
//        backtrackCountOnly(new ArrayList<>(), startDigit, step, tries, found);
//        return found[0] ? tries[0] : Integer.MAX_VALUE;
//    }
//
//    private void backtrackCountOnly(List<Integer> path, int startDigit, int step, int[] tries, boolean[] found) throws Exception {
//        if (path.size() == 3) {
//            int[] pwd = {path.get(0), path.get(1), path.get(2)};
//            if (!satisfyAllClues(pwd)) return;
//            tries[0]++;
//            String pwdStr = "" + pwd[0] + pwd[1] + pwd[2];
//            String hash = hashPassword(pwdStr);
//            if (hash.equalsIgnoreCase(targetHash)) {
//                found[0] = true;
//            }
//            return;
//        }
//        for (int i = startDigit; i >= 0 && i <= 9; i += step) {
//            path.add(i);
//            backtrackCountOnly(path, startDigit, step, tries, found);
//            path.remove(path.size() - 1);
//            if (found[0]) return;
//        }
//    }
//
//    private int runCluePriorityCountOnly() throws Exception {
//        List<int[]> candidates = new ArrayList<>();
//        for (int i = 0; i <= 9; i++)
//            for (int j = 0; j <= 9; j++)
//                for (int k = 0; k <= 9; k++)
//                    candidates.add(new int[]{i, j, k});
//
//        candidates.sort((a, b) -> Integer.compare(countClueMatches(b), countClueMatches(a)));
//
//        int tries = 0;
//        for (int[] pwd : candidates) {
//            if (!satisfyAllClues(pwd)) continue;
//            tries++;
//            String pwdStr = "" + pwd[0] + pwd[1] + pwd[2];
//            String hash = hashPassword(pwdStr);
//            if (hash.equalsIgnoreCase(targetHash)) return tries;
//        }
//        return Integer.MAX_VALUE;
//    }

    public HashMap<String, Object> start() throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = PasswordSolver.class.getClassLoader().getResourceAsStream("password_data.json");
        if (is == null) throw new RuntimeException("无法找到资源文件 password_data.json");
        JsonNode root = mapper.readTree(is);
        targetHash = root.get("L").asText();

        StringBuilder qs = new StringBuilder();
        qs.append("哈希：\n").append(targetHash).append("\n线索：\n");
        clues = new ArrayList<>();
        for (JsonNode clue : root.get("C")) {
            int[] c = new int[clue.size()];
            for (int i = 0; i < clue.size(); i++) c[i] = clue.get(i).asInt();
            clues.add(c);
            qs.append(Arrays.toString(c)).append("\n");
        }
        map.put("question", qs.toString());

        StringBuilder ans = new StringBuilder();
        ans.append("=== Method 1: 回溯顺序递增 ===\n");
        ans.append(runBacktracking(0, 1));

        ans.append("=== Method 2: 回溯倒序递减 ===\n");
        ans.append(runBacktracking(9, -1));

        ans.append("=== Method 3: 回溯按线索优先排序 ===\n");
        ans.append(runBacktrackingWithCluePriority());

        map.put("ans", ans.toString());
        return map;
    }

    // 从startDigit开始，每次递增step
    private String runBacktracking(int startDigit, int step) throws Exception {
        StringBuilder result = new StringBuilder();
        int[] tries = {0};
        boolean[] found = {false};
        backtrack(new ArrayList<>(), startDigit, step, tries, result, found);
        if (!found[0]) result.append("未找到匹配密码！\n");
        return result.toString();
    }

    // 尝试所有可能的三位组合
    private void backtrack(List<Integer> path, int startDigit, int step, int[] tries, StringBuilder res, boolean[] found) throws Exception {
        if (path.size() == 3) {
            int[] pwd = {path.get(0), path.get(1), path.get(2)};
            if (!satisfyAllClues(pwd)) return;
            tries[0]++;
            String pwdStr = "" + pwd[0] + pwd[1] + pwd[2];
            String hash = hashPassword(pwdStr);
            if (hash.equalsIgnoreCase(targetHash)) {
                res.append("匹配成功！密码为: ").append(pwdStr).append("\n");
                res.append("总尝试次数: ").append(tries[0]).append("\n");
                found[0] = true;
            }
            return;
        }
        for (int i = startDigit; i >= 0 && i <= 9; i += step) {
            path.add(i);
            backtrack(path, startDigit, step, tries, res, found);
            path.remove(path.size() - 1);
            if (found[0]) return;
        }
    }

    public static int countClueMatches(int[] pwd) {
        int count = 0;
        List<Integer> pwdList = Arrays.asList(pwd[0], pwd[1], pwd[2]);
        for (int[] clue : clues) {
            if (matchClue(pwdList, clue)) count++;
        }
        return count;
    }

    // 生成所有三位数组合
    // 接着统计符合线索的三位数
    // 然后进行校验
    private String runBacktrackingWithCluePriority() throws Exception {
        StringBuilder result = new StringBuilder();
        List<int[]> candidates = new ArrayList<>();
        for (int i = 0; i <= 9; i++)
            for (int j = 0; j <= 9; j++)
                for (int k = 0; k <= 9; k++)
                    candidates.add(new int[]{i, j, k});

        candidates.sort((a, b) -> Integer.compare(countClueMatches(b), countClueMatches(a)));

        int tries = 0;
        for (int[] pwd : candidates) {
            if (!satisfyAllClues(pwd)) continue;
            tries++;
            String pwdStr = "" + pwd[0] + pwd[1] + pwd[2];
            String hash = hashPassword(pwdStr);
            if (hash.equalsIgnoreCase(targetHash)) {
                result.append("匹配成功！密码为: ").append(pwdStr).append("\n");
                result.append("总尝试次数: ").append(tries).append("\n");
                return result.toString();
            }
        }
        return "未找到匹配密码！\n";
    }

    private static boolean satisfyAllClues(int[] pwd) {
        List<Integer> list = Arrays.asList(pwd[0], pwd[1], pwd[2]);
        for (int[] clue : clues) {
            if (!matchClue(list, clue)) return false;
        }
        return true;
    }

    private static boolean matchClue(List<Integer> pwd, int[] clue) {
        if (clue.length == 2) {
            int pos = clue[0] - 1;
            if (pos < 0 || pos > 2 || pwd.size() <= pos) return true;
            int val = pwd.get(pos);
            return clue[1] == 0 ? val % 2 == 0 : val % 2 == 1;
        }
        if (clue.length == 3) {
            for (int i = 0; i < 3; i++) {
                if (clue[i] != -1 && (pwd.size() <= i || pwd.get(i) != clue[i])) return false;
            }
            if (Arrays.equals(clue, new int[]{-1, -1, -1})) {
                Set<Integer> set = new HashSet<>(pwd);
                return set.size() == 3 && isPrime(pwd.get(0)) && isPrime(pwd.get(1)) && isPrime(pwd.get(2));
            }
        }
        return true;
    }

    private static boolean isPrime(int n) {
        return n == 2 || n == 3 || n == 5 || n == 7;
    }

    private static String hashPassword(String pwd) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] pwdBytes = pwd.getBytes(StandardCharsets.UTF_8);
        byte[] combined = new byte[FIXED_SALT.length + pwdBytes.length];
        System.arraycopy(FIXED_SALT, 0, combined, 0, FIXED_SALT.length);
        System.arraycopy(pwdBytes, 0, combined, FIXED_SALT.length, pwdBytes.length);
        byte[] hash = md.digest(combined);
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
