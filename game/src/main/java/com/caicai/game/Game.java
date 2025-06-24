package com.caicai.game;

import com.caicai.game.common.Point;
import com.caicai.game.common.Result;
import com.caicai.game.common.ResultFactory;
import com.caicai.game.maze.Maze;
import com.caicai.game.maze.MazeFactory;
import com.caicai.game.role.Hero;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class Game {
    Maze maze;
    Hero hero;
    MazeFactory mazeFactory;
    Point curPos;
    ResultFactory resultFactory;

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
//        {
//        maze:
//
//        }
//
        return res;
    }

    public Result handleBlock(Point point) {
        log.info("handleBlock: {}", point);
        return switch (maze.getBlock(point)) {
//            the methods below are all atomic so they should return a full result when called
            case START -> S();
            case GOLD -> G();
            case EXIT -> E();
            case BOSS -> B();
            case TRAP -> T();
            case PATH -> P();
            case SKILL -> Sk();
            default -> resultFactory.fail()
                                    .put("error", "Unknown block type: " + maze.getBlock(point));
        };
    }

    private Result P() {

        return resultFactory.ok();


    }

    public Result getNextPoint() {
//!do move point and return the effect of handleBlock
        log.info("getNextPoint");
//        op the block here

//      ===================================
        return handleBlock(this.curPos);
    }

    //
    public Result G() {

        return resultFactory.ok();

//        log.info(" {} is {} ", "log", log);

    }

    /**
     * Handle the boss block.
     * return a list of skills in turn
     */
    public Result B() {

        return resultFactory.ok();
    }

    /**
     * Handle the start block.
     * Initialize the hero's position and other necessary attributes.
     */
    public Result S() {

        return resultFactory.ok();


    }

    public Result L() {

        return resultFactory.ok();


    }

    public Result T() {

        return resultFactory.ok();


    }

    public Result E() {

        log.info("Exiting the game.");
        return resultFactory.ok();

    }

    /**
     * Handle the skill block.
     * just tell
     */
    private Result Sk() {

        return resultFactory.ok();

    }
}
