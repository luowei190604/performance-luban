package com.lyc.performance.luban.application.vo;

import lombok.Data;

import java.util.UUID;

@Data
public class UserInfo {

    private String citys = "深圳,北京,上海,广州,重庆,苏州";
    private String sexs = "男,女";

    private String id;
    private String username;
    private String sex;
    private String city;
    private String sign;
    private String experience;
    private String score;
    private String classify;
    private String wealth;

    public UserInfo(String userName) {
        this.username = userName;
        this.id =  UUID.randomUUID().toString();
        this.sex = sexs.split(",")[1];
        this.city = citys.split(",")[1];
        this.sign = citys.split(",")[1] + System.currentTimeMillis();
        this.experience = UUID.randomUUID().toString();
        this.score = System.currentTimeMillis() + "";
        this.classify = "this is classify";
        this.wealth = System.currentTimeMillis() + "";
    }

}
