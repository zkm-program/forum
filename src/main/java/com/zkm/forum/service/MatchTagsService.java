package com.zkm.forum.service;

import com.zkm.forum.model.entity.MatchTags;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zkm.forum.model.entity.User;
import com.zkm.forum.model.vo.matchTags.MatchTagsVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 张凯铭
* @description 针对表【match_tags(标签匹配结果历史记录)】的数据库操作Service
* @createDate 2025-05-05 22:19:17
*/
public interface MatchTagsService extends IService<MatchTags> {
    public List<MatchTagsVo> getBasicMatchTags(User user);

}
