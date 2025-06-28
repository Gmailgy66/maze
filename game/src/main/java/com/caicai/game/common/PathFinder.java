package com.caicai.game.common;

import com.caicai.game.maze.Maze;
import com.caicai.game.maze.PointUtil;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.caicai.game.maze.BlockType.WALL;
import static com.caicai.game.maze.PointUtil.getDis;

@Component
public interface PathFinder {

    default List<Point> solve(Maze maze) {
        return null;
    }

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


}

