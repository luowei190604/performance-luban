package com.lyc.performance.luban.storage.repository.file;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lyc.performance.luban.storage.prebe.HeartInfo;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HeartInfoFileStorageRepository {

    private static HeartInfoFileStorageRepository instanceFileStorageRepository = new HeartInfoFileStorageRepository();
    private String base_path;
    private String fileName = "instanceInfo.json";

    private HeartInfoFileStorageRepository() {
        String osName = System.getProperty("os.name").toLowerCase();
        base_path = osName.startsWith("windows") ? "D:\\lubanFileRepository\\instance" : "/home/performance/lubanFileRepository/instance";
        File file = new File(base_path);
        if(!file.exists()) {
            file.mkdirs();
        }
    }

    public static HeartInfoFileStorageRepository getInstance() {
        return instanceFileStorageRepository;
    }

    public void saveInstanceInfo(HeartInfo instance) throws IOException {
        String appCode = instance.getAppCode();
        String filePath = base_path + File.separator +  appCode + "_" + fileName;
        File file = new File(filePath);
        FileUtils.write(file,JSON.toJSONString(instance), StandardCharsets.UTF_8,true);
        FileUtils.write(file,"\n\r",StandardCharsets.UTF_8,true);
    }

    public List<HeartInfo> allInstance(String appCode) throws IOException {
        List<HeartInfo> resultList = new ArrayList<>();
        String filePath = base_path + File.separator +  appCode + "_" + fileName;
        File file = new File(filePath);
        if(!file.exists()) {
            file.createNewFile();
            return resultList;
        }
        parseFileContent(file,resultList);
        return resultList;
    }

    private void parseFileContent(File file,List<HeartInfo> resultList) throws IOException {
        String s = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        String[] arr = s.split("\n\r");
        for (String str : arr) {
            if(Objects.nonNull(str) && !str.equals("") && str.startsWith("{") && str.endsWith("}")) {
                resultList.add(JSONObject.parseObject(str,HeartInfo.class));
            }
        }
    }

    public List<HeartInfo> allInstance() throws IOException {
        List<HeartInfo> resultList = new ArrayList<>();
        File file = new File(base_path);
        if(file.isDirectory()) {
            File[] files = file.listFiles();
            for (File subFile : files) {
                parseFileContent(subFile,resultList);
            }
        }
        return resultList;
    }

    public void saveOrUpdateHeartInfo(HeartInfo targetInfo) throws IOException {
        String appCode = targetInfo.getAppCode();
        String filePath = base_path + File.separator +  appCode + "_" + fileName;
        File file = new File(filePath);
        List<HeartInfo> heartInfos = allInstance(appCode);
        if(!heartInfos.contains(targetInfo)) {
            FileUtils.write(file,JSON.toJSONString(targetInfo),StandardCharsets.UTF_8,true);
            FileUtils.write(file,"\n\r",StandardCharsets.UTF_8,true);
            return;
        }
        // 清空文件内容
        FileUtils.write(file,"",StandardCharsets.UTF_8,false);
        for (HeartInfo info : heartInfos) {
            HeartInfo writeInfo = info.equals(targetInfo) ? targetInfo : info;
            FileUtils.write(file,JSON.toJSONString(writeInfo),StandardCharsets.UTF_8,true);
            FileUtils.write(file,"\n\r",StandardCharsets.UTF_8,true);
        }
    }

    public static void main(String[] args) throws IOException {
        HeartInfoFileStorageRepository repository = new HeartInfoFileStorageRepository();
        long nowTime = System.currentTimeMillis();
        String appCodeNames = "app-consumer,user-provide,vehicle-info,vehicle-provide,common-provide,vehicleDb-provide,vehicleInfo-provide";
        String ipStrs = "192.168.56.101,192.168.56.102,192.168.56.103,192.168.56.104";
        for (String appCode : appCodeNames.split(",")) {
            for (String ip : ipStrs.split(",")) {
                HeartInfo heartInfo = new HeartInfo();
                heartInfo.setAppCode(appCode);
                heartInfo.setVmName(appCode + ".jar");
                heartInfo.setIp(ip);
                heartInfo.setStatus("0");
                repository.saveOrUpdateHeartInfo(heartInfo);
            }
        }
    }
}
