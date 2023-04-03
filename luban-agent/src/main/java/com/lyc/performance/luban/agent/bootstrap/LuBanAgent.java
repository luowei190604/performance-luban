package com.lyc.performance.luban.agent.bootstrap;

import com.lyc.performance.luban.agent.agentclient.AgentClient;
import com.lyc.performance.luban.agent.config.LuBanGlobalConfig;
import com.lyc.performance.luban.agent.core.LuBanByteBuddyListener;
import com.lyc.performance.luban.agent.transfer.LuBanByteBuddyTransformer;
import com.lyc.performance.luban.agent.transfer.LuBanByteCodeTransfer;
import com.lyc.performance.luban.storage.constant.AgentConstant;
import com.lyc.performance.luban.storage.repository.file.FileStorageRepository;
import com.lyc.performance.luban.storage.vo.BaseVo;
import com.lyc.performance.luban.storage.vo.JavaMethodVo;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.*;
import java.util.stream.Collectors;

import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;

@Slf4j
public class LuBanAgent {

    private static List<String> interceptList= new ArrayList<>();

    static {
        interceptList.add("org.apache.http.impl.execchain.MainClientExec");
        interceptList.add("org.apache.catalina.connector.CoyoteAdapter");
    }

    public static void premain(String agentArgs, Instrumentation instrumentation) throws Exception {
        log.info("LuBan static agent start to load");
        LuBanGlobalConfig.initCode();
        AgentBuilder builder = new AgentBuilder.Default()
                .ignore(nameStartsWith("net.bytebuddy").or(nameStartsWith("io.commons")))
                .type(getTypeMatcher())
                .transform(new LuBanByteBuddyTransformer())
                .with(new LuBanByteBuddyListener());
        builder.installOn(instrumentation);
        instrumentation.addTransformer(new LuBanByteCodeTransfer(true),true);
        startCommandServer(LuBanGlobalConfig.getLubanAppCode(),LuBanGlobalConfig.getTargetBootName());
        log.info("LuBan static agent transfer added");
    }

    private static ElementMatcher<? super TypeDescription> getTypeMatcher() {

        return new ElementMatcher.Junction.AbstractBase<NamedElement>() {
            @Override
            public boolean matches(NamedElement target) {
                String actualName = target.getActualName();
                return interceptList.contains(actualName);
            }
        };
    }


    private static void startCommandServer(String appCode,String vmName){
        new Thread("luban-agent-heartThread") {
            @Override
            public void run() {
                log.info("LuBan client start!");
                String luBanHost = LuBanGlobalConfig.getLuBanHost();
                AgentClient agentClient = new AgentClient(appCode,vmName);
                doConnect(agentClient,luBanHost);
            }

            private void doConnect(AgentClient agentClient,String luBanHost) {
                try {
                    agentClient.connect2Server(luBanHost,8815);
                } catch (Exception e) {
                    log.error("connect 2 server:{} error:{}",luBanHost,e.toString());
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            doConnect(agentClient,luBanHost);
                        }
                    },1000*10);
                }
            }
        }.start();
    }

    public static void agentmain(String agentArgs, Instrumentation instrumentation) throws Exception {
        String appCode = LuBanGlobalConfig.getLubanAppCode();
        instrumentation.addTransformer(new LuBanByteCodeTransfer(false), true);
        List<Class<?>> dbClasses = dbConfigVos(appCode);
        List<Class<?>> memClass = memConfigVos();
        Set<Class<?>> classes = mergeClazz(dbClasses, memClass);
        for (Class<?> clazz : classes) {
            log.info("agentMain load class name is:{}",clazz.getName());
            instrumentation.retransformClasses(clazz);
        }
    }

    private static List<Class<?>> dbConfigVos(String appCode) throws IOException {
        FileStorageRepository repository = FileStorageRepository.getInstance();
        List<BaseVo> baseVos = repository.readVosByAppCode(appCode);
        return baseVos.stream().filter(vo -> vo.getConstant().equals(AgentConstant.JAVA_METHOD) && vo.getStatus().equals("0"))
                .map(vo -> (JavaMethodVo)vo)
                .map(LuBanAgent::transfer).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static Class<?> transfer(JavaMethodVo vo) {
        String className = vo.getClassName();
        return transfer(className);
    }

    private static Class<?> transfer(String className) {
        try {
            return Class.forName(className,false, LuBanAgent.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            try {
                return Class.forName(className,false, LuBanGlobalConfig.getBootLoader());
            } catch (ClassNotFoundException ex) {
                log.error("load class:{} error:{}",className,e.getMessage());
            }
        }
        return null;
    }


    private static List<Class<?>> memConfigVos() {
//        Map<JavaMethodVo, CtMethod> handledMethodMap = LuBanGlobalConfig.getHandledMethodMap();
//        Set<JavaMethodVo> javaMethodVos = handledMethodMap.keySet();
//        return javaMethodVos.stream()
//                .map(LuBanAgent::transfer)
//                .collect(Collectors.toList());
        Map<String, byte[]> agentMap = LuBanGlobalConfig.getAgentMap();
        return agentMap.keySet()
                .stream()
                .map(LuBanAgent::transfer)
                .collect(Collectors.toList());

    }

    private static Set<Class<?>> mergeClazz(List<Class<?>> dbClazz,List<Class<?>> memClazz) {
        Set<Class<?>> clazzSet = new HashSet<>();
        clazzSet.addAll(dbClazz);
        clazzSet.addAll(memClazz);
        return clazzSet;
    }

}