package com.lyc.performance.luban.agent.agentclient;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lyc.performance.luban.agent.transfer.TransferHolder;
import com.lyc.performance.luban.storage.prebe.HeartInfo;
import com.lyc.performance.luban.storage.util.IpUtil;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
public class AgentClientHandler extends ChannelInboundHandlerAdapter {

    private String appCode;
    private String vmName;

    public AgentClientHandler(String appCode,String vmName) {
        this.appCode = appCode;
        this.vmName = vmName;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("luban client receive msg:{}",msg.toString());
        HeartInfo command = JSONObject.parseObject(msg.toString(), HeartInfo.class);
        String appCode = command.getAppCode();
        String vmName = command.getVmName();
        Integer commandCode = command.getCommandCode();
        if (appCode.equals(this.appCode) && vmName.equals(this.vmName)) {
            if (Objects.nonNull(commandCode) && 1 == commandCode)
                // 第一次把涉及到修改的增强之后的类的字节码回退到原始状态
                TransferHolder.setLocal(true);
                attachVm(vmName);

                // 30秒之后再次attach做最终修改
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        attachVm(vmName);
                    }
                },30 * 1000);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        HeartInfo heartInfo = new HeartInfo();
        heartInfo.setIp(IpUtil.perferAddress("10."));
        heartInfo.setAppCode(appCode);
        heartInfo.setVmName(vmName);
        heartInfo.setStatus("0");
        heartInfo.setTimeStamp(System.currentTimeMillis());
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ctx.channel().writeAndFlush(JSON.toJSONString(heartInfo));
            }
        },1000,1000*10);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    //超时则关闭链路
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.READER_IDLE) {
                InetSocketAddress inetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
                String ip = inetSocketAddress.getAddress().getHostAddress();
                System.out.println((ip + ":" + inetSocketAddress.getPort() + "close"));
                ctx.channel().close();
            }
        }
    }

    private void attachVm(String targetVmName) {
        try {
            List<VirtualMachineDescriptor> list = VirtualMachine.list();
            for (VirtualMachineDescriptor vmd : list) {
                if (vmd.displayName().equals(targetVmName)) {
                    VirtualMachine virtualMachine = VirtualMachine.attach(vmd.id());
                    String path = getAgentJarPath();
                    virtualMachine.loadAgent(path,getAgentArgs());
                    virtualMachine.detach();
                }
            }
        } catch (Exception e) {
            log.error("attachVm:{} error:{}",targetVmName,e.toString());
        }
    }

    private String getAgentJarPath() {
        String osName = System.getProperty("os.name").toLowerCase();
        String path = AgentClientHandler.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        return osName.startsWith("windows")
                ? path.substring(1)
                : path;
    }

    private String getAgentArgs() {
        return "test";
    }

}
