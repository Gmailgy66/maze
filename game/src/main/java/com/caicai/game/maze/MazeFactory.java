package com.caicai.game.maze;

import com.caicai.game.GameApplication;
import com.caicai.game.common.Point;
import com.caicai.game.conf.GameConf;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class MazeFactory {

    private final GameApplication gameApplication;
    @Autowired
    GameConf gameConf;

    MazeFactory(GameApplication gameApplication) {
        this.gameApplication = gameApplication;
    }

    public Maze getMaze() {
        log.info("Maze generated with size: {}", gameConf.getSize());
        Maze maze = new Maze(gameConf.getSize());
        init();
        log.info("inited limits of the blocks");
        genMaze(0,0, gameConf.getSize() - 1, gameConf.getSize() - 1, new Random(), maze);
//        build(0, 0, gameConf.getSize() - 1, gameConf.getSize() - 1, maze, Integer.valueOf(MAXSP));
        log.info("inited will link the blocks");
//        mklink(maze);
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
    private final double GBASE = 0.1;
    private final double SKBASE = 0.05;
    private final double TBASE = 0.05;
    private Integer MAXSP;

    Map<BlockType, Integer> maxBlockType = new HashMap<>();

    @PostConstruct
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
        maze.buildExtraInfo();
        // check whether exit and start is in the maze
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
        }
        if (maze.getEXIT() == null) {
            Set<Point> paths = maze.getPaths();
            Point randomPoint = paths.stream()
                                     .skip(new Random().nextInt(paths.size()))
                                     .findFirst()
                                     .orElse(null);
            paths.remove(randomPoint);
            maze.setBlock(randomPoint, BlockType.EXIT);
            log.error("Maze has no exit point");
        }
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
        while (true) {
            int randType = (int) (Math.random() * typeCnt) % typeCnt;
            BlockType blockType = BlockType.values()[randType];
            // has limit and is need by the caller
            if ((neededType == null || neededType.contains(blockType)) && maxBlockType.containsKey(blockType)) {
                int max = maxBlockType.get(blockType);
                if (max > 0) {
                    maxBlockType.put(blockType, max - 1);
                    return blockType;
                } else {
                    continue;
                }
                // else will continue
            } else if (maxBlockType.containsKey(blockType) == false) {
                if (excludedType == null || excludedType.contains(blockType) == false) {
                    return blockType;
                } else if (excludedType.contains(blockType)) {
                    log.info("wall is generated when try to destroy the wall");
                }
            }
        }
    }

    /**
     * check whether the maze is linked
     */
    int[] x_ = new int[]{0, 0, 1, -1, 1, -1, 1, -1};
    int[] y_ = new int[]{-1, 1, 0, 0, 1, -1, -1, 1};






    /**
     * 在给定的线上打开一扇位置随机的门
     */
    private void openAdoor(int x1, int y1, int x2, int y2, Random r, Maze maze) {
        int pos;
        if (x1 == x2) {
            pos = y1 + r.nextInt((y2 - y1) / 2 + 1) * 2;//在奇数位置开门
            maze.setBlock(x1, pos, randBlock());
//            blocked[x1][pos] = false;
        } else if (y1 == y2) {
            pos = x1 + r.nextInt((x2 - x1) / 2 + 1) * 2;
            maze.setBlock(pos, y1, randBlock());
//            blocked[pos][y1] = false;
        } else {
            System.out.println("wrong");
        }
    }

    /**
     * 迷宫生成算法，采用递归方式实现，随机画横竖两条线，然后在线上随机开三扇门
     *
     * @param x：迷宫起点的x坐标
     * @param y：迷宫起点的y坐标
     * @param height：迷宫的高度
     * @param width：迷宫的宽度  ***********
     *                     *         *
     *                     *         *
     *                     ***********
     *                     针对上述迷宫，四个参数为：1,1,2,9
     */
    private void genMaze(int x, int y, int height, int width, Random r, Maze maze) {
        int xPos, yPos;

        if (height <= 2 || width <= 2) {
            return;
        }

        //横着画线，在偶数位置画线
        xPos = x + r.nextInt(height / 2) * 2 + 1;
        for (int i = y; i < y + width; i++) {
            maze.setBlock(xPos, i, BlockType.WALL);
//            blocked[xPos][i] = true;
        }

        //竖着画一条线，在偶数位置画线
        yPos = y + r.nextInt(width / 2) * 2 + 1;
        for (int i = x; i < x + height; i++) {
            maze.setBlock(i, yPos, BlockType.WALL);
//            blocked[i][yPos] = true;
        }

        //随机开三扇门，左侧墙壁为1，逆时针旋转
        int isClosed = r.nextInt(4) + 1;
        switch (isClosed) {
            case 1:
                openAdoor(xPos + 1, yPos, x + height - 1, yPos,r,maze);// 2
                openAdoor(xPos, yPos + 1, xPos, y + width - 1,r,maze);// 3
                openAdoor(x, yPos, xPos - 1, yPos,r,maze);// 4
                break;
            case 2:
                openAdoor(xPos, yPos + 1, xPos, y + width - 1,r,maze);// 3
                openAdoor(x, yPos, xPos - 1, yPos,r,maze);// 4
                openAdoor(xPos, y, xPos, yPos - 1,r,maze);// 1
                break;
            case 3:
                openAdoor(x, yPos, xPos - 1, yPos,r,maze);// 4
                openAdoor(xPos, y, xPos, yPos - 1,r,maze);// 1
                openAdoor(xPos + 1, yPos, x + height - 1, yPos,r,maze);// 2
                break;
            case 4:
                openAdoor(xPos, y, xPos, yPos - 1,r,maze);// 1
                openAdoor(xPos + 1, yPos, x + height - 1, yPos,r,maze);// 2
                openAdoor(xPos, yPos + 1, xPos, y + width - 1,r,maze);// 3
                break;
            default:
                break;
        }

        // 左上角
        genMaze(x, y, xPos - x, yPos - y, r, maze);
        // 右上角
        genMaze(x, yPos + 1, xPos - x, width - yPos + y - 1, r, maze);
        // 左下角
        genMaze(xPos + 1, y, height - xPos + x - 1, yPos - y, r, maze);
        // 右下角
        genMaze(xPos + 1, yPos + 1, height - xPos + x - 1, width - yPos + y - 1, r, maze);
    }


}
