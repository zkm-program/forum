package com.zkm.forum.model.dto.post;

import com.zkm.forum.common.PageRequest;
import lombok.Data;

import java.io.Serializable;
@Data
public class PostSearchRequest extends PageRequest implements Serializable {
    String keyWords;
}
