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
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(true)
            );

            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            page.navigate(searchUrl);
            page.waitForSelector("a.richImgLnk");

            // 保存页面调试用（等价于 selenium 的 pageSource）
            try (FileWriter writer = new FileWriter("bing_debug_playwright.html")) {
                writer.write(page.content());
            } catch (IOException e) {
                e.printStackTrace();
            }

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
