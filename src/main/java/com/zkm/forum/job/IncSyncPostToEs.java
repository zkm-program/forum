package com.zkm.forum.job;

import com.zkm.forum.esdao.PostEsDao;
import com.zkm.forum.mapper.PostMapper;
import com.zkm.forum.model.dto.post.PostEsDTO;
import com.zkm.forum.model.entity.Post;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import cn.hutool.core.collection.CollUtil;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class IncSyncPostToEs {
    @Resource
    private PostMapper postMapper;
    @Resource
    private PostEsDao postEsDao;

    @Scheduled(fixedRate = 60 * 1000 * 5)
    public void work() {
        Date fiveMinutesAgoDate = new Date(new Date().getTime() - 5 * 60 * 1000L);
        List<Post> posts = postMapper.lsitPostWithTime(fiveMinutesAgoDate);
        if (CollUtil.isEmpty(posts)) {
            log.error("没有新数据");
            return;
        }
        List<PostEsDTO> postEsDTOS = posts.stream().map(PostEsDTO::objToDto).collect(Collectors.toList());
        final int pageSize = 500;
        int total = postEsDTOS.size();
        for (int i = 0; i < total; i = i + pageSize) {
            int end = Math.min(i + pageSize, total);
            log.info("sync from {} to {}", i, end);
            postEsDao.saveAll(postEsDTOS.subList(i, end));
        }
        log.info("IncSyncPostToEs end, total {}", total);
    }
}
