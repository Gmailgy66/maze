package com.caicai.game.controller;

import com.caicai.game.Game;
import com.caicai.game.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Router {

    @Autowired
    Game game;

    @RequestMapping("/init")
    public Result init() {
        return game.init();
    }

    @RequestMapping("/step")
    public Result step() {
        // Here you would implement the logic to move the hero in the maze
        return game.getNextPoint();
    }
}
