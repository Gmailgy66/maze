package com.caicai.game.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.caicai.game.Game;
import com.caicai.game.common.Result;
import com.caicai.game.conf.GameConf;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class Router {

    @Autowired
    Game game;
    @Autowired
    private GameConf gameConf;

    @RequestMapping("/init")
    public String init(Model model, @RequestParam(name = "size", required = false) Integer size) {
        if (size != null && size >= 3 && size < 100) {
            if (size % 2 == 0) {
                size += 1; // Ensure size is odd
            }
            gameConf.setSize(size);
            log.info("Game size set to {}", size);
        }
        var result = game.init();
        System.out.println(result);
        model.addAttribute("result", result);
        return "maze";
        // return game.init();
    }

    @RequestMapping("/game.html")
    public String maze(@RequestParam(name = "size", required = false) Integer size) {
        if (size != null && size >= 3 && size < 100) {
            if (size % 2 == 0) {
                size += 1; // Ensure size is odd
            }
            gameConf.setSize(size);
            log.info("Game size set to {}", size);
        }
        var result = game.init();
        // System.out.println(result);
        return "game";
        // return game.init();
    }

    @ResponseBody
    @RequestMapping("/fullUpdate")
    public Result fullUpdate() {
        return game.fullInfo();
    }

    @ResponseBody
    @RequestMapping("/nextStep")
    public Result step() {
        return game.getNextPoint();
    }

    @RequestMapping("/combat")
    public String combat(Model model) {
        model.addAttribute("result", game.openCombat());
        return "combat";
    }

    @ResponseBody
    @RequestMapping("/nextTurn")
    public Result nextTurn() {
        return game.nextTurn();
    }

}
