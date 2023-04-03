package com.lyc.performance.luban.storage.vo;

import com.lyc.performance.luban.storage.constant.AgentConstant;
import lombok.Data;

import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

@Data
public class JavaMethodVo extends BaseVo{
    private String className;
    private String methodName;
    private String[] argsTypes;
    private String methodBody;

    public JavaMethodVo() {
        this.setConstant(AgentConstant.JAVA_METHOD);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JavaMethodVo vo = (JavaMethodVo) o;
        return Objects.equals(className, vo.className) &&
                Objects.equals(methodName, vo.methodName) &&
                Arrays.equals(argsTypes, vo.argsTypes);
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        hashCode += className.hashCode();
        hashCode += methodName.hashCode();
        for (String paramType : argsTypes) {
            hashCode += paramType.hashCode();
        }
        return hashCode;
    }
}
