package com.jiahe.intellipichub_ddd.interfaces.dto.space.analyze;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间使用排行分析请求，仅管理员用
 * 响应就不需要定义新对象了，直接用entity的space对象即可
 */
@Data
public class SpaceRankAnalyzeRequest implements Serializable {

    /**
     * 排名前 N 的空间
     */
    private Integer topN = 10;

    private static final long serialVersionUID = 1L;
}
