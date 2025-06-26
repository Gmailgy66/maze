package com.caicai.game.common;

import java.util.Objects;

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Point other = (Point) obj;
        return this.x == other.x && this.y == other.y;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    @Override
    public int compareTo(Point o) {
        return this.getX() <= o.getY() && this.getY() < o.getY() ? -1 : 1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
        // Alternative implementations:
        // return 31 * x + y;
        // return x * 1000 + y; // if coordinates are small
    }

}
