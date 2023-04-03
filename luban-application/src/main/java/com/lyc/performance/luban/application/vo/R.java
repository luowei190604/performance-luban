package com.lyc.performance.luban.application.vo;

import lombok.Data;

@Data
public class R<T> {

    private int code;
    private String msg;
    private T data;

    private R() {

    }

    public static <T> R<T> success(T data) {
        R<T> response = new R<>();
        response.code = 0;
        response.msg = "";
        response.data = data;
        return response;
    }

    public static <T> R<T> failed(String msg) {
        R<T> response = new R<>();
        response.code = 1;
        response.msg = msg;
        return response;
    }
}
