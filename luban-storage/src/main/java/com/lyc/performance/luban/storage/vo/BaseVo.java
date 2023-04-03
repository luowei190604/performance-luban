package com.lyc.performance.luban.storage.vo;

import com.lyc.performance.luban.storage.constant.AgentConstant;
import lombok.Data;

import java.io.Serializable;

@Data
public class BaseVo implements Serializable {
    private AgentConstant constant;
    private String appCode;
    private String status; // 0 正常使用 1 禁用
    private String configName; // 配置名称
    private String id; // 配置id
}
