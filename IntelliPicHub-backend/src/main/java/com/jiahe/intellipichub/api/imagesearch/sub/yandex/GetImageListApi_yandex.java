package com.jiahe.intellipichub.api.imagesearch.sub.yandex;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jiahe.intellipichub.api.imagesearch.model.ImageSearchResult;
import com.jiahe.intellipichub.exception.BusinessException;
import com.jiahe.intellipichub.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class GetImageListApi_yandex {

    /**
     * 从HTML中提取cbirSimilar部分的正则表达式
     */
    private static final Pattern CBIR_SIMILAR_PATTERN = Pattern.compile("\"cbirSimilar\":\\s*\\{\\s*\"thumbs\":\\s*(\\[.*?\\])");
    
    /**
     * 获取图片列表
     *
     * @param imagePageUrl 搜索结果页面URL
     * @return 图片搜索结果列表
     */
    public static List<ImageSearchResult> getImageList(String imagePageUrl) {
        try {
            log.info("Accessing search result page: {}", imagePageUrl);
            
            // 使用JSoup发起GET请求
            Document document = Jsoup.connect(imagePageUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(30000)
                    .get();
            
            // 处理响应
            return processResponse(document);
            
        } catch (Exception e) {
            log.error("Failed to get image list", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Failed to get image list");
        }
    }

    /**
     * 处理接口响应内容
     *
     * @param document JSoup文档对象
     * @return 图片搜索结果列表
     */
    private static List<ImageSearchResult> processResponse(Document document) {
        List<ImageSearchResult> results = new ArrayList<>();
        
        try {
            // 获取完整的HTML内容
            String html = document.outerHtml();
            
            // 处理HTML中的转义字符
            String htmlDecoded = html.replace("&quot;", "\"").replace("&amp;", "&");
            
            // 使用正则表达式提取cbirSimilar部分
            Matcher thumbsMatch = CBIR_SIMILAR_PATTERN.matcher(htmlDecoded);
            
            if (!thumbsMatch.find()) {
                log.warn("未找到cbirSimilar数据");
                return results;
            }
            
            // 提取JSON字符串并处理可能的转义字符
            String thumbsJson = thumbsMatch.group(1).replace("\\", "");
            
            try {
                // 解析JSON数据
                JSONArray thumbsList = JSONUtil.parseArray(thumbsJson);
                log.info("从cbirSimilar中找到 {} 个图片", thumbsList.size());
                
                for (int i = 0; i < thumbsList.size(); i++) {
                    JSONObject item = thumbsList.getJSONObject(i);
                    ImageSearchResult result = new ImageSearchResult();
                    
                    // 获取缩略图URL
                    String thumbnailUrl = item.getStr("imageUrl", "");
                    if (thumbnailUrl.startsWith("//")) {
                        thumbnailUrl = "https:" + thumbnailUrl;
                    }
                    result.setThumbUrl(thumbnailUrl);
                    
                    // 从linkUrl提取原图URL
                    String linkUrl = item.getStr("linkUrl", "");
                    if (!linkUrl.isEmpty()) {
                        // 提取原图URL的img_url参数
                        Pattern imgUrlPattern = Pattern.compile("img_url=([^&]+)");
                        Matcher imgUrlMatcher = imgUrlPattern.matcher(linkUrl);
                        
                        if (imgUrlMatcher.find()) {
                            String encodedUrl = imgUrlMatcher.group(1);
                            // URL解码
                            String originalUrl = URLDecoder.decode(encodedUrl, "UTF-8");
                            result.setFromUrl(originalUrl);
                        }
                    }
                    
                    // 只添加至少有一个URL不为空的结果
                    if (!result.getThumbUrl().isEmpty() || !result.getFromUrl().isEmpty()) {
                        results.add(result);
                    }
                }
                
            } catch (Exception e) {
                log.error("解析JSON数据失败: {}", e.getMessage());
            }
            
            log.info("成功提取 {} 个图片结果", results.size());
            return results;
            
        } catch (Exception e) {
            log.error("处理响应失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "处理图片搜索结果失败");
        }
    }

    public static void main(String[] args) {
        try {
            // 首先获取搜索结果页面URL
            String imageUrl = "https://jiahe-intellipichub-1352763103.cos.ap-hongkong.myqcloud.com/public/1912515518735192066/2025-04-16_tBtae0quoZ5C4pTd.webp";
            String searchResultUrl = GetImagePageUrlApi_yandex.getImagePageUrl(imageUrl);
            
            System.out.println("Getting image list from: " + searchResultUrl);
            
            // 记录开始时间
            long startTime = System.currentTimeMillis();
            
            // 获取图片列表
            List<ImageSearchResult> imageList = getImageList(searchResultUrl);
            
            // 计算运行时间
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            // 打印结果
            System.out.println("\n=== Test Results ===");
            System.out.println("Total images found: " + imageList.size());
            System.out.println("First 20 results:");
            
            // 直接输出对象原始格式
            imageList.stream().limit(20).forEach(System.out::println);
            
            System.out.println("\nExecution time: " + duration + " ms (" + (duration / 1000.0) + " seconds)");
            
        } catch (Exception e) {
            System.err.println("Error during test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
