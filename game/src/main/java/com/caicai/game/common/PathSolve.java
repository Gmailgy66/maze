package com.caicai.game.common;

import com.caicai.game.maze.BlockType;
import com.caicai.game.maze.Maze;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PathSolve implements PathFinder {
    //    xia you shang zuo
    boolean[][] vis;
    ArrayList<Point> path = new ArrayList<>();
    Point[] mov = new Point[]{new Point(1, 0), new Point(0, 1),
                              new Point(-1, 0), new Point(0, -1)};
    Map<Point, Point> par = new HashMap<>();
    Map<Point, Integer> onTheRoad = new HashMap<>();
    List<Point> toExit = new ArrayList<>();
    List<Point> toLocker = new ArrayList<>();
    List<Point> toBoss = new ArrayList<>();
    Map<Point, Integer> profitFromRoot = new HashMap<>();

    public List<Point> solve(Maze maze) {
        vis = new boolean[maze.getBoardSize()][maze.getBoardSize()];
        int profit = dfs(maze, maze.getSTART());
        buildRoad(maze, maze.getSTART());
        path.addAll(toBoss.reversed());
        path.addAll(toBoss);
        path.addAll(toLocker.reversed());
        path.addAll(toLocker);
        path.addAll(toExit.reversed());
        profit -= BlockType.FAKE_EXIT_SCORE;
        System.out.println("path is " + path);
        System.out.println("max profit is :" + profit);
        return path;
    }

    public int buildRoad(Maze maze, Point root) {
//        0000
//        0001 is EXIT
//        0010 is LOCKER
//        0100 is BOSS
//
        if (root == null || !onTheRoad.containsKey(root)) {
            return 0;
        }
        int res = 0;
        if (root.equals(maze.getEXIT())) {
            res |= 1; // EXIT
        } else if (root.equals(maze.getLOCKER())) {
            res |= 2; // LOCKER
        } else if (root.equals(maze.getBossPoint())) {
            res |= 4; // BOSS
        }
        Integer subPoints = onTheRoad.get(root);
        int x = root.getX();
        int y = root.getY();
        for (int i = 0; i < 4; i++) {
            if ((subPoints & (1 << i)) > 0) {
                path.add(root);
                int nx = x + mov[i].getX();
                int ny = y + mov[i].getY();
                int subRes= buildRoad(maze, new Point(nx, ny));
                res|=subRes;
                if ((res & 1) != 0) {
                    toExit.add(root);
                }
                if ((res & 2) != 0) {
                    toLocker.add(root);
                }
                if ((res & 4) != 0) {
                    toBoss.add(root);
                }
                path.add(root);
            }
        }
        if (!root.equals(path.getLast())) {
            path.add(root);
        }
        return res;
    }

    public int dfs(Maze maze, Point now) {
//        ! actually the same state will not be visited again
//        if(isInPath.containsKey(now) && IsInPath.get(now)) {
//        return
//    }
        int x = now.getX();
        int y = now.getY();
//        ! the root is not marked as visited so it will cause a extra visit
        vis[x][y] = true;
        List<Point> vList = new ArrayList();
        int profit = 0;
        int subPoints = 0000;
        BlockType type = maze.getBlock(x, y);
        if (type == BlockType.GOLD) {
            profit += Maze.GOLD_SCORE;
        } else if (type == BlockType.TRAP) {
            profit += maze.TRAP_SCORE;
        } else if (type == BlockType.EXIT) {
            profit += BlockType.FAKE_EXIT_SCORE;
        } else if (type == BlockType.LOCKER) {
            profit += BlockType.FAKE_LOCKER_SCORE;
        } else if (type == BlockType.BOSS) {
            profit += BlockType.FAKE_BOSS_SCORE;
        }
        for (int i = 0; i < 4; i++) {
            int nx = x + mov[i].getX();
            int ny = y + mov[i].getY();
            if (nx >= 0 && nx < maze.getBoardSize() && ny >= 0 && ny < maze.getBoardSize() && maze.getBlock(
                    nx, ny) != BlockType.WALL && !vis[nx][ny]) {
                vis[nx][ny] = true;
                int subPro = dfs(maze, new Point(nx, ny));
                if (subPro > 0) {
                    subPoints |= 1 << i;
                    profit += subPro;
                }
            }
        }
        if (profit > 0) {
            onTheRoad.put(now, subPoints);
        }
        return profit;
    }


}
