package com.caicai.game.maze;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.caicai.game.common.Point;
import static com.caicai.game.common.RandUtil.randEven;
import static com.caicai.game.common.RandUtil.randOdd;
import com.caicai.game.conf.GameConf;

import lombok.extern.slf4j.Slf4j;

/**
 * 迷宫生成工厂类
 * 负责使用递归分割算法创建迷宫，并添加各种特殊元素
 */
@Slf4j
@Component
public class MazeFactory {

    /**
     * 游戏配置信息
     */
    @Autowired
    GameConf gameConf;

    /**
     * 获取生成的迷宫
     *
     * @return 完成初始化和构建的迷宫对象
     */
    public Maze getMaze() {
        log.info("Maze generated with size: {}", gameConf.getSize());
        Maze maze = new Maze(gameConf.getSize());
        init();
        log.info("inited limits of the blocks");
        try {
            FileOutputStream fo = new FileOutputStream("maze.txt", false);
            fo.write(String.valueOf(maze.getBoardSize()).getBytes());
            fo.write('\n');
            fo.close();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("{} is {} ", "e", e);
        }
        genMaze(1, 1, gameConf.getSize(), gameConf.getSize(), new Random(),
                maze);
        // build(0, 0, gameConf.getSize() - 1, gameConf.getSize() - 1, maze,
        // Integer.valueOf(MAXSP));
        log.info("inited will link the blocks");
        // mklink(maze);
        log.info("linked the blocks");
        postBuild(maze);
        log.info("recheck the SPECIAL Points");
        print2(maze);
        return maze;
    }

