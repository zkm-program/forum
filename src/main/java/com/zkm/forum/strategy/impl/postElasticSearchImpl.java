package com.zkm.forum.strategy.impl;

import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service("postElasticSearchImpl")
public class postElasticSearchImpl {
    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

}
