package com.caicai.game.maze;

import com.caicai.game.utils.Point;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class Maze {
    List<List<BlockType>> board;
    String name;
//    G a
//
    Set<Point> gold;
    Set<Point> lockers;
    Set<Point> traps;
//    final Point BOSS;



}
