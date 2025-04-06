package com.zkm.forum.constant;

public interface RedisConstant {

    String POST_VIEWS_COUNT = "post:views:count";
    String USER_POST_READ = "user:post:read";
    String READ_POST_IDS = "read:post:ids";
    String USER_RECOMMEND = "user:recommned";
    String PRE_CACHE_POST_LOGIN = "pre:cachec:post_login";
    String PRE_CACHE_POST_LOGOUT = "pre:cachec:post_logout";
    String USER_GEO = "user:geo";
    String USER_SIGNIN = "user:signin";

    static String getRedisUserSignin(int year,int week,Long userId){
        return String.format("%s:%s:%s:%s",USER_SIGNIN,year,week,userId);
    }
}
