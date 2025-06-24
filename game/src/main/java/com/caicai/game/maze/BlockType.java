package com.caicai.game.maze;

import lombok.Getter;

public enum BlockType {
    START("S"), EXIT("E"), WALL("#"), PATH("-"), TRAP("T"), GOLD("G", 10)
    //        public static final int scorePlus = 10;
    , LOCKER("L"), BOSS("B"), //    ENEMY("X"),
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

    @Getter
    final int score;

    static boolean isSpecial(BlockType type) {
        return type != WALL && type != PATH;
    }

    public static int getTypeCnt() {
        return BlockType.values().length;
    }

}
