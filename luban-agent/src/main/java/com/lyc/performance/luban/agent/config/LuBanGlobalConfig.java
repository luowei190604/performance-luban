package com.lyc.performance.luban.agent.config;

import com.lyc.performance.luban.storage.env.PerformanceEnvironment;
import com.lyc.performance.luban.storage.vo.JavaMethodVo;
import javassist.CtMethod;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class LuBanGlobalConfig {

    private static final String LUBAN_CODE_NAME = "LUBAN_APP_CODE";
    private static String LUBAN_APP_CODE;

    private static final String LUBAN_HOST_NAME = "LUBAN_HOST";
    private static String LUBAN_HOST;

    private static final String TARGET_BOOT_NAME = "TARGET_APP_BOOT_NAME";
    private static String TARGET_APP_BOOT_NAME;

    private static ClassLoader bootLoader;

    // 用于存储被增强的类原始字节码，用于解决当某个配置失效时可以回退到原始字节码
    private static Map<String, byte[]> agentMap = new HashMap<>();

    // 已经增强的方法
    private static List<CtMethod> handledMethods = new ArrayList<>();

    // 已经增强的方法
    private static Map<JavaMethodVo,CtMethod> handledMethodMap = new HashMap<>();


    public static void initCode() {
        String luBanCode = System.getProperty(LUBAN_CODE_NAME);
        if(Objects.isNull(luBanCode)) {
            throw new RuntimeException("can not run with lu-ban without luBanCode,please run app with -DLUBAN_APP_CODE param!");
        }
        LUBAN_APP_CODE = luBanCode;

        // 鲁班控制台dashbord地址，首先从命令读取，如果没有，从配置文件中读取默认值
        String luBanHost = System.getProperty(LUBAN_HOST_NAME);
        LUBAN_HOST = Objects.isNull(luBanHost)
                ? PerformanceEnvironment.getInstance().getProperty("performance.luban-host", String.class)
                : luBanHost;

        String targetBootName = System.getProperty(TARGET_BOOT_NAME);
        if (Objects.isNull(targetBootName)) {
            throw new RuntimeException("can not run with lu-ban without targetBootName,please run app with -DTARGET_APP_BOOT_NAME param!");
        }
        TARGET_APP_BOOT_NAME = targetBootName;

        log.info("init luban-agent success,LUBAN_APP_CODE:{},LUBAN_HOST:{},TARGET_APP_BOOT_NAME:{}",LUBAN_APP_CODE,LUBAN_HOST,TARGET_APP_BOOT_NAME);
    }

    public static String getLubanAppCode() {
        return LUBAN_APP_CODE;
    }

    public static String getTargetBootName() {
        return TARGET_APP_BOOT_NAME;
    }

    public static String getLuBanHost() {
        return LUBAN_HOST;
    }

    public static void setBootLoader(ClassLoader loader) {
        synchronized (LuBanGlobalConfig.class) {
            if(Objects.isNull(bootLoader)) {
                log.info("准备设置springboot的fatjar类加载器");
                bootLoader = loader;
            }
        }
    }

    public static ClassLoader getBootLoader() {
        return bootLoader;
    }

    public static boolean judgeIsExist(String className) {
        return agentMap.containsKey(className);
    }

    public static void saveByteCode(String className,byte[] byteCode) {
        if(!agentMap.containsKey(className)) {
            agentMap.put(className,byteCode);
        }
    }

    public static byte[] originalCode(String className) {
        return agentMap.get(className);
    }

    public static void clearOriginalCode(String className) {
        agentMap.remove(className);
    }

    public static void saveMethod(JavaMethodVo javaMethodVo, CtMethod method) {
        // 只存储第一次增强时的字节码
        if (!handledMethodMap.containsKey(javaMethodVo)) {
            handledMethodMap.put(javaMethodVo,method);
            System.out.println("添加CtMethod成功!");
        }
    }

    public static CtMethod originalMethod(JavaMethodVo javaMethodVo) {
        return handledMethodMap.get(javaMethodVo);
    }

    public static void clearOriginalMethod(JavaMethodVo javaMethodVo) {
        handledMethodMap.remove(javaMethodVo);
    }

    public static Map<String,byte[]> getAgentMap() {
        return agentMap;
    }

    public static Map<JavaMethodVo,CtMethod> getHandledMethodMap() {
        return handledMethodMap;
    }
}
