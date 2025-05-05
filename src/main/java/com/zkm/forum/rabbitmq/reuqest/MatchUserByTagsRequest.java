package com.zkm.forum.rabbitmq.reuqest;

import com.zkm.forum.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MatchUserByTagsRequest {
    private List<String> tags;
    private User loginUser;
}
