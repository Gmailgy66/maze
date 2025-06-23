package com.caicai.game.maze;

import lombok.Getter;
import lombok.Setter;

public enum BlockType {
    START("S"),
    EXIT("E"),
    WALL("#"),
    PATH(" "),
    TRAP("T"),
    GOLD("G"),
    LOCKER("L"),
    BOSS("B"),
    ENEMY("X"),
    SKILL("SK");

    @Getter
    private final String signal;
    @Setter
    @Getter
    int score;

    BlockType(String signal) {
        this.signal = signal;
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
