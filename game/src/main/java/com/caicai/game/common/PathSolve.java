package com.caicai.game.common;

import com.caicai.game.maze.BlockType;
import com.caicai.game.maze.Maze;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PathSolve {
    boolean[][] vis;
    ArrayList<Point> path = new ArrayList<>();
    Point[] mov = new Point[]{new Point(1, 0), new Point(0, 1),
                              new Point(-1, 0), new Point(0, -1)};
    Map<Point, Point> par = new HashMap<>();

    public List<Point> solve(Maze maze) {
        vis = new boolean[maze.getBoardSize()][maze.getBoardSize()];
        Ret dfs = dfs(maze, maze.getSTART(), 0);
        return dfs.path;
//        return path;
    }

    public Ret dfs(Maze maze, Point now, Integer profit) {
        int x = now.getX();
        int y = now.getY();
        List<Point> vList = new ArrayList();
        for (int i = 0; i < 4; i++) {
            int nx = x + mov[i].getX();
            int ny = y + mov[i].getY();
            if (nx >= 0 && nx < maze.getBoardSize() && ny >= 0 && ny < maze.getBoardSize() && maze.getBlock(nx, ny) != BlockType.WALL && !vis[nx][ny]) {
                vis[nx][ny] = true;
                if (maze.getBlock(x, y) == BlockType.GOLD) {
                    profit += maze.GOLD_SCORE;
                } else if (maze.getBlock(x, y) == BlockType.TRAP) {
                    profit += maze.TRAP_SCORE;
                }
                Integer subProfit = 0;
                var res = dfs(maze, new Point(nx, ny), subProfit);
                if (res != null) {
                    subProfit += res.pro;
                    profit += subProfit;
                    vList.addAll(res.getPath());
                    vList.add(now);
                }

                if (profit > 0) {
                    path.add(new Point(nx, ny));
                }
            }
        }
        if (profit <= 0) {
            return null;
        }
        if (vList.isEmpty()) {
            vList.add(now);
        }
        return new Ret(vList, profit);
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
