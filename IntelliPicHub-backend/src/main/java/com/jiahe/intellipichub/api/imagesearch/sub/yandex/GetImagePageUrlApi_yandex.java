package com.jiahe.intellipichub.api.imagesearch.sub.yandex;

import cn.hutool.core.util.URLUtil;
import com.jiahe.intellipichub.exception.BusinessException;
import com.jiahe.intellipichub.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class GetImagePageUrlApi_yandex {

    /**
     * 从HTML中提取serpid的正则表达式
     */
    private static final Pattern SERPID_PATTERN = Pattern.compile("\"serpid\":\"([^\"]+)\"");

    /**
     * 从HTML中提取cbir_id的正则表达式
     */
    private static final Pattern CBIR_ID_PATTERN = Pattern.compile("cbir_id=([^&\"]+)");

    /**
     * 获取图片页面地址
     *
     * @param imageUrl 要搜索的图片URL
     * @return 搜索结果页面的URL
     */
    public static String getImagePageUrl(String imageUrl) {
        // 1. 准备请求参数
        String encodedImageUrl = URLUtil.encode(imageUrl);
        
        // 请求地址
        String searchUrl = "https://yandex.com/images/search?rpt=imageview&url=" + encodedImageUrl;
        log.info("Accessing Yandex search URL: {}", searchUrl);
        
        try {
            // 2. 使用JSoup直接获取并解析HTML
            Document document = Jsoup.connect(searchUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(20000)
                    .get();
            
            // 3. 提取参数
            String html = document.html();
            String cbirId = extractParameter(html, CBIR_ID_PATTERN);
            String serpId = extractParameter(html, SERPID_PATTERN);
            
            if (cbirId == null || serpId == null) {
                log.error("Failed to extract required search parameters, cbirId: {}, serpId: {}", cbirId, serpId);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Failed to extract image search parameters");
            }
            
            // 构建最终的搜索结果URL
            String resultUrl = String.format(
                "https://yandex.com/images/search?cbir_id=%s&cbir_page=similar&rpt=imageview&source-serpid=%s&url=https://avatars.mds.yandex.net/get-images-cbir/%s/orig",
                cbirId, serpId, cbirId
            );
            
            log.info("Generated search result URL: {}", resultUrl);
            return resultUrl;
            
        } catch (Exception e) {
            log.error("Error getting image search result URL: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Image search failed");
        }
    }
    
    /**
     * 从HTML中提取参数
     * 
     * @param html HTML内容
     * @param pattern 匹配模式
     * @return 提取的参数值
     */
    private static String extractParameter(String html, Pattern pattern) {
        Matcher matcher = pattern.matcher(html);
        return matcher.find() ? matcher.group(1) : null;
    }
    
    /**
     * 测试方法
     */
    public static void main(String[] args) {
        try {
            // 测试图片URL
            String testImageUrl = "https://jiahe-intellipichub-1352763103.cos.ap-hongkong.myqcloud.com/public/1920912817099210753/20250509100706252-jyNdiBmh_thumbnail.png";
            
            System.out.println("Starting test for getting image search URL...");
            System.out.println("Test image URL: " + testImageUrl);
            
            // 记录开始时间
            long startTime = System.currentTimeMillis();
            
            // 调用方法
            String resultUrl = getImagePageUrl(testImageUrl);
            
            // 计算运行时间
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            // 打印结果
            System.out.println("\n=== Test Results ===");
            System.out.println("Search result URL: " + resultUrl);
            System.out.println("Execution time: " + duration + " ms (" + (duration / 1000.0) + " seconds)");
            System.out.println("Test completed");
        } catch (Exception e) {
            System.err.println("Error during test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
