package com.caicai.game.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.springframework.stereotype.Component;

import static com.caicai.game.maze.BlockType.WALL;
import com.caicai.game.maze.Maze;
import com.caicai.game.maze.PointUtil;
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

    default int h() {
        return 0;
    }
}

@Component
class Dp implements PathFinder {
    @Override
    public Point getNextPoint(Maze maze, Point curPos) {
        List<Point> surrendPoints = PointUtil.getSurrendPoints(maze, curPos);
        return null;

        // surrendPoints.forEach();
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
//            the greedy here only calculate the distance to the current position but ignored the wall on the way so it may be not precise

            double score = maze.getScore(p1);
            Double dis = PointUtil.getDis(p1, curPos);
            double score2 = maze.getScore(p2);
            Double dis2 = PointUtil.getDis(p2, curPos);
            return -Double.compare(dis, dis2);
        }).orElse(null);
        maze.getGold().remove(best);
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