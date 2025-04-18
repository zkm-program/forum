-- auto-generated definition
create table user
(
    id              bigint auto_increment comment 'id'
        primary key,
    userName        varchar(256)                             not null comment '用户姓名',
    tags            varchar(1024)                            null,
    userPassword    varchar(256)                             not null comment '用户密码',
    matchCount      int            default 1                 not null comment '用户匹配次数',
    userRole        varchar(256)   default 'user'            not null comment '用户角色',
    createTime      datetime       default CURRENT_TIMESTAMP not null comment '用户创建时间',
    updateTime      datetime       default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '用户更新时间',
    isDelete        tinyint        default 0                 not null comment '是否删除',
    userAvatar      varchar(1024)                            null,
    gender          varchar(256)                             not null comment '用户性别',
    userQqEmail     varchar(256)                             not null comment '用户QQ邮箱',
    isReported      tinyint        default 0                 null comment '0是没被举报，1是被举报了',
    reportUserId    bigint                                   not null comment '举报用户id',
    reportResults   varchar(256)   default ''                null comment '被举报原因',
    superMatchCount int            default 1                 null comment '用户超级匹配次数',
    longitude       decimal(10, 6) default 0.000000          null comment '经度',
    dimension       decimal(10, 6) default 0.000000          null comment '维度',
    fitnessId       bigint                                   null comment '健身id',
    followerCount   int            default 0                 not null comment '被关注数',
    introduction    varchar(256)   default ''                not null comment '用户简介',
    constraint uniIdx_userQqEmail
        unique (userQqEmail)
)
    comment '用户表' collate = utf8mb4_unicode_ci;

create index idx_userQqEmail
    on user (userQqEmail);



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
    reportResults    VARCHAR(256) default '' comment "被举报原因",
    reportUserId     bigint                                                        not null comment "举报用户id",
    questionId       bigint       default 0                                        not null comment '问题id',
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



create table if not exists question
(
    id            bigint auto_increment comment 'id' primary key,
    question      varchar(200)                           null comment '问题',
    tags          varchar(1024)                          null comment '标签列表（json 数组）',
    favourNum     int          default 0                 not null comment '收藏数',
    userId        bigint                                 not null comment '创建用户 id',
    createTime    datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime    datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_top        tinyint                                NOT NULL DEFAULT 0 COMMENT '是否置顶 0否 1是',
    is_featured   tinyint                                NOT NULL DEFAULT 0 COMMENT '是否推荐 0否 1是',
    isDelete      tinyint      default 0                 not null comment '是否删除',
    isReported    TINYINT      default 0 comment "0是没被举报，1是被举报了",
    viewCount     int          DEFAULT 0                 not null comment '浏览量',
    reportResults VARCHAR(256) default '' comment "被举报原因",
    reportUserId  bigint                                 not null comment "举报用户id",
    questionCount int          default 0                 not null comment '回答个数',
    index idx_userId (userId)
) comment '问题' collate = utf8mb4_unicode_ci;

create table question_cocern
(
    id         bigint auto_increment comment 'id'
        primary key,
    questionId bigint                             not null comment '问题 id',
    userId     bigint                             not null comment '关注这个问题用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '问题关注';

create index idx_questionId
    on question_cocern (questionId);

create index idx_userId
    on question_cocern (userId);



CREATE TABLE user_follow
(
    id          bigint   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id     bigint   NOT NULL COMMENT '被关注用户ID',
    follower_id bigint   NOT NULL COMMENT '关注者用户ID',
    create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_follower (user_id, follower_id) COMMENT '用户与关注者唯一索引',
    KEY idx_follower (follower_id) COMMENT '关注者查询优化'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户关注关系表';