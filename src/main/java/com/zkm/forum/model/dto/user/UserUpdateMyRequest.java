package com.zkm.forum.model.dto.user;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


@Data
public class UserUpdateMyRequest implements Serializable {
    private Long id;


    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 用户标签,json数组里面都是字符串形式
     */
    private List<String> tags;














     ;

    /**
     * 用户性别
     */
    private String gender;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
