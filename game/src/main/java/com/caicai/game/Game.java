package com.caicai.game;

import com.caicai.game.combat.Combat;
import com.caicai.game.common.Point;
import com.caicai.game.common.Result;
import com.caicai.game.common.ResultFactory;
import com.caicai.game.conf.GameConf;
import com.caicai.game.maze.Maze;
import com.caicai.game.maze.MazeFactory;
import com.caicai.game.maze.PointUtil;
import com.caicai.game.role.Hero;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.caicai.game.maze.BlockType.GOLD;

@Component
@Slf4j
public class Game {
    Maze maze;
    Combat combat;
    Hero hero;
    Point curPos;
    ResultFactory resultFactory;


    @Autowired
    MazeFactory mazeFactory;
    @Resource(name = "greedy")
    PathFinder pathFinder;
    @Autowired
    private GameConf gameConf;

    Map<String, Object> baseFields = new HashMap<>();

    Game() {
//        reset();


        resultFactory = new ResultFactory(baseFields);
        log.info("Game constructor");
    }

    public void reset() {
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
        curPos.setX((int) (Math.random() * gameConf.getSize()));
        curPos.setY((int) (Math.random() * gameConf.getSize()));
//        Point.randPoint(gameConf.getSize(), gameConf.getSize());
        return resultFactory.ok();
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
        result.put("fight", combat.next());
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

    default public List<Point> getBestWay(Maze maze, Point p1, Point p2) {
        return null;
    }

    default public List<Point> getBestWayByDp(Maze maze, Point p1, Point p2) {
        int x1 = p1.getX();
        int y1 = p1.getY();
        int x2 = p2.getX();
        int y2 = p2.getY();
        int w = y2 - y1;
        int h = x2 - x1;
//        get the smallest cost from p1 to p2
        int[][] dp = new int[h][w];
        Arrays.fill(dp, Arrays.copyOf(new int[w], Integer.MAX_VALUE));
        for (int i = 1; i <= h; i++) {
            for (int j = 1; j <= w; j++) {
//                dp[i][j] = dp[]
            }
        } return null;
    }
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
        List<Point> surrendPoints = PointUtil.getSurrendPoints(maze, curPos);
//        find a  Point that is closest to curPos
        Point best = surrendPoints.stream()
                                  .filter(p -> maze.getGold().contains(p))
                                  .max((p1, p2) -> {
                                      double score = maze.getBlock(p1)
                                                         .getScore();
                                      Double dis = PointUtil.getDis(p1, curPos);
                                      double score2 = maze.getBlock(p1)
                                                          .getScore();
                                      Double dis2 = PointUtil.getDis(p1, curPos);
                                      return (int) (score / dis) > (int) (score2 / dis2) ? 1 : -1;
                                  })
                                  .orElse(null);
        nextTar = best;
        return null;
    }

    public Point nextTar = null;

    public Point getExit(Maze maze, Point point) {
        return null;
    }


}