package com.lyc.performance.luban.agent.interceptor.http;

import com.alibaba.fastjson.JSON;
import com.lyc.performance.luban.agent.config.ContextHolder;
import com.lyc.performance.luban.agent.config.LuBanGlobalConfig;
import com.lyc.performance.luban.storage.constant.AgentConstant;
import com.lyc.performance.luban.storage.repository.file.FileStorageRepository;
import com.lyc.performance.luban.storage.util.YCReflectionUtil;
import com.lyc.performance.luban.storage.vo.HttpMethodVo;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Slf4j
public class HttpInterceptor {

    private FileStorageRepository repository = FileStorageRepository.getInstance();

    @RuntimeType
    public Object intercept(@This Object target, @Origin Method targetMethod, @Super Object superObj, @AllArguments Object[] args, @SuperCall Callable<?> zuper) {
        Object result = null;
        try {
            Boolean local = ContextHolder.getLocal();
            HttpMethodVo httpConfig = getHttpConfig(args);
            result = Objects.nonNull(local) && local && Objects.nonNull(httpConfig)
                    ? new LuBanHttpResponse(httpConfig.getResponseContent())
                    : zuper.call();
        } catch (Exception e) {
            log.error("http拦截器异常:{}",e.toString());
        }
        return result;
    }

    private HttpMethodVo getHttpConfig(Object[] args) throws Exception {
        // 第一个参数是:HttpRoute route
        // 第二个参数是：HttpRequestWrapper request
        // 第三个参数是:HttpClientContext context
        // 第四个参数是:HttpExecutionAware execAware
        Object requestWrapper = args[1];
        String method = (String) YCReflectionUtil.invokeMethod(requestWrapper, "getMethod", null);
        log.info("http远程调用的方法类型是:{}",method);

        URI httpUri = (URI)YCReflectionUtil.invokeMethod(requestWrapper, "getURI", null);
        log.info("http远程调用的路径是:{}",httpUri.getRawPath());

        Object httpHost = YCReflectionUtil.invokeMethod(requestWrapper, "getTarget", null);
        String hostStr =(String)YCReflectionUtil.invokeMethod(httpHost, "getHostName", null);
        log.info("http远程调用的主机是:{}",hostStr);

        int hostPort = (int)YCReflectionUtil.invokeMethod(httpHost, "getPort", null);
        log.info("http远程调用主机端口号是:{}",hostPort);

        String appCode = LuBanGlobalConfig.getLubanAppCode();

        HttpMethodVo relVo = new HttpMethodVo();
        relVo.setAppCode(appCode);
        relVo.setConstant(AgentConstant.HTTP_METHOD);
        relVo.setRequestType(method);
        relVo.setUri(httpUri.getRawPath());
        relVo.setPort(hostPort);
        relVo.setHostAddress(hostStr);

        List<HttpMethodVo> httpMethodVos = repository
                .readVosByAppCode(appCode)
                .stream()
                .filter(vo -> vo.getConstant().equals(AgentConstant.HTTP_METHOD) && "0".equals(vo.getStatus()))
                .map(vo -> (HttpMethodVo)vo)
                .collect(Collectors.toList());

        log.info("查询到的http数据是:{}",JSON.toJSONString(httpMethodVos));
        for (HttpMethodVo httpMethodVo : httpMethodVos) {
           if(httpMethodVo.equals(relVo)) {
               log.info("命中http配置：{}", JSON.toJSONString(httpMethodVo));
               return httpMethodVo;
           }
        }
        return null;
    }

}
