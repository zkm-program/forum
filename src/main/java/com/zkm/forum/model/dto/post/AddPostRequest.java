package com.zkm.forum.model.dto.post;

import com.zkm.forum.model.entity.Tag;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

@Data
public class AddPostRequest implements Serializable {
    private Long id;
    // todo 写了这个注解，就能不用再impl类中写校验是否为空了？
    @NotBlank(message = "文章标题不能为空")
    private String title;
    @NotBlank(message = "文章内容不能为空")
    private String content;
    private String article_abstract;
    @NotBlank(message = "文章标签不能为空")
    private List<String> tags;
    @NotBlank(message = "作者id不能为空")
    private Long userId;
    @NotBlank(message = "文章状态不能为空")
    private Integer status;
    @NotBlank(message = "文章类型不能为空")
    private Integer type;
    private String password;
    private String original_url;

}
