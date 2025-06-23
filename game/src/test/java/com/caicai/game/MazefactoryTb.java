package com.caicai.game;

import com.caicai.game.maze.Maze;
import com.caicai.game.maze.MazeFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@SpringBootTest
class MazefactoryTb {
    @Autowired
    MazeFactory mazeFactory;

    @Test
    public void test() throws IOException {
        log.info(" {} is {} ", "System.getProperty(\"user.dir\")", System.getProperty("user.dir"));
        Maze maze = mazeFactory.getMaze();
//        FileOutputStream fo = new FileOutputStream("tmp"+System.currentTimeMillis());
        FileOutputStream fo = new FileOutputStream("data/tmp");
        log.info("Maze generated: \n{}", maze.toString());
        fo.write(maze.toString().getBytes(StandardCharsets.UTF_8));
        fo.close();
    }
}