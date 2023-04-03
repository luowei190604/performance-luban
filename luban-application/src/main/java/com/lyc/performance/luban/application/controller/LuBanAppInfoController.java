package com.lyc.performance.luban.application.controller;

import com.lyc.performance.luban.application.hook.LuBanProbeHook;
import com.lyc.performance.luban.application.util.PageUtil;
import com.lyc.performance.luban.application.vo.AppInfo;
import com.lyc.performance.luban.application.vo.PageRequest;
import com.lyc.performance.luban.application.vo.R;
import com.lyc.performance.luban.storage.prebe.HeartInfo;
import com.lyc.performance.luban.storage.repository.file.HeartInfoFileStorageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("luBanApp")
public class LuBanAppInfoController {

    @Autowired
    private LuBanProbeHook hook;
    private HeartInfoFileStorageRepository repository = HeartInfoFileStorageRepository.getInstance();

    @PostMapping("pageAppInfo")
    public R<?> pageAppInfo(PageRequest request) {
        try {
            List<AppInfo> appInfos = new ArrayList<>();
            List<HeartInfo> heartInfos = repository.allInstance();
            Map<String, List<HeartInfo>> groupMap = heartInfos.stream()
                    .filter(info -> info.getStatus().equals("0"))
                    .collect(Collectors.groupingBy(heartInfo -> String.join("&", heartInfo.getAppCode(),
                            heartInfo.getVmName())));

            Set<Map.Entry<String, List<HeartInfo>>> entries = groupMap.entrySet();

            for (Map.Entry<String, List<HeartInfo>> entry : entries) {
                String key = entry.getKey();
                List<HeartInfo> value = entry.getValue();
                AppInfo appInfo = new AppInfo();
                appInfo.setAppCode(key.split("&")[0]);
                appInfo.setVmName(key.split("&")[1]);
                appInfo.setInstanceCount(value.size());
                appInfo.setInstances(value);
                appInfos.add(appInfo);
            }

            return R.success(PageUtil.getPageJson(appInfos,request));
        } catch (Exception e) {
            return R.failed(e.getMessage());
        }
    }

    @PostMapping("appDetail")
    public R<?> appDetail(@RequestParam(required = true,name = "appCode") String appCode) {
        try {
            List<HeartInfo> heartInfos = repository.allInstance();
            List<HeartInfo> filterList = heartInfos.stream().filter(heartInfo -> {
                return heartInfo.getAppCode().equals(appCode);
            }).collect(Collectors.toList());
            return R.success(filterList);
        } catch (Exception e) {
            String msg =  e instanceof NullPointerException ? "空指针异常" : e.getMessage();
            return R.failed(msg);
        }
    }

    @PostMapping("updateAppByteCode")
    public R<?> updateAppByteCode(@RequestParam(required = true,name = "appCode") String appCode) {
        try {
            List<HeartInfo> heartInfos = repository.allInstance();
            List<HeartInfo> filterList = heartInfos.stream().filter(heartInfo -> {
                return heartInfo.getAppCode().equals(appCode);
            }).collect(Collectors.toList());

            for (HeartInfo heartInfo : filterList) {
                hook.attachVm(appCode,heartInfo.getVmName(),heartInfo.getIp());
            }
            return R.success(null);
        } catch (IOException ie) {
            return R.failed(ie.getMessage());
        } catch (Exception e) {
            String msg =  e instanceof NullPointerException ? "空指针异常" : e.getMessage();
            return R.failed(msg);
        }
    }
}
