package com.caicai.game.controller;

import com.caicai.game.Game;
import com.caicai.game.common.Result;
import com.caicai.game.conf.GameConf;
import com.caicai.game.maze.BlockType;
import com.caicai.game.quiz.Question;
import com.caicai.game.role.Hero;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;

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

    @RequestMapping("/loadMaze")
    public String loadMaze() {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("maze.json");
        if (is == null) {
            System.out.println("open file failed");
            throw new RuntimeException("failed");
        }

        String jsonStr = new Scanner(is, StandardCharsets.UTF_8).useDelimiter("\\A").next();
        JSONObject obj = new JSONObject(jsonStr);
        JSONArray mazeJson = obj.getJSONArray("maze");

        int rows = mazeJson.length();
        int cols = mazeJson.getJSONArray(0).length();
        game.maze.board = new BlockType[rows][cols];

        for (int i = 0; i < rows; i++) {
            JSONArray row = mazeJson.getJSONArray(i);
            for (int j = 0; j < cols; j++) {
                String t = row.getString(j);
                if (t.equals("#")) {
                    game.maze.board[i][j] = BlockType.WALL;
                } else if (t.equals("S")) {
                    game.maze.board[i][j] = BlockType.START;
                } else if (t.equals("G")) {
                    game.maze.board[i][j] = BlockType.GOLD;
                } else if (t.equals("E")) {
                    game.maze.board[i][j] = BlockType.EXIT;
                } else if (t.equals("T")) {
                    game.maze.board[i][j] = BlockType.TRAP;
                } else if (t.equals("L")) {
                    game.maze.board[i][j] = BlockType.LOCKER;
                } else {
                    game.maze.board[i][j] = BlockType.PATH;
                }
            }
        }
        game.hero = new Hero();
        game.curPos = game.maze.getSTART();
        game.baseFields.put("hero", game.hero);
        game.baseFields.put("curPos", game.curPos);
        return "game";
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

    @ResponseBody
    @RequestMapping("/nextPointWithPath")
    public Result nextPointWithPath() {
        return game.getFullPath();
    }

    @RequestMapping("/combat")
    public String combat(Model model) {
//        model.addAttribute("result", game.openCombat());
        game.openCombat();
        return "combat";
    }

    @ResponseBody
    @RequestMapping("/nextTurn")
    public Result nextTurn() {
        return game.nextTurn();
    }

    @ResponseBody
    @RequestMapping("/quiz")
    public Map<String, Object> quiz() {
        return new Question().start();
    }

    // 待会删掉
    @RequestMapping("/test")
    public String test() {
        return "test";
    }
}
