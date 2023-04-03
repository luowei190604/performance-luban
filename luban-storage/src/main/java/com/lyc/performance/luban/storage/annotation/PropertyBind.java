package com.lyc.performance.luban.storage.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PropertyBind {

    // 属性绑定前缀
    String prefix();
}
