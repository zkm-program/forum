package com.zkm.forum.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * 邮箱和密码加密工具，可逆
 */
public class EmailPasswordEncryptorUtils {
    // todo 没测试能否正常从yml文件中取出
    @Value("${aes.key}")
    private static final String AES_KEY = ""; // 需妥善保管
    private static final Logger log = LoggerFactory.getLogger(EmailPasswordEncryptorUtils.class);

    /**
     * 加密操作
     *
     * @param email
     * @return
     */
    public static String encrypt(String email) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(AES_KEY.getBytes(), "AES"));
            return Base64.getEncoder().encodeToString(cipher.doFinal(email.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return "加密错误，请稍后再试";
        }

    }

    /**
     * 解密操作
     *
     * @param encryptedEmail
     * @return
     * @throws Exception
     */
    public static String decrypt(String encryptedEmail) {
        try{
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(AES_KEY.getBytes(), "AES"));
            return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedEmail)));
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
            return "解密失败请稍后再试";
        }
    }


    // 使用示例
//    String encryptedEmail = EmailEncryptor.encrypt("user@example.com"); // 存储到数据库
//    String originalEmail = EmailEncryptor.decrypt(encryptedEmail); // 解密使用
}
