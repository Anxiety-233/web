package com.lab.equipment.dto;

import lombok.Data;

@Data
public class ResultDTO<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> ResultDTO<T> success(T data) {
        ResultDTO<T> r = new ResultDTO<>();
        r.setCode(200);
        r.setMsg("操作成功");
        r.setData(data);
        return r;
    }

    public static <T> ResultDTO<T> success() {
        return success(null);
    }

    public static <T> ResultDTO<T> error(String msg) {
        ResultDTO<T> r = new ResultDTO<>();
        r.setCode(500);
        r.setMsg(msg);
        return r;
    }
}