package com.zkm.forum.job;


import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.DeleteObjectsRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zkm.forum.config.properties.OssConfigProperties;

import com.zkm.forum.model.entity.FitnessImage;

import com.zkm.forum.service.FitnessImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.util.List;


/**
 * 物理删除对象存储中没用的文件
 */
@Slf4j
@Component
public class DelteFailureOssJob {
    @Resource
    private FitnessImageService fitnessImageService;
    @Resource
    private OssConfigProperties ossConfigProperties; // 假设这是你配置属性的注入方式

    //    @Scheduled(cron = "0 0 5,11,17 * * ?", zone = "Asia/Shanghai")
    //每天的早上5，上午11，下午17点各执行一次
    @Scheduled(cron = "0 0 5,11,17 * * ?")
    public void work() {
        String ossDomain = ossConfigProperties.getOssDomain();
        QueryWrapper<FitnessImage> fitnessImageQueryWrapper = new QueryWrapper<>();
        fitnessImageQueryWrapper.eq("type", 3);
        fitnessImageQueryWrapper.select("id", "pictureUrl");
        List<FitnessImage> fitnessImageList = fitnessImageService.list(fitnessImageQueryWrapper);
        List<Long> idList = fitnessImageList.stream().map(FitnessImage::getId).toList();
        List<String> filePaths = fitnessImageList.stream().map(fitnessImage -> extractFilePathFromUrl(fitnessImage.getPictureUrl(), ossDomain)).toList();
        deleteFileFromOSS(filePaths);
        fitnessImageService.removeByIds(idList);
    }

    private String extractFilePathFromUrl(String url, String ossDomain) {
        // 确保URL是以正确的OSS域名结尾，这里以"sky-out-sq.oss-cn-beijing.aliyuncs.com/"为例
        if (url.contains(ossDomain)) {
            // 找到域名在URL中的结束位置
            int startIndex = url.indexOf(ossDomain) + ossDomain.length();
            // 提取出从域名之后开始到URL结尾的部分作为filePath
            return url.substring(startIndex);
        } else {
            throw new IllegalArgumentException("提供的URL不匹配指定的OSS域名");
        }
    }

    /**
     * 删除OSS上指定路径的文件。
     *
     * @param filePaths 文件在OSS上的路径。
     */
    private void deleteFileFromOSS(List<String> filePaths) {
        // 创建OSS客户端
        OSS ossClient = new OSSClientBuilder().build(
                ossConfigProperties.getEndpoint(),
                ossConfigProperties.getAccessKeyId(),
                ossConfigProperties.getAccessKeySecret());
        DeleteObjectsRequest request = new DeleteObjectsRequest(ossConfigProperties.getBucketName()).withKeys(filePaths);

        try {
            // 删除文件
            ossClient.deleteObjects(request);
        }catch (Exception e){
            log.error("删除文件失败：{}", e.getMessage());
        }
        finally {
            // 关闭OSS客户端
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
