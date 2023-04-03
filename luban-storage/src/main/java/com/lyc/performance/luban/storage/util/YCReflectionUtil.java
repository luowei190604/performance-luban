package com.lyc.performance.luban.storage.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class YCReflectionUtil {

    /**
     * 反射调用方法
     *
     * @param target 对象
     * @param methodName 方法名称
     * @param parameters 参数列表
     * @return 调用结果
     * @throws Exception 异常
     */
    public static Object invokeMethod(Object target,String methodName,Object... parameters) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> targetClazz = target.getClass();
        if(Objects.isNull(parameters)) {
            Method method = null;
            try {
                method = targetClazz.getDeclaredMethod(methodName,null);
            } catch (NoSuchMethodException e) {
                method = targetClazz.getMethod(methodName,null);
            }
            return method.invoke(target,null);
        } else {
            Class<?>[] types = new  Class<?>[parameters.length];
            for (int i = 0;i<parameters.length;i++) {
                types[i] = parameters[i].getClass();
            }
            Method method = null;
            try {
                method = targetClazz.getDeclaredMethod(methodName,types);
            } catch (NoSuchMethodException e) {
                method = targetClazz.getMethod(methodName,null);
            }
            return method.invoke(target,parameters);
        }

    }

    /**
     * 获取类及所有父类的字段名称列表
     *
     * @param targetClazz 对象Clazz
     * @return 获取类及所有父类的字段名称
     */
    public static List<Field> objAllFields(Class<?> targetClazz) {
        LinkedList<Field> fields = new LinkedList<>();
        while (targetClazz.getSuperclass() != null) {
            Field[] declaredFields = targetClazz.getDeclaredFields();
            for (Field f : declaredFields) {
                f.setAccessible(true);
                if (f.getName().equals("serialVersionUID")) {
                    continue;
                }
                fields.add(f);
            }
            targetClazz = targetClazz.getSuperclass();
        }
        return fields;
    }

    /**
     * 字段名称和字段Field映射
     *
     * @param clazz 对象Clazz
     * @return 字段名称和字段Field映射
     */
    public static Map<String, Field> getJFieldClazzMap(Class<?> clazz) {
        Map<String, Field> nameClassMap = new HashMap<>();
        List<Field> fields = objAllFields(clazz);
        fields.forEach(field -> {
            field.setAccessible(true);
            String jName = field.getName();
            nameClassMap.put(jName,field);
        });
        return nameClassMap;
    }

    public static Method getMethod(Object target,String methodName,Object[] args) throws NoSuchMethodException {
        Class<?> targetClazz = target.getClass();
        Class<?>[] classTypes =  new Class<?>[args.length];
        int count = 0;
        for (Object obj : args) {
            classTypes[count] = obj.getClass();
            count++;
        }
        return targetClazz.getDeclaredMethod(methodName, classTypes);
    }
}
