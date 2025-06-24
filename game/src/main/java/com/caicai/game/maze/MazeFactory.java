package com.caicai.game.maze;

import com.caicai.game.GameApplication;
import com.caicai.game.conf.GameConf;
import com.caicai.game.common.Point;
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
        build(0, 0, gameConf.getSize() - 1, gameConf.getSize() - 1, maze, Integer.valueOf(MAXSP));
        log.info("inited will link the blocks");
        mklink(maze);
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

    void mklink(Maze maze) {
        int size = maze.getSize();
        boolean[][] visited = new boolean[size][size];
        List<Point> blocks = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                // do bfs to merge all point in the same block
                if (maze.getBlock(i, j) != BlockType.WALL && visited[i][j] == false) {
                    Queue<Point> aux = new ArrayDeque<>();
                    aux.add(new Point(i, j));
                    blocks.add(new Point(i, j));
                    while (!aux.isEmpty()) {
                        Point p = aux.poll();
                        visited[p.getX()][p.getY()] = true;
                        for (int k = 0; k < 8; k++) {
                            int nx = p.getX() + x_[k];
                            int ny = p.getY() + y_[k];
                            if (maze.getBlock(nx, ny) == BlockType.WALL || visited[nx][ny]) {
                                continue;
                            } else {
                                aux.add(new Point(nx, ny));
                            }
                        }
                    }
                }
            }
        }
        // to to link all blocks to the first block
        for (int i = 1; i < blocks.size(); i++) {
            doLink(blocks.get(0), blocks.get(i), maze);
        }
    }

    void doLink(Point lu, Point rb, Maze maze) {
        // assert s is at the left up of rb;
        if (lu.compareTo(rb) != -1) {
            // swap
            Point temp = lu;
            lu = rb;
            rb = temp;
        }
        int lx = lu.getX();
        int ly = lu.getY();
        int rx = rb.getX();
        int ry = rb.getY();
        while (lx < rx && ly < ry) {
            lx++;
            ly++;
            if (maze.getBlock(lx, ly) == BlockType.WALL) {
                maze.setBlock(lx, ly, randBlock(Set.of(BlockType.WALL)));
            }
        }
        while (lx < rx) {
            lx++;
            if (maze.getBlock(lx, ly) == BlockType.WALL) {
                maze.setBlock(lx, ly, randBlock(Set.of(BlockType.WALL)));
            }
        }
        while (ly < ry) {
            ly++;
            if (maze.getBlock(lx, ly) == BlockType.WALL) {
                maze.setBlock(lx, ly, randBlock(Set.of(BlockType.WALL)));
            }
        }

    }

    /**
     * 生成迷宫
     *
     * @param Point lu 迷宫左上角点
     * @param Point rd 迷宫右下角点
     * @return 迷宫对象
     */
    double[] getRandCap() {
        double[] percent = new double[]{0.25, 0.25, 0.25, 0.25};
        double tot = 1;
        Random rand = new Random();
        // max origin
        double v = rand.nextDouble(0.2, 0.6);
        for (int i = 0; i < 4; i++) {
            percent[i] = v;
            tot -= v;
            v = rand.nextDouble(0, tot);
        }
        return percent;

    }

    void build(int x0, int y0, int x1, int y1, Maze maze, Integer mxSp) {

        if (x0 > x1 || y0 > y1) {
            return;
        }
        if (x0 == x1 && y0 == y1) {
            // ! 只有一个点
            BlockType blockType = randBlock(null, mxSp < 1 ? Set.of(BlockType.WALL, BlockType.PATH) : null);
            maze.setBlock(x0, y0, blockType);
            return;
        }
        // !the board should be divided into 4 parts to enable connected
        // [0, 2]
        // [0,1] [2,2]

        int midX = (x0 + x1) / 2;
        int midY = (y0 + y1) / 2;
        List<Point[]> points = new ArrayList<>();
        // generate the 4 parts\
        // let the order the finish the 4 part randomly
        points.add(new Point[]{new Point(x0, y0), new Point(midX, midY)});
        points.add(new Point[]{new Point(midX + 1, y0), new Point(x1, midY)});
        points.add(new Point[]{new Point(x0, midY + 1), new Point(midX, y1)});
        points.add(new Point[]{new Point(midX + 1, midY + 1),
                               new Point(x1, y1)});
        // shuffle the points
        Collections.shuffle(points);
        double[] percent = getRandCap();
        for (int i = 0; i < points.size(); i++) {
            Point[] p = points.get(i);
            build(p[0].getX(), p[0].getY(), p[1].getX(), p[1].getY(), maze, (int) Math.ceil(mxSp * percent[i]));
        }
        // check whether the inner maze is linked
        // if not linked then check whether the inner has any block except the wall, if
        // so then
    }
}
