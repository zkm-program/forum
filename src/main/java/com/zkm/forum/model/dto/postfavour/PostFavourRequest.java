package com.zkm.forum.model.dto.postfavour;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PostFavourRequest {
    @NotNull(message = "请选择帖子")
    private Long postId;
}
