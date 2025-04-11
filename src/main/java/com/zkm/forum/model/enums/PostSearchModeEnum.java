package com.zkm.forum.model.enums;

import lombok.Getter;

public enum PostSearchModeEnum {
    MYSQL("mysql","postMysqlSearchImpl"),
    ELASTICSEARCH("elasticsearch","postElasticSearchImpl");
    @Getter
    private String mode;
    @Getter
    private String message;

    PostSearchModeEnum(String mode, String message) {
        this.mode = mode;
        this.message=message;
    }
    public static String getMessageByMode(String mode){
        for (PostSearchModeEnum postSearchModeEnum : PostSearchModeEnum.values()) {
            if (postSearchModeEnum.getMode().equals(mode)){
                return postSearchModeEnum.getMessage();
            }
        }
        return null;
    }

}
