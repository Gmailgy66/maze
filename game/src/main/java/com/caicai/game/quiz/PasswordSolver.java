package com.caicai.game.quiz;

// 在 PasswordSolver.java 中添加以下完整逻辑
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

    public HashMap<String,Object> start() throws Exception {
        HashMap<String,Object> map = new HashMap<>();
        // 读取 JSON 文件
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = PasswordSolver.class.getClassLoader().getResourceAsStream("password_data.json");
        if (is == null) {
            throw new RuntimeException("无法找到资源文件 password_data.json");
        }
        JsonNode root = mapper.readTree(is);
//        JsonNode root = mapper.readTree(new File("password_data.json"));
        targetHash = root.get("L").asText();
        StringBuilder qs = new StringBuilder();
        qs.append("哈希：\n").append(targetHash).append("\n");
        qs.append("线索：\n");
        // 读取线索
        clues = new ArrayList<>();
        for (JsonNode clue : root.get("C")) {
            int[] c = new int[clue.size()];
            for (int i = 0; i < clue.size(); i++) {
                c[i] = clue.get(i).asInt();
            }
            clues.add(c);
            qs.append(Arrays.toString(c)).append("\n");
        }

        map.put("question", qs.toString());

        StringBuilder ans = new StringBuilder();

        // 执行三种方法
        ans.append("=== Method 1: 从000顺序尝试 ===\n");
        ans.append(runMethod(generateRange(0, 999, 1)));

        ans.append("=== Method 2: 从999倒序尝试 ===\n");
        ans.append(runMethod(generateRange(999, 0, -1)));

        ans.append("=== Method 3: 优先满足线索排序 ===\n");
        ans.append(runMethod(generateCluePriority()));

        map.put("ans",ans.toString());
        return map;
    }

    // 通用尝试入口
    public static String runMethod(List<int[]> candidates) throws Exception {
        int tries = 0;
        StringBuilder res = new StringBuilder();
        for (int[] pwd : candidates) {
            if (!satisfyAllClues(pwd)) continue;
            tries++;

            String pwdStr = "" + pwd[0] + pwd[1] + pwd[2];
            String hash = hashPassword(pwdStr);
            if (hash.equalsIgnoreCase(targetHash)) {
                res.append("匹配成功！密码为: ").append(pwdStr).append("\n");
                res.append("总尝试次数: ").append(tries).append("\n");
            }
        }
        res.append("未找到匹配密码！\n");
        return res.toString();
    }

    // 方法1和2：数字范围生成器
    public static List<int[]> generateRange(int start, int end, int step) {
        List<int[]> list = new ArrayList<>();
        for (int i = start; step > 0 ? i <= end : i >= end; i += step) {
            int[] pwd = new int[]{i / 100 % 10, i / 10 % 10, i % 10};
            list.add(pwd);
        }
        return list;
    }

    // 方法3：优先满足线索多的组合
    public static List<int[]> generateCluePriority() {
        List<int[]> list = new ArrayList<>();
        for (int i = 0; i <= 9; i++)
            for (int j = 0; j <= 9; j++)
                for (int k = 0; k <= 9; k++) {
                    int[] pwd = {i, j, k};
                    list.add(pwd);
                }

        list.sort((a, b) -> Integer.compare(countClueMatches(b), countClueMatches(a)));
        return list;
    }

    public static int countClueMatches(int[] pwd) {
        int count = 0;
        for (int[] clue : clues) {
            if (matchClue(Arrays.asList(pwd[0], pwd[1], pwd[2]), clue)) count++;
        }
        return count;
    }

    // 判断所有线索是否满足
    private static boolean satisfyAllClues(int[] pwd) {
        List<Integer> list = Arrays.asList(pwd[0], pwd[1], pwd[2]);
        for (int[] clue : clues) {
            if (!matchClue(list, clue)) return false;
        }
        return true;
    }

    // 判断单个线索
    private static boolean matchClue(List<Integer> pwd, int[] clue) {
        if (clue.length == 2) {
            int pos = clue[0] - 1;
            if (pos < 0 || pos > 2 || pwd.size() <= pos) return true;
            int val = pwd.get(pos);
            return clue[1] == 0 ? val % 2 == 0 : val % 2 == 1;
        }

        if (clue.length == 3) {
            for (int i = 0; i < 3; i++) {
                if (clue[i] != -1) {
                    if (pwd.size() <= i || pwd.get(i) != clue[i]) return false;
                }
            }

            if (Arrays.equals(clue, new int[]{-1, -1, -1})) {
                if (pwd.size() < 3) return true;
                Set<Integer> set = new HashSet<>(pwd);
                return set.size() == 3 && isPrime(pwd.get(0)) && isPrime(pwd.get(1)) && isPrime(pwd.get(2));
            }
        }

        return true;
    }

    private static boolean isPrime(int n) {
        return n == 2 || n == 3 || n == 5 || n == 7;
    }

    // 哈希函数（带盐）
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
