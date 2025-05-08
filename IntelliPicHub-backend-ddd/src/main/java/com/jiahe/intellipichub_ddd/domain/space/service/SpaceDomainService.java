package com.jiahe.intellipichub_ddd.domain.space.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jiahe.intellipichub_ddd.domain.space.entity.Space;
import com.jiahe.intellipichub_ddd.domain.user.entity.User;
import com.jiahe.intellipichub_ddd.interfaces.dto.space.SpaceQueryRequest;

/**
* @author jiahe
* @description 针对表【space(Space)】的数据库操作Service
* @createDate 2025-04-15 14:48:07
*/
public interface SpaceDomainService  {
    /**
     * 根据space空间级别填充空间对象
     * @param space
     */
    public void fillSpaceBySpaceLevel(Space space);

    /**
     * 获取查询对象
     * @param spaceQueryRequest
     * @return
     */
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);


    /**
     * 校验空间权限
     * @param loginUser
     * @param space
     */
    void checkSpaceAuth(User loginUser, Space space);

}



