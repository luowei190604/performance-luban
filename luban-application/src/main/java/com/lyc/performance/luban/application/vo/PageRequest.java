package com.lyc.performance.luban.application.vo;

import lombok.Data;

@Data
public class PageRequest {
    private int page;
    private int limit;
    private String userName;
    private String address;
}
