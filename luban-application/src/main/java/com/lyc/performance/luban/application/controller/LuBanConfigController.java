package com.lyc.performance.luban.application.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lyc.performance.luban.application.exception.LuBanParaCheckException;
import com.lyc.performance.luban.application.util.PageUtil;
import com.lyc.performance.luban.application.vo.PageRequest;
import com.lyc.performance.luban.application.vo.R;
import com.lyc.performance.luban.storage.repository.file.FileStorageRepository;
import com.lyc.performance.luban.storage.util.YCReflectionUtil;
import com.lyc.performance.luban.storage.vo.BaseVo;
import com.lyc.performance.luban.storage.vo.HttpMethodVo;
import com.lyc.performance.luban.storage.vo.JavaMethodVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("luBanConfig")
public class LuBanConfigController {

    FileStorageRepository repository = FileStorageRepository.getInstance();

    @PostMapping("pageConfig")
    public R<?> pageConfig(PageRequest request) {
        try {
            List<BaseVo> baseVos = repository.readVos();
            return R.success(PageUtil.getPageJson(baseVos,request));
        } catch (Exception e) {
            String msg =  e instanceof NullPointerException ? "空指针异常" : e.getMessage();
            return R.failed(msg);
        }
    }

    @GetMapping("detail")
    public R<?> detail(@RequestParam(required = true) String appCode,@RequestParam(required = true)String id) {
        try {
            return R.success(repository.detail(id,appCode));
        } catch (Exception e) {
            String msg =  e instanceof NullPointerException ? "空指针异常" : e.getMessage();
            return R.failed(msg);
        }
    }

    @GetMapping("delete")
    public R<?> delete(@RequestParam(required = true) String appCode, @RequestParam(required = true)String id) {
        try {
            repository.deleteVo(id,appCode);
            return R.success(null);
        } catch (Exception e) {
            String msg =  e instanceof NullPointerException ? "空指针异常" : e.getMessage();
            return R.failed(msg);
        }
    }

    @PostMapping("update")
    public R<?> update(@RequestBody JSONObject vo) {
        try {
            String constant = vo.getString("constant");
            if(constant.equals("JAVA_METHOD")) {
                JavaMethodVo javaMethodVo = JSONObject.parseObject(JSON.toJSONString(vo), JavaMethodVo.class);
                repository.saveOrUpdate(javaMethodVo);
            }
            if (constant.equals("HTTP_METHOD")) {
                HttpMethodVo httpMethodVo = JSONObject.parseObject(JSON.toJSONString(vo), HttpMethodVo.class);
                repository.saveOrUpdate(httpMethodVo);
            }
            return R.success(null);
        } catch (Exception e) {
            String msg =  e instanceof NullPointerException ? "空指针异常" : e.getMessage();
            return R.failed(msg);
        }
    }


    @PostMapping("addJavaMethodVo")
    public R<?> addJavaMethodVo(@RequestBody JavaMethodVo vo) {
        try {
            checkParam(vo);
            if(StringUtils.isBlank(vo.getId())) {
                vo.setId(UUID.randomUUID().toString());
            }
            repository.save(vo);
            return R.success(null);
        } catch (Exception e) {
            String msg =  e instanceof NullPointerException ? "空指针异常" : e.getMessage();
            return R.failed(msg);
        }
    }

    @PostMapping("addHttpMethodVo")
    public R<?> addHttpMethodVo(@RequestBody HttpMethodVo vo) {
        try {
            checkParam(vo);
            if(StringUtils.isBlank(vo.getId())) {
                vo.setId(UUID.randomUUID().toString());
            }
            repository.save(vo);
            return R.success(null);
        } catch (Exception e) {
            String msg =  e instanceof NullPointerException ? "空指针异常" : e.getMessage();
            return R.failed(msg);
        }
    }


    private void checkParam(BaseVo vo) throws IllegalAccessException {
        List<String> fieldNames = new ArrayList<>();
        if(Objects.isNull(vo)) {
            throw new LuBanParaCheckException("参数为空");
        }

        if(StringUtils.isAnyEmpty(vo.getAppCode(),vo.getConfigName(),vo.getStatus())) {
            throw new LuBanParaCheckException("参数 appCode,configName,status 均不能为空！");
        }
        fieldNames.add("appCode");
        fieldNames.add("configName");
        fieldNames.add("status");


        if (vo instanceof JavaMethodVo) {
           JavaMethodVo jVo =  (JavaMethodVo)vo;
           if (StringUtils.isAnyEmpty(jVo.getClassName(),jVo.getMethodName(), jVo.getMethodBody())) {
               throw new LuBanParaCheckException("参数 className,methodName,methodBody 均不能为空！");
           }
            fieldNames.add("className");
            fieldNames.add("methodName");
            fieldNames.add("methodBody");
        }

        if (vo instanceof HttpMethodVo) {
            HttpMethodVo hvo = (HttpMethodVo)vo;
            if (StringUtils.isAnyEmpty(hvo.getHostAddress(),hvo.getUri(),hvo.getRequestType(),hvo.getContentType(),hvo.getResponseContent())) {
                throw new LuBanParaCheckException("参数 hostAddress,uri,requestTypes,contentType,responseContent 均不能为空！");
            }
            fieldNames.add("hostAddress");
            fieldNames.add("uri");
            fieldNames.add("requestType");
            fieldNames.add("contentType");
            fieldNames.add("responseContent");
        }
        for (String fieldName : fieldNames) {
            replaceFieldBlank(vo,fieldName);
        }
    }

    private void replaceFieldBlank(Object target,String targetFiledName) throws IllegalAccessException {
        List<Field> fields = YCReflectionUtil.objAllFields(target.getClass());
        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName();
            if(fieldName.equals(targetFiledName)) {
                Object obj = field.get(target);
                if(obj instanceof String && Objects.nonNull(obj)) {
                    String val = String.valueOf(obj).trim();
                    field.set(target,val);
                }
            }
        }
    }
}
