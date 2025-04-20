-- Create database
create database if not exists intellipichub;

-- Switch database
use intellipichub;

-- User table
create table if not exists user
(
    id           bigint auto_increment comment 'Primary ID' primary key,
    userAccount  varchar(256)                           not null comment 'Account',
    userPassword varchar(512)                           not null comment 'Password',
    userName     varchar(256)                           null comment 'Username',
    userAvatar   varchar(1024)                          null comment 'User Avatar URL',
    userProfile  varchar(512)                           null comment 'User Profile Description',
    userRole     varchar(256) default 'user'            not null comment 'User Role: user/admin',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment 'Edit Time',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment 'Create Time',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment 'Update Time',
    isDelete     tinyint      default 0                 not null comment 'Is Deleted',

    vipExpireTime datetime     null comment 'VIP Expiration Time',
    vipCode       varchar(128) null comment 'VIP Redemption Code',
    vipNumber     bigint       null comment 'VIP Number',

    shareCode     varchar(20)  DEFAULT NULL COMMENT 'Share Code',
    inviteUser    bigint       DEFAULT NULL COMMENT 'Inviter User ID',

    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
) comment 'User Table' collate = utf8mb4_unicode_ci;


-- 图片表
create table if not exists picture
(
    id           bigint auto_increment comment 'ID' primary key,
    url          varchar(512)                       not null comment 'Image URL',
    name         varchar(128)                       not null comment 'Image Name',
    introduction varchar(512)                       null comment 'Introduction',
    category     varchar(64)                        null comment 'Category',
    tags         varchar(512)                      null comment 'Tags (JSON array)',
    picSize      bigint                             null comment 'Image Size',
    picWidth     int                                null comment 'Image Width',
    picHeight    int                                null comment 'Image Height',
    picScale     double                             null comment 'Image Aspect Ratio',
    picFormat    varchar(32)                        null comment 'Image Format',
    userId       bigint                             not null comment 'Creator User ID',
    createTime   datetime default CURRENT_TIMESTAMP not null comment 'Creation Time',
    editTime     datetime default CURRENT_TIMESTAMP not null comment 'Edit Time',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment 'Update Time',
    isDelete     tinyint  default 0                 not null comment 'Is Deleted',
    INDEX idx_name (name),                 -- Improve query performance based on image name
    INDEX idx_introduction (introduction), -- Used for image introduction by fuzzy search
    INDEX idx_category (category),         -- Improve query performance based on category
    INDEX idx_tags (tags),                 -- Improve query performance based on tags
    INDEX idx_userId (userId)              -- Improve query performance based on user ID
) comment 'Image' collate = utf8mb4_unicode_ci;

-- 添加审核相关字段
ALTER TABLE picture
    -- 添加新列
    ADD COLUMN reviewStatus INT DEFAULT 0 NOT NULL COMMENT 'Review Status: 0-Pending; 1-Approved; 2-Rejected',
    ADD COLUMN reviewMessage VARCHAR(512) NULL COMMENT 'Review Message',
    ADD COLUMN reviewerId BIGINT NULL COMMENT 'Reviewer ID',
    ADD COLUMN reviewTime DATETIME NULL COMMENT 'Review Time';

-- Create an index based on the reviewStatus column
CREATE INDEX idx_reviewStatus ON picture (reviewStatus);

-- 添加原始图像URL字段
ALTER TABLE picture ADD COLUMN originalUrl VARCHAR(512) NULL COMMENT 'Original Image URL' AFTER url;


-- 添加缩略图URL字段
ALTER TABLE picture ADD COLUMN thumbnailUrl VARCHAR(512) NULL COMMENT 'thumbnail Image URL' AFTER originalUrl;

-- 空间表
 create table if not exists space
(
    id         bigint auto_increment comment 'ID' primary key,
    spaceName  varchar(128)                       null comment 'Space Name',
    spaceLevel int      default 0                 null comment 'Space Level: 0-Common; 1-Professional; 2-Flagship',
    maxSize    bigint   default 0                 null comment 'Maximum total size of images in the space',
    maxCount   bigint   default 0                 null comment 'Maximum number of images in the space',
    totalSize  bigint   default 0                 null comment 'Total size of images in the space',
    totalCount bigint   default 0                 null comment 'Total number of images in the space',
    userId     bigint                             not null comment 'Creator User ID',
    createTime datetime default CURRENT_TIMESTAMP not null comment 'Creation Time',
    editTime   datetime default CURRENT_TIMESTAMP not null comment 'Edit Time',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment 'Update Time',
    isDelete   tinyint  default 0                 not null comment 'Is Deleted',
    -- Index design
    index idx_userId (userId),        -- Improve query performance based on user
    index idx_spaceName (spaceName),  -- Improve query performance based on space name
    index idx_spaceLevel (spaceLevel) -- Improve query performance based on space level
) comment 'Space' collate = utf8mb4_unicode_ci;

-- Add new column
ALTER TABLE picture
    ADD COLUMN spaceId  bigint  null comment 'Space ID (null indicates public space)';

-- Create index
CREATE INDEX idx_spaceId ON picture (spaceId);

ALTER TABLE picture
    ADD COLUMN picColor varchar(16) null comment 'Main color tone of picture';


