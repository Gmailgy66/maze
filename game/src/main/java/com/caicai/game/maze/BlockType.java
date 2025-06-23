package com.caicai.game.maze;

import lombok.Getter;
import lombok.Setter;

public enum BlockType {
    START("S"),
    EXIT("E"),
    WALL("#"),
    PATH("-"),
    TRAP("T"),
    GOLD("G"),
    LOCKER("L"),
    BOSS("B"),
    //    ENEMY("X"),
    SKILL("$");

    @Getter
    private final String signal;
    @Setter
    @Getter
    int score;

    BlockType(String signal) {
        this.signal = signal;
    }

    static boolean isSpecial(BlockType type) {
        return type != WALL && type != PATH;
    }

    public static int getTypeCnt() {
        return BlockType.values().length;
    }

//    public String getSignal() {
//        return switch (signal) {
//            case "S" -> "S";
//            case "E" -> "E";
//            case "#" -> "#";
//            case " " -> " ";
//        }
//    }
}
