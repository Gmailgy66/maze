package com.caicai.game.maze;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.caicai.game.conf.GameConf;
import com.caicai.game.utils.Point;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Component
public class MazeFactory {
    @Autowired
    GameConf gameConf;

    MazeFactory() {
        init();
    }

    public Maze getMaze() {
        Maze maze = new Maze(gameConf.getSize());
        build(new Point(0, 0), new Point(gameConf.getSize() - 1, gameConf.getSize() - 1), maze);
        log.info("Maze generated with size: {}", gameConf.getSize());
        return maze;
    }

    /**
     * 根据配置文件生成迷宫
     *
     * @return 迷宫对象
     */
    private final double GBASE = 0.3;
    private final double RBASE = 0.1;
    private final double TBASE = 0.05;
    Map<String, Integer> maxBlockType = new HashMap<>();

    void init() {
        int size = gameConf.getSize();
        size *= size;
        // leverl determines the number of resources, traps, lockers, and enemies
        // ! GOLD and TRAP and SKILLL will occupy 45%
        double level = gameConf.getLevel() / 15.0;
        maxBlockType.put("G", (int) (GBASE - level) * size);
        maxBlockType.put("T", (int) (TBASE + level) * size);
        maxBlockType.put("L", 1);
        maxBlockType.put("B", 1);
        maxBlockType.put("Sk", (int) Math.max((int) (RBASE + level) * size, level * 2));
    }

    BlockType randBlock() {
        int typeCnt = BlockType.getTypeCnt();
        while (true) {
            int randType = (int) (Math.random() * typeCnt) % typeCnt;
            BlockType blockType = BlockType.values()[randType];
            if (maxBlockType.containsKey(blockType.getSignal())) {
                int max = maxBlockType.get(blockType.getSignal());
                if (max > 0) {
                    maxBlockType.put(blockType.getSignal(), max - 1);
                    return blockType;
                }
                // else will continue
            } else {
                // ! WALL or SPACE
                return blockType; // Default case, no limit
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
    void build(Point lu, Point rd, Maze maze) {
        if (lu.equals(rd)) {
            return;
        }
        int x0 = lu.getX();
        int x1 = rd.getX();
        int y0 = lu.getY();
        int y1 = rd.getY();
        for (int i = x0; i < x1; i++) {
            for (int j = y0; j < y1; j++) {
                BlockType blockType = randBlock();
                maze.setBlock(i, j, blockType);
            }
        }
        // !the board should be divided into 4 parts to enable connected
        int midX = (x0 + x1) / 2;
        int midY = (y0 + y1) / 2;
        build(new Point(x0, y0), new Point(midX, midY), maze); // Top-left
        build(new Point(midX, y0), new Point(x1, midY), maze);
    }
}
