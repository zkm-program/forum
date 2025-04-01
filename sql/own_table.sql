create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userName     varchar(256) not null comment '用户姓名',
    isReported       TINYINT      default 0 comment "0是没被举报，1是被举报了",
    tags         varchar(1024)         default null comment '用户标签,json数组里面都是字符串形式',
    userQqEmail  varchar(256) not null comment '用户邮箱',
    userPassword varchar(256) not null comment '用户密码',
    matchCount   int          not null default 1 comment '用户匹配次数',
    userRole     varchar(256) not null default 'user' comment '用户角色',
    createTime   datetime              default CURRENT_TIMESTAMP not null comment '用户创建时间',
    updateTime   datetime              default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '用户更新时间',
    isDelete     tinyint      not null default 0 comment '是否删除',
    userAvatar   varchar(1024)         default null comment '用户头像',
    gender       varchar(256) not null comment '用户性别',
    index idx_userQqEmail (userQqEmail),
    CONSTRAINT uniIdx_userQqEmail UNIQUE (userQqEmail)

) comment '用户表' collate = utf8mb4_unicode_ci;



create table if not exists tag
(
    id            bigint auto_increment comment 'id' primary key,
    tageName      varchar(256) default null comment '标签名称',
    isParent      tinyint      default null comment '0 -不是，1-是',
    createTime    datetime     default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime    datetime     default CURRENT_TIMESTAMP NULL on update CURRENT_TIMESTAMP comment '创修改时间',
    isDelete      tinyint      default 0                 not null comment '0是没删，1是没删',
    parentTagName varchar(256) DEFAULT null comment '父标签名',
    index idx_tageName (tageName)
) comment '标签';

create table if not exists post
(
    id               bigint auto_increment comment 'id' primary key,
    title            varchar(512)                                                  null comment '标题',
    content          text                                                          null comment '内容',
    tags             varchar(1024)                                                 null comment '标签列表（json 数组）',
    thumbNum         int          default 0                                        not null comment '点赞数',
    favourNum        int          default 0                                        not null comment '收藏数',
    userId           bigint                                                        not null comment '创建用户 id',
    createTime       datetime     default CURRENT_TIMESTAMP                        not null comment '创建时间',
    updateTime       datetime     default CURRENT_TIMESTAMP                        not null on update CURRENT_TIMESTAMP comment '更新时间',
    status           tinyint      default 0 comment '0-公开，1-私有，2-草稿',
    article_abstract varchar(500) DEFAULT NULL COMMENT '文章摘要，如果该字段为空，默认取文章的前500个字符作为摘要',
    is_top           tinyint                                                       NOT NULL DEFAULT 0 COMMENT '是否置顶 0否 1是',
    is_featured      tinyint                                                       NOT NULL DEFAULT 0 COMMENT '是否推荐 0否 1是',
    isDelete         tinyint      default 0                                        not null comment '是否删除',
    type             tinyint                                                       NOT NULL DEFAULT 1 COMMENT '文章类型 1原创 2转载 3翻译',
    password         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '访问密码',
    original_url     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '原文链接',
    isReported       TINYINT      default 0 comment "0是没被举报，1是被举报了",
    viewCount        int          DEFAULT 0                                        not null comment '浏览量',
    index idx_userId (userId)
) comment '帖子' collate = utf8mb4_unicode_ci;

-- 帖子收藏表（硬删除）
create table if not exists post_favour
(
    id         bigint auto_increment comment 'id' primary key,
    postId     bigint                             not null comment '帖子 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_postId (postId),
    index idx_userId (userId)
) comment '帖子收藏';

-- 帖子点赞表（硬删除）
create table if not exists post_thumb
(
    id         bigint auto_increment comment 'id' primary key,
    postId     bigint                             not null comment '帖子 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_postId (postId),
    index idx_userId (userId)
) comment '帖子点赞';

