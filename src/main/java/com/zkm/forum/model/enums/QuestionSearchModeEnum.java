package com.zkm.forum.model.enums;

import lombok.Getter;


public enum QuestionSearchModeEnum {
    MYSQL("mysql","questionMysqlSearchImpl"),
    ELASTICSEARCH("elasticsearch","questionElasticSearchImpl");
    @Getter
    private String mode;
    @Getter
    private String message;

    QuestionSearchModeEnum(String mode, String message) {
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
