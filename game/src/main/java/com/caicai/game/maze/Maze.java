package com.caicai.game.maze;

import com.caicai.game.common.Point;
import com.caicai.game.role.Boss;
import com.caicai.game.role.Skill;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static com.caicai.game.maze.BlockType.PATH;

@Slf4j
@Data
public class Maze {
    private Boss boss;
    private BlockType[][] board;
    private boolean[][] vis;
    private String title = "吴哥窟";
    private Set<Point> gold = new HashSet<>();
    // private Set<Point> lockers;
    private Set<Point> traps = new HashSet<>();
    private Set<Point> Paths = new HashSet<>();
    Map<Point, Skill> skillMap = new HashMap<>();
    private Point START;
    private Point EXIT;
    private Point LOCKER;
    private int validSize;
    private int boardSize;
    private int[][] scores;

    // final Point BOSS;
    Maze(int size) {
        this.validSize = size;
        this.boardSize = size + 2; // +2 for walls
        this.board = new BlockType[boardSize][boardSize];
        Arrays.stream(board).forEach(row -> Arrays.fill(row, BlockType.PATH));
        for (int i = 0; i < boardSize; i++) {
            board[i][0] = BlockType.WALL;
            board[i][this.boardSize - 1] = BlockType.WALL;
            board[0][i] = BlockType.WALL;
            board[this.boardSize - 1][i] = BlockType.WALL;
        }
        this.vis = new boolean[size][size];
    }

    void setScores(int[][] scores) {
        this.scores = scores;
    }

    void buildExtraInfo() {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                final Point np = new Point(i, j);
                switch (board[i][j]) {
                    case GOLD -> gold.add(np);
                    case TRAP -> traps.add(np);
                    case START -> START = np;
                    case EXIT -> EXIT = np;
                    case PATH -> Paths.add(np);
                    case SKILL -> skillMap.put(np, Skill.randomSkill());
                }
            }
        }
    }

    public void doStepOnPoint(Point point) {
        BlockType block = getBlock(point);
        switch (block) {
            case SKILL -> {
                skillMap.remove(point);
                setBlock(point, PATH);
            }
            case GOLD -> {
                gold.remove(point);
                setBlock(point, PATH);
            }
            default -> {
                log.info(" {} is stepped{} ", "point", point.toString());
            }
        }

    }

    public void setBlock(Point point, BlockType blockType) {
        board[point.getX()][point.getY()] = blockType;
    }

    public void setBlock(int x, int y, BlockType blockType) {
        board[x][y] = blockType;
    }

    public int getScore(int x, int y) {
//        return scores[x][y];
        return 0;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                sb.append(board[i][j].getSignal());
                // sb.append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public BlockType getBlock(int i, int j) {
        if (i < 0 || i >= validSize || j < 0 || j >= validSize) {
            return BlockType.WALL; // out of bounds
        } else {
            return board[i][j];
        }
    }

    public BlockType getBlock(Point point) {
        int i = point.getX();
        int j = point.getY();
        if (i < 0 || i >= validSize || j < 0 || j >= validSize) {
            return BlockType.WALL; // out of bounds
        } else {
            return board[i][j];
        }
    }

}
