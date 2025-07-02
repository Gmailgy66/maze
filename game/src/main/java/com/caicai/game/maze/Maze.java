package com.caicai.game.maze;

import com.caicai.game.common.Point;
import com.caicai.game.role.Boss;
import com.caicai.game.role.Skill;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static com.caicai.game.maze.BlockType.PATH;

/**
 * 迷宫类
 * 负责存储和管理迷宫的结构、元素和状态
 * 包含迷宫的地图、特殊点位和玩家可交互的元素
 */
@Slf4j
@Data
public class Maze {
    /**
     * Boss对象，迷宫中的最终挑战
     */
    private Boss boss;
    /**
     * Boss所在的位置坐标
     */
    private Point BossPoint;

    /**
     * 迷宫的二维数组表示，存储每个位置的方块类型
     */
    public BlockType[][] board;
    /**
     * 访问标记数组，用于标记哪些位置已被访问
     */
    private boolean[][] vis;
    /**
     * 迷宫的标题
     */
    private String title = "吴哥窟";
    /**
     * 金币位置的集合
     */
    private Set<Point> gold = new HashSet<>();
    // private Set<Point> lockers;
    /**
     * 陷阱位置的集合
     */
    private Set<Point> traps = new HashSet<>();
    /**
     * 可通行路径的集合
     */
    private Set<Point> Paths = new HashSet<>();
    /**
     * 技能与位置的映射关系
     */
    Map<Point, Skill> skillMap = new HashMap<>();
    /**
     * 起点位置
     */
    private Point START;
    /**
     * 终点位置
     */
    private Point EXIT;
    /**
     * 锁的位置
     */
    private Point LOCKER;
    /**
     * 有效迷宫大小（不包括边界墙）
     */
    private int validSize;
    /**
     * 迷宫总大小（包括边界墙）
     */
    private int boardSize;
    /**
     * 各位置的分数矩阵
     */
    private int[][] scores;
    /**
     * 金币的得分常量
     */
    public static final int GOLD_SCORE = 50;
    /**
     * 陷阱的得分常量
     */
    public static final int TRAP_SCORE = -30;
    /**
     * 技能的得分常量
     */
    public static final int SKILL_SCORE = 0;

    /**
     * 构造迷宫对象
     *
     * @param size 迷宫的有效大小（不包括边界墙）
     */
    public Maze(int size) {
        this.scores = new int[size + 2][size + 2];
        this.validSize = size;
        this.boardSize = size + 2; // +2 for walls
        this.board = new BlockType[boardSize][boardSize];
        // 初始化迷宫，默认所有位置为PATH
        Arrays.stream(board).forEach(row -> Arrays.fill(row, BlockType.PATH));
        // 设置迷宫的四周边界为墙
        for (int i = 0; i < boardSize; i++) {
            board[i][0] = BlockType.WALL;
            board[i][this.boardSize - 1] = BlockType.WALL;
            board[0][i] = BlockType.WALL;
            board[this.boardSize - 1][i] = BlockType.WALL;
        }
        this.vis = new boolean[size][size];
    }

    /**
     * 构建迷宫的额外信息
     * 遍历迷宫中的所有位置，根据方块类型初始化相应的集合和属性
     */
    void buildExtraInfo() {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                final Point np = new Point(i, j);
                switch (board[i][j]) {
                    case GOLD -> {
                        gold.add(np);
                        setScore(i, j, 5);
                    }
                    case TRAP -> {
                        traps.add(np);
                        setScore(i, j, -3);
                    }
                    case START -> START = np;
                    case LOCKER -> LOCKER = np;
                    case EXIT -> EXIT = np;
                    case PATH -> Paths.add(np);
                    case SKILL -> skillMap.put(np, Skill.randomSkill());
                    case BOSS -> {
                        boss = new Boss(new Random().nextInt(300) + 100,
                                        Skill.randomSkill(),
                                        Skill.randomSkill());
                        BossPoint = np;
                    }
                }
            }
        }
    }

    /**
     * 处理玩家踩到某个点位的效果
     *
     * @param point 玩家踩到的位置
     */
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

    /**
     * 设置指定位置的方块类型
     *
     * @param point     要设置的位置
     * @param blockType 要设置的方块类型
     */
    public void setBlock(Point point, BlockType blockType) {
        board[point.getX()][point.getY()] = blockType;
    }

    /**
     * 设置指定坐标的方块类型
     *
     * @param x         X坐标
     * @param y         Y坐标
     * @param blockType 要设置的方块类型
     */
    public void setBlock(int x, int y, BlockType blockType) {
        board[x][y] = blockType;
    }

    /**
     * 设置指定位置的分数
     *
     * @param x     X坐标
     * @param y     Y坐标
     * @param score 分数值
     */
    public void setScore(int x, int y, int score) {
        scores[x][y] = score;
        return;
    }

    /**
     * 获取指定位置的分数
     *
     * @param x X坐标
     * @param y Y坐标
     * @return 该位置的分数
     */
    public int getScore(int x, int y) {
        return scores[x][y];
    }

    /**
     * 获取指定点位的分数
     *
     * @param p1 要获取分数的位置
     * @return 该位置的分数
     */
    public int getScore(Point p1) {
        int x = p1.getX();
        int y = p1.getY();
        return scores[x][y];
    }

    /**
     * 将迷宫转换为字符串表示
     *
     * @return 迷宫的字符串表示，每个方块用其对应的符号表示
     */
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

    /**
     * 获取指定坐标的方块类型
     *
     * @param i X坐标
     * @param j Y坐标
     * @return 该位置的方块类型，如果越界则返回WALL
     */
    public BlockType getBlock(int i, int j) {
        if (i < 0 || i > validSize || j < 0 || j > validSize) {
            return BlockType.WALL; // out of bounds
        } else {
            return board[i][j];
        }
    }

    /**
     * 获取指定点位的方块类型
     *
     * @param point 要查询的位置
     * @return 该位置的方块类型，如果越界则返回WALL
     */
    public BlockType getBlock(Point point) {
        int i = point.getX();
        int j = point.getY();
//        [1,1+validSize-1]
        if (i <= 0 || i > validSize || j <= 0 || j > validSize) {
            return BlockType.WALL; // out of bounds
        } else {
            return board[i][j];
        }
    }
}
