package com.caicai.game;

import com.caicai.game.maze.Maze;
import com.caicai.game.maze.MazeFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class MazefactoryTb {
    @Autowired
    MazeFactory mazeFactory;
    @Test
    public void test() {
        Maze maze = mazeFactory.getMaze();
        log.info("Maze generated: \n{}", maze);
    }
}