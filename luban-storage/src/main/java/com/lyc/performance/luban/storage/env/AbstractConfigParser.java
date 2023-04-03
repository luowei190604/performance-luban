package com.lyc.performance.luban.storage.env;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public abstract class AbstractConfigParser {

    String suffix;

    AbstractConfigParser(String suffix) {
        this.suffix = suffix;
    }

    public Map<String,Object> parseConfigFile(String fileName) {
        int index = fileName.lastIndexOf(".");
        String suffixName = fileName.substring(index + 1);
        if (!suffixName.equals(suffix)) {
            throw new RuntimeException("wrong suffix fileName can not parser");
        }
        Map<String, Object> result = new HashMap<>();
        InputStream in = AbstractConfigParser.class.getClassLoader().getResourceAsStream(fileName);
        if (Objects.isNull(in)) {
            return result;
        }
        doParseConfigFile(in,result);
        return result;
    }

    abstract void doParseConfigFile(InputStream in,Map<String, Object> result);

    static class PropertyParser extends AbstractConfigParser {

        PropertyParser(String suffix) {
            super(suffix);
        }

        @Override
        void doParseConfigFile(InputStream in, Map<String, Object> result) {
            Properties properties = new Properties();
            try {
                properties.load(in);
                Enumeration<?> enumeration = properties.propertyNames();
                while (enumeration.hasMoreElements()) {
                    String key = (String)enumeration.nextElement();
                    result.put(key,properties.get(key));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class YmlParser extends AbstractConfigParser {

        YmlParser(String suffix) {
            super(suffix);
        }

        @Override
        void doParseConfigFile(InputStream in, Map<String, Object> result) {
            Yaml yaml = new Yaml();
            LinkedHashMap<String,Object> linkedMap = (LinkedHashMap<String,Object>)yaml.load(in);
            transfer2Map("",linkedMap,result);
        }

        private void transfer2Map(String initKey,Map<String,Object> map,Map<String,Object> result) {
            Set<Map.Entry<String, Object>> entries = map.entrySet();
            for (Map.Entry<String,Object> entry : entries) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof LinkedHashMap) {
                    LinkedHashMap<String,Object> subMap = (LinkedHashMap<String,Object>)value;
                    String subKey = "".equals(initKey)
                            ? key.concat(".")
                            : initKey.concat(key).concat(".");
                    transfer2Map(subKey,subMap,result);
                } else {
                    if ("".equals(initKey)) {
                        result.put(key,value);
                    } else {
                        result.put(initKey.concat(key),value);
                    }
                }
            }
        }
    }

    public static AbstractConfigParser createParser(String type) {
        if ("yml".equals(type)) {
            return new YmlParser("yml");
        }
        if ("properties".equals(type)) {
            return new PropertyParser("properties");
        }

        throw new RuntimeException("can not support this type:" + type);
    }


}