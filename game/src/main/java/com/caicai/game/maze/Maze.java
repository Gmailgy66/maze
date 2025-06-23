package com.caicai.game.maze;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
@Data
public class Maze {
    List<List<BlockType>> board;

}
