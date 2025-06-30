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
    Map<Point, Integer> profitFromRoot = new HashMap<>();

    public List<Point> solve(Maze maze) {
        vis = new boolean[maze.getBoardSize()][maze.getBoardSize()];
        int profit = dfs(maze, maze.getSTART());
        buildRoad(maze, maze.getSTART());
        path.addAll(toExit.reversed());
        profit -= BlockType.FAKE_EXIT_SCORE;
        System.out.println("path is " + path);
        System.out.println("max profit is :" + profit);
        return path;
    }

    public boolean buildRoad(Maze maze, Point root) {
        if (root == null || !onTheRoad.containsKey(root)) {
            return false;
        }
        boolean res = false;
        if (res |= root.equals(maze.getEXIT())) {
            toExit.add(root);
        }
        Integer subPoints = onTheRoad.get(root);
        int x = root.getX();
        int y = root.getY();
        for (int i = 0; i < 4; i++) {
            if ((subPoints & (1 << i)) > 0) {
                path.add(root);
                int nx = x + mov[i].getX();
                int ny = y + mov[i].getY();
                if (res |= buildRoad(maze, new Point(nx, ny))) {
                    toExit.add(root);
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
        if (maze.getBlock(x, y) == BlockType.GOLD) {
            profit += maze.GOLD_SCORE;
        } else if (maze.getBlock(x, y) == BlockType.TRAP) {
            profit += maze.TRAP_SCORE;
        } else if (maze.getBlock(x, y) == BlockType.EXIT) {
            profit += BlockType.FAKE_EXIT_SCORE;
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