    void print(Maze maze) {
        BlockType[][] board = maze.getBoard();
        JSONArray vJSONArray = new JSONArray();
        JSONObject vJSONObject = new JSONObject();

        for (BlockType[] l : board) {
            JSONArray in = new JSONArray();
            for (BlockType blockType : l) {
                in.put(blockType.getSignal());
            }
            vJSONArray.put(in);
        }
        vJSONObject.put("maze", vJSONArray);
        System.out.println(vJSONObject.toString());
        try {
            FileWriter fw = new FileWriter("maze.json");
            fw.write(vJSONObject.toString());
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    void print2(Maze maze) {
        BlockType[][] board = maze.getBoard();
        JSONArray vJSONArray = new JSONArray();
        JSONObject vJSONObject = new JSONObject();

        for (BlockType[] l : board) {
            JSONArray in = new JSONArray();
            for (BlockType blockType : l) {
                in.put(blockType.getSignal());
            }
            vJSONArray.put(in);
        }
        StringBuilder formatted = new StringBuilder();
        formatted.append("{\n  \"maze\": [\n");
        for (int i = 0; i < vJSONArray.length(); i++) {
            formatted.append("    ").append(vJSONArray.getJSONArray(i).toString());
            if (i < vJSONArray.length() - 1) {
                formatted.append(",");
            }
            formatted.append("\n");
        }
        formatted.append("  ]\n}");

        System.out.println(formatted.toString());

        try {
            FileWriter fw = new FileWriter( "maze.json");
            fw.write(formatted.toString());
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 金币基础生成概率
     */
    private final double GBASE = 0.3;
    /**
     * 技能基础生成概率
     */
    private final double SKBASE = 0.1;
    /**
     * 陷阱基础生成概率
     */
    private final double TBASE = 0.1;
    /**
     * 特殊元素的最大数量
     */
    private Integer MAXSP;

    /**
     * 各类型方块的最大数量限制
     */
    Map<BlockType, Integer> maxBlockType = new HashMap<>();

    /**
     * 初始化各类型方块的数量限制
     * 根据游戏配置的难度级别调整特殊元素的数量
     */
    void init() {
        int size = gameConf.getSize();
        size *= size;
        MAXSP = (int) (size * 1);
        // leverl determines the number of resources, traps, lockers, and enemies
        // ! GOLD and TRAP and SKILLL will occupy no more than 40%
        double level = gameConf.getLevel() / 15.0;
        Random rand = new Random();
        double v = rand.nextDouble(-0.1, 0.05);
        maxBlockType.put(BlockType.GOLD, (int) ((GBASE + v) * size));
        v = rand.nextDouble(-0.05, 0.05);
        maxBlockType.put(BlockType.TRAP, (int) ((TBASE + v) * size));
        maxBlockType.put(BlockType.LOCKER, 1);
        maxBlockType.put(BlockType.BOSS, 1);
        maxBlockType.put(BlockType.EXIT, 1);
        maxBlockType.put(BlockType.START, 1);
        maxBlockType.put(BlockType.SKILL,
                (int) Math.min((int) ((SKBASE + level) * size),
                        level * 2 + 1));
    }

    /**
     * 迷宫构建后的处理
     * 检查并确保迷宫包含起点和终点，若没有则随机生成
     *
     * @param maze 要处理的迷宫对象
     */
    void postBuild(Maze maze) {
        // check whether exit and start is in the maze
        maze.buildExtraInfo();
        if (maze.getSTART() == null) {
            log.error("Maze has no start point");
            // do gen a start point
            Random rand = new Random();
            // get a random path// 使用Stream API
            Set<Point> paths = maze.getPaths();
            Point randomPoint = paths.stream()
                    .skip(new Random().nextInt(paths.size()))
                    .findFirst()
                    .orElse(null);
            paths.remove(randomPoint);
            maze.setBlock(randomPoint, BlockType.START);
            maze.setSTART(randomPoint);
        }
        if (maze.getEXIT() == null) {
            Set<Point> paths = maze.getPaths();
            Point randomPoint = paths.stream()
                    .skip(new Random().nextInt(paths.size()))
                    .findFirst()
                    .orElse(null);
            paths.remove(randomPoint);
            maze.setBlock(randomPoint, BlockType.EXIT);
            maze.setEXIT(randomPoint);
            log.error("Maze has no exit point");
        }
        // if (maze.getEXIT() == null) {
        // Set<Point> paths = maze.getPaths();
        // Point randomPoint = paths.stream()
        // .skip(new Random().nextInt(paths.size()))
        // .findFirst()
        // .orElse(null);
        // paths.remove(randomPoint);
        // maze.setBlock(randomPoint, BlockType.EXIT);
        // maze.setEXIT(randomPoint);
        // log.error("Maze has no exit point");
        // }
    }

    /**
     * 绘制路径点列表（未实现）
     *
     * @param maze 迷宫对象
     * @param l    点列表
     */
    public void doDraw(Maze maze, List<Point> l) {
        // 未实现
    }

    /**
     * 随机生成一个方块类型
     * 根据条件和限制随机选择一个方块类型
     *
     * @param condition 可选条件参数，包括排除类型和必需类型
     * @return 随机选择的方块类型
     */
    BlockType randBlock(Object... condition) {
        int typeCnt = BlockType.getTypeCnt();
        Set excludedType = null;
        Set neededType = null;
        if (condition != null && condition.length != 0) {
            excludedType = (Set) condition[0];
        }
        if (condition != null && condition.length >= 2) {
            neededType = (Set) condition[1];
        }
        // this func shouldn't return a WALL
        while (true) {
            int randType = (int) (Math.random() * typeCnt) % typeCnt;
            BlockType blockType = BlockType.values()[randType];
            // has limit and is need by the caller
            if ((neededType == null || neededType.contains(
                    blockType)) && maxBlockType.containsKey(blockType)) {
                int max = maxBlockType.get(blockType);
                if (max > 0) {
                    maxBlockType.put(blockType, max - 1);
                    return blockType;
                }
            } else if (blockType != BlockType.WALL) {
                return BlockType.PATH;
            }
        }
    }

    /**
     * 生成迷宫的核心方法
     * 使用递归分割算法生成迷宫
     *
     * @param x      起始x坐标
     * @param y      起始y坐标
     * @param height 区域高度
     * @param width  区域宽度
     * @param r      随机数生成器
     * @param maze   迷宫对象
     */
    private void genMaze(int x, int y, int height, int width, Random r, Maze maze) {
        int xPos, yPos;
        if (height <= 2 || width <= 2) {
            return;
        }

        // 横着画线，在偶数位置画线
        xPos = randEven(x, x + height);
        for (int i = y; i < y + width; i++) {
            maze.setBlock(xPos, i, BlockType.WALL);
            // blocked[xPos][i] = true;
        }

        // 竖着画一条线，在偶数位置画线
        yPos = randEven(y, y + width);
        for (int i = x; i < x + height; i++) {
            maze.setBlock(i, yPos, BlockType.WALL);
            // blocked[i][yPos] = true;
        }

        // 在隔墙上创建四个随机通道点，并在其中随机选择一个不设置（即保留为通道）
        Point[] points = { new Point(xPos, randOdd(yPos, y + width)),
                new Point(xPos, randOdd(y, yPos)),
                new Point(randOdd(x, xPos), yPos),
                new Point(randOdd(xPos, x + height), yPos) };
        int jump = r.nextInt(0, 4);
        // doDraw(xPos, yPos, points, jump, maze);

        try {
            FileOutputStream fo = new FileOutputStream("maze.txt", true);
            fo.write(new Point(xPos, yPos).toString().getBytes());
            fo.write('\n');
            for (int j = 0; j < 4; j++) {
                if (j == jump) {
                    continue;
                }
                maze.setBlock(points[j], randBlock());
                fo.write(points[j].toString().getBytes());
                fo.write('\n');
            }
            // fo.write(maze.toString().getBytes());
            fo.write('\n');
            fo.close();

            // 递归处理四个象限
            // 左上角
            genMaze(x, y, xPos - x, yPos - y, r, maze);
            // 右上角
            genMaze(x, yPos + 1, xPos - x, width - yPos + y - 1, r, maze);
            // 左下角
            genMaze(xPos + 1, y, height - xPos + x - 1, yPos - y, r, maze);
            // 右下角
            genMaze(xPos + 1, yPos + 1, height - xPos + x - 1,
                    width - yPos + y - 1, r, maze);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("{} is {} ", "e", e);
        }
    }
}
