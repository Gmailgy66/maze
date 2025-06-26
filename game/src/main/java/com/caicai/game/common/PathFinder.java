package com.caicai.game.common;

import com.caicai.game.maze.Maze;
import com.caicai.game.maze.PointUtil;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.caicai.game.maze.BlockType.WALL;
import static com.caicai.game.maze.PointUtil.getDis;

@Component
public interface PathFinder {
    Point getNextPoint(Maze maze, Point curPos);

    default public List<Point> getBestWay(Maze maze, Point p1, Point p2) {
        return null;
    }

    default public List<Point> getBestWayByAStar(Maze maze, Point p1, Point p2) {
        // acctually there is only a way from the given point to the next
        // so here only need to get the distinct path as fast as possible
        Map<Point, Integer> cost = new HashMap<>();
        Map<Point, Point> par = new HashMap<>();
        Set<Point> vis = new HashSet<>();
        PriorityQueue<Point> openSet = new PriorityQueue<>((p1_, p2_) -> {
            return cost.get(p1_) + getDis(p1_, p2) > cost.get(p2_) + getDis(p2_, p2) ? 1 : -1;
        });
        cost.put(p1, 0);
        par.put(p1, p1);
        openSet.add(p1);
        List<Point> res = new ArrayList<>();
        while (openSet.isEmpty() == false) {
            Point cp = openSet.poll();
            // openSet.remove();
            vis.add(cp);
            List<Point> nxt = PointUtil.get4SurrendPoints(maze, cp);
            if (nxt.contains(p2)) {
                par.put(p2, cp);
                Point p = p2;
                res.add(p);
                while (par.get(p) != p1) {
                    p = par.get(p);
                    res.add(p);
                }
                res.add(p1);
                break;
            }
            nxt.stream()
               .filter(p -> vis.contains(p) == false && maze.getBlock(p) != WALL && openSet.contains(p) == false)
               .forEach(p -> {
                   cost.put(p, cost.get(cp) + 1);
                   par.put(p, cp);
                   openSet.add(p);
               });
        }
        return res.reversed();
    }

    default public int getDisByAStar(Maze maze, Point p1, Point p2) {
        // acctually there is only a way from the given point to the next
        // so here only need to get the distinct path as fast as possible
        Map<Point, Integer> cost = new HashMap<>();
        Map<Point, Point> par = new HashMap<>();
        Set<Point> vis = new HashSet<>();
        PriorityQueue<Point> openSet = new PriorityQueue<>((p1_, p2_) -> {
            return cost.get(p1_) + getDis(p1_, p2) > cost.get(p2_) + getDis(p2_, p2) ? 1 : -1;
        });
        cost.put(p1, 0);
        par.put(p1, p1);
        openSet.add(p1);
        List<Point> res = new ArrayList<>();
        while (openSet.isEmpty() == false) {
            Point cp = openSet.poll();
            // openSet.remove();
            vis.add(cp);
            List<Point> nxt = PointUtil.get4SurrendPoints(maze, cp);
            if (nxt.contains(p2)) {
                par.put(p2, cp);
                Point p = p2;
                res.add(p);
                while (par.get(p) != p1) {
                    p = par.get(p);
                    res.add(p);
                }
                res.add(p1);
                break;
            }
            nxt.stream()
               .filter(p -> vis.contains(p) == false && maze.getBlock(p) != WALL && openSet.contains(p) == false)
               .forEach(p -> {
                   cost.put(p, cost.get(cp) + 1);
                   par.put(p, cp);
                   openSet.add(p);
               });
        }
        return res.size();
    }

    //    public List getList() {
//    }
    default int h() {
        return 0;
    }
}

@Component
class Dp implements PathFinder {
    private List<Point> specialPoints;
    private int[][] costs;
    private int[] values;
    private int[] dp;
    private int[] parent;

    @Override
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
    }

    // 通过最小公共祖先算出任意两点之间的边权(n+*log2)
    // 整个迷宫是一颗树，起点作为根0
    // 预先计算出起点到每个金币，技能点之间的陷阱扣分数作为负边权(n*n^2)
    // 每个特殊点积分加成作为点权
    // (出口,boss可以视作点权为0的特殊点,直接置于点数组最后,可以直接得出到达boss处所可以获得的最大积分)
    // ! dp[i]为从起点开始到[i-1]特殊点可以获得的积分最大值，i-1个点必须被使用
    // dp[i] = max(...dp[j]+cost[i,j]+v[i-1]) // j < i (n*n)
    // run n times from start to every point to get the weight of the path
    public void buildGraph(Maze maze) {
        // Initialize special points list (gold + skills + exit + boss)
        specialPoints = new ArrayList<>();
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
        Map<Point, Integer> startDis = new HashMap<>();
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
        List<Point> path = new ArrayList<>();

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

@Component
class Greedy implements PathFinder {
    @Override
    public Point getNextPoint(Maze maze, Point curPos) {
        // List<Point> surrendPoints = PointUtil.getSurrendPoints(maze, curPos);
        // List<Point> surrendPoints = .toList();
        // find a Point that is closest to curPos
        // .filter(p -> maze.getGold().contains(p))
        Point best = maze.getGold().stream().max((p1, p2) -> {
            // the greedy here only calculate the distance to the current position but
            // ignored the wall on the way so it may be not precise
            double score = maze.getScore(p1);
            Double dis = PointUtil.getDis(p1, curPos);
            double score2 = maze.getScore(p2);
            Double dis2 = PointUtil.getDis(p2, curPos);
            return -Double.compare(dis, dis2);
        }).orElse(null);
        if (best == null) {
            // return maze.getBoss();
            return maze.getEXIT();
        }
        return best;
    }

    public Point nextTar = null;
    public Point getExit(Maze maze, Point point) {
        return null;
    }

}