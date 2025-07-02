package com.caicai.game.maze;

import lombok.Getter;

import java.util.Comparator;

/**
 * 迷宫方块类型的枚举
 * 定义了迷宫中所有可能出现的方块类型及其属性
 */
public enum BlockType implements Comparable<BlockType> {
    /** 起点，玩家开始的位置 */
    START("S"), 
    /** 终点，完成迷宫的目标位置 */
    EXIT("E"), 
    /** 墙壁，玩家不能通过的障碍物 */
    WALL("#"), 
    /** 普通路径，玩家可以通行的区域 */
    PATH("-"), 
    /** 陷阱，会对玩家造成负面影响 */
    TRAP("T"), 
    /** 金币，可以增加玩家的分数 */
    GOLD("G", 10),
    /** 锁，需要钥匙才能打开的特殊障碍 */
    LOCKER("L"), 
    /** Boss，游戏中的强大敌人 */
    BOSS("B"), 
    /** 技能，可以被玩家获取的特殊能力 */
    SKILL("$");
    
    /** 方块在地图上显示的符号 */
    @Getter
    private final String signal;

    /**
     * 构造方块类型
     * @param signal 方块的显示符号
     * @param args 可选参数，如果包含Integer则作为分数
     */
    BlockType(String signal, Object... args) {
        this.signal = signal;
        if (args != null && args.length > 0 && args[0] instanceof Integer) {
            this.score = (Integer) args[0];
        } else {
            this.score = 0; // default score if not specified
        }
    }

    /** 金币的分数常量 */
    public static final int GOLD_SCORE = 50;
    /** 陷阱的分数常量 */
    public static final int TRAP_SCORE = -30;
    /** 假出口的分数常量 */
    public static final int FAKE_EXIT_SCORE = 9999;
    public static final int FAKE_BOSS_SCORE = 9999;
    public static final int FAKE_LOCKER_SCORE = 9999;

    /** 方块的分数值 */
    @Getter
    final int score;

    /**
     * 判断方块类型是否为特殊类型
     * @param type 要判断的方块类型
     * @return 如果不是墙壁或普通路径则返回true
     */
    static boolean isSpecial(BlockType type) {
        return type != WALL && type != PATH;
    }

    /**
     * 获取方块类型的排序优先级
     * @param type 要获取排序优先级的方块类型
     * @return 方块类型的排序优先级值
     */
    public static int getOrder(BlockType type) {
        return switch (type) {
            case GOLD -> 10;
            case TRAP -> -3;
            default -> 0;
        };
    }

    /**
     * 获取方块类型的总数
     * @return 枚举中定义的方块类型总数
     */
    public static int getTypeCnt() {
        return BlockType.values().length;
    }

    /**
     * 方块类型的分数比较器
     * 用于根据分数对方块类型进行排序
     */
    static class CompareByScore implements Comparator<BlockType> {
        @Override
        public int compare(BlockType o1, BlockType o2) {
            return Integer.compare(o1.getScore(), o2.getScore());
        }
    }
}
