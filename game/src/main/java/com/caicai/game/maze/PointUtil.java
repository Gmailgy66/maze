package com.caicai.game.maze;

import com.caicai.game.common.Point;

import java.util.List;

public class PointUtil {
    static final int[] x_ = new int[]{0, 0, 1, -1, 1, -1, 1, -1};
    static final int[] y_ = new int[]{-1, 1, 0, 0, 1, -1, -1, 1};
    public static  List<Point> getSurrendPoints(Maze maze, Point point) {
        List<Point> points = new java.util.ArrayList<>();
        for (int i = 0; i < x_.length; i++) {
            int x = point.getX() + x_[i];
            int y = point.getY() + y_[i];
            if (x >= 0 && x < maze.getSize() && y >= 0 && y < maze.getSize()) {
                points.add(new Point(x, y));
            }
        }
        return points;
    }
}
