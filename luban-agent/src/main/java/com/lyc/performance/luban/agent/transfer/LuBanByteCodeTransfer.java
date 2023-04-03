package com.lyc.performance.luban.agent.transfer;

import com.lyc.performance.luban.agent.config.LuBanGlobalConfig;
import com.lyc.performance.luban.storage.repository.file.FileStorageRepository;
import com.lyc.performance.luban.storage.vo.BaseVo;
import com.lyc.performance.luban.storage.vo.JavaMethodVo;
import javassist.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Slf4j
public class LuBanByteCodeTransfer implements ClassFileTransformer {

    private FileStorageRepository repository = FileStorageRepository.getInstance();
    private ClassPool classPool = new ClassPool();
    private List<JavaMethodVo> baseVos;
    private Boolean isPreMain;
    private Boolean isAgentClear = Boolean.FALSE;


    public LuBanByteCodeTransfer(Boolean isPreMain) throws IOException {
        try {
            if (!isPreMain) {
                this.isAgentClear = TransferHolder.getControlSwitch();
            }
            classPool.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
            baseVos = repository.readVosByAppCode(LuBanGlobalConfig.getLubanAppCode()).stream()
                    .filter(vo -> vo.getStatus().equals("0") && vo instanceof JavaMethodVo)
                    .map(vo -> (JavaMethodVo)vo)
                    .collect(Collectors.toList());
            log.info("是否是增强清理:{}",isAgentClear);
        } catch (Exception e) {
            log.error("构造转化器失败:{}",e.toString());
        } finally {
            if(!isPreMain) {
                TransferHolder.setControlSwitch();
            }
        }

//        else {
//            Class<? extends ClassLoader> aClass = Thread.currentThread().getContextClassLoader().getClass();
//            classPool.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
//            baseVos = repository.readVosByAppCode(LuBanGlobalConfig.getLubanAppCode()).stream()
//                    .filter(vo ->  vo instanceof JavaMethodVo)
//                    .map(vo -> (JavaMethodVo)vo)
//                    .collect(Collectors.toList());
//
//            Map<JavaMethodVo, CtMethod> handledMethodMap = LuBanGlobalConfig.getHandledMethodMap();
//            Set<JavaMethodVo> deleteSet = new HashSet<>();
//            Set<JavaMethodVo> javaMethodVos = handledMethodMap.keySet();
//            Set<JavaMethodVo> copySet = new HashSet<>(javaMethodVos);
//            for (JavaMethodVo oldVo : javaMethodVos) {
//                long count = baseVos.stream().filter(dbVo -> judgeMatch(oldVo, dbVo)).count();
//                // 如果有匹配的，则表示该次增强依然对该方法有调整，可以直接按照配置处理
//                if(count > 0) {
//                    deleteSet.add(oldVo);
//                }
//                // 如果在内存中有，在db中不存在，则该条配置被删除了,此时在做增强时，需要把该配置对应的方法还原
//                else {
//                    oldVo.setStatus("1");
//                }
//            }
//            copySet.removeAll(deleteSet);
//            baseVos.addAll(copySet);
//            log.info("当前应用的配置信息是:{}", JSON.toJSONString(baseVos));
//        }

    }

    private boolean judgeMatch(JavaMethodVo oldVo, JavaMethodVo dbVo) {
        return oldVo.getClassName().equals(dbVo.getClassName())
                && Arrays.equals(oldVo.getArgsTypes(),dbVo.getArgsTypes())
                && oldVo.getMethodName().equals(dbVo.getMethodName());
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        byte[] transformed = classfileBuffer;
        if(Objects.isNull(className)) {
            return transformed;
        }
        if (Objects.nonNull(isAgentClear) && isAgentClear) {
            return LuBanGlobalConfig.originalCode(className.replace("/", "."));
        }

        try {
            initBootClassLoader(loader);
            classPool.appendClassPath(new LoaderClassPath(loader));
            String replaceClassName = className.replace("/", ".");
            JavaMethodVo jMethodVo = getJMethodVo(replaceClassName);
            if(Objects.nonNull(jMethodVo)) {
                CtClass ctClass = classPool.get(replaceClassName);
                if(ctClass.isFrozen()) {
                    ctClass.defrost();
                }
                CtMethod[] methods = ctClass.getMethods();
                for (CtMethod method : methods) {
                    if(hitConfigMethod(method,jMethodVo)) {
                        LuBanGlobalConfig.saveByteCode(replaceClassName,transformed.clone());
                        if ("0".equals(jMethodVo.getStatus())) {
                            log.info("class:{} methodName:{} hit agentConfig",ctClass.getName(),method.getName());
                            String methodBody = jMethodVo.getMethodBody();
                            String proxyByteCode = dynamicProxyMethod(method, methodBody);
                            if(ctClass.isFrozen()) {
                                ctClass.defrost();
                            }
                            method.insertBefore(proxyByteCode);
                        }
                    }
                }
                transformed = ctClass.toBytecode();
            }

        } catch (Throwable e) {
            log.error("luBan transfer byte coder error:{}",e.toString());
        }
        return transformed;
    }

    private void initBootClassLoader(ClassLoader loader) {
        if(Objects.nonNull(loader) && Objects.nonNull(loader.getClass())) {
            if (loader.getClass().getName().equals("org.springframework.boot.loader.LaunchedURLClassLoader")
                    && Objects.isNull(LuBanGlobalConfig.getBootLoader())) {
                log.info("springboot类加载器命中");
                LuBanGlobalConfig.setBootLoader(loader);
            }
        }
    }

    private String dynamicProxyMethod(CtMethod method, String configBody) throws NotFoundException {
        CtClass returnType = method.getReturnType();
        String simpleName = returnType.getSimpleName();

        StringBuilder methodBuilder = new StringBuilder();
        methodBuilder.append("java.lang.Boolean local = com.byd.performance.luban.agent.config.ContextHolder.getLocal();");
        methodBuilder.append(" if(\"true\".equals(String.valueOf(local))) {");
        methodBuilder.append(configBody);
        if (simpleName.equals("void")) {
            methodBuilder.append("return ;");
        }
        methodBuilder.append(" }");
        return methodBuilder.toString();
    }

    private JavaMethodVo getJMethodVo(String className) throws IOException {
        for (BaseVo vo : baseVos) {
            if(vo instanceof JavaMethodVo) {
                JavaMethodVo jvo = (JavaMethodVo)vo;
                String configClassName = jvo.getClassName();
                if(configClassName.equals(className)) {
                    return (JavaMethodVo)vo;
                }
            }
        }
        return null;
    }

    private boolean isCommonMethod(String methodName) {
        String commonMethodNames = "equals,finalize,toString,getClass,notifyAll,hashCode,wait,notify,main,clone";
        return commonMethodNames.contains(methodName);
    }

    private boolean hitConfigMethod(CtMethod method,BaseVo configClassVo) throws NotFoundException, CannotCompileException {
        JavaMethodVo jvo = (JavaMethodVo)configClassVo;
        String[] argsTypes = jvo.getArgsTypes();
        String methodName = jvo.getMethodName();

        if(!methodName.equals(method.getName())) {
            return false;
        }

        CtClass[] parameterTypes = method.getParameterTypes();
        if (argsTypes.length != parameterTypes.length) {
            return false;
        }

        for (int i=0;i<argsTypes.length;i++) {
            String argName = argsTypes[i];
            String ctName = parameterTypes[i].getName();
            if (!argName.equals(ctName)) {
                return false;
            }
        }
        return true;
    }
}
