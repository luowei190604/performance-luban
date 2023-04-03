package com.lyc.performance.luban.application.vo;

import com.lyc.performance.luban.storage.prebe.HeartInfo;
import lombok.Data;

import java.util.List;

@Data
public class AppInfo {

    private String appCode;
    private String vmName;
    private int instanceCount;
    private List<HeartInfo> instances;

}
