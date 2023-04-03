package com.lyc.performance.luban.storage.prebe;

import lombok.Data;

import java.util.Objects;

@Data
public class HeartInfo {
    private String appCode;
    private long timeStamp;
    private String vmName;
    private Integer commandCode;
    private String status;
    private String ip;

    public HeartInfo() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HeartInfo heartInfo = (HeartInfo) o;
        return  Objects.equals(appCode, heartInfo.appCode) &&
                Objects.equals(vmName, heartInfo.vmName) &&
                Objects.equals(ip, heartInfo.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appCode, timeStamp, vmName, commandCode, status, ip);
    }
}
