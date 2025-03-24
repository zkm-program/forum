create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userName     varchar(256) not null comment '用户姓名',
    isReported   Boolean               default false comment '用户是否被举报',
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
    id         bigint auto_increment comment 'id' primary key,
    tageName   varchar(256) default null comment '标签名称',
    isParent   tinyint      default null comment '0 -不是，1-是',
    createTime datetime     default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime     default CURRENT_TIMESTAMP NULL on update CURRENT_TIMESTAMP comment '创修改时间',
    isDelete   tinyint      default 0                 not null comment '0是没删，1是没删',
    parentTagName varchar(256) DEFAULT null comment '父标签名',
    index idx_tageName (tageName)
) comment '标签';


