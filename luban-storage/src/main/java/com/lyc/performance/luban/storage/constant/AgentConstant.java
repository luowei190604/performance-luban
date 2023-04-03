package com.lyc.performance.luban.storage.constant;

public enum AgentConstant {
    JAVA_METHOD(0,"java方法"),
    DUBBO_METHOD(1,"dubbo方法"),
    HTTP_METHOD(2,"http调用");

    private int code;
    private String des;

    private AgentConstant(int code, String des) {
        this.code = code;
        this.des = des;
    }
}
