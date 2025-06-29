package com.caicai.game;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.caicai.game.combat.Combat;
import com.caicai.game.common.PathFinder;
import com.caicai.game.common.PathSolve;
import com.caicai.game.common.Point;
import com.caicai.game.common.Result;
import com.caicai.game.common.ResultFactory;
import com.caicai.game.common.pathFinderImpl.GreedyPathFinder;
import com.caicai.game.conf.GameConf;
import com.caicai.game.maze.BlockType;
import com.caicai.game.maze.Maze;
import com.caicai.game.maze.MazeFactory;
import com.caicai.game.role.Hero;
import com.caicai.game.role.Skill;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

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
        String jsonStr = new Scanner(is, StandardCharsets.UTF_8).useDelimiter("\\A")
                .next();
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

    public Result handleBlock(Point point) {
        log.info("handleBlock: {}", point);
        Result res = switch (maze.getBlock(point)) {
            // the methods below are all atomic so they should return a full result when
            // called
            case START -> S();
            case GOLD -> G();
            case EXIT -> E();
            case BOSS -> B();
            case TRAP -> T();
            case PATH -> P();
            case SKILL -> Sk();
            case LOCKER -> L();
            default -> resultFactory.fail()
                    .put("error", "Unknown block type: " + maze.getBlock(point));
        };
        maze.doStepOnPoint(point);
        curPos.setX((int) (Math.random() * gameConf.getSize()));
        curPos.setY((int) (Math.random() * gameConf.getSize()));
        // Point.randPoint(gameConf.getSize(), gameConf.getSize());
        return resultFactory.ok();
    }

    private Result P() {
        return resultFactory.ok().put("type", "PATH");
    }

    public Result getNextPoint() {
        // !do move point and return the effect of handleBlock
        log.info("getNextPoint");
        // op the block here

        return handleBlock(this.curPos);
    }

    // public HashMap<String, Object> startFight() {
    // log.info("nextTurn");
    // HashMap<String,Object> map = combat.start();
    // return map;
    // }

    //
    public Result G() {
        hero.setScore(hero.getScore() + maze.GOLD_SCORE);
        Random random = new Random();
        return resultFactory.ok().put("type", "GOLD");

    }

    /**
     * Handle the boss block.
     * return a list of skills in turn
     */
    public Result B() {
        return resultFactory.ok().put("type", "BOSS");
    }

    /**
     * Handle the start block.
     * Initialize the hero's position and other necessary attributes.
     */
    public Result S() {
        return resultFactory.ok().put("type", "START");
    }

    public Result L() {
        return resultFactory.ok().put("type", "LOCKER");
    }

    public Result T() {
        hero.setScore(hero.getScore() + maze.TRAP_SCORE);
        return resultFactory.ok().put("type", "TRAP");
    }

    public Result E() {
        log.info("Exiting the game.");
        return resultFactory.ok().put("type", "EXIT");
    }

    /**
     * Handle the skill block.
     * just tell
     */
    private Result Sk() {
        hero.setScore(hero.getScore() + maze.SKILL_SCORE);
        if (hero.skills.size() < 5) {
            hero.skills.add(Skill.randomSkill());
        }
        return resultFactory.ok().put("type", "SKILL");
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

    public Result initBossConfig(InputStream is) {
        try {
            String jsonStr = new Scanner(is, StandardCharsets.UTF_8).useDelimiter("\\A").next();
            JSONObject obj = new JSONObject(jsonStr);

            // Validate boss configuration structure
            if (!obj.has("B") || !obj.has("PlayerSkills")) {
                return Result.fail().put("msg", "Invalid boss configuration format. Missing 'B' or 'PlayerSkills'");
            }

            JSONArray bossArray = obj.getJSONArray("B");
            JSONArray skillsArray = obj.getJSONArray("PlayerSkills");

            // Validate boss HP values
            for (int i = 0; i < bossArray.length(); i++) {
                int hp = bossArray.getInt(i);
                if (hp <= 0) {
                    return Result.fail().put("msg", "Boss HP must be positive, found: " + hp);
                }
            }

            // Validate skills format
            for (int i = 0; i < skillsArray.length(); i++) {
                JSONArray skill = skillsArray.getJSONArray(i);
                if (skill.length() != 2) {
                    return Result.fail().put("msg", "Each skill must have exactly 2 values [damage, cooldown]");
                }
                int damage = skill.getInt(0);
                int cooldown = skill.getInt(1);
                if (damage <= 0 || cooldown < 0) {
                    return Result.fail().put("msg", "Invalid skill values: damage must be > 0, cooldown must be >= 0");
                }
            }

            log.info("Boss configuration loaded successfully: {} bosses, {} skills",
                    bossArray.length(), skillsArray.length());

            return Result.ok().put("msg", "Boss configuration loaded successfully")
                    .put("bossCount", bossArray.length())
                    .put("skillCount", skillsArray.length());

        } catch (Exception e) {
            log.error("Failed to parse boss configuration", e);
            return Result.fail().put("msg", "Failed to parse boss configuration: " + e.getMessage());
        }
    }

}
