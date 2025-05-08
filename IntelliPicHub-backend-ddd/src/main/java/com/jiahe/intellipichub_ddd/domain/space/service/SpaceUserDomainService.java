package com.jiahe.intellipichub_ddd.domain.space.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jiahe.intellipichub_ddd.domain.space.entity.SpaceUser;
import com.jiahe.intellipichub_ddd.interfaces.dto.spaceuser.SpaceUserQueryRequest;

/**
* @author jiahe
* @description 针对表【space_user(Space-User Association)】的数据库操作Service
* @createDate 2025-04-25 06:10:24
*/
public interface SpaceUserDomainService {



    public QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);


}
