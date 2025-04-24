package com.jiahe.intellipichub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jiahe.intellipichub.model.dto.space.SpaceAddRequest;
import com.jiahe.intellipichub.model.dto.space.SpaceQueryRequest;
import com.jiahe.intellipichub.model.entity.Space;
import com.jiahe.intellipichub.model.entity.User;
import com.jiahe.intellipichub.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author jiahe
* @description 针对表【space(Space)】的数据库操作Service
* @createDate 2025-04-15 14:48:07
*/
public interface SpaceService extends IService<Space> {

    /**
     * 创建控件
     * @param spaceAddRequest
     * @param loginUser
     * @return
     */
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    /**
     * 校验空间，使用情况是前端已经传来了Space类型的请求数据
     * @param space
     * @param add 是否是创建时
     */
    public void validSpace(Space space, boolean add);

    /**
     * 获取空间包装类，单条
     * @param space
     * @param request
     * @return
     */

    /**
     *
     * @param space
     * @param request
     * @return
     */
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    /**
     *
     * @param spacePage
     * @param request
     * @return
     */
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);


    /**
     * 根据space空间级别填充空间对象
     * @param space
     */
    public void fillSpaceBySpaceLevel(Space space);

    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);


    void checkSpaceAuth(User loginUser, Space space);

}



