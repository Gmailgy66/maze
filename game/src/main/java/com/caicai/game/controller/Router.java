package com.caicai.game.controller;

import com.caicai.game.Game;
import com.caicai.game.common.Result;
import com.caicai.game.conf.GameConf;
import com.caicai.game.quiz.Question;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
public class Router {

    @Autowired
    Game game;
    @Autowired
    private GameConf gameConf;

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

    @PostMapping("/uploadMaze")
    @ResponseBody
    public Result uploadMaze(@RequestParam("mazeFile") MultipartFile mazeFile) throws Exception {
        if (mazeFile.isEmpty()) {
            return Result.fail().put("msg", "File is empty");
        }
        return game.init(mazeFile.getInputStream());

    }

    @ResponseBody
    @RequestMapping("/loadMaze")
    public Result loadMaze() {
        InputStream is = this.getClass()
                             .getClassLoader()
                             .getResourceAsStream("maze.json");
        if (is == null) {
            System.out.println("open file failed");
            throw new RuntimeException("failed");
        }
        return game.init(is);
    }

    @ResponseBody
    @RequestMapping("/fullUpdate")
    public Result fullUpdate() {
        return game.fullInfo();
    }

    @ResponseBody
    @RequestMapping("/dpPath")
    public Result dpPath() {
        return game.getFullPath();
    }

    @ResponseBody
    @RequestMapping("/greedyPath")
    public Result greedyPath() {
        return game.getGreedyPath();
    }

    @RequestMapping("/combat")
    public String combat(Model model) {
        model.addAttribute("result",game.openCombat());
        return "combat";
    }

    @ResponseBody
    @RequestMapping("/quiz")
    public Map<String, Object> quiz() throws Exception {
        return new Question().start();
    }

    // 待会删掉
    @RequestMapping("/test")
    public String test() {
        return "test";
    }
}
