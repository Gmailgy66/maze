package com.caicai.game.controller;

import com.caicai.game.Game;
import com.caicai.game.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class Router {

    @Autowired
    Game game;

    @RequestMapping("/init")
    public String init(Model model) {
        var result = game.init();
        System.out.println(result);
        model.addAttribute("result", result);
        return "maze";
//        return game.init();
    }
@ResponseBody
    @RequestMapping("/step")
    public Result step() {
        // Here you would implement the logic to move the hero in the maze
        return game.getNextPoint();
    }

    @RequestMapping("/combat")
    public String combat(Model model) {
        // Here you would implement the logic to move the hero in the maze
        model.addAttribute("result", game.openCombat());
        return "combat";
//        return game.getNextPoint();
    }
}
