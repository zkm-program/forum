package com.zkm.forum.model.dto.post;

import lombok.Data;

import java.io.Serializable;
@Data
public class PostSearchRequest implements Serializable {
    String keyWords;
}
