package com.caicai.game.maze;

import com.caicai.game.role.Hero;
import lombok.Data;

public enum BlockType {
    START("S"), EXIT("E"), WALL("#"), PATH(" "), TRAP("T"), RESOURCE("G"), LOCKER("L"), BOSS("B"), ENEMY("X");
    private final String signal;

    BlockType(String signal) {
        this.signal = signal;
    }
    public String getSignal() {
        return signal;
    }
}
