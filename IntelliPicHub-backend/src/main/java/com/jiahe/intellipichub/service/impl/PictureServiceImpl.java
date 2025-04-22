package com.jiahe.intellipichub.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiahe.intellipichub.api.aliyunai.AliYunAiApi;
import com.jiahe.intellipichub.api.aliyunai.model.CreateOutPaintingTaskRequest;
import com.jiahe.intellipichub.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.jiahe.intellipichub.exception.BusinessException;
import com.jiahe.intellipichub.exception.ErrorCode;
import com.jiahe.intellipichub.exception.ThrowUtils;
import com.jiahe.intellipichub.manager.CosManager;
import com.jiahe.intellipichub.manager.FileManager;
import com.jiahe.intellipichub.manager.upload.FilePictureUpload;
import com.jiahe.intellipichub.manager.upload.PictureUploadTemplate;
import com.jiahe.intellipichub.manager.upload.UrlPictureUpload;
import com.jiahe.intellipichub.mapper.PictureMapper;
import com.jiahe.intellipichub.model.dto.file.UploadPictureResult;
import com.jiahe.intellipichub.model.dto.picture.*;
import com.jiahe.intellipichub.model.entity.Picture;
import com.jiahe.intellipichub.model.entity.Space;
import com.jiahe.intellipichub.model.entity.User;
import com.jiahe.intellipichub.model.enums.PictureReviewStatusEnum;
import com.jiahe.intellipichub.model.vo.PictureVO;
import com.jiahe.intellipichub.model.vo.UserVO;
import com.jiahe.intellipichub.service.PictureService;
import com.jiahe.intellipichub.service.SpaceService;
import com.jiahe.intellipichub.service.UserService;
import com.jiahe.intellipichub.utils.ColorSimilarUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jiahe
 * @description 针对表【intellipic(Image)】的数据库操作Service实现
 * @createDate 2025-04-05 09:54:35
 */
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {

    @Resource
    private FileManager fileManager;

    @Resource
    private UserService userService;

    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UrlPictureUpload urlPictureUpload;
    @Autowired
    private CosManager cosManager;

    @Resource
    private SpaceService spaceService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private AliYunAiApi aliYunAiApi;

    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

        // 校验空间是否存在
        Long spaceId = pictureUploadRequest.getSpaceId();
        if (spaceId != null) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "Space is not exist");
            // 校验是否有空间的权限，仅空间所有者才能上传
            if (!loginUser.getId().equals(space.getUserId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "Only the space owner can upload pictures");
            }
            // 校验额度
            if (space.getTotalCount() >= space.getMaxCount()) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "The space has reached the maximum number of pictures");
            }
            if (space.getTotalSize() >= space.getMaxSize()) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "The space has reached the maximum size");
            }
        }


        // 判断新增还是删除
        Long pictureId = null;
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
        }
        // 如果是更新，判断图片是否存在
        if (pictureId != null) {
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "Picture is not exist");

            // 仅本人或者管理员可以编辑图片 
            if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "Only the user himself or the administrator can edit the picture");
            }
            // 校验空间是否一致
            // 没传spaceId，则复用原有图片的spaceId
            if (spaceId == null) {
                if (oldPicture.getSpaceId() != null) {
                    spaceId = oldPicture.getSpaceId();
                }
            } else {
                // 传了sapceId，则必须要和原空间的spaceId一致
                if (ObjUtil.notEqual(spaceId, oldPicture.getSpaceId())) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "Space is not consistent");
                }
            }

        }
        // 上传图片,得到图片信息
        // 按照用户id划分目录 => 按照空间划分目录
        String uploadPathPrefix;
        if (spaceId == null) {
            // 公共图库
            uploadPathPrefix = String.format("public/%s", loginUser.getId());
        } else {
            // 个人图库
            uploadPathPrefix = String.format("space/%s", spaceId);
        }
        // 根据inputSource的类型来决定上传方式.filePictureUpload是抽象类PictureUploadTemplate 的一个具体子类的实例，抽象类不能被实例化但可以实例化他的子类
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);
        // 构造要入库的图片信息
        Picture picture = new Picture();
        picture.setSpaceId(spaceId); // 指定空间id 
        picture.setUrl(uploadPictureResult.getUrl()); // 保存压缩后的webp图像的url
        picture.setOriginalUrl(uploadPictureResult.getOriginalUrl()); // 保存原始图像URL
        picture.setThumbnailUrl(uploadPictureResult.getThumbnailUrl()); // 保存缩略图URL
        // 支持外层传递图片名称
        String picName = uploadPictureResult.getPicName();
        if (pictureUploadRequest != null && StrUtil.isNotBlank(pictureUploadRequest.getPicName())) {
            picName = pictureUploadRequest.getPicName();
        }
        picture.setName(picName);
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());
        picture.setPicColor(uploadPictureResult.getPicColor());

        // 设置简介（如果提供）
        if (pictureUploadRequest != null && StrUtil.isNotBlank(pictureUploadRequest.getIntroduction())) {
            picture.setIntroduction(pictureUploadRequest.getIntroduction());
        }

        // 设置分类（如果提供，否则使用默认"Other"）
        if (pictureUploadRequest != null && StrUtil.isNotBlank(pictureUploadRequest.getCategory())) {
            picture.setCategory(pictureUploadRequest.getCategory());
        } else {
            picture.setCategory("Other");
        }

        // 设置标签（如果提供，否则使用默认["Other"]）
        if (pictureUploadRequest != null && CollUtil.isNotEmpty(pictureUploadRequest.getTags())) {
            picture.setTags(JSONUtil.toJsonStr(pictureUploadRequest.getTags()));
        } else {
            List<String> defaultTags = new ArrayList<>();
            defaultTags.add("Other");
            picture.setTags(JSONUtil.toJsonStr(defaultTags));
        }

        // 插入数据库之前补充审核参数
        this.fillReviewParams(picture, loginUser);

        // 操作数据库
        // 如果pictureId不为空，表示更新，否则是新增
        if (pictureId != null) {
            // 如果是更新，需要补充id和edit time
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }

        // 开启事务
        Long finalSpaceId = spaceId;
        // 更新空间使用额度
        transactionTemplate.execute(status -> {
            // 插入数据
            boolean result = this.saveOrUpdate(picture); // 根据是否有id来决定是更新还是插入
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "Fail to update picture,failed to operate database");
            if (finalSpaceId != null) {
                // 有spaceId的话则更新空间额度
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, finalSpaceId)
                        .setSql("totalSize = totalSize + " + picture.getPicSize())
                        .setSql("totalCount = totalCount + 1")
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "Fail to update size or count");
            }

            return picture;
        });


        // todo 如果是更新，则清理图片资源


        // 返回图片信息
        return PictureVO.objToVo(picture);
    }


    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();

        // 宽度相关参数
        Integer minPicWidth = pictureQueryRequest.getMinPicWidth();
        Integer maxPicWidth = pictureQueryRequest.getMaxPicWidth();
        // 如果没有范围值但有精确值，则将精确值同时设为最小值和最大值
        if ((minPicWidth == null && maxPicWidth == null) && pictureQueryRequest.getPicWidth() != null) {
            minPicWidth = pictureQueryRequest.getPicWidth();
            maxPicWidth = pictureQueryRequest.getPicWidth();
        }

        // 高度相关参数
        Integer minPicHeight = pictureQueryRequest.getMinPicHeight();
        Integer maxPicHeight = pictureQueryRequest.getMaxPicHeight();
        // 如果没有范围值但有精确值，则将精确值同时设为最小值和最大值
        if ((minPicHeight == null && maxPicHeight == null) && pictureQueryRequest.getPicHeight() != null) {
            minPicHeight = pictureQueryRequest.getPicHeight();
            maxPicHeight = pictureQueryRequest.getPicHeight();
        }

        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();


        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();

        Long spaceId = pictureQueryRequest.getSpaceId();
        boolean nullSpaceId = pictureQueryRequest.isNullSpaceId();

        Date startEditTime = pictureQueryRequest.getStartEditTime();
        Date endEditTime = pictureQueryRequest.getEndEditTime();


        // 从多字段中搜索
        if (StrUtil.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("name", searchText)
                    .or()
                    .like("introduction", searchText)
            );
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);

        // 统一使用范围查询逻辑处理宽度
        queryWrapper.ge(ObjUtil.isNotEmpty(minPicWidth), "picWidth", minPicWidth);
        queryWrapper.le(ObjUtil.isNotEmpty(maxPicWidth), "picWidth", maxPicWidth);

        // 统一使用范围查询逻辑处理高度
        queryWrapper.ge(ObjUtil.isNotEmpty(minPicHeight), "picHeight", minPicHeight);
        queryWrapper.le(ObjUtil.isNotEmpty(maxPicHeight), "picHeight", maxPicHeight);

        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);

        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);

        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.isNull(nullSpaceId, "spaceId");

        queryWrapper.ge(ObjUtil.isNotEmpty(startEditTime), "editTime", startEditTime);
        queryWrapper.lt(ObjUtil.isNotEmpty(endEditTime), "editTime", endEditTime);


        // JSON 数组查询
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        // 对象转封装类
        PictureVO pictureVO = PictureVO.objToVo(picture);
        // 关联查询用户信息
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    /**
     * 分页获取图片封装
     */
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        // 对象列表 => 封装对象列表
        List<PictureVO> pictureVOList = pictureList.stream()
                .map(PictureVO::objToVo)
                .collect(Collectors.toList());
        // 1. 关联查询用户信息
        // 从图片列表中提取所有用户ID，并收集到一个Set集合中（无重复元素）
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        // 根据这些用户ID查询用户信息，并按用户ID分组存储到Map中
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息，找到每一张图片对应的User
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        // 修改数据时，id 不能为空，有参数则校验
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id can not be empty");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url is too long");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "introduce is too long");
        }
    }

    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        // 1.校验参数
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        // 不允许把通过或者拒绝改为待审核(请求状态不应该是审核中)
        if (id == null || reviewStatusEnum == null || PictureReviewStatusEnum.REVIEWING.equals(reviewStatusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Invalid review status");
        }
        // 2. 判断是否存在
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "Picture is not exist");
        // 3. 判断审核状态是否重复
        if (oldPicture.getReviewStatus().equals(reviewStatus)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Do not repeat review");
        }
        // 4. 数据库操作，MyBatis-Plus 在执行 updateById 操作时，默认只会更新实体对象中非空的字段。所以不用担心审查状态的Picture完全覆盖掉老Picture
        Picture updatePicture = new Picture();
        BeanUtils.copyProperties(pictureReviewRequest, updatePicture);
        updatePicture.setReviewerId(loginUser.getId());
        updatePicture.setReviewTime(new Date());
        boolean result = this.updateById(updatePicture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "Fail to update picture review status");
    }

    /**
     * 填充审核参数
     *
     * @param picture
     * @param loginUser
     */
    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            // 管理员自动过审  
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewMessage("Admin automatically passed");
            picture.setReviewTime(new Date());
        } else {
            // 非管理员，创建或编辑都要改为待审核  
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }


    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        String searchText = pictureUploadByBatchRequest.getSearchText();
        // 格式化数量
        Integer count = pictureUploadByBatchRequest.getCount();
        Integer first = pictureUploadByBatchRequest.getFirst();
        // count是1-35之间的整数，first是最小值为1的整数，
        // 处理异常情况：如果count为null、非整数、小于1或大于35，则设置为默认值20
        // 处理异常情况：如果first为null、非整数或小于1，则设置为默认值1
        try {
            if (count == null || count < 1 || count > 35) {
                count = 20;
            }
        } catch (Exception e) {
            // 捕获可能的类型转换异常
            count = 20;
        }

        try {
            if (first == null || first < 1) {
                first = 1;
            }
        } catch (Exception e) {
            // 捕获可能的类型转换异常
            first = 1;
        }

        ThrowUtils.throwIf(count > 35, ErrorCode.PARAMS_ERROR, "Can not upload more than 35 pictures");
        // 名称前缀默认为搜索关键词
        String namePrefix = pictureUploadByBatchRequest.getNamePrefix();
        if (StrUtil.isBlank(namePrefix)) {
            namePrefix = searchText;
        }

        // 获取分类和标签
        String category = pictureUploadByBatchRequest.getCategory();
        List<String> tags = pictureUploadByBatchRequest.getTags();

        // 要抓取的地址
        String fetchUrl = String.format("https://www.bing.com/images/async?q=%s&first=%d&count=%d&mmasync=1", searchText, first, count);
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("Fail to get page", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Fail to get page");
        }
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjUtil.isNull(div)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Fail to get element");
        }

        //图片元素
        Elements imgElementList = div.select(".iusc");
        int uploadCount = 0;

        for (Element imgElement : imgElementList) {

            // 获取data-m属性中的JSON字符串
            String dataM = imgElement.attr("m");
            String fileUrl;
            try {
                // 解析JSON字符串
                JSONObject jsonObject = JSONUtil.parseObj(dataM);
                // 获取murl字段（原始图片URL）
                fileUrl = jsonObject.getStr("murl");
            } catch (Exception e) {
                log.error("Fail to parse picture url", e);
                continue;
            }

            if (StrUtil.isBlank(fileUrl)) {
                log.info("Current url is empty,skipped: {}", fileUrl);
                continue;
            }

            // 处理图片上传地址，防止出现转义问题
            int questionMarkIndex = fileUrl.indexOf("?");
            if (questionMarkIndex > -1) {
                fileUrl = fileUrl.substring(0, questionMarkIndex);
            }

            // 上传图片
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            pictureUploadRequest.setFileUrl(fileUrl);
            int picNumber = uploadCount + first;
            if (StrUtil.isNotBlank(namePrefix)) {
                // 设置图片名称，序号连续递增
                pictureUploadRequest.setPicName(namePrefix + picNumber);
            }
            try {
                PictureVO pictureVO = this.uploadPicture(fileUrl, pictureUploadRequest, loginUser);

                // 上传成功后，设置标签和分类
                if (pictureVO != null && pictureVO.getId() != null) {
                    Picture picture = new Picture();
                    picture.setId(pictureVO.getId());

                    // 设置分类
                    if (StrUtil.isNotBlank(category)) {
                        picture.setCategory(category);
                    }

                    // 设置标签
                    if (CollUtil.isNotEmpty(tags)) {
                        // 标签在数据库中是以JSON字符串形式存储的
                        picture.setTags(JSONUtil.toJsonStr(tags));
                    }

                    // 只更新非空字段
                    boolean updateResult = this.updateById(picture);
                    if (updateResult) {
                        log.info("Successfully updated picture tags and category, id = {}", pictureVO.getId());
                    } else {
                        log.error("Failed to update picture tags and category, id = {}", pictureVO.getId());
                    }
                }

                log.info("Picture upload success, id = {}", pictureVO.getId());
                uploadCount++;
            } catch (Exception e) {
                log.error("Picture upload fail", e);
                continue;
            }
            if (uploadCount >= count) {
                break;
            }
        }
        return uploadCount;
    }

    @Async
    @Override
    public void clearPictureFile(Picture oldPicture) {
        // 判断该图片是否被多条记录使用
        String pictureUrl = oldPicture.getUrl();
        long count = this.lambdaQuery()
                .eq(Picture::getUrl, pictureUrl)
                .count();
        // 不止一条记录用到了该图片，则不清理
        if (count > 1) {
            return;
        }
        // 删除压缩后的webp图片
        cosManager.deleteObject(pictureUrl);
        // 删除缩略图
        String thumbnailUrl = oldPicture.getThumbnailUrl();
        if (StrUtil.isNotBlank(thumbnailUrl)) {
            cosManager.deleteObject(thumbnailUrl);
        }
        // 删除原图
        String originalUrl = oldPicture.getOriginalUrl();
        if (StrUtil.isNotBlank(originalUrl)) {
            cosManager.deleteObject(originalUrl);
        }

    }


    @Override
    public void deletePicture(long pictureId, User loginUser) {
        ThrowUtils.throwIf(pictureId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

        // 判断是否存在
        Picture oldPicture = this.getById(pictureId);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 校验权限
        this.checkPictureAuth(loginUser, oldPicture);

        // 开启事务
        transactionTemplate.execute(status -> {
            // 操作数据库
            boolean result = this.removeById(pictureId);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

            // 仅当 spaceId 不为空时更新空间大小和数量
            if (oldPicture.getSpaceId() != null) {
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, oldPicture.getSpaceId())
                        .setSql("totalSize = totalSize - " + oldPicture.getPicSize())
                        .setSql("totalCount = totalCount -1")
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "Fail to update size or count");
            }
            return true;
        });


        // 清理图片资源
        this.clearPictureFile(oldPicture);
    }

    /**
     * 按照和提供的String picColor相似度程度对图片进行排序返回列表,以实现颜色搜图功能
     * @param spaceId
     * @param picColor
     * @param loginUser
     * @return
     */
    @Override
    public List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser) {
        // 1. 校验参数
        ThrowUtils.throwIf(spaceId == null || StrUtil.isBlank(picColor), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 2. 校验空间权限
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "Space is not exists");
        if (!loginUser.getId().equals(space.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "No space auth");
        }
        // 3. 查询该空间下所有图片（必须有主色调）
        List<Picture> pictureList = this.lambdaQuery()
                .eq(Picture::getSpaceId, spaceId)
                .isNotNull(Picture::getPicColor)
                .list();
        // 如果没有图片，直接返回空列表
        if (CollUtil.isEmpty(pictureList)) {
            return Collections.emptyList();
        }
        // 将目标颜色转为 Color 对象
        Color targetColor = Color.decode(picColor);
        // 4. 计算相似度并排序
        List<Picture> sortedPictures = pictureList.stream()
                .sorted(Comparator.comparingDouble(picture -> {
                    // 提取图片主色调
                    String hexColor = picture.getPicColor();
                    // 没有主色调的图片放到最后
                    if (StrUtil.isBlank(hexColor)) {
                        return Double.MAX_VALUE;
                    }
                    Color pictureColor = Color.decode(hexColor);
                    // 越大越相似,sort默认从小到大排序,故加负号
                    return -ColorSimilarUtils.calculateSimilarity(targetColor, pictureColor);
                }))
                // 取前 12 个
                .limit(12)
                .collect(Collectors.toList());

        // 转换为 PictureVO
        return sortedPictures.stream()
                .map(PictureVO::objToVo)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser) {
        List<Long> pictureIdList = pictureEditByBatchRequest.getPictureIdList();
        Long spaceId = pictureEditByBatchRequest.getSpaceId();
        String category = pictureEditByBatchRequest.getCategory();
        List<String> tags = pictureEditByBatchRequest.getTags();
        // 1. 获取和校验参数
        ThrowUtils.throwIf(spaceId == null || CollUtil.isEmpty(pictureIdList), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 2. 校验空间权限
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "Space not exist");
        if (!loginUser.getId().equals(space.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "No access permission to the space.");
        }
        // 3. 查询指定图片（进选择需要的字段）
        List<Picture> pictureList = this.lambdaQuery()
                .select(Picture::getId, Picture::getSpaceId)
                .eq(Picture::getSpaceId, spaceId)
                .in(Picture::getId, pictureIdList)
                .list();
        if(pictureList.isEmpty()){
            return;
        }
        // 4. 更新分类和标签
        pictureList.forEach(picture->{
            if(StrUtil.isNotBlank(category)){
                picture.setCategory(category);
            }
            if(!CollUtil.isEmpty(tags)){
                picture.setTags(JSONUtil.toJsonStr(tags));
            }
            boolean result = this.updateBatchById(pictureList);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "Fail to update picture by batch");
        });

        // 批量重命名
        String nameRule = pictureEditByBatchRequest.getNameRule();
        fillPictureWithNameRule(pictureList, nameRule);
        // 5，操作数据库批量进行更新
        boolean result = this.updateBatchById(pictureList);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser) {
        // 获取图片信息
        Long pictureId = createPictureOutPaintingTaskRequest.getPictureId();
        // Picture picture = this.getById(pictureId);
        // ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "Picture not exist");
        Picture picture = Optional.ofNullable(this.getById(pictureId))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR, "Picture not exist"));
        
        // 权限校验
        checkPictureAuth(loginUser, picture);
        
        // 构造请求参数
        CreateOutPaintingTaskRequest createOutPaintingTaskRequest = new CreateOutPaintingTaskRequest();
        CreateOutPaintingTaskRequest.Input input = new CreateOutPaintingTaskRequest.Input();
        input.setImageUrl(picture.getUrl());
        createOutPaintingTaskRequest.setInput(input);
        createOutPaintingTaskRequest.setParameters(createPictureOutPaintingTaskRequest.getParameters());
        
        // 创建任务
        return aliYunAiApi.createOutPaintingTask(createOutPaintingTaskRequest);
        
    }   

    /**
     * nameRule:图片名称_{index}
     * @param pictureList
     * @param nameRule
     */
    private void fillPictureWithNameRule(List<Picture> pictureList, String nameRule) {
        if(StrUtil.isBlank(nameRule)||CollUtil.isEmpty(pictureList)){
            return;
        }
        long count =1;
        try{
            for(Picture picture:pictureList){
                String pictureName = nameRule.replaceAll("\\{index}", String.valueOf(count++));
                picture.setName(pictureName);
            }
        }catch(Exception e){
            log.error("Name resolution error, ", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Name resolution error");
        }
    }


    @Override
    public void editPicture(PictureEditRequest pictureEditRequest, User loginUser) {
        // 在此处将实体类和 DTO 进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);
        // 注意将 list 转为 string
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        // 设置编辑时间
        picture.setEditTime(new Date());
        // 数据校验
        this.validPicture(picture);

        // 插入数据库之前补充审核参数,任何用户的编辑行为都应该将审核通过改为审核中
        this.fillReviewParams(picture, loginUser);

        // 判断是否存在
        long id = pictureEditRequest.getId();
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 校验权限
        this.checkPictureAuth(loginUser, oldPicture);

        // 操作数据库
        boolean result = this.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

    }


    @Override
    public void checkPictureAuth(User loginUser, Picture picture) {
        Long spaceId = picture.getSpaceId();
        long loginUserId = loginUser.getId();
        if (spaceId == null) {
            // 公共图库，仅本人和管理员可操作
            if (!picture.getUserId().equals(loginUserId) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "Only the user himself or the administrator can edit the picture");
            }
        } else {
            // 空间图库，仅空间所有者可操作
            if (!picture.getUserId().equals(loginUserId)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "Only the space owner can edit the picture");
            }
        }
    }


}




