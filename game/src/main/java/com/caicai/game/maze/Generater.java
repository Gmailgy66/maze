package com.caicai.game.maze;

import com.caicai.game.conf.GameConf;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Data
@Slf4j
@Component
public class Generater {
    @Autowired
    GameConf gameConf;
    public Maze generate() {
        Maze maze = new Maze();
        return maze;
    }
}
