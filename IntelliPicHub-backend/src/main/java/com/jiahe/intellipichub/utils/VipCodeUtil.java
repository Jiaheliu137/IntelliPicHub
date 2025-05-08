package com.jiahe.intellipichub.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * VIP兑换码工具类
 */
@Slf4j
public class VipCodeUtil {
    
    // 缓存已使用的VIP兑换码，避免频繁读写文件
    private static final Map<String, Boolean> usedVipCodeCache = new ConcurrentHashMap<>();
    
    // VIP兑换码JSON文件路径
    private static final String VIP_CODE_FILE_PATH = "biz/vipCode.json";
    
    /**
     * 检查VIP兑换码是否有效
     * 
     * @param vipCode 要检查的VIP兑换码
     * @return 如果兑换码有效且未使用返回true，否则返回false
     */
    public static boolean isValidVipCode(String vipCode) {
        // 先检查缓存中是否已标记为已使用
        if (Boolean.TRUE.equals(usedVipCodeCache.get(vipCode))) {
            return false;
        }
        
        try {
            JSONArray codesArray = readVipCodesFromFile();
            for (int i = 0; i < codesArray.size(); i++) {
                JSONObject codeObj = codesArray.getJSONObject(i);
                if (codeObj.getStr("code").equals(vipCode) && !codeObj.getBool("hasUsed")) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("检查VIP兑换码有效性失败", e);
        }
        return false;
    }
    
    /**
     * 标记VIP兑换码为已使用
     * 
     * @param vipCode 要标记的VIP兑换码
     * @return 操作是否成功
     */
    public static boolean markVipCodeAsUsed(String vipCode) {
        try {
            JSONArray codesArray = readVipCodesFromFile();
            boolean found = false;
            
            // 查找并更新兑换码状态
            for (int i = 0; i < codesArray.size(); i++) {
                JSONObject codeObj = codesArray.getJSONObject(i);
                if (codeObj.getStr("code").equals(vipCode) && !codeObj.getBool("hasUsed")) {
                    codeObj.set("hasUsed", true);
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                return false;
            }
            
            // 将更新后的内容写入文件
            boolean writeResult = writeVipCodesToFile(codesArray);
            if (writeResult) {
                // 更新内存缓存
                usedVipCodeCache.put(vipCode, true);
            }
            return writeResult;
        } catch (Exception e) {
            log.error("标记VIP兑换码为已使用失败", e);
            return false;
        }
    }
    
    /**
     * 从文件读取VIP兑换码列表
     * 
     * @return VIP兑换码JSON数组
     */
    private static JSONArray readVipCodesFromFile() {
        try {
            String json = ResourceUtil.readUtf8Str(VIP_CODE_FILE_PATH);
            return JSONUtil.parseArray(json);
        } catch (Exception e) {
            log.error("读取VIP兑换码文件失败", e);
            return new JSONArray();
        }
    }
    
    /**
     * 将VIP兑换码列表写入文件
     * 
     * @param codesArray VIP兑换码JSON数组
     * @return 写入是否成功
     */
    private static boolean writeVipCodesToFile(JSONArray codesArray) {
        try {
            // 优先使用外部配置文件路径
            String updatedJson = codesArray.toString();
            
            // 尝试获取配置文件真实路径
            File configFile = getConfigFile();
            if (configFile != null && configFile.exists() && configFile.canWrite()) {
                FileUtil.writeUtf8String(updatedJson, configFile);
                log.info("成功写入VIP兑换码文件: {}", configFile.getAbsolutePath());
                return true;
            } else {
                log.warn("无法写入VIP兑换码文件，配置文件不存在或无写入权限");
                return false;
            }
        } catch (Exception e) {
            log.error("写入VIP兑换码文件失败", e);
            return false;
        }
    }
    
    /**
     * 获取配置文件
     * 
     * @return 配置文件对象，如果找不到则返回null
     */
    private static File getConfigFile() {
        try {
            // 尝试从外部文件系统获取文件
            File externalFile = new File(System.getProperty("user.dir"), "config/" + VIP_CODE_FILE_PATH);
            if (externalFile.exists()) {
                return externalFile;
            }
            
            // 尝试从classpath获取
            File classpathFile = ResourceUtils.getFile("classpath:" + VIP_CODE_FILE_PATH);
            if (classpathFile.exists()) {
                return classpathFile;
            }
        } catch (FileNotFoundException e) {
            log.warn("无法从classpath获取VIP兑换码文件", e);
        }
        
        // 都找不到则尝试在用户目录创建配置文件
        try {
            Path userConfigDir = Paths.get(System.getProperty("user.home"), ".intellipichub", "config");
            Files.createDirectories(userConfigDir);
            Path userConfigFile = userConfigDir.resolve("vipCode.json");
            
            // 如果用户配置文件不存在，则从classpath复制一份
            if (!Files.exists(userConfigFile)) {
                String defaultConfig = ResourceUtil.readUtf8Str(VIP_CODE_FILE_PATH);
                Files.write(userConfigFile, defaultConfig.getBytes(StandardCharsets.UTF_8), 
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
            
            return userConfigFile.toFile();
        } catch (IOException e) {
            log.error("创建用户配置文件失败", e);
        }
        
        return null;
    }
} 