package com.jiahe.intellipichub.api.imagesearch.sub.bing;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jiahe.intellipichub.api.imagesearch.model.ImageSearchResult;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class GetImageListApi_bing {

    public static List<ImageSearchResult> getImageList(String searchUrl) {
        List<ImageSearchResult> results = new ArrayList<>();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-gpu");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36");
        // 无头模式可能触发反爬虫
        options.addArguments("--headless=new");

        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get(searchUrl);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.richImgLnk")));

            try (FileWriter writer = new FileWriter("bing_debug.html")) {
                writer.write(driver.getPageSource());
            } catch (IOException e) {
                e.printStackTrace();
            }

            List<WebElement> aTags = driver.findElements(By.cssSelector("a.richImgLnk"));

            for (WebElement aTag : aTags) {
                String dataM = aTag.getAttribute("data-m");
                try {
                    WebElement img = aTag.findElement(By.tagName("img"));
                    if (dataM != null && img != null) {
                        JSONObject json = JSONUtil.parseObj(dataM);
                        String murl = json.getStr("murl");
                        String thumb = img.getAttribute("src");
                        ImageSearchResult result = new ImageSearchResult();
                        result.setFromUrl(murl);
                        result.setThumbUrl(thumb);
                        results.add(result);
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }

        return results;
    }

    public static void main(String[] args) {
        String imageUrl = "https://jiahe-intellipichub-1352763103.cos.ap-hongkong.myqcloud.com/public/1912515518735192066/2025-04-17_FHYH4M3mZXNjwQFI_thumbnail.png";
        String searchUrl = GetImagePageUrlApi_bing.getImagePageUrl(imageUrl);
        List<ImageSearchResult> results = getImageList(searchUrl);

        int i = 1;
        for (ImageSearchResult result : results) {
            System.out.println("#" + (i++));
            System.out.println("Thumbnail: " + result.getThumbUrl());
            System.out.println("Original : " + result.getFromUrl());
            System.out.println("--------");
        }
    }
}
