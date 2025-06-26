package com.caicai.game.maze;

import java.util.List;

import com.caicai.game.common.Point;

public class PointUtil {
    static final int[] x_ = new int[] { 0, 0, 1, -1, 1, -1, 1, -1 };
    static final int[] y_ = new int[] { -1, 1, 0, 0, 1, -1, -1, 1 };

    public static List<Point> getSurrendPoints(Maze maze, Point point) {
        List<Point> points = new java.util.ArrayList<>();
        for (int i = 0; i < x_.length; i++) {
            int x = point.getX() + x_[i];
            int y = point.getY() + y_[i];
            if (x >= 0 && x < maze.getValidSize() && y >= 0 && y < maze.getValidSize()) {
                points.add(new Point(x, y));
            }
        }
        return points;
    }

    public static List<Point> get4SurrendPoints(Maze maze, Point point) {
        List<Point> points = new java.util.ArrayList<>();
        for (int i = 0; i < 4; i++) {
            int x = point.getX() + x_[i];
            int y = point.getY() + y_[i];
            if (x >= 1 && x <= maze.getValidSize() && y >= 1 && y <= maze.getValidSize()) {
                points.add(new Point(x, y));
            }
        }
        return points;
    }

    // manhattan distance
    static public double getDis(Point p1, Point p2) {
        return Math.abs(p1.getX() - p2.getX()) + Math.abs(p1.getY() - p2.getY());
    }

    public static void copy(Point from, Point to) {
        if (from == null || to == null) {
            return;
        }
        to.setX(from.getX());
        to.setY(from.getY());
    }
}
