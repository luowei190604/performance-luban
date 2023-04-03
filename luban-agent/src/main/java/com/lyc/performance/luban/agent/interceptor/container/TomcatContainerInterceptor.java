package com.lyc.performance.luban.agent.interceptor.container;

import com.lyc.performance.luban.agent.config.ContextHolder;
import com.lyc.performance.luban.storage.util.YCReflectionUtil;
import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.Callable;

public class TomcatContainerInterceptor {

    @RuntimeType
    public Object intercept(@This Object target, @Origin Method targetMethod, @Super Object superObj, @AllArguments Object[] args, @SuperCall Callable<?> zuper) {
        Object result = null;
        try {
            Object arg = args[0];
            Object getHeader = YCReflectionUtil.invokeMethod(arg, "getHeader", "luBan_mock");
            if (Objects.nonNull(getHeader) && (getHeader.equals("true") || getHeader.equals("false"))) {
                Boolean flag = Boolean.parseBoolean(getHeader.toString());
                ContextHolder.setLocal(flag);
            }
            return zuper.call();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ContextHolder.clear();
        }
        return result;
    }
}
