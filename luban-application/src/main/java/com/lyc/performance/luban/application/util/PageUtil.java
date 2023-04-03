package com.lyc.performance.luban.application.util;

import com.alibaba.fastjson.JSONObject;
import com.lyc.performance.luban.application.vo.PageRequest;

import java.util.List;

public class PageUtil {

    public static JSONObject getPageJson(List<?> datas, PageRequest request) {
        JSONObject result = new JSONObject();
        int limit = request.getLimit();
        int page = request.getPage();
        int size = datas.size();
        int startIndex = (page-1) * limit;
        int endIndex = page * limit > size ? size : page * limit;
        result.put("code",0);
        result.put("msg","");
        result.put("count",datas.size());
        result.put("data",datas.subList(startIndex,endIndex));
        return result;
    }
}
