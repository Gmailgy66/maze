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
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.caicai.game.maze.BlockType.GOLD;
import static com.caicai.game.maze.BlockType.WALL;
import static com.caicai.game.maze.PointUtil.getDis;

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

    public Result nextPointWithPath() {
        return resultFactory.ok()
                            .put("path", pathFinder.getBestWayByAStar(maze, curPos, maze.getEXIT()))
                            .put("target", maze.getEXIT());
    }
}

@Component
interface PathFinder {
    Point getNextPoint(Maze maze, Point curPos);

    default public List<Point> getBestWay(Maze maze, Point p1, Point p2) {
        return null;
    }

    default public List<Point> getBestWayByAStar(Maze maze, Point p1, Point p2) {
        // acctually there is only a way from the given point to the next
        // so here only need to get the distinct path as fast as possible
        Map<Point, Integer> cost = new HashMap<>();
        Map<Point, Point> par = new HashMap<>();
        Set<Point> vis = new HashSet<>();
        PriorityQueue<Point> openSet = new PriorityQueue<>((p1_, p2_) -> {
            return cost.get(p1_) + getDis(p1_, p2) > cost.get(p2_) + getDis(p2_, p2) ? 1 : -1;
        });
        cost.put(p1, 0);
        par.put(p1, p1);
        openSet.add(p1);
        List<Point> res = new ArrayList<>();
        while (openSet.isEmpty() == false) {
            Point cp = openSet.poll();
//            openSet.remove();
            vis.add(cp);
            List<Point> nxt = PointUtil.get4SurrendPoints(maze, cp);
            if (nxt.contains(p2)) {
                par.put(p2, cp);
                Point p = p2;
                res.add(p);
                while (par.get(p) != p1) {
                    p = par.get(p);
                    res.add(p);
                }
                break;
            }
            nxt.stream()
               .filter(p -> vis.contains(p) == false && maze.getBlock(p) != WALL && openSet.contains(p) == false)
               .forEach(p -> {
                   cost.put(p, cost.get(cp) + 1);
                   par.put(p, cp);
                   openSet.add(p);
               });
        }
        return res;
    }

    default int h() {
        return 0;
    }
}

@Component
class Dp implements PathFinder {
    @Override
    public Point getNextPoint(Maze maze, Point curPos) {
        List<Point> surrendPoints = PointUtil.getSurrendPoints(maze, curPos);
        return null;

        // surrendPoints.forEach();
    }
}

@Component
class Greedy implements PathFinder {
    @Override
    public Point getNextPoint(Maze maze, Point curPos) {
        List<Point> surrendPoints = PointUtil.getSurrendPoints(maze, curPos);
        // find a Point that is closest to curPos
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