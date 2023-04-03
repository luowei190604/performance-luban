package com.lyc.performance.luban.application.hook;

import com.alibaba.fastjson.JSON;
import com.lyc.performance.luban.storage.prebe.HeartInfo;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class LuBanProbeHook implements CommandLineRunner {

    private static final int LuBan_Pro_Port = 8815;
    private AgentMainServerHandler handler = new AgentMainServerHandler();

    @Override
    public void run(String... args) throws Exception {
        LuBanAgentMainServer server = new LuBanAgentMainServer(handler);
        server.start(LuBan_Pro_Port);
        handler.checkClientState();
    }

    public void attachVm(String appCode,String vmName,String targetIp) {
        ConcurrentHashMap<String, ChannelHandlerContextWrapper> appCodeMap = handler.getAppCodeMap();
        String key = String.join("_",appCode,vmName,targetIp);
        if(!appCodeMap.containsKey(key)) {
            String message = String.format("can not find appCode:%s message",appCode);
            throw new RuntimeException(message);
        }
        ChannelHandlerContextWrapper wrapper = appCodeMap.get(key);
        ChannelHandlerContext channelHandlerContext = wrapper.getCtx();
        if (Objects.isNull(channelHandlerContext)) {
            String message = String.format("can not find appCode:%s vmName:%s channel",appCode,vmName);
            throw new RuntimeException(message);
        }

        HeartInfo heartInfo = new HeartInfo();
        heartInfo.setAppCode(appCode);
        heartInfo.setVmName(vmName);
        heartInfo.setTimeStamp(System.currentTimeMillis());
        heartInfo.setCommandCode(1);
        channelHandlerContext.channel().writeAndFlush(JSON.toJSONString(heartInfo));
        log.info("send command success!");
    }

}
