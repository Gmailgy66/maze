package com.caicai.game.common.pathFinderImpl;

import com.caicai.game.common.Point;
import com.caicai.game.maze.BlockType;
import com.caicai.game.maze.Maze;
import com.caicai.game.maze.PointUtil;

import java.util.*;

public class GreedyPathFinder {
    public Point nextTar = null;

    public GreedyPathFinder() {
    }

    // this method should ensure find the exit
    // during the path it should try to get more Gold
    Maze maze;
    boolean[][] vis;
    Set<Point> triggered = new HashSet<>();
    List<Point> path = new ArrayList<>();
    int profit = 0;

    public boolean dfs(Point root, Point target) {
        if (root == null) {
            return false;
        }
        path.add(root);
        if (target.equals(root)) {
            return true;
        }
        if (!triggered.contains(root)) {
            profit += maze.getScore(root);
            triggered.add(root);
        }
        List<Point> points = PointUtil.get4SurrendPoints(maze, root);
        vis[root.getX()][root.getY()] = true;
        Collections.sort(points, (p1, p2) -> {
            int order = BlockType.getOrder(maze.getBlock(p1));
            double score2 = maze.getScore(p2);
            int order2 = BlockType.getOrder(maze.getBlock(p2));
            if (triggered.contains(p1)) {
                order = 0;
            }
//            avoid be attracted 2 times by the used gold
            if (triggered.contains(p2)) {
                order2 = 0;
            }
            return Integer.compare(order, order);
        });
        for (Point point : points) {
            // ! if the hero already get the target pos then stop the search
            if (vis[point.getX()][point.getY()] == false) {
                if (dfs(point, target)) {
                    return true;
                }
                if (root.equals(path.getLast()) == false) {
                    path.add(root);
                }
            }
        }
            return false;
    }

    public List<Point> solve(Maze maze) {
        this.maze = maze;
        int boardSize = maze.getBoardSize();
        vis = new boolean[boardSize][boardSize];
        Point curPos = maze.getSTART();
        if (maze.getBossPoint() != null) {
            dfs(maze.getSTART(), maze.getBossPoint());
            curPos = maze.getBossPoint();
        }
        // fill vis with false
        vis = new boolean[boardSize][boardSize];
        dfs(curPos, maze.getEXIT());
        System.out.println("path: " + path);
        System.out.println("profit: " + profit);
        return path;
    }
}