package com.zkm.forum.utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

public class UserBasicEncryptorUtils {

    // 配置参数（可根据需求调整）
    private static final int ITERATIONS = 100000; // 迭代次数
    private static final int KEY_LENGTH = 256;    // 密钥长度
    private static final int SALT_LENGTH = 16;    // 盐值长度

    /**
     * 单向加密 BigDecimal
     */
    public static String secureHash(BigDecimal value) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // 1. 生成随机盐值
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);

        // 2. 基于PBKDF2加密
        PBEKeySpec spec = new PBEKeySpec(
                value.toString().toCharArray(),
                salt,
                ITERATIONS,
                KEY_LENGTH
        );
        byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .getEncoded();

        // 3. 组合盐值和哈希结果（存储时需包含盐值）
        byte[] combined = new byte[salt.length + hash.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(hash, 0, combined, salt.length, hash.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * 验证数值是否匹配加密结果
     */
    public static boolean verify(BigDecimal input, String storedHash) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // 1. 解码Base64并分离盐值和哈希
        byte[] combined = Base64.getDecoder().decode(storedHash);
        byte[] salt = new byte[SALT_LENGTH];
        System.arraycopy(combined, 0, salt, 0, salt.length);

        // 2. 用相同盐值重新计算哈希
        PBEKeySpec spec = new PBEKeySpec(
                input.toString().toCharArray(),
                salt,
                ITERATIONS,
                KEY_LENGTH
        );
        byte[] newHash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .getEncoded();

        // 3. 对比哈希值
        byte[] originalHash = new byte[combined.length - salt.length];
        System.arraycopy(combined, salt.length, originalHash, 0, originalHash.length);
        return Arrays.equals(newHash, originalHash);
    }

}
