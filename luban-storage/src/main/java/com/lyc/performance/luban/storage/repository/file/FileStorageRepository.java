package com.lyc.performance.luban.storage.repository.file;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lyc.performance.luban.storage.constant.AgentConstant;
import com.lyc.performance.luban.storage.vo.BaseVo;
import com.lyc.performance.luban.storage.vo.HttpMethodVo;
import com.lyc.performance.luban.storage.vo.JavaMethodVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class FileStorageRepository {

    private static FileStorageRepository fileStorageRepository = new FileStorageRepository();
    private String fileName = "luban.txt";
    private String base_path;

    private FileStorageRepository() {
        String osName = System.getProperty("os.name").toLowerCase();
        base_path = osName.startsWith("windows") ? "D:\\lubanFileRepository" : "/home/performance/lubanFileRepository";
        File file = new File(base_path);
        if(!file.exists()) {
            file.mkdirs();
        }
    }

    public static FileStorageRepository getInstance() {
        return fileStorageRepository;
    }

    public void save(BaseVo targetVo) throws IOException {
        String appCode = targetVo.getAppCode();
        String filePath = base_path + File.separator +  appCode + "_" + fileName;
        List<BaseVo> list = readVosByAppCode(appCode);
        long count = list.stream().filter(vo -> vo.getConfigName().equals(targetVo.getConfigName())).count();
        if (count == 0) {
            File file = new File(filePath);
            FileUtils.write(file, JSON.toJSONString(targetVo),StandardCharsets.UTF_8,true);
            FileUtils.write(file,"\n\r",StandardCharsets.UTF_8,true);
            return;
        }
        throw new RuntimeException("配置名称已存在");
    }

    public void saveOrUpdate(BaseVo targetVo) throws IOException {
        String appCode = targetVo.getAppCode();
        String configName = targetVo.getConfigName();
        String filePath = base_path + File.separator +  appCode + "_" + fileName;
        File file = new File(filePath);
        List<BaseVo> list = readVosByAppCode(appCode);
        if (Objects.nonNull(list) && list.size() != 0) {
            FileUtils.write(file,"",StandardCharsets.UTF_8,false);
            for (BaseVo vo : list) {
                String oldName = vo.getConfigName();
                if(oldName.equals(configName)) {
                    FileUtils.write(file, JSON.toJSONString(targetVo),StandardCharsets.UTF_8,true);
                } else {
                    FileUtils.write(file, JSON.toJSONString(vo),StandardCharsets.UTF_8,true);
                }
                FileUtils.write(file,"\n\r",StandardCharsets.UTF_8,true);
            }
        } else {
            FileUtils.write(file, JSON.toJSONString(targetVo),StandardCharsets.UTF_8,true);
            FileUtils.write(file,"\n\r",StandardCharsets.UTF_8,true);
        }
    }

    public void deleteVo(String targetId,String appCode) throws IOException {
        String filePath = base_path + File.separator +  appCode + "_" + fileName;
        File file = new File(filePath);
        List<BaseVo> list = readVosByAppCode(appCode);
        FileUtils.write(file,"",StandardCharsets.UTF_8,false);
        for (BaseVo vo : list) {
            String oldId = vo.getId();
            if(!oldId.equals(targetId)) {
                FileUtils.write(file, JSON.toJSONString(vo),StandardCharsets.UTF_8,true);
                FileUtils.write(file,"\n\r",StandardCharsets.UTF_8,true);
            }
        }
    }

    public BaseVo detail(String targetId,String appCode) throws IOException {
        List<BaseVo> list = readVosByAppCode(appCode);
        for (BaseVo vo : list) {
            String oldId = vo.getId();
            if(oldId.equals(targetId)) {
                return vo;
            }
        }
        return null;
    }

    public List<BaseVo> readVos() throws IOException {
        List<BaseVo> voJsons = new ArrayList<>();
        String filePath = base_path;
        File file = new File(filePath);
        if(file.isDirectory()) {
            File[] files = file.listFiles();
            for (File subFile : files) {
                if(!subFile.isDirectory()) {
                    parseVos(voJsons, subFile);
                }
            }
        }
        return voJsons;
    }

    public List<BaseVo> readVosByAppCode(String appCode) throws IOException {
        List<BaseVo> resultList = new ArrayList<>();
        String filePath = base_path + File.separator +  appCode + "_" + fileName;
        File file = new File(filePath);
        if(!file.exists()) {
            file.createNewFile();
            return resultList;
        }
        parseVos(resultList, file);
        return resultList;
    }

    private void parseVos(List<BaseVo> voJsons, File subFile) throws IOException {
        String s = FileUtils.readFileToString(subFile, StandardCharsets.UTF_8);
        String[] arr = s.split("\n\r");
        for (String str : arr) {
            if (Objects.nonNull(str) && !str.equals("") && str.startsWith("{") && str.endsWith("}")) {
                BaseVo baseVo = JSONObject.parseObject(str, BaseVo.class);
                if (baseVo.getConstant().equals(AgentConstant.JAVA_METHOD)) {
                    voJsons.add(JSONObject.parseObject(str, JavaMethodVo.class));
                }
                if (baseVo.getConstant().equals(AgentConstant.HTTP_METHOD)) {
                    voJsons.add(JSONObject.parseObject(str, HttpMethodVo.class));
                }
            }
        }
    }
}
