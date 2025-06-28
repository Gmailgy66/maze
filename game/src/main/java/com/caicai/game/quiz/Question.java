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

    public HashMap<String, Object> start() throws Exception {
        PasswordSolver ps = new PasswordSolver();
        HashMap<String, Object> map = ps.start();
        return map;
    }
}
