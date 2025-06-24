package com.caicai.game.common;

import java.util.List;
import java.util.Map;

//@Component
public class ResultFactory {
    //    here store the quote of the obj
//    List<Object> requiredFields;
    Map<String, Object> requiredFields;
    ResultFactory resultFactory;
    public ResultFactory(Map requiredFields) {
        this.requiredFields = requiredFields;
        this.resultFactory = new ResultFactory(requiredFields);
    }

    public Result ok() {
        Result res = new Result();
        res.put("code", "0");
        requiredFields.forEach((k,v) -> {
            res.put(k,v);
        });
        return res;
    }
    public Result fail() {
        Result res = new Result();
        res.put("code", "-1");
        res.put("msg", "fail");
        return res;
    }
}
