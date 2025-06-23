package com.caicai.game.utils;

import lombok.Data;

@Data
public class Point {
    private int x;
    private int y;

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
}
