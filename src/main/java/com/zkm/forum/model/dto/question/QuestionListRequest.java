package com.zkm.forum.model.dto.question;

import com.zkm.forum.common.PageRequest;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuestionListRequest extends PageRequest {
    /**
     *用户查看问题列表时，可以选择时间进行筛选
     */
    private LocalDateTime start;
    private LocalDateTime end;
}
