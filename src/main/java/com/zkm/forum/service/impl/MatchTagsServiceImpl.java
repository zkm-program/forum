package com.zkm.forum.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zkm.forum.model.entity.MatchTags;
import com.zkm.forum.model.entity.User;
import com.zkm.forum.model.vo.matchTags.MatchTagsVo;
import com.zkm.forum.service.MatchTagsService;
import com.zkm.forum.mapper.MatchTagsMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
* @author 张凯铭
* @description 针对表【match_tags(标签匹配结果历史记录)】的数据库操作Service实现
* @createDate 2025-05-05 22:19:17
*/
@Service
public class MatchTagsServiceImpl extends ServiceImpl<MatchTagsMapper, MatchTags>
    implements MatchTagsService{

    @Override
    public List<MatchTagsVo> getBasicMatchTags(User user) {
        Long userId = user.getId();
        QueryWrapper<MatchTags> matchTagsQueryWrapper = new QueryWrapper<>();
        matchTagsQueryWrapper.eq("userId",userId);
        List<MatchTags> matchTagsList = this.list(matchTagsQueryWrapper);

        List<MatchTagsVo> list = matchTagsList.stream().map(matchTags -> {
            MatchTagsVo matchTagsVo = new MatchTagsVo();
            BeanUtils.copyProperties(matchTags, matchTagsVo);
            matchTagsVo.setTags(JSONUtil.toList(matchTags.getTags(), String.class));
            return matchTagsVo;
        }).toList();
        return list;
    }
}




