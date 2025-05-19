//package com.jiahe.intellipichub.api.imagesearch.sub.bing;
//
//import cn.hutool.json.JSONObject;
//import cn.hutool.json.JSONUtil;
//import io.github.bonigarcia.wdm.WebDriverManager;
//import org.openqa.selenium.By;
//import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.WebElement;
//import org.openqa.selenium.chrome.ChromeDriver;
//import org.openqa.selenium.chrome.ChromeOptions;
//import org.openqa.selenium.support.ui.ExpectedConditions;
//import org.openqa.selenium.support.ui.WebDriverWait;
//
//import java.io.FileWriter;
//import java.io.IOException;
//import java.time.Duration;
//import java.util.ArrayList;
//import java.util.List;
//
//@Deprecated
//public class BingImageSearch {
//
//    public static class ImageResult {
//        public String originalUrl;
//        public String thumbnailUrl;
//
//        public ImageResult(String originalUrl, String thumbnailUrl) {
//            this.originalUrl = originalUrl;
//            this.thumbnailUrl = thumbnailUrl;
//        }
//    }
//
//    public static List<ImageResult> searchImage(String imageUrl) {
//        List<ImageResult> results = new ArrayList<>();
//
//        String searchUrl = "https://www.bing.com/images/search?view=detailv2&iss=sbi&q=imgurl:" + imageUrl;
//
//        ChromeOptions options = new ChromeOptions();
//        options.addArguments("--disable-gpu");
//        options.addArguments("--remote-allow-origins=*");
//        options.addArguments("--no-sandbox");
//        options.addArguments("--disable-dev-shm-usage");
//        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36");
//        // options.addArguments("--headless"); // 可以调试时先注释
//
//        WebDriverManager.chromedriver().setup();
//        WebDriver driver = new ChromeDriver(options);
//
//        try {
//            driver.get(searchUrl);
//
//            // 使用显式等待代替 Thread.sleep
//            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
//            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.richImgLnk")));
//
//            // 保存页面 HTML 调试用
//            try (FileWriter writer = new FileWriter("bing_debug.html")) {
//                writer.write(driver.getPageSource());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            List<WebElement> aTags = driver.findElements(By.cssSelector("a.richImgLnk"));
//
//            for (WebElement aTag : aTags) {
//                String dataM = aTag.getAttribute("data-m");
//                try {
//                    WebElement img = aTag.findElement(By.tagName("img"));
//                    if (dataM != null && img != null) {
//                        JSONObject json = JSONUtil.parseObj(dataM);
//                        String murl = json.getStr("murl");
//                        String thumb = img.getAttribute("src");
//                        results.add(new ImageResult(murl, thumb));
//                    }
//                } catch (Exception ignored) {}
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            driver.quit();
//        }
//
//        return results;
//    }
//
//    public static void main(String[] args) {
//        String imageUrl = "https://jiahe-intellipichub-1352763103.cos.ap-hongkong.myqcloud.com/space/null/20250420005702128-0dv2l2dL.webp";
//        List<ImageResult> results = searchImage(imageUrl);
//
//        int i = 1;
//        for (ImageResult result : results) {
//            System.out.println("#" + (i++));
//            System.out.println("Thumbnail: " + result.thumbnailUrl);
//            System.out.println("Original : " + result.originalUrl);
//            System.out.println("--------");
//        }
//    }
//}
