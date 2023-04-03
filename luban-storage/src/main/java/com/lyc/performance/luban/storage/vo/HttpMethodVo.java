package com.lyc.performance.luban.storage.vo;

import com.lyc.performance.luban.storage.constant.AgentConstant;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

@Data
public class HttpMethodVo extends BaseVo {
    private String hostAddress;
    private int port;
    private String uri;
    private String requestType;
    private String contentType;
    private String responseContent;

    public HttpMethodVo() {
        this.setConstant(AgentConstant.HTTP_METHOD);
    }

    public HttpMethodVo(String hostAddress,int port,String uri,String requestType,String contentType) {
        this.setConstant(AgentConstant.HTTP_METHOD);
        this.hostAddress = hostAddress;
        this.port = port;
        this.uri = uri;
        this.requestType = requestType;
        this.contentType = contentType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HttpMethodVo that = (HttpMethodVo) o;
        return port == that.port &&
                Objects.equals(hostAddress, that.hostAddress) &&
                Objects.equals(uri, that.uri) &&
                StringUtils.equalsIgnoreCase(requestType,that.requestType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), hostAddress, port, uri, requestType, contentType);
    }
}
