package com.lyc.performance.luban.storage.env;

import com.lyc.performance.luban.storage.annotation.PropertyBind;
import com.lyc.performance.luban.storage.util.YCReflectionUtil;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PerformanceEnvironment {

    private static final String ENV_FILE_NAME_PRO = "performance-luban.properties";
    private static final String ENV_FILE_NAME_YML = "performance-luban.yml";

    private Map<String,Object> env = new HashMap<>();

    private volatile static PerformanceEnvironment environment;

    private PerformanceEnvironment() {
        Map<String, Object> ymlMap = AbstractConfigParser.createParser("yml").parseConfigFile(ENV_FILE_NAME_YML);
        env.putAll(ymlMap);
        Map<String, Object> proMap = AbstractConfigParser.createParser("properties").parseConfigFile(ENV_FILE_NAME_PRO);
        env.putAll(proMap);
    }

    public static PerformanceEnvironment getInstance() {
        if(Objects.isNull(environment)) {
            synchronized (PerformanceEnvironment.class) {
                if(Objects.isNull(environment)) {
                    environment = new PerformanceEnvironment();
                }
            }
        }
        return environment;
    }

    public Object getProperty(String key) {
        return env.get(key);
    }

    public <T> T getProperty(String key,Class<? extends T> clazz) {
        Object res = env.get(key);
        if (Objects.isNull(res)) {
            return null;
        }
        if (res.getClass().isAssignableFrom(clazz)) {
            return (T)res;
        }
        throw new RuntimeException("can not find key:" + key + " with type of :" + clazz);
    }

    public <T> T bindProperty(Class<? extends T> clazz) throws Exception {
        PropertyBind annotation = clazz.getAnnotation(PropertyBind.class);
        if (Objects.isNull(annotation)) {
            throw new RuntimeException("class:"+ clazz + " not annotated with @ PropertyBind");
        }

        String prefix = annotation.prefix();
        if (StringUtils.isBlank(prefix)) {
            throw new RuntimeException("Annotation @PropertyBind must have prefix attribute");
        }
        T instance = clazz.newInstance();
        List<Field> fields = YCReflectionUtil.objAllFields(clazz);
        for (Field field : fields) {
            field.setAccessible(true);
            String name = field.getName();
            String key = prefix + "." + name;
            Object val = getProperty(key);
            field.set(instance,val);
        }
        return instance;
    }

}
