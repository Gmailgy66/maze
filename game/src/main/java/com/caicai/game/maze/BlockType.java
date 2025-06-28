package com.caicai.game.maze;

import lombok.Getter;

import java.util.Comparator;

public enum BlockType implements Comparable<BlockType> {
    START("S"), EXIT("E"), WALL("#"), PATH("-"), TRAP("T"), GOLD("G", 10)
    // public static final int scorePlus = 10;
    , LOCKER("L"), BOSS("B"), // ENEMY("X"),
    SKILL("$");
    @Getter
    private final String signal;

    BlockType(String signal, Object... args) {
        this.signal = signal;
        if (args != null && args.length > 0 && args[0] instanceof Integer) {
            this.score = (Integer) args[0];
        } else {
            this.score = 0; // default score if not specified
        }
    }


    public static final int GOLD_SCORE = 5;
    public static final int TRAP_SCORE = -3;
    public static final int FAKE_EXIT_SCORE = 9999;
    @Getter
    final int score;

    static boolean isSpecial(BlockType type) {
        return type != WALL && type != PATH;
    }

    public static int getOrder(BlockType type) {
        return switch (type) {
            case GOLD -> 10;
            case TRAP -> -3;
            default -> 0;
        };
    }

    public static int getTypeCnt() {
        return BlockType.values().length;
    }

    static class CompareByScore implements Comparator<BlockType> {
        @Override
        public int compare(BlockType o1, BlockType o2) {
            return Integer.compare(o1.getScore(), o2.getScore());
        }
    }
}
