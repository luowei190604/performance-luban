package com.lyc.performance.luban.agent.interceptor.http;

import org.apache.http.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

public class LuBanHttpResponse implements CloseableHttpResponse {

    private HttpResponse original;

    public LuBanHttpResponse(String result) throws UnsupportedEncodingException {
        ProtocolVersion protocolVersion = new ProtocolVersion("http 1.1", 1, 1);
        BasicStatusLine statusLine = new BasicStatusLine(protocolVersion, 200, "sucess");
        BasicHttpResponse basicHttpResponse = new BasicHttpResponse(statusLine);
        StringEntity stringEntity = new StringEntity(result);
        basicHttpResponse.setEntity(stringEntity);
        this.original = basicHttpResponse;
    }

    @Override
    public void close() throws IOException {

    }

    public StatusLine getStatusLine() {
        return this.original.getStatusLine();
    }

    public void setStatusLine(StatusLine statusline) {
        this.original.setStatusLine(statusline);
    }

    public void setStatusLine(ProtocolVersion ver, int code) {
        this.original.setStatusLine(ver, code);
    }

    public void setStatusLine(ProtocolVersion ver, int code, String reason) {
        this.original.setStatusLine(ver, code, reason);
    }

    public void setStatusCode(int code) throws IllegalStateException {
        this.original.setStatusCode(code);
    }

    public void setReasonPhrase(String reason) throws IllegalStateException {
        this.original.setReasonPhrase(reason);
    }

    public HttpEntity getEntity() {
        return this.original.getEntity();
    }

    public void setEntity(HttpEntity entity) {
        this.original.setEntity(entity);
    }

    public Locale getLocale() {
        return this.original.getLocale();
    }

    public void setLocale(Locale loc) {
        this.original.setLocale(loc);
    }

    public ProtocolVersion getProtocolVersion() {
        return this.original.getProtocolVersion();
    }

    public boolean containsHeader(String name) {
        return this.original.containsHeader(name);
    }

    public Header[] getHeaders(String name) {
        return this.original.getHeaders(name);
    }

    public Header getFirstHeader(String name) {
        return this.original.getFirstHeader(name);
    }

    public Header getLastHeader(String name) {
        return this.original.getLastHeader(name);
    }

    public Header[] getAllHeaders() {
        return this.original.getAllHeaders();
    }

    public void addHeader(Header header) {
        this.original.addHeader(header);
    }

    public void addHeader(String name, String value) {
        this.original.addHeader(name, value);
    }

    public void setHeader(Header header) {
        this.original.setHeader(header);
    }

    public void setHeader(String name, String value) {
        this.original.setHeader(name, value);
    }

    public void setHeaders(Header[] headers) {
        this.original.setHeaders(headers);
    }

    public void removeHeader(Header header) {
        this.original.removeHeader(header);
    }

    public void removeHeaders(String name) {
        this.original.removeHeaders(name);
    }

    public HeaderIterator headerIterator() {
        return this.original.headerIterator();
    }

    public HeaderIterator headerIterator(String name) {
        return this.original.headerIterator(name);
    }

    /** @deprecated */
    @Deprecated
    public HttpParams getParams() {
        return this.original.getParams();
    }

    /** @deprecated */
    @Deprecated
    public void setParams(HttpParams params) {
        this.original.setParams(params);
    }
}
