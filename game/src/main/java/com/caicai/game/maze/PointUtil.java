package com.caicai.game.maze;

import com.caicai.game.common.Point;

import java.util.List;

/**
 * 坐标点工具类
 * 提供处理迷宫中坐标点的各种实用方法
 */
public class PointUtil {
    /** 8个方向的X坐标偏移量（上、下、左、右、右上、左下、右下、左上） */
    static final int[] x_ = new int[]{0, 0, 1, -1, 1, -1, 1, -1};
    /** 8个方向的Y坐标偏移量（上、下、左、右、右上、左下、右下、左上） */
    static final int[] y_ = new int[]{-1, 1, 0, 0, 1, -1, -1, 1};

    /**
     * 获取一个点周围8个方向的相邻点
     * @param maze 迷宫对象，用于检查边界
     * @param point 中心点
     * @return 周围有效的点列表
     */
    public static List<Point> getSurrendPoints(Maze maze, Point point) {
        List<Point> points = new java.util.ArrayList<>();
        for (int i = 0; i < x_.length; i++) {
            int x = point.getX() + x_[i];
            int y = point.getY() + y_[i];
            // 检查坐标是否在迷宫范围内
            if (x >= 0 && x < maze.getValidSize() && y >= 0 && y < maze.getValidSize()) {
                points.add(new Point(x, y));
            }
        }
        return points;
    }

    /**
     * 获取一个点周围4个方向（上下左右）的相邻点
     * 只返回迷宫边界内且不是墙的点
     * @param maze 迷宫对象，用于检查边界和方块类型
     * @param point 中心点
     * @return 周围有效的点列表
     */
    public static List<Point> get4SurrendPoints(Maze maze, Point point) {
        List<Point> points = new java.util.ArrayList<>();
        // 只遍历前4个方向：上下左右
        for (int i = 0; i < 4; i++) {
            int x = point.getX() + x_[i];
            int y = point.getY() + y_[i];
            // 检查坐标是否在迷宫有效范围内且不是墙
            if (x >= 1 && x <= maze.getValidSize() && y >= 1 && y <= maze.getValidSize() && maze.getBlock(x, y) != BlockType.WALL) {
                points.add(new Point(x, y));
            }
        }
        return points;
    }

    /**
     * 计算两点之间的曼哈顿距离
     * 曼哈顿距离 = |x1-x2| + |y1-y2|
     * @param p1 第一个点
     * @param p2 第二个点
     * @return 两点之间的曼哈顿距离
     */
    static public double getDis(Point p1, Point p2) {
        return Math.abs(p1.getX() - p2.getX()) + Math.abs(p1.getY() - p2.getY());
    }

    /**
     * 复制一个点的坐标到另一个点
     * @param from 源点
     * @param to 目标点
     */
    public static void copy(Point from, Point to) {
        if (from == null || to == null) {
            return;
        }
        to.setX(from.getX());
        to.setY(from.getY());
    }
}
