package com.zkm.forum.model.enums;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum UserRoleEnum {
    USER("用户","user"),
    ADMIN("管理员","admin"),
    BAN("被封号","ban");

    private final String text;
    private final String value;
    UserRoleEnum(String text, String value) {
        this.text=text;
        this.value=value;
    }
    public static List<String> getValues(){
        return Arrays.stream(UserRoleEnum.values()).map(item->item.value).collect(Collectors.toList());
    }
    public static UserRoleEnum getEnumByValue(String value){
        if(ObjectUtils.isEmpty(value)){
            return null;
        }
        for(UserRoleEnum userRoleEnum:UserRoleEnum.values()){
            if(userRoleEnum.value.equals(value)){
                return userRoleEnum;
            }
        }
        return null;
    }
}

