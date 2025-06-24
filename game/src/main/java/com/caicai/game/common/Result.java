package com.caicai.game.common;

import com.caicai.game.role.Hero;

import java.util.HashMap;

public class Result extends HashMap<String, Object> {
    Hero hero;

    //    Maze maze;
// new signal
    static public Result ok() {
        Result res = new Result();
        res.put("code", "0");
        return res;
    }

    public Result put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    static public Result fail() {
        Result res = new Result();
        res.put("code", "-1");
        return res;
    }
}
