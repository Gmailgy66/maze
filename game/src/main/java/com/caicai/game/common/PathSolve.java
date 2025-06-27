package com.caicai.game.common;

import com.caicai.game.maze.BlockType;
import com.caicai.game.maze.Maze;
import lombok.Data;
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

    public List<Point> solve(Maze maze) {
        vis = new boolean[maze.getBoardSize()][maze.getBoardSize()];
        int profit = dfs(maze, maze.getSTART());
        buildRoad(maze, maze.getSTART());
        System.out.println("path is " + path);
        System.out.println("max profit is :" + profit);
        return path;
//        return path;
    }

    public void buildRoad(Maze maze, Point root) {
        if (root == null || !onTheRoad.containsKey(root)) {
            return;
        }
        Integer subPoints = onTheRoad.get(root);
        int x = root.getX();
        int y = root.getY();
        for (int i = 0; i < 4; i++) {
            if ((subPoints & (1 << i)) > 0) {
                path.add(root);
                int nx = x + mov[i].getX();
                int ny = y + mov[i].getY();
                buildRoad(maze, new Point(nx, ny));
                path.add(root);
            }
        }
        if (!root.equals(path.getLast())) {
            path.add(root);
        }
    }

    public int dfs(Maze maze, Point now) {
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
        }
        for (int i = 0; i < 4; i++) {
            int nx = x + mov[i].getX();
            int ny = y + mov[i].getY();
            if (nx >= 0 && nx < maze.getBoardSize() && ny >= 0 && ny < maze.getBoardSize() && maze.getBlock(nx, ny) != BlockType.WALL && !vis[nx][ny]) {
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

    @Data
    static class Ret {
        Ret(List<Point> path, Integer pro) {
            this.path = path;
            this.pro = pro;
        }

        List<Point> path;
        Integer pro;
    }
}
