package com.caicai.game;

import com.caicai.game.combat.Combat;
import com.caicai.game.common.Point;
import com.caicai.game.common.Result;
import com.caicai.game.common.ResultFactory;
import com.caicai.game.maze.Maze;
import com.caicai.game.maze.MazeFactory;
import com.caicai.game.maze.PointUtil;
import com.caicai.game.role.Hero;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.caicai.game.maze.BlockType.GOLD;

@Component
@Slf4j
public class Game {
    Maze maze;
    Hero hero;
    MazeFactory mazeFactory;
    Point curPos;
    ResultFactory resultFactory;
    @Resource(name = "greedy")
    PathFinder pathFinder;

    @Autowired
    Game(MazeFactory mazeFactory) {
        this.maze = mazeFactory.getMaze();
        this.hero = new Hero();
        Map<String, Object> baseFields = new HashMap<>();
        baseFields.put("hero", hero);
        baseFields.put("position", curPos);
//        baseFields.put("maze", maze);
        resultFactory = new ResultFactory(baseFields);
        log.info("Game constructor");
    }

    public Result init() {
        log.info("Game init");
        Result res = Result.ok();
        res.put("maze", maze);
        res.put("hero", hero);
        return res;
    }

    public Result openCombat() {
        log.info("openCombat");
        var result = resultFactory.ok();
        result.put("fight", "open combat!");
        return result;
    }

    public Result handleBlock(Point point) {
        log.info("handleBlock: {}", point);
        Result res = switch (maze.getBlock(point)) {
//            the methods below are all atomic so they should return a full result when called
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
        return res;
    }

    private Result P() {
        return resultFactory.ok().put("type", "PATH");
    }

    public Result getNextPoint() {
//!do move point and return the effect of handleBlock
        log.info("getNextPoint");
//        op the block here

        return handleBlock(this.curPos);
    }

    public Result nextTurn() {
        log.info("nextTurn");
        var result = resultFactory.ok();
        result.put("fight",combat.next());
        return result;
    }

    //
    public Result G() {
        hero.setScore(hero.getScore() + GOLD.getScore());
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
        return resultFactory.ok().put("type", "SKILL");
    }
}

@Component
interface PathFinder {
    Point getNextPoint(Maze maze, Point curPos);
}

@Component
class Dp implements PathFinder {
    @Override
    public Point getNextPoint(Maze maze, Point curPos) {
        List<Point> surrendPoints = PointUtil.getSurrendPoints(maze, curPos);
        return null;

//        surrendPoints.forEach();
    }
}

@Component
class Greedy implements PathFinder {
    @Override
    public Point getNextPoint(Maze maze, Point curPos) {
        // Implement the greedy pathfinding logic here
        return null; // Placeholder return value
    }
}