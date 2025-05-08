package com.jiahe.intellipichub_ddd.interfaces.controller;

import com.jiahe.intellipichub_ddd.infrastructure.common.BaseResponse;
import com.jiahe.intellipichub_ddd.infrastructure.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class MainController {

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public BaseResponse<String> health() {
        return ResultUtils.success("ok");
    }
}
