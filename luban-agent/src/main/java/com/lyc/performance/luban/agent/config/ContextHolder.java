package com.lyc.performance.luban.agent.config;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ContextHolder {

    private static ThreadLocal<Boolean> contextThreadLocal = new ThreadLocal<>();

    public static void setLocal(Boolean flag) {
        contextThreadLocal.set(flag);
    }

    public static Boolean getLocal() {
        return contextThreadLocal.get();
    }

    public static void clear() {
        contextThreadLocal.remove();
    }

}
