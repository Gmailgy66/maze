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
        return res;
    }

    public Result handleBlock(Point point) {
        log.info("handleBlock: {}", point);
        switch (maze.getBlock(point)) {
//            the methods below are all atomic so they should return a full result when called
            case START -> S();
            case GOLD -> G();
            case EXIT -> E();
            case BOSS -> B();
            case TRAP -> T();
            case PATH -> P();
            case SKILL -> Sk();
            default -> log.warn("Unknown point: {}", point);
        }
        return Result.ok();
    }

    private void P() {

    }

    public Result getNextPoint() {
//!do move point and return the effect of handleBlock
        log.info("getNextPoint");
//        op the block here
//      ===================================
        return handleBlock(this.curPos);
    }

    //
    public void G() {

    }

    /**
     * Handle the boss block.
     * return a list of skills in turn
     */
    public void B() {
        resultFactory.ok();
    }

    /**
     * Handle the start block.
     * Initialize the hero's position and other necessary attributes.
     */
    public void S() {

    }

    public void L() {

    }

    public void T() {

    }

    public void E() {
        log.info("Exiting the game.");
    }

    /**
     * Handle the skill block.
     * just tell
     */
    private void Sk() {
        resultFactory.ok().put("skill", hero.getSkills());
    }
}
