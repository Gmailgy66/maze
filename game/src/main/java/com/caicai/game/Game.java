package com.caicai.game;

import com.caicai.game.combat.Combat;
import com.caicai.game.common.*;
import com.caicai.game.conf.GameConf;
import com.caicai.game.maze.Maze;
import com.caicai.game.maze.MazeFactory;
import com.caicai.game.role.Hero;
import com.caicai.game.role.Skill;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

    public Result openCombat() {
        combat = new Combat(hero, maze.getBoss());
        log.info("openCombat");
        var result = resultFactory.ok();
        result.put("fight", "open combat!");
        return result;
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


    public Result nextTurn() {
        log.info("nextTurn");
        var result = resultFactory.ok();
        result.put("fight", combat.next());
        return result;
    }

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
        System.out.println(path);
        return resultFactory.ok().put("path", path);
    }

    // the data should contains the actually pos is

}
