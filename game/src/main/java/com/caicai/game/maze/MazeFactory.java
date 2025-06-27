package com.caicai.game.maze;

import com.caicai.game.common.Point;
import com.caicai.game.conf.GameConf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.util.*;

import static com.caicai.game.common.RandUtil.randEven;
import static com.caicai.game.common.RandUtil.randOdd;

@Slf4j
@Component
public class MazeFactory {

    @Autowired
    GameConf gameConf;

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
        genMaze(1, 1, gameConf.getSize(), gameConf.getSize(), new Random(), maze);
        // build(0, 0, gameConf.getSize() - 1, gameConf.getSize() - 1, maze,
        // Integer.valueOf(MAXSP));
        log.info("inited will link the blocks");
        // mklink(maze);
        log.info("linked the blocks");
        postBuild(maze);
        log.info("recheck the SPECIAL Points");
        return maze;
    }

    /**
     * 根据配置文件生成迷宫
     *
     * @return 迷宫对象
     */
    private final double GBASE = 0.2;
    private final double SKBASE = 0.1;
    private final double TBASE = 0.1;
    private Integer MAXSP;

    Map<BlockType, Integer> maxBlockType = new HashMap<>();

    void init() {
        int size = gameConf.getSize();
        size *= size;
        MAXSP = (int) (size * 1);
        // leverl determines the number of resources, traps, lockers, and enemies
        // ! GOLD and TRAP and SKILLL will occupy no more than 40%
        double level = gameConf.getLevel() / 15.0;
        Random rand = new Random();
        double v = rand.nextDouble(-0.05, 0.1);
        maxBlockType.put(BlockType.GOLD, (int) ((GBASE - level + v) * size));
        v = rand.nextDouble(-0.05, 0.1);
        maxBlockType.put(BlockType.TRAP, (int) ((TBASE + level + v) * size));
        maxBlockType.put(BlockType.LOCKER, 1);
        maxBlockType.put(BlockType.BOSS, 1);
        maxBlockType.put(BlockType.EXIT, 1);
        maxBlockType.put(BlockType.START, 1);
        maxBlockType.put(BlockType.SKILL, (int) Math.min((int) ((SKBASE + level) * size), level * 2 + 1));
    }

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
    }
    public void doDraw(Maze maze, List<Point>l) {

    }

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
//        this func shouldn't return a WALL
        while (true) {
            int randType = (int) (Math.random() * typeCnt) % typeCnt;
            BlockType blockType = BlockType.values()[randType];
            // has limit and is need by the caller
            if ((neededType == null || neededType.contains(blockType)) && maxBlockType.containsKey(blockType)) {
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
     * 生成迷宫
     * 递归分割法
     *
     * @param x      起始x坐标
     * @param y      起始y坐标
     * @param height 高度
     * @param width  宽度
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
        Point[] points = {new Point(xPos, randOdd(yPos, y + width)),
                          new Point(xPos, randOdd(y, yPos)),
                          new Point(randOdd(x, xPos), yPos),
                          new Point(randOdd(xPos, x + height), yPos)};
        int jump = r.nextInt(0, 4);
//        doDraw(xPos, yPos, points, jump, maze);

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
//            fo.write(maze.toString().getBytes());
            fo.write('\n');
            fo.close();

            genMaze(x, y, xPos - x, yPos - y, r, maze);
            // 右上角
            genMaze(x, yPos + 1, xPos - x, width - yPos + y - 1, r, maze);
            // 左下角
            genMaze(xPos + 1, y, height - xPos + x - 1, yPos - y, r, maze);
            // 右下角
            genMaze(xPos + 1, yPos + 1, height - xPos + x - 1, width - yPos + y - 1, r, maze);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("{} is {} ", "e", e);
        }
    }

}
