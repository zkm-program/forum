package com.zkm.forum.blackfilter;

import cn.hutool.bloomfilter.BitMapBloomFilter;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.util.List;
import java.util.Map;

/**
 * 黑名单过滤工具类
 */
@Slf4j
public class BlackIpUtils {


    //使用静态方法定义并生成一个全局的布隆过滤器，布隆过滤器的数据一般存储在内存中
    private static BitMapBloomFilter bloomFilter = new BitMapBloomFilter(100);

    // 判断 ip 是否在黑名单里
    public static boolean isBlackIp(String ip) {
        return bloomFilter.contains(ip);
    }

    /**
     * 重建 ip 黑名单
     *就是把nacos配置中心中的ip黑名单，取出来放到布隆过滤器中，因为布隆过滤器不支持删除里面的元素，
     * 所以每次只要nacos配置中心中的ip黑名单发生变化，我就会重新执行这个方法
     * @param configInfo
     */
    public static void rebuildBlackIp(String configInfo) {
        if (StrUtil.isBlank(configInfo)) {
            configInfo = "{}";
        }
        // 解析 yaml 文件
        Yaml yaml = new Yaml();
        //把configInfo转换为map类型
        Map map = yaml.loadAs(configInfo, Map.class);
        // 获取 IP 黑名单
        List<String> blackIpList = (List<String>) map.get("blackIpList");
        // 加锁防止并发
        synchronized (BlackIpUtils.class) {
            if (CollUtil.isNotEmpty(blackIpList)) {
                // 注意构造参数的设置
//                BitMapBloomFilter bitMapBloomFilter = new BitMapBloomFilter(958506);
                BitMapBloomFilter bitMapBloomFilter = new BitMapBloomFilter(958);
                for (String blackIp : blackIpList) {
                    bitMapBloomFilter.add(blackIp);
                }
                bloomFilter = bitMapBloomFilter;
            } else {
                bloomFilter = new BitMapBloomFilter(100);
            }
        }
    }
}
