package com.jiahe.intellipichub_ddd.application.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jiahe.intellipichub_ddd.interfaces.dto.spaceuser.SpaceUserAddRequest;
import com.jiahe.intellipichub_ddd.interfaces.dto.spaceuser.SpaceUserQueryRequest;
import com.jiahe.intellipichub_ddd.domain.space.entity.SpaceUser;
import com.jiahe.intellipichub_ddd.interfaces.vo.space.SpaceUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author jiahe
* @description 针对表【space_user(Space-User Association)】的数据库操作Service
* @createDate 2025-04-25 06:10:24
*/
public interface SpaceUserApplicationService extends IService<SpaceUser> {
    /**
     * 创建空间成员
     * @param spaceUserAddRequest
     * @return
     */
    public long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

    /**
     * 校验空间成员
     * @param spaceUser
     * @param add 是否是创建时
     */
    public void validSpaceUser(SpaceUser spaceUser, boolean add);

    /**
     * 获取空间成员包装类，单条
     * @param spaceUser
     * @param request
     * @return
     */
    public SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    /**
     * 获取空间成员包装类。列表
     * @param spaceUserList
     * @return
     */
    public List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);


    public QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);


}
