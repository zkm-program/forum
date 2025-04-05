package com.zkm.forum.utils;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.MessageDigest;

@Component
public class FileUtils {
    public String getMd5(InputStream inputStream) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("md5");
            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                md5.update(buffer, 0, length);
            }
            return new String(Hex.encodeHex(md5.digest()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public String getExName(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

}
