package com.lyc.performance.luban.application.hook;

import com.alibaba.fastjson.JSONObject;
import com.lyc.performance.luban.storage.prebe.HeartInfo;
import com.lyc.performance.luban.storage.repository.file.HeartInfoFileStorageRepository;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Data
public class AgentMainServerHandler extends ChannelInboundHandlerAdapter {

    private ConcurrentHashMap<String,ChannelHandlerContextWrapper> appCodeMap = new ConcurrentHashMap<>();
    private HeartInfoFileStorageRepository heartInfoFileStorageRepository = HeartInfoFileStorageRepository.getInstance();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("{} 上线了",getClientAddress(ctx));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.debug("receive client :{} msg:{}",getClientAddress(ctx),msg.toString());
        HeartInfo heartInfo = JSONObject.parseObject(msg.toString(), HeartInfo.class);
        heartInfoFileStorageRepository.saveOrUpdateHeartInfo(heartInfo);

        String appCode = heartInfo.getAppCode();
        String vmName = heartInfo.getVmName();
        String ip = heartInfo.getIp();
        String key = String.join("_",appCode,vmName,ip);
        ChannelHandlerContextWrapper wrapper = new ChannelHandlerContextWrapper(ctx, heartInfo.getTimeStamp());
        appCodeMap.put(key,wrapper);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private String getClientAddress(ChannelHandlerContext ctx) {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        String ip = inetSocketAddress.getAddress().getHostAddress();
        int port = inetSocketAddress.getPort();
        return ip + ":" + port;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("{} 下线了",getClientAddress(ctx));
    }

    public void checkClientState() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @SneakyThrows
            @Override
            public void run() {
                long checkTime = System.currentTimeMillis();
                Set<String> removeKeySet = new HashSet<>();
                Set<Map.Entry<String, ChannelHandlerContextWrapper>> entries = appCodeMap.entrySet();
                for (Map.Entry<String, ChannelHandlerContextWrapper> entry : entries) {
                    ChannelHandlerContextWrapper wrapper = entry.getValue();
                    long lastHeatTime = wrapper.getLastHeatTime();
                    if(checkTime - lastHeatTime > 1000 * 2) {
                        removeKeySet.add(entry.getKey());
                    }
                }
                if (!CollectionUtils.isEmpty(removeKeySet)) {
                    for (String s : removeKeySet) {
                        appCodeMap.remove(s);
                        String[] arr = s.split("_");
                        HeartInfo heartInfo = new HeartInfo();
                        heartInfo.setAppCode(arr[0]);
                        heartInfo.setVmName(arr[1]);
                        heartInfo.setIp(arr[2]);
                        heartInfo.setStatus("1");
                        heartInfoFileStorageRepository.saveOrUpdateHeartInfo(heartInfo);
                    }
                }
            }
        },1000,1000*3);
    }
}
