package com.caicai.game.common.pathFinderImpl;

import com.caicai.game.common.PathFinder;
import com.caicai.game.common.Point;
import com.caicai.game.maze.Maze;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DpPathFinder implements PathFinder {
    private List<Point> specialPoints;
    private int[][] costs;
    private int[] values;
    private int[] dp;
    private int[] parent;

    public DpPathFinder() {
    }

    public Point getNextPoint(Maze maze, Point curPos) {
        buildGraph(maze);

        // Find the optimal path and return the first point to visit
        List<Point> optimalPath = getOptimalPath(maze);

        if (optimalPath.isEmpty()) {
            return maze.getEXIT();
        }

        // Return the first unvisited special point
        for (Point p : optimalPath) {
            if (!p.equals(curPos) && maze.getGold().contains(p)) {
                return p;
            }
        }

        return maze.getEXIT();
    }// 通过最小公共祖先算出任意两点之间的边权(n+*log2)

    // 整个迷宫是一颗树，起点作为根0
    // 预先计算出起点到每个金币，技能点之间的陷阱扣分数作为负边权(n*n^2)
    // 每个特殊点积分加成作为点权
    // (出口,boss可以视作点权为0的特殊点,直接置于点数组最后,可以直接得出到达boss处所可以获得的最大积分)
    // ! dp[i]为从起点开始到[i-1]特殊点可以获得的积分最大值，i-1个点必须被使用
//!    问题在于没有确定前驱状态,需要先进行拓扑排序确定前驱
    // dp[i] = max(...dp[j]+cost[i,j]+v[i-1]) // j < i (n*n)
    // run n times from start to every point to get the weight of the path
    public void buildGraph(Maze maze) {
        // Initialize special points list (gold + skills + exit + boss)
        specialPoints = new ArrayList<Point>();
        specialPoints.addAll(maze.getGold());
        if (maze.getBoss() != null) {
            specialPoints.add(maze.getBossPoint());
        }
        specialPoints.add(maze.getEXIT());

        int n = specialPoints.size();
        costs = new int[n][n];
        values = new int[n];
        dp = new int[n + 1];
        parent = new int[n + 1];

        // Calculate values for each special point
        for (int i = 0; i < n; i++) {
            Point p = specialPoints.get(i);
            if (maze.getGold().contains(p)) {
                values[i] = maze.getScore(p);
            } else {
                values[i] = 0; // boss and exit have 0 value
            }
        }

        // Calculate costs between all pairs of special points
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    costs[i][j] = 0;
                } else {
                    costs[i][j] = getDisByAStar(maze, specialPoints.get(i), specialPoints.get(j));
                }
            }
        }

        // Calculate distances from start to each special point
        Map<Point, Integer> startDis = new HashMap<Point, Integer>();
        for (Point p : specialPoints) {
            startDis.put(p, getDisByAStar(maze, maze.getSTART(), p));
        }

        // DP calculation
        // dp[i] = maximum score achievable visiting exactly i special points
        dp[0] = 0;
        parent[0] = -1;

        for (int i = 1; i <= n; i++) {
            dp[i] = Integer.MIN_VALUE;
            for (int j = 0; j < n; j++) {
                Point currentPoint = specialPoints.get(j);
                int currentValue = values[j];
                int pathCost;

                if (i == 1) {
                    // First point from start
                    pathCost = startDis.get(currentPoint);
                } else {
                    // Find best previous state
                    int bestPrevScore = Integer.MIN_VALUE;
                    for (int k = 0; k < n; k++) {
                        if (k != j) {
                            int prevScore = dp[i - 1] - values[k] + costs[k][j];
                            if (prevScore > bestPrevScore) {
                                bestPrevScore = prevScore;
                            }
                        }
                    }
                    pathCost = bestPrevScore;
                }

                int totalScore = pathCost + currentValue;
                if (totalScore > dp[i]) {
                    dp[i] = totalScore;
                    parent[i] = j;
                }
            }
        }
        return;
    }

    private List<Point> getOptimalPath(Maze maze) {
        List<Point> path = new ArrayList<Point>();

        // Find the best ending state
        int bestScore = Integer.MIN_VALUE;
        int bestCount = 0;

        for (int i = 1; i <= specialPoints.size(); i++) {
            if (dp[i] > bestScore) {
                bestScore = dp[i];
                bestCount = i;
            }
        }

        // Reconstruct path (simplified version)
        // In a full implementation, you'd need to track the actual sequence
        // For now, return points sorted by value/distance ratio
        return specialPoints.stream()
                            .filter(p -> maze.getGold().contains(p))
                            .sorted((p1, p2) -> {
                                double ratio1 = (double) maze.getScore(p1) / getDisByAStar(maze, maze.getSTART(), p1);
                                double ratio2 = (double) maze.getScore(p2) / getDisByAStar(maze, maze.getSTART(), p2);
                                return Double.compare(ratio2, ratio1);
                            })
                            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
}