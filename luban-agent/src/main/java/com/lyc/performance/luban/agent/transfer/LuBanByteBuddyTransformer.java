package com.lyc.performance.luban.agent.transfer;

import com.lyc.performance.luban.agent.interceptor.container.TomcatContainerInterceptor;
import com.lyc.performance.luban.agent.interceptor.http.HttpInterceptor;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.utility.JavaModule;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class LuBanByteBuddyTransformer implements AgentBuilder.Transformer {

    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule) {

        String actualName = typeDescription.getActualName();
        if (actualName.equals("org.apache.http.impl.execchain.MainClientExec")) {
            return builder
                    .method(named("execute"))
                    .intercept(MethodDelegation.to(new HttpInterceptor()));
        }

        if(actualName.equals("org.apache.catalina.connector.CoyoteAdapter")) {
            return builder
                    .method(named("service"))
                    .intercept(MethodDelegation.to(new TomcatContainerInterceptor()));
        }

        return null;
    }
}
