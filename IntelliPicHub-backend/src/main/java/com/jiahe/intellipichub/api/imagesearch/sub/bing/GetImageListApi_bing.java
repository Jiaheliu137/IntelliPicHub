package com.jiahe.intellipichub.api.imagesearch.sub.bing;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jiahe.intellipichub.api.imagesearch.model.ImageSearchResult;
import com.microsoft.playwright.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetImageListApi_bing {

    public static List<ImageSearchResult> getImageList(String searchUrl) {
        List<ImageSearchResult> results = new ArrayList<>();

        try (Playwright playwright = Playwright.create()) {
            // 启动浏览器
            Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true)
            );

            // 设置大视口以便加载更多内容
            BrowserContext context = browser.newContext(
                new Browser.NewContextOptions().setViewportSize(1920, 3000)
            );
            Page page = context.newPage();

            // 打开搜索页面
            page.navigate(searchUrl);
            page.waitForSelector("a.richImgLnk");

            // 模拟滚动页面加载更多图片
            for (int i = 0; i < 5; i++) {
                page.evaluate("window.scrollBy(0, 1500);");
                Thread.sleep(1000);  // 可根据实际延迟微调
            }

            // 保存调试页面
            try (FileWriter writer = new FileWriter("bing_debug_playwright.html")) {
                writer.write(page.content());
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 解析图片信息
            List<ElementHandle> imageLinks = page.querySelectorAll("a.richImgLnk");

            for (ElementHandle link : imageLinks) {
                String dataM = link.getAttribute("data-m");
                ElementHandle img = link.querySelector("img");

                if (dataM != null && img != null) {
                    String thumbUrl = img.getAttribute("src");

                    JSONObject json = JSONUtil.parseObj(dataM);
                    String murl = json.getStr("murl");

                    ImageSearchResult result = new ImageSearchResult();
                    result.setFromUrl(murl);
                    result.setThumbUrl(thumbUrl);
                    results.add(result);
                }
            }

            browser.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    public static void main(String[] args) {
        String imageUrl = "https://jiahe-intellipichub-1352763103.cos.ap-hongkong.myqcloud.com/public/1920912817099210753/20250509100706252-jyNdiBmh.webp";
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
