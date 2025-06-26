package com.caicai.game.common;

import com.caicai.game.maze.BlockType;
import com.caicai.game.maze.Maze;

import java.util.ArrayList;

public class PathSolve {
    boolean[][]vis;
    ArrayList<Point> path = new ArrayList<>();
    Point[] mov = new Point[]{new Point(1, 0), new Point(0, 1), new Point(-1, 0), new Point(0, -1)};

    public ArrayList<Point> solve(Maze maze) {
        vis = new boolean[maze.getBoardSize()][maze.getBoardSize()];
        dfs(maze, maze.getSTART(), 0);
        return path;
    }

    public void dfs(Maze maze, Point now, int profit) {
        int x = now.getX();
        int y = now.getY();
        for (int i = 0; i < 4; i++) {
            int nx = x + mov[i].getX();
            int ny = y + mov[i].getY();
            if (nx >= 0 && nx < maze.getBoardSize() && ny >= 0 && ny < maze.getBoardSize() && maze.getBlock(nx, ny) != BlockType.WALL && !vis[nx][ny]) {
                vis[nx][ny] = true;
                if (maze.getBlock(nx, ny) == BlockType.GOLD) {
                    profit += maze.GOLD_SCORE;
                } else if (maze.getBlock(nx, ny) == BlockType.TRAP) {
                    profit += maze.TRAP_SCORE;
                }
                dfs(maze, new Point(nx, ny), profit);
                if (profit >= 0) {
                    path.add(new Point(nx, ny));
                }
            }
        }
    }
}
