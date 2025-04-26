package com.zkm.forum.model.dto.user;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


@Data
public class UserUpdateMyRequest implements Serializable {
    /**
     * 用户id
     */
    private Long id;

    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 用户性别
     */
    private String gender;
    /**
     * 用户介绍
     */
    private String introduction;
    /**
     * 用户头像
     */
    private String userAvatar;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
