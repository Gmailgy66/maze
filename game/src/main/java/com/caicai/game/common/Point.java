package com.caicai.game.common;

import lombok.Data;

@Data
public class Point implements Comparable<Point> {
    private int x;
    private int y;

    public static Point randPoint(int maxX, int maxY) {
        return new Point((int) (Math.random() * maxX), (int) (Math.random() * maxY));
    }
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point() {
        this(0, 0);
    }

    public boolean equals(Point other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        return this.x == other.x && this.y == other.y;
    }

    @Override
    public String toString() {
        return "Point{" + "x=" + x + ", y=" + y + '}';
    }

    @Override
    public int compareTo(Point o) {
        return this.getX() <= o.getY() && this.getY() < o.getY() ? -1 : 1;
    }
}
