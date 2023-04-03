package com.lyc.performance.luban.application.hook;

import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChannelHandlerContextWrapper {
    private ChannelHandlerContext ctx;
    private long lastHeatTime;
}
