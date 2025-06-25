package com.caicai.game.quiz;
import java.util.*;

public class Question {
    boolean ok;
    public Question(){
        ok = false;
    }

    public void solve(int[] a, ArrayList<Integer> ans, int n, boolean[] used) {
        if (n == 3) {
            if (ans.get(0) == a[0] && ans.get(1) == a[1] && ans.get(2) == a[2]) {
                ok = true;
            }
            return;
        }
        for (int i = 0; i < 10; i++) {
            if (used[i]) {
                continue;
            }
            used[i] = true;
            ans.add(i);
            solve(a, ans, n + 1, used);
            if (ok)
                return;
            ans.removeLast();
            used[i] = false;
        }
    }

    public void start() {
        // 随机生成三位数
        int[] a = new int[3];
        Set<Integer> nums = new HashSet<>();
        Random random = new Random();
        while (nums.size() < 3) {
            int num = random.nextInt(10);
            nums.add(num);
        }
        int i = 0;
        for (Integer x : nums) {
            a[i++] = x;
        }
        boolean[] used = new boolean[10];
        String question = """
                问题：
                有一个三位数互不重复的排列，请你使用回溯法找出这个三位数，将答案填写在下方的方框中
                """;
        System.out.println(question);
        ArrayList<Integer> ans = new ArrayList<>();
        solve(a, ans, 0, used);
        System.out.println(ans);
    }
}
