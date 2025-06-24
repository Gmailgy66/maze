package com.caicai.game.maze;

import com.caicai.game.common.Point;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class Maze {

    private BlockType[][] board;
    private String title = "吴哥窟";
    private Set<Point> gold = new HashSet<>();
    // private Set<Point> lockers;
    private Set<Point> traps = new HashSet<>();
    private Set<Point> Paths = new HashSet<>();
    private Point START;
    private Point EXIT;
    private Point LOCKER;

    private int size;

    // final Point BOSS;
    Maze(int size) {
        this.size = size;
        this.board = new BlockType[size][size];
        // this.BOSS = new Point(size - 1, size - 1);
    }

    void buildExtraInfo() {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                switch (board[i][j]) {
                    case GOLD:
                        gold.add(new Point(i, j));
                        break;
                    case TRAP:
                        traps.add(new Point(i, j));
                        break;
                    case START:
                        START = new Point(i, j);
                        break;
                    case EXIT:
                        EXIT = new Point(i, j);
                        break;
                    case PATH:
                        Paths.add(new Point(i, j));
                        break;
                }
            }
        }
    }

    public void setBlock(Point point, BlockType blockType) {
        board[point.getX()][point.getY()] = blockType;
    }

    public void setBlock(int x, int y, BlockType blockType) {
        board[x][y] = blockType;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                sb.append(board[i][j].getSignal());
                // sb.append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public BlockType getBlock(int i, int j) {
        if (i < 0 || i >= size || j < 0 || j >= size) {
            return BlockType.WALL; // out of bounds
        } else {
            return board[i][j];
        }
    }

    public BlockType getBlock(Point point) {
        int i = point.getX();
        int j = point.getY();
        if (i < 0 || i >= size || j < 0 || j >= size) {
            return BlockType.WALL; // out of bounds
        } else {
            return board[i][j];
        }
    }
}
