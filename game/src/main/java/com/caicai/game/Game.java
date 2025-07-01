package com.caicai.game;

import com.caicai.game.combat.Combat;
import com.caicai.game.common.*;
import com.caicai.game.common.pathFinderImpl.GreedyPathFinder;
import com.caicai.game.conf.GameConf;
import com.caicai.game.maze.BlockType;
import com.caicai.game.maze.Maze;
import com.caicai.game.maze.MazeFactory;
import com.caicai.game.role.Hero;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

@Component
@Slf4j
public class Game {
    public Maze maze;
    Combat combat;
    public Hero hero;
    public Point curPos;
    ResultFactory resultFactory;

    @Autowired
    MazeFactory mazeFactory;
    @Resource(name = "pathSolve")
    PathFinder pathFinder;
    @Autowired
    private GameConf gameConf;

    public Map<String, Object> baseFields = new HashMap<>();

    Game() {
        resultFactory = new ResultFactory(baseFields);
        log.info("Game constructor");
    }

    @PostConstruct
    public void reset() {
        System.out.println(maze);
        this.maze = mazeFactory.getMaze();
        this.hero = new Hero();
        this.curPos = maze.getSTART();
        baseFields.put("hero", hero);
        baseFields.put("position", curPos);
    }

    public Result init() {
        reset();
        Result res = Result.ok();
        res.put("maze", maze);
        res.put("hero", hero);
        return res;
    }

    public Result init(InputStream is) {
        String jsonStr = new Scanner(is, StandardCharsets.UTF_8).useDelimiter(
                "\\A").next();
        JSONObject obj = new JSONObject(jsonStr);
        JSONArray mazeJson = obj.getJSONArray("maze");
        int rows = mazeJson.length();
        int cols = mazeJson.getJSONArray(0).length();
        maze = new Maze(rows);
        for (int i = 0; i < rows; i++) {
            JSONArray row = mazeJson.getJSONArray(i);
            for (int j = 0; j < cols; j++) {
                String t = row.getString(j);
                if (t.equals("#")) {
                    maze.board[i + 1][j + 1] = BlockType.WALL;
                } else if (t.equals("S")) {
                    maze.setSTART(new Point(i + 1, j + 1));
                    maze.board[i + 1][j + 1] = BlockType.START;
                } else if (t.equals("G")) {
                    maze.board[i + 1][j + 1] = BlockType.GOLD;
                } else if (t.equals("E")) {
                    maze.setEXIT(new Point(i + 1, j + 1));
                    maze.board[i + 1][j + 1] = BlockType.EXIT;
                } else if (t.equals("T")) {
                    maze.board[i + 1][j + 1] = BlockType.TRAP;
                } else if (t.equals("L")) {
                    maze.setLOCKER(new Point(i + 1, j + 1));
                    maze.board[i + 1][j + 1] = BlockType.LOCKER;
                } else {
                    maze.board[i + 1][j + 1] = BlockType.PATH;
                }
            }
        }
        hero = new Hero();
        curPos = maze.getSTART();
        baseFields.put("hero", hero);
        baseFields.put("curPos", curPos);
        baseFields.put("maze", maze);
        return resultFactory.ok();
    }

    public HashMap<String, Object> openCombat() {
        combat = new Combat();
        log.info("openCombat");
        HashMap<String, Object> map = combat.start();
        return map;
    }

    public Result fullInfo() {
        log.info("fullInfo");
        return resultFactory.ok().put("maze", maze);
    }


    public Result getFullPath() {
        PathSolve pathSolve = new PathSolve();
        List<Point> path = pathSolve.solve(maze);
        return resultFactory.ok().put("path", path);
    }

    public Result getGreedyPath() {
        GreedyPathFinder pathFinder = new GreedyPathFinder();
        List<Point> path = pathFinder.solve(maze);
        return resultFactory.ok().put("path", path);
    }

    // the data should contains the actually pos is

    public Map<String, Object> initBossConfig(InputStream is) {

        Map<String, Object> res = new HashMap<>();

        String jsonStr = new Scanner(is, StandardCharsets.UTF_8).useDelimiter(
                "\\A").next();
        JSONObject obj = new JSONObject(jsonStr);
        Combat combat1 = new Combat();
        return combat1.startWithJson(obj);
    }

}
