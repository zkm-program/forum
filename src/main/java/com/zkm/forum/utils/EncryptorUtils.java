package com.zkm.forum.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 不可逆的加密方式，针对于用户的基本信息
 */
public class EncryptorUtils {
    // 推荐：BCrypt（自动加盐，防止彩虹表攻击）
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public static String hashPassword(String rawPassword) {
        return encoder.encode(rawPassword);
    }
    public static boolean checkPassword(String rawPassword, String hashedPassword) {
        return encoder.matches(rawPassword, hashedPassword);
    }
    // 使用示例
//    String hashedPwd = PasswordUtils.hashPassword("user123"); // 存储到数据库
//    boolean isValid = PasswordUtils.checkPassword("user123", hashedPwd); // 验证密码
}
