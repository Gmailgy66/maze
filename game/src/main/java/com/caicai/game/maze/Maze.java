package com.caicai.game.maze;

import com.caicai.game.utils.Point;
import lombok.Data;

import java.util.Set;

@Data
public class Maze {

    BlockType[][] board;
    String title = "吴哥窟";
    Set<Point> gold;
    Set<Point> lockers;
    Set<Point> traps;
    int size;

    // final Point BOSS;
    Maze(int size) {
        this.size = size;
        this.board = new BlockType[size][size];
        // this.BOSS = new Point(size - 1, size - 1);
    }

    public void setBlock(Point point, BlockType blockType) {
        board[point.getX()][point.getY()] = blockType;
    }

    public void setBlock(int x, int y, BlockType blockType) {
        board[x][y] = blockType;
    }

    public void toString(StringBuilder sb) {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                sb.append(board[i][j].getSignal());
            }
            sb.append("\n");
        }
    }
}
