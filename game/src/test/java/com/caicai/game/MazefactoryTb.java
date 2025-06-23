package com.caicai.game;

import com.caicai.game.maze.Maze;
import com.caicai.game.maze.MazeFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

@Slf4j
@SpringBootTest
class MazefactoryTb {
    @Autowired
    MazeFactory mazeFactory;
    @Test
    public void test() throws FileNotFoundException {
        Maze maze = mazeFactory.getMaze();
        FileOutputStream fo = new FileOutputStream("tmp"+System.currentTimeMillis());
        log.info("Maze generated: \n{}", maze.toString());
    }
}