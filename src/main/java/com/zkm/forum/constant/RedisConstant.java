package com.zkm.forum.constant;

public interface RedisConstant {

    /**
     * 浏览量
     */
    String POST_VIEWS_COUNT = "post:views:count";
    /**
     * 每个用户近期读过的post的id
     */
    String USER_POST_READ = "user:post:read";

    String READ_POST_IDS = "read:post:ids";
    /**
     * 给每个用户推送刚看完相似的文章
     */
    String USER_RECOMMEND = "user:recommned";
    /**
     * 登录用户推荐文章
     */
    String PRE_CACHE_POST_LOGIN = "pre:cachec:post_login";
    /**
     * 未登录用户推荐文章
     */
    String PRE_CACHE_POST_LOGOUT = "pre:cachec:post_logout";
    /**
     * 每个用户地理位置标识
     */
    String USER_GEO = "user:geo";
    /**
     * 每个用户每周签到标识
     */
    String USER_SIGNIN = "user:signin";

    static String getRedisUserSignin(int year,int week,Long userId){
        return String.format("%s:%s:%s:%s",USER_SIGNIN,year,week,userId);
    }
}
