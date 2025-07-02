package com.caicai.game.quiz;
import java.io.File;
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
//    public HashMap<String,Object> start() throws Exception{
//        PasswordSolver ps = new PasswordSolver();
//        File folder = new File("game/data/password");
//        if(!folder.exists() || !folder.isDirectory()){
//            throw new RuntimeException("文件夹不存在");
//        }
//        File[] jsonFiles = folder.listFiles((dir,name) -> name.endsWith(".json"));
//        if(jsonFiles == null || jsonFiles.length == 0){
//            throw new RuntimeException("找不到json文件");
//        }
//        int totalTries = 0;
//        StringBuilder log = new StringBuilder();
//        for(File file : jsonFiles){
//            int tries = ps.solveFromFile(file);
//            totalTries += tries;
//            log.append(file.getName()).append(" ").append("尝试次数: ").append(tries).append("\n");
//        }
//        log.append("总尝试次数：").append(totalTries).append("\n");
//        HashMap<String,Object> map = new HashMap<>();
//        map.put("result",log.toString());
//        map.put("totalTries",totalTries);
//        return map;
//    }
}
