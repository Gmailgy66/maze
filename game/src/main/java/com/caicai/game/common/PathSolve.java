package com.caicai.game.common;

import com.caicai.game.maze.BlockType;
import com.caicai.game.maze.Maze;
import org.springframework.stereotype.Component;

import java.util.*;

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
    long dp[][];

    public List<Point> solve(Maze maze) {
        int boardSize = maze.getBoardSize();
        vis = new boolean[boardSize][boardSize];
        dp = new long[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++) {
            Arrays.fill(dp[i], Integer.MIN_VALUE);
        }
        long profit = byDp(maze, maze.getSTART());

        buildRoad(maze, maze.getSTART());
        toBoss = getBestWayByAStar(maze, maze.getSTART(), maze.getBossPoint());
        toLocker = getBestWayByAStar(maze, maze.getBossPoint(),
                                     maze.getLOCKER());
        toExit = getBestWayByAStar(maze, maze.getLOCKER(), maze.getEXIT());
        path.addAll(toBoss);
        path.addAll(toLocker);
        path.addAll(toExit);
        profit = dp[maze.getSTART().getX()][maze.getSTART().getY()];
        profit -= BlockType.FAKE_EXIT_SCORE + BlockType.FAKE_LOCKER_SCORE + BlockType.FAKE_BOSS_SCORE;
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
                int subRes = buildRoad(maze, new Point(nx, ny));
                res |= subRes;
                path.add(root);
            }
        }
        if ((res & 1) != 0) {
            toExit.add(root);
        }
        if ((res & 2) != 0) {
            toLocker.add(root);
        }
        if ((res & 4) != 0) {
            toBoss.add(root);
        }
        if (!root.equals(path.getLast())) {
            path.add(root);
        }
        return res;
    }

    //    ( 6,13)
    public int dfs(Maze maze, Point now) {
//        ! actually the same state will not be visited again
        int x = now.getX();
        int y = now.getY();
//        ! the root is not marked as visited so it will cause a extra visit
        vis[x][y] = true;
        int profit = getProfit(maze, maze.getBlock(x, y));
        int subPoints = 0000;
        BlockType type = maze.getBlock(x, y);
        profit = getProfit(maze, type);
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

    private static int getProfit(Maze maze, BlockType type) {
        if (type == BlockType.GOLD) {
            return Maze.GOLD_SCORE;
        } else if (type == BlockType.TRAP) {
            return maze.TRAP_SCORE;
        } else if (type == BlockType.EXIT) {
            return BlockType.FAKE_EXIT_SCORE;
        } else if (type == BlockType.LOCKER) {
            return BlockType.FAKE_LOCKER_SCORE;
        } else if (type == BlockType.BOSS) {
            return BlockType.FAKE_BOSS_SCORE;
        }
        return 0;
    }

    public long byDp(Maze maze, Point now) {
//        ! actually the same state will not be visited again
        int x = now.getX();
        int y = now.getY();
//        ! the root is not marked as visited so it will cause a extra visit
        if (dp[x][y] != Integer.MIN_VALUE) {
            return dp[x][y];
        }
        vis[x][y] = true;
        long subPro = 0;
        dp[x][y] = getProfit(maze, maze.getBlock(x, y));
        int tar = 0;
        for (int status = 0; status < 1 << 4; status++) {
            long tmp = 0;
            for (int j = 0; j < 4; j++) {
                if ((status & (1 << j)) > 0) {
                    int nx = x + mov[j].getX();
                    int ny = y + mov[j].getY();
                    if (nx >= 0 && nx < maze.getBoardSize() && ny >= 0 && ny < maze.getBoardSize() && maze.getBlock(
                            nx, ny) != BlockType.WALL && !vis[nx][ny]) {
                        vis[nx][ny] = true;
                        tmp += byDp(maze, new Point(nx, ny));
                        vis[nx][ny] = false;
                    }
                }
                if (subPro < tmp && tmp > 0) {
                    subPro = tmp;
                    tar = status;
                }
            }
        }
        dp[x][y] = Math.max(0, subPro) + dp[x][y];
        if (dp[x][y] > 0) {
            onTheRoad.put(now, tar);
        }
        return dp[x][y];
    }
}
